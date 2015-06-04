package scatterbox.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class ImportVanKasteren {

   String dataFileName = "/Users/knoxs/Documents/datasets/kasterenDataset/kasterenSenseData.txt";

   String annotationFileName = "/Users/knoxs/Documents/datasets/kasterenDataset/kasterenActData.txt";

   File dataFile = new File(dataFileName);

   File annotationFile = new File(annotationFileName);

   BufferedReader dataFileReader;

   BufferedReader annotationFileReader;

   Connection conn = null;

   String insertDataCommand = "insert into events (start_time, end_time, id, java_type) values (\"START\", \"END\", \"OBJECT\", \"scatterbox.event.KasterenEvent\")";

   String insertAnnotationCommand = "insert into annotations (start_time, end_time, annotation) values (\"START\", \"END\", \"ANNOTATION\")";

   HashMap<Integer, String> objects = new HashMap<Integer, String>();

   HashMap<Integer, String> annotations = new HashMap<Integer, String>();

   //String[] annotations = {"leavehouse", "usetoilet", "takeshower", "gotobed", "preparebreakfast", "preparedinner", "getdrink"};
   /**
    * Format of the sql timestamp. Allows easy conversion to date format
    */
   final DateTimeFormatter dateTimeFormatter = DateTimeFormat
         .forPattern("dd-MMM-yyyy HH:mm:ss");

   public static void main(String[] args) throws FileNotFoundException {
      ImportVanKasteren ivk = new ImportVanKasteren();
      ivk.connectToDatabase();
      ivk.dataFileReader = new BufferedReader(new InputStreamReader(
            new DataInputStream(new FileInputStream(ivk.dataFileName))));
      ivk.annotationFileReader = new BufferedReader(new InputStreamReader(
            new DataInputStream(new FileInputStream(ivk.annotationFileName))));
      ivk.setUpAnnotations();
      ivk.setUpObjects();

      ivk.getData();
      ivk.getAnnotations();
   }

   private void getData() {
      String line;
      try {
         while ((line = dataFileReader.readLine()) != null) {
            String[] readingArray = line.split("\t");

            DateTime startTime = dateTimeFormatter
                  .parseDateTime(readingArray[0]);
            Timestamp startTimestamp = new Timestamp(startTime.getMillis());
            DateTime endTime = dateTimeFormatter.parseDateTime(readingArray[1]);
            Timestamp endTimestamp = new Timestamp(endTime.getMillis());
            int id = Integer.parseInt(readingArray[2]);
            //The reason for -1 is because, kasteren starts id names at 1, not 0, but the array starts at 0.
            String object = objects.get(id);

            insertStatement(insertDataCommand.replace("START",
                  startTimestamp.toString()).replace("END",
                  endTimestamp.toString()).replace("OBJECT", object));
         }
      } catch (Exception ioe) {
         ioe.printStackTrace();
      }

   }

   private void getAnnotations() {
      String line;
      try {
         while ((line = annotationFileReader.readLine()) != null) {
            String[] readingArray = line.split("\t");

            DateTime startTime = dateTimeFormatter
                  .parseDateTime(readingArray[0]);
            Timestamp startTimestamp = new Timestamp(startTime.getMillis());
            DateTime endTime = dateTimeFormatter.parseDateTime(readingArray[1]);
            Timestamp endTimestamp = new Timestamp(endTime.getMillis());
            int id = Integer.parseInt(readingArray[2]);
            //The reason for -1 is because, kasteren starts id names at 1, not 0, but the array starts at 0.
            String annotation = annotations.get(id);

            insertStatement(insertAnnotationCommand.replace("START",
                  startTimestamp.toString()).replace("END",
                  endTimestamp.toString()).replace("ANNOTATION", annotation));
         }
      } catch (Exception ioe) {
         ioe.printStackTrace();
      }
   }

   public boolean insertStatement(String an_sql_statement) {
      System.out.println(an_sql_statement);
      boolean success = false;
      //System.out.println(an_sql_statement);
      Statement statement;
      try {
         statement = conn.createStatement();
         if (conn != null) {
            success = statement.execute(an_sql_statement);
            statement.close();
         } else {
            System.err.println("No database connection!!!");
         }
      } catch (SQLException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return success;
   }

   public boolean connectToDatabase() {
      boolean connected = false;

      String userName = "root";
      String password = "";
      String url = "jdbc:mysql://localhost:3306/tvk";

      try {
         Class.forName("com.mysql.jdbc.Driver").newInstance();

         conn = DriverManager.getConnection(url, userName, password);

         if (conn != null) {
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

   private void setUpObjects() {
      objects.put(1, "microwave");
      objects.put(5, "halltoiletdoor");
      objects.put(6, "hallbathroomdoor");
      objects.put(7, "cupscupboard");
      objects.put(8, "fridge");
      objects.put(9, "platescupboard");
      objects.put(12, "frontdoor");
      objects.put(13, "dishwasher");
      objects.put(14, "toiletflush");
      objects.put(17, "freezer");
      objects.put(18, "panscupboard");
      objects.put(20, "washingmachine");
      objects.put(23, "groceriescupboard");
      objects.put(24, "hallbedroomdoor");
   }

   private void setUpAnnotations() {
      annotations.put(1, "leavehouse");
      annotations.put(4, "usetoilet");
      annotations.put(5, "takeshower");
      annotations.put(10, "gotobed");
      annotations.put(13, "preparebreakfast");
      annotations.put(15, "preparedinner");
      annotations.put(17, "getdrink");
   }

}
