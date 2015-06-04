package scatterbox.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import org.joda.time.DateTime;

public class ReadSimulationFiles {

   private final String root = "/Users/seamusknox/Documents/datasets/";

   private final String activityRootDirectory = root + "activity";

   private final String bluetoothRootDirectory = root + "Bluetooth";

   private final String gTalkRootDirectory = root + "GTalk";

   private final String gCalRootDirectory = root + "GCal";

   private final String ubisenseRootDirectory = root + "ubisense.log";


   Connection conn = null;

   Statement statement = null;

   public static void main(String[] args){

      ReadSimulationFiles rsf = new ReadSimulationFiles();

      boolean connected = rsf.connectToDatabase();
      System.out.println(connected);

      //senseReadings(rsf.ubisenseRootDirectory);
      //rsf.test(rsf.bluetoothRootDirectory);
      //rsf.importActivityReadings(rsf.activityRootDirectory);

      //rsf.importGTalkReadings(rsf.gTalkRootDirectory);
      //rsf.importGCalReadings(rsf.gCalRootDirectory);
      rsf.importAnnotations();


      if(rsf.conn != null){
         try {
            rsf.conn.close();
         } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
   }

   public boolean connectToDatabase(){
      boolean connected = false;

      String userName = "knox";
      String password = "ljilja";
      String url = "jdbc:mysql://localhost:3306/sensor_readings";

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

   /**
    * Return a list of strings representing the files contained in the 
    * directory
    * @param rootDirectoryString
    * @return
    */
   private List<String> getDataFiles(String rootDirectoryString){
      List<String> allDataFiles = new LinkedList<String>();

      File rootDirectory = new File(rootDirectoryString);
      String[] categories = rootDirectory.list();

      /**
       * Lists the files in the root directory of the sensor's readings
       * if the files themselves are kept there, then read them,
       * otherwise, go into each category directory and get the
       * files out. 
       */
      if(categories != null){
         for(String categoryString : categories){
            File category = new File(rootDirectoryString + "/" + categoryString);

            if(category.isDirectory()){
               String[] dataFiles = category.list();

               for(String dataFile : dataFiles){
                  if(dataFile.contains(".txt") && !dataFile.contains(".txt~")){
                     allDataFiles.add(rootDirectoryString + "/" + categoryString + "/" + dataFile);
                  }
               }

            }else{
               if(categoryString.contains(".txt")){
                  //If the files are in the root directory for whatever reason
                  allDataFiles.add(rootDirectoryString + "/" + categoryString);
               }
            }
         }
      }else{
         System.err.println("You sure the directory exists?");
      }
      return allDataFiles;
   }

   public void test(String rootString){
      List<String> fileList = getDataFiles(rootString);
      File readings = null;
      for(String file : fileList){
         readings = new File(file);
         FileReader fr;
         try {
            fr = new FileReader(readings);
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            while(line != null){
               System.out.println(line);
               line = br.readLine();
            }

         } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
   }

   public void importAnnotations(){
      final SimpleDateFormat f = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
      try{
         //Get Directory
         File inputWorkbook = new File("ericaannotations.xls");
         int writeRowIndex = 0;
         Workbook readWbook = Workbook.getWorkbook(inputWorkbook);   
         Sheet sheet = readWbook.getSheet(0);
         int rowindex = 0;
         int numRows = sheet.getRows();
         DateTime tempTimestamp;
         Timestamp timestamp;
         String[] date;
         String[] time;
         String annotation;
         Cell[] dates = sheet.getColumn(0);
         Cell[] times = sheet.getColumn(1);
         Cell[] annotations = sheet.getColumn(2);

         for(int j=0;j<numRows;j++){
            if(times[j].getContents() != ""){
               System.out.println(times[j].getContents());
               date = dates[j].getContents().split("/");
               time = times[j].getContents().split(":");
               annotation = annotations[j].getContents();
               if(annotation.charAt(0) == ' '){
                  annotation=annotation.substring(1);
               }
               int year = Integer.parseInt(date[2])+2000;
               int month = Integer.parseInt(date[0]);
               int day = Integer.parseInt(date[1]);
               int hour = Integer.parseInt(time[0]);
               int minute = Integer.parseInt(time[1]);
               tempTimestamp = new DateTime(year, month, day, hour, minute, 0, 0);
               timestamp = new Timestamp(tempTimestamp.getMillis());
               System.out.println(timestamp);
               String command = "insert into annotations (timestamp, annotation, username)" +
               " values (\""+timestamp+"\"," +
               "\"" + annotation+"\"," +
               "\"erica\")";
               System.out.println(command);
               statement = conn.createStatement();
               if(conn != null){
                  statement.execute(command);
                  statement.close();
               }else{
                  System.err.println("No database connection!!!");
               }
            }
            readWbook.close();
         }
      }catch(BiffException e){
         System.out.println("We have a problem: " + e.getMessage());
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (SQLException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      System.out.println("Finished");
   }

   public void importBluetoothReadings(String rootString){

      List<String> fileList = getDataFiles(rootString);

      File readings = null;

      try {
         for(String fileString : fileList){
            readings = new File(fileString);

            FileReader fr = new FileReader(readings);
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            String[] brokenName = fileString.split("_");

            while(line != null){
               if(line.contains("device,")){
                  System.out.println(fileString);
                  System.out.println(line);
                  String[] brokenLine = line.split(",");

                  statement = conn.createStatement();

                  String spotter_address = brokenName[1];
                  String mac_address = brokenLine[1];
                  String device_type = brokenLine[2];

                  //DateTime dt = new DateTime(brokenLine[2].replace("_", "T").replace(" ", ""));	

                  //Timestamp timestamp = new Timestamp(dt.getMillis());

                  Timestamp timestamp = new Timestamp(Long.parseLong(brokenLine[3]));

                  String command = "insert into bluetooth (timestamp, spotter_address, mac_address, device_type)" +
                  " values (\""+timestamp+"\"," +
                  "\"" + spotter_address+"\"," +
                  "\"" + mac_address +"\"," +
                  "\"" + device_type + "\")";

                  System.out.println(command);

                  if(conn != null){
                     statement.execute(command);
                     statement.close();
                  }else{
                     System.err.println("No database connection!!!");
                  }
                  line = br.readLine();
               }else{
                  line = br.readLine();
               }
            }
         }
      } catch (FileNotFoundException e) {
         System.err.println("Bluetooth Data file not found.");
      } catch (IOException e) {
         System.err.println("Reading Failed.");
      } catch (SQLException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

   }

   public void importActivityReadings(String rootString){

      List<String> fileList = getDataFiles(rootString);


      File readings = null;

      try {
         for(String fileString : fileList){
            System.out.println(fileString);
            readings = new File(fileString);

            FileReader fr = new FileReader(readings);
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            String[] brokenName = fileString.split("/");

            while(line != null){
               if(!line.contains("#")){
                  line = brokenName[6]+" " + line;

                  System.out.println(line);

                  String[] brokenLine = line.split(" ");

                  DateTime dt = new DateTime(brokenLine[3].replace('_', 'T'));

                  String user = brokenLine[0];
                  String keystrokes = brokenLine[1];
                  String mousepresses = brokenLine[2];
                  Long timestampInMillis = dt.getMillis();
                  Timestamp timestamp = new Timestamp(timestampInMillis);

                  String command = "insert into activity (user, keystrokes, mousepresses, timestamp)" +
                  " values (\""+user+"\"," +
                  "\"" + keystrokes+"\"," +
                  "\"" + mousepresses +"\"," +
                  "\"" + timestamp + "\")";

                  Statement s = conn.createStatement();

                  s.execute(command);

                  s.close();

                  line = br.readLine();
               }else{
                  line = br.readLine();
               }
            }
         }
      } catch (FileNotFoundException e) {
         System.err.println("Bluetooth Data file not found.");
      } catch (IOException e) {
         System.err.println("Reading Failed.");
      } catch (SQLException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   public void importGCalReadings(String rootString){

      List<String> fileList = getDataFiles(rootString);

      File readings = null;

      try {

         for(String fileString : fileList){
            System.out.println(fileString);
            readings = new File(fileString);

            FileReader fr = new FileReader(readings);
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            while(line != null){
               final String[] brokenLine = line.split(" ");
               DateTime dt = new DateTime(brokenLine[brokenLine.length - 1].replace('_', 'T'));
               Timestamp timestamp = new Timestamp(dt.getMillis());
               int index = 0;
               if(!brokenLine[0].contains("http:")){
                  index = 1;
               }

               String gCalID = brokenLine[index];
               String title = brokenLine[index + 1];
               String participants = brokenLine[index + 2];

               dt = new DateTime(brokenLine[index + 3].replace('_', 'T'));
               Timestamp startTime = new Timestamp(dt.getMillis());

               dt = new DateTime(brokenLine[index + 4].replace('_', 'T'));
               Timestamp endTime = new Timestamp(dt.getMillis());

               String location = "null";
               String description = "null";

               if(brokenLine.length == (index + 7)){
                  description = brokenLine[index + 5];
               }else if(brokenLine.length == (index + 8)){
                  location = brokenLine[index + 5];
                  description = brokenLine[index + 6];
               }

               String[] gcalidArray = gCalID.split("/");
               String username = gcalidArray[5].replace("%40", "@");

               String command = "insert into gcal (username, timestamp, gcalid, title, participants, starttime, endtime, location, description)" +
               " values (\""+username+"\"," +
               "\"" + timestamp +"\"," +
               "\"" + gCalID +"\"," +
               "\"" + title +"\"," +
               "\"" + participants +"\"," +
               "\"" + startTime +"\"," +
               "\"" + endTime +"\"," +
               "\"" + location +"\"," +
               "\"" + description + "\")";

               Statement s;

               s = conn.createStatement();

               s.execute(command);

               s.close();

               //Line contains:
               //Timestamp, email, emailmd5, online(always online cos readings not made if not online), status, message
               line = br.readLine();
               System.out.println(line);
            }
            fr.close();
            br.close();

         }
      } catch (FileNotFoundException e) {
         System.err.println("GTalk file not found.");
      } catch (IOException e) {
         System.err.println("Reading Failed.");
      } catch (SQLException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }



   }

   public void importGTalkReadings(String rootString){
      List<String> fileList = getDataFiles(rootString);

      File readings = null;

      try {

         for(String fileString : fileList){
            System.out.println(fileString);
            readings = new File(fileString);

            FileReader fr = new FileReader(readings);
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            int counter = 0;
            while(line != null){
               counter++;
               System.out.println(counter);
               System.out.println(fileString);
               final String[] brokenLine = line.split(" ");
               DateTime dt = new DateTime(brokenLine[0].replace('_', 'T'));
               Timestamp timestamp = new Timestamp(dt.getMillis());

               String email = brokenLine[1];
               String status = brokenLine[4];
               String message = "";

               if(brokenLine.length > 5 && brokenLine[5] != "null"){
                  for(int i=5;i<brokenLine.length;i++){
                     message += " " + brokenLine[i];
                  }
               }else{
                  message = "null";
               }

               System.out.println(message);

               String command = "insert into gtalk (timestamp, email, availability, message)" +
               " values (\""+timestamp+"\"," +
               "\"" + email +"\"," +
               "\"" + status +"\"," +
               "\"" + message.replace("\"", "") + "\")";

               Statement s;

               s = conn.createStatement();

               s.execute(command);

               s.close();

               //Line contains:
               //Timestamp, email, emailmd5, online(always online cos readings not made if not online), status, message
               line = br.readLine();
               System.out.println(line);
            }
            fr.close();
            br.close();

         }
      } catch (FileNotFoundException e) {
         System.err.println("GTalk file not found.");
      } catch (IOException e) {
         System.err.println("Reading Failed.");
      } catch (SQLException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   public void importUbisenseReadings(String fileString){
      try {
         System.out.println(fileString);
         File readings = new File(fileString);

         FileReader fr = new FileReader(readings);
         BufferedReader br = new BufferedReader(fr);
         String line = br.readLine();
         int counter = 0;
         while(line != null){
            counter++;
            System.out.println(counter);
            System.out.println(fileString);
            final String[] brokenLine = line.split(" ");

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss_E");
            Date d = format.parse(brokenLine[0]);

            final Timestamp timestamp = new Timestamp(d.getTime());

            String xCoordinate = brokenLine[1];
            String yCoordinate = brokenLine[2];
            String zCoordinate = brokenLine[3];
            String tagID = brokenLine[4];
            if(brokenLine.length == 6){
               tagID += brokenLine[5];
            }
            String userName = tagID;
            if(tagID.contains("179")){
               userName = "Graham Williamson";
            }else if(tagID.contains("187")){
               userName = "Stephen Knox";
            }else if(tagID.contains("183") || tagID.contains("180")){
               userName = "Matt Stabeler";
            }else if(tagID.contains("173")){
               userName = "Susan McKeever";
            }else if(tagID.contains("196")){
               userName = "Juan Ye Erica";
            }else if(tagID.contains("172")){
               userName = "Adrian Clear";
            }


            String command = "insert into ubisense (timestamp, xCoordinate, yCoordinate, zCoordinate, tagID, userName)" +
            " values (\""+timestamp+"\"," +
            "\"" + xCoordinate +"\"," +
            "\"" + yCoordinate +"\"," +
            "\"" + zCoordinate +"\"," +
            "\"" + tagID +"\"," +
            "\"" + userName + "\")";

            Statement s;

            s = conn.createStatement();

            s.execute(command);

            s.close();

            //Line contains:
            //Timestamp, email, emailmd5, online(always online cos readings not made if not online), status, message
            line = br.readLine();
            System.out.println(line);
         }
         fr.close();
         br.close();
      } catch (FileNotFoundException e) {
         System.err.println("Ubisense file not found.");
      } catch (IOException e) {
         System.err.println("Reading Failed.");
      } catch (SQLException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (ParseException e) {
         System.err.println("Problem occurred parsing the date!");
         System.err.println(e.getLocalizedMessage());
      }
   }

   public void importUserLogs(){

   }

}
