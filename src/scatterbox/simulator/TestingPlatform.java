package scatterbox.simulator;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class TestingPlatform {

   List<DateTime> my_StartTimes = new LinkedList<DateTime>();
   List<DateTime> my_EndTimes = new LinkedList<DateTime>();
   List<Integer> my_ks = new LinkedList<Integer>();
   List<Double> my_thresholds = new LinkedList<Double>();   
   List<String> my_simulationType = new LinkedList<String>();
   /**
    * Each cell may contain a number of file names, separated by commas
    */
   List<String> my_trainingFileSets = new LinkedList<String>();
   int my_numberOfSimulations;
   final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
   File my_TestingFile = new File("XLS/Simulator.xls"); 
   Workbook testWbook;
   Sheet times;
   

   public TestingPlatform(){
      try {
         testWbook = Workbook.getWorkbook(my_TestingFile);
         System.out.println("got workbook");
         times = testWbook.getSheet(0);
         System.out.println("got sheet");
      } catch (BiffException e) {
         System.err.println("A Biff exception has occurred: "+e.getMessage());
      } catch (IOException e) {
         System.err.println("An IO exception has occurred: "+e.getMessage());
      }   

   }

   private void getSimulationInformation(){
      Cell[] simulationType = times.getColumn(0);
      Cell[] startTimes = times.getColumn(1);
      Cell[] endTimes = times.getColumn(2);
      Cell[] ks = times.getColumn(3);
      Cell[] thresholds = times.getColumn(4);
      Cell[] trainingFiles = times.getColumn(5);

      //i=1 because of the title row
      for(int i=1;i<simulationType.length;i++){
         String simType = simulationType[i].getContents();
         if(!simType.equalsIgnoreCase("")){
            my_simulationType.add(simulationType[i].getContents());
            my_StartTimes.add(dateTimeFormatter.parseDateTime(startTimes[i].getContents()));
            my_EndTimes.add(dateTimeFormatter.parseDateTime(endTimes[i].getContents()));
            my_ks.add(Integer.parseInt(ks[i].getContents()));
            my_thresholds.add(Double.parseDouble(thresholds[i].getContents()));
            my_trainingFileSets.add(trainingFiles[i].getContents());
         }
      }
   }

   public static void main(String[] args) {
      TestingPlatform tp = new TestingPlatform();
      tp.getSimulationInformation();
      Simulator simulator = null;
      for(int i=0; i<tp.my_simulationType.size();i++){
         System.out.println(tp.my_simulationType.get(i));
         if(tp.my_simulationType.get(i).equalsIgnoreCase("tvk")){
            simulator = new KasterenSimulator(
                  tp.my_StartTimes.get(i), 
                  tp.my_EndTimes.get(i), 
                  tp.my_ks.get(i), 
                  tp.my_thresholds.get(i), 
                  tp.my_trainingFileSets.get(i));
         }else if(tp.my_simulationType.get(i).equalsIgnoreCase("placelab")){
            simulator = new PlacelabSimulator(
                  tp.my_StartTimes.get(i), 
                  tp.my_EndTimes.get(i), 
                  tp.my_ks.get(i), 
                  tp.my_thresholds.get(i), 
                  tp.my_trainingFileSets.get(i));
         }else if(tp.my_simulationType.get(i).equalsIgnoreCase("casl")){
            simulator = new CASLSimulator(
                  tp.my_StartTimes.get(i), 
                  tp.my_EndTimes.get(i), 
                  tp.my_ks.get(i), 
                  tp.my_thresholds.get(i), 
                  tp.my_trainingFileSets.get(i));
         }
         simulator.simulate();
         while (!simulator.isDone()) 
         { 
            try { 
               Thread.sleep(100); 
            } 
            catch (InterruptedException e) {} 
         }
      }
   }
}