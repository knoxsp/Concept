package scatterbox.simulator;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import scatterbox.properties.Properties;


public class DatabaseHandler {
   /**
    * Database connection for this class
    */
   Connection conn = null;
   /**
    * Logger for the class
    */
   private Logger my_logger = Logger.getLogger(getClass().getName());
   /**
    * The object used to communicate with the database.
    */
   Statement my_statement;

   private String my_userName;
   private String my_password;
   private String my_url;
   private String[] my_queries;

   /**
    * Create a database handler which connects to a database with default information
    */
   public DatabaseHandler(){
      getDatabaseDetails();
      connectToDatabase();
   }

   public DatabaseHandler(String a_userName, String a_password, String a_url){
      my_userName = a_userName;
      my_password = a_password;
      my_url = a_url;
      connectToDatabase();
   }

   /**
    * Get the event queue from the appropriate tables in the database
    * @param a_startTime
    * @param a_duration
    * @return
    */
   public EventQueue getSensorDataAsQueue(String[] some_queries, DateTime a_startTime, int a_duration){
      EventQueue eventQueue = new EventQueue();
      String start = new Timestamp(a_startTime.getMillis()).toString();
      String end = new Timestamp(a_startTime.getMillis() + a_duration).toString();
      String currentQuery;
      ResultSet[] allResults = new ResultSet[some_queries.length];

      for(int i=0; i<some_queries.length; i++){
         currentQuery = some_queries[i];
         currentQuery = currentQuery.replace("'start'", "'"+start+"'").replace("'end'", "'"+end+"'");
         System.out.println(currentQuery);
         allResults[i] = QueryDatabase(currentQuery);
      }
      eventQueue.addData(allResults);

      Collections.sort(eventQueue);
      return eventQueue;
   }

   /**
    * Take a database query and query the database. 
    * Returns a resultset containing the results.
    * @param query
    * @return
    */
   public ResultSet QueryDatabase(String query){
      ResultSet queryResult = null;
      try {

         my_statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

         queryResult = my_statement.executeQuery(query);         

      } catch (SQLException e) {
         my_logger.warning("An exception has occurred while querying the database: \n" + e.getLocalizedMessage());
      }

      return queryResult;

   }

   private boolean connectToDatabase(){
      boolean connected = false;

      try {
         Class.forName ("com.mysql.jdbc.Driver").newInstance();

         conn = DriverManager.getConnection (my_url, my_userName, my_password);

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

   public void getDatabaseDetails(){
      try {
         Properties.load("properties/database.properties");
         JSONObject properties = (JSONObject) Properties.getProperties();
         JSONObject databaseProperties = (JSONObject) properties.get("database");
         my_userName = (String) databaseProperties.get("username");
         my_password = (String) databaseProperties.get("password");
         my_url = (String) databaseProperties.get("url");

         JSONObject queries = (JSONObject) properties.get("queries");
         int numQueries = queries.length();
         my_queries = new String[numQueries];
         for(int i=0;i<numQueries; i++){
            my_queries[i] = (String) queries.get(String.valueOf(i));
         }

      } catch (JSONException e) {
         my_logger.severe("JSON error when loading properties" + e.getLocalizedMessage());
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

}
