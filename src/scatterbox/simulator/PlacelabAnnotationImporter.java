package scatterbox.simulator;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PlacelabAnnotationImporter {
   List<String> my_XMLFiles = new LinkedList<String>();

   Connection conn = null;

   /**
    * Format of the sql timestamp. Allows easy conversion to date format
    */
   final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss.SSS");
   
   String my_sql_annotation_string = "insert into annotations (start_time, end_time, annotation, category, sub_category) " +
   "values " +
   "(\"STARTTIME\", \"ENDTIME\", \"ANNOTATION\", \"CATEGORY\", \"JINGLE\")";

   /**
    * @param args
    */
   public static void main(String[] args) {
      // TODO Auto-generated method stub
      PlacelabAnnotationImporter pai = new PlacelabAnnotationImporter();
      pai.getListOfXMLFiles("/Users/seamusknox/Documents/datasets/placelab/annotations");
      pai.connectToDatabase();
      for(String annotationFile : pai.my_XMLFiles){
      List<String> l = pai.importAnnotations(annotationFile);
      for(String s: l){
         System.out.println(s);
         pai.insertAnnotation(s);
      }
      }

   }

   /**
    * Returns a list containing all the xml files within a specified directory.
    * @param a_directory
    */
   private void getListOfXMLFiles(String a_directory){
      File annotationDirectory = new File(a_directory);
      if(!annotationDirectory.isDirectory()){
         System.err.println("Not a directory. Are you sure you have specified the correct location?");
      }else{
         String[] annotationFiles = annotationDirectory.list();
         for(String annotationFile : annotationFiles){
            if(annotationFile.contains(".xml")){
               my_XMLFiles.add(a_directory + "/" + annotationFile);
            }
         }
      }
   }

   /**
    * 
    * @param a_fileName
    */
   private List<String> importAnnotations(String a_fileName){
      List<String> sqlAnnotations = new LinkedList<String>();
      try {

         File file = new File(a_fileName);
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         DocumentBuilder db = dbf.newDocumentBuilder();
         Document doc = db.parse(file);
         doc.getDocumentElement().normalize();
         NodeList nodeLst = doc.getElementsByTagName("DATA");
         Node fstNode = nodeLst.item(0);
         
         //Get the list of annotations
         NodeList annotations = fstNode.getChildNodes();
         //Each annotation is an element containing an element.
         for(int i=0;i<annotations.getLength();i++){
            String sqlAnnotation = my_sql_annotation_string;
            Node annotation = annotations.item(i);
            if(annotation.getNodeType() == Node.ELEMENT_NODE){
               Element e = (Element) annotation;
               String date = e.getAttribute("DATE");
               String start = e.getAttribute("STARTTIME");
               String end = e.getAttribute("ENDTIME");
               //If there is no specifies start time or end time, ignore annotation
               if(!(end.equalsIgnoreCase("") || start.equalsIgnoreCase(""))){
                  //Add start time
                  DateTime startTime = dateTimeFormatter.parseDateTime(date+" "+start);
                  Timestamp startTimestamp = new Timestamp(startTime.getMillis());
                  sqlAnnotation = sqlAnnotation.replace("STARTTIME", startTimestamp.toString());       
                  //Add end time
                  DateTime endTime = dateTimeFormatter.parseDateTime(date+" "+end);
                  Timestamp endTimestamp = new Timestamp(endTime.getMillis());
                  sqlAnnotation = sqlAnnotation.replace("ENDTIME", endTimestamp.toString());
                  //Get the actual annotation
                  NodeList anns = e.getElementsByTagName("VALUE");
                  Element ann = (Element) anns.item(0);
                  //Add each of the annotation properties to the query
                  sqlAnnotation = sqlAnnotation.replace("ANNOTATION", ann.getAttribute("LABEL"));
                  sqlAnnotation = sqlAnnotation.replace("CATEGORY", ann.getAttribute("CATEGORY"));
                  sqlAnnotation = sqlAnnotation.replace("JINGLE", ann.getAttribute("SUBCATEGORY"));
                  //Add the current query to the list of queries.
                  sqlAnnotations.add(sqlAnnotation.replace("/", "or"));
               }
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return sqlAnnotations;
   }

   public boolean insertAnnotation(String an_sql_statement){
      boolean success = false;
      //System.out.println(an_sql_statement);
      Statement statement;
      try {
         statement = conn.createStatement();
         if(conn != null){
            success = statement.execute(an_sql_statement);
            statement.close();
         }else{
            System.err.println("No database connection!!!");
         }
      } catch (SQLException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return success;
   }

   public boolean connectToDatabase(){
      boolean connected = false;

      String userName = "root";
      String password = "";
      String url = "jdbc:mysql://localhost:3306/placelab";

      try {
         Class.forName ("com.mysql.jdbc.Driver").newInstance();

         conn = DriverManager.getConnection (url, userName, password);

         if(conn != null){
            connected = true;
         }
      } catch (InstantiationException e) {
         e.printStackTrace();
      } catch (IllegalAccessException e) {
         e.printStackTrace();
      } catch (ClassNotFoundException e) {
         e.printStackTrace();
      } catch (SQLException e) {
         e.printStackTrace();
      }


      return connected;
   }
}
