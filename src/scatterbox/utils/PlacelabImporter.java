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
import java.util.LinkedList;
import java.util.List;

import org.joda.time.DateTimeZone;

public class PlacelabImporter {

   private final String root = "/Users/seamusknox/Documents/datasets/placelab/2006-09-14/";

   Connection conn = null;

   Statement statement = null;

   DateTimeZone my_timeZone = DateTimeZone.forOffsetHours(-4);

   public static void main(String[] args) {

      PlacelabImporter rsf = new PlacelabImporter();

      boolean connected = rsf.connectToDatabase();
      System.out.println(connected);

      //rsf.importRFID();
      rsf.importOtherSensors();

      if (rsf.conn != null) {
         try {
            rsf.conn.close();
         } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
   }

   public boolean connectToDatabase() {
      boolean connected = false;

      String userName = "root";
      String password = "";
      String url = "jdbc:mysql://localhost:3306/placelab";

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

   /**
    * Return a list of strings representing the files contained in the 
    * directory
    * @param rootDirectoryString
    * @return
    */
   private List<String> getDataFiles(String rootDirectoryString) {
      List<String> allDataFiles = new LinkedList<String>();

      File rootDirectory = new File(rootDirectoryString);
      String[] categories = rootDirectory.list();

      /**
       * Lists the files in the root directory of the sensor's readings
       * if the files themselves are kept there, then read them,
       * otherwise, go into each category directory and get the
       * files out. 
       */
      if (categories != null) {
         for (String categoryString : categories) {
            File category = new File(rootDirectoryString + "/" + categoryString);

            if (category.isDirectory()) {
               String[] dataFiles = category.list();

               for (String dataFile : dataFiles) {
                  if (dataFile.contains(".decoded") && !dataFile.contains("~")) {
                     allDataFiles.add(rootDirectoryString + "/"
                           + categoryString + "/" + dataFile);
                  }
               }

            } else {
               if (categoryString.contains(".decoded")) {
                  //If the files are in the root directory for whatever reason
                  allDataFiles.add(rootDirectoryString + "/" + categoryString);
               }
            }
         }
      } else {
         System.err.println("You sure the directory exists?");
      }
      return allDataFiles;
   }

   public void importRFID() {

      List<String> fileList = getDataFiles(root + "rfid");

      File readings = null;

      try {
         for (String fileString : fileList) {
            readings = new File(fileString);

            FileReader fr = new FileReader(readings);
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();

            while (line != null) {
               //System.out.println(fileString);
               //System.out.println(line);
               String[] brokenLine = line.split(" ");

               statement = conn.createStatement();
               //Remove 5 hours from this timestamp to reflect the correct time zone(EST)
               Timestamp timestamp = new Timestamp(Long
                     .parseLong(brokenLine[1])
                     - (5 * 3600 * 1000));

               String computer_id = brokenLine[7];

               String sensor_id = brokenLine[10];

               final String command = "insert into rfid (sensed_at, sensor_id, computer_id, java_type)"
                     + " values (\""
                     + timestamp
                     + "\","
                     + "\""
                     + sensor_id
                     + "\","
                     + "\""
                     + computer_id
                     + "\","
                     + "\"scatterbox.event.RFIDEvent\")";

               System.out.println(command);
               statement.execute(command);
               statement.close();
               line = br.readLine();
            }
            statement.close();
            br.close();
         }
      } catch (FileNotFoundException e) {
         System.err.println("RFID files not found.");
      } catch (IOException e) {
         System.err.println("Reading Failed.");
      } catch (SQLException e) {
         System.err.println("Reading Failed. SQL error ocurred.");
      }

   }

   /**
    * Import either the composite data(location and object) or environmental data
    * depending on the fine name.
    */
   public void importOtherSensors() {
      List<String> fileList = getDataFiles(root);
      for (String fileString : fileList) {
         System.out.println(fileString);
         if (fileString.contains("Composite")) {
            importCompositeData(fileString);
         } else {
            importEnvironmentalData(fileString);
         }
      }
   }

   /**
    * Read the composite data files for each day and enter them into the database
    * @param a_fileName
    */
   private void importCompositeData(String a_fileName) {

      File readings = null;

      try {
         readings = new File(a_fileName);

         FileReader fr = new FileReader(readings);
         BufferedReader br = new BufferedReader(fr);
         String line = br.readLine();

         while (line != null) {
            //System.out.println(a_fileName);
            //System.out.println(line);
            String[] brokenLine = line.split(" ");
            //Get the sensor type first, as we want to only import some sensor types.
            int sensor_type = Integer.parseInt(brokenLine[8]);
            if (sensor_type == 0 || sensor_type == 11) {

               //Remove 5 hours from this timestamp to reflect the correct time zone(EST)
               Timestamp timestamp = new Timestamp(Long
                     .parseLong(brokenLine[1])
                     - (5 * 3600 * 1000));

               int channel = Integer.parseInt(brokenLine[4]);

               int sensor_id = Integer.parseInt(brokenLine[6]);

               String java_type = "";

               java_type = "scatterbox.event.PlacelabEvent";

               int sensor_reading = Integer.parseInt(brokenLine[10]);

               final String command = "insert into objectmotion (sensed_at, sensor_id, sensor_type, sensor_reading, java_type)"
                     + " values (\""
                     + timestamp
                     + "\","
                     + "\""
                     + sensor_id
                     + "\","
                     + "\""
                     + sensor_type
                     + "\","
                     + "\""
                     + sensor_reading
                     + "\","
                     + "\""
                     + java_type
                     + "\")";
               //
               statement = conn.createStatement();
               //System.out.println(command);
               statement.execute(command);
            }
            statement.close();
            line = br.readLine();
         }
         br.close();
         statement.close();
      } catch (FileNotFoundException e) {
         System.err.println("RFID files not found.");
      } catch (IOException e) {
         System.err.println("Reading Failed.");
      } catch (SQLException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   private void importEnvironmentalData(String a_fileName) {

      File readings = null;

      try {
         readings = new File(a_fileName);

         FileReader fr = new FileReader(readings);
         BufferedReader br = new BufferedReader(fr);
         String line = br.readLine();

         while (line != null) {
            //System.out.println(a_fileName);
            //System.out.println(line);
            String[] brokenLine = line.split(" ");
            //Get the sensor type first, as we want to only import some sensor types.
            int sensor_type = Integer.parseInt(brokenLine[7]);
            if (sensor_type == 20 || sensor_type == 22
                  || sensor_type == 23 || sensor_type == 24) {

               //Remove 5 hours from this timestamp to reflect the correct time zone(EST)
               Timestamp timestamp = new Timestamp(Long
                     .parseLong(brokenLine[1])
                     - (5 * 3600 * 1000));

               String sensor_id = brokenLine[10];

               String java_type = "scatterbox.event.PlacelabEvent";
               String table = "";
               if (sensor_type == 20) {
                  table = "light";
               } else if (sensor_type == 22) {
                  table = "gas";
               } else if (sensor_type == 23) {
                  table="current";
               } else if (sensor_type == 24) {
                  table="water";
               }

               double sensor_reading = Double.parseDouble(brokenLine[13]);

               final String command = "insert into "+table+" (sensed_at, sensor_type, sensor_id, sensor_reading, java_type)"
                     + " values (\""
                     + timestamp
                     + "\","
                     + "\""
                     + sensor_type
                     + "\","
                     + "\""
                     + sensor_id
                     + "\","
                     + "\""
                     + sensor_reading
                     + "\","
                     + "\""
                     + java_type
                     + "\")";
               
               statement = conn.createStatement();
               //System.out.println(command);
               statement.execute(command);
            }
            statement.close();
            line = br.readLine();
         }
         br.close();
         statement.close();
      } catch (FileNotFoundException e) {
         System.err.println("RFID files not found.");
      } catch (IOException e) {
         System.err.println("Reading Failed.");
      } catch (SQLException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

}
