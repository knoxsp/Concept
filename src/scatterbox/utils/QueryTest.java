package scatterbox.utils;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.joda.time.DateTime;

import scatterbox.simulator.DatabaseHandler;

public class QueryTest {
   
   
   public static void main(String[] args){
      
      DateTime startDate = new DateTime(2006, 9, 1, 18, 0, 0, 0);
      DateTime endDate = new DateTime(2006, 9, 30, 23, 0, 0, 0);
      getAverageUsage("gas", startDate, endDate);
         
   }
   
   private static void getAverageUsage(String a_tableName, DateTime a_startDate, DateTime an_endDate){
      DatabaseHandler dbh = new DatabaseHandler();
      
      Timestamp startTimestamp = new Timestamp(a_startDate.getMillis());
      Timestamp endTimestamp = new Timestamp(an_endDate.getMillis());
      
      String dateQuery = "select distinct Date(sensed_at) from "+a_tableName+" where sensed_at between "
      + "\""+startTimestamp+"\""
      +" and "
      + "\""+endTimestamp+"\"";
      System.out.println(dateQuery);
      ResultSet dates = dbh.QueryDatabase(dateQuery);
      
      int startTime = 18*60*60*1000;
      int endTime = 23*60*60*1000;
      
      String averageQuery = "SELECT AVG(sensor_reading) FROM placelab.gas where sensed_at";
      String timeClause = " between \"START\" and \"END\" or sensed_at";
      
      try {
         if(dates.next()){
            dates.beforeFirst();
            while(dates.next()){
               String currentTimeClause = timeClause;
                 Date currentDate = dates.getDate(1);
                 Long dateInMillis = currentDate.getTime(); 
                 Long startDateInMillis = dateInMillis + (long)startTime;
                 Timestamp t = new Timestamp(startDateInMillis);
                 Long endDateInMillis = dateInMillis + (long)endTime;
                 Timestamp t1 = new Timestamp(endDateInMillis);
                 currentTimeClause = currentTimeClause.replace("START", t.toString()).replace("END", t1.toString());
                 averageQuery = averageQuery + currentTimeClause;
            }
         }

      averageQuery = averageQuery.substring(0,averageQuery.length()-13);
      System.out.println(averageQuery);
      ResultSet average = dbh.QueryDatabase(averageQuery);
      if(average.next()){
         System.out.println(average.getDouble(1));
      }
      } catch (SQLException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
   
   
   
}
