package scatterbox.utils;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.joda.time.DateTime;

public class ResultsToXLS {
   File my_ResultsFile = new File("XLS/Results.xls"); 
   WritableWorkbook my_resultsWorkbook;
   WritableSheet resultSheet;
   
   public ResultsToXLS(){
      try {
         Workbook workbook = Workbook.getWorkbook(my_ResultsFile);
         WorkbookSettings wbSettings = new WorkbookSettings();

         wbSettings.setLocale(new Locale("en", "EN"));

         my_resultsWorkbook = Workbook.createWorkbook(my_ResultsFile, workbook);
         
         resultSheet = my_resultsWorkbook.getSheet(new DateTime().dayOfMonth().getAsString());
         if(resultSheet == null){
            resultSheet =  my_resultsWorkbook.createSheet(new DateTime().dayOfMonth().getAsString(), 0);
         }
      
      } catch (IOException e) {
         System.err.println("An IO exception has occurred: "+e.getMessage());
      } catch (BiffException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } 
   }
   
   
   public boolean addResult(DateTime startTime, DateTime endTime, String[] trainingFiles, String activity,  int k, double t, double attempts, double recall, double precision, double fMeasure, int casebaseSize){
      
      int numRows = resultSheet.getRows();      
      try {
         resultSheet.addCell(new Label(0, numRows, startTime.toString()));
         resultSheet.addCell(new Label(1, numRows, endTime.toString()));
         String allTrainingFiles = "";
         for(String s: trainingFiles){
            allTrainingFiles+=(s+", ");
         }
         resultSheet.addCell(new Label(2, numRows, allTrainingFiles));
         resultSheet.addCell(new Label(3, numRows, activity));
         resultSheet.addCell(new Number(4, numRows, k));
         resultSheet.addCell(new Number(5, numRows, t));
         resultSheet.addCell(new Number(6, numRows, attempts));
         resultSheet.addCell(new Number(7, numRows, recall));
         resultSheet.addCell(new Number(8, numRows, precision));
         resultSheet.addCell(new Number(9, numRows, fMeasure));
         //resultSheet.addCell(new Number(10, numRows, casebaseSize));
      } catch (RowsExceededException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (WriteException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return true;
   }
   
   /**
    * This closes the stream and saves the xls file. 
    */
   public void closeXLS(){
      try {
         my_resultsWorkbook.write();
      my_resultsWorkbook.close();
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (WriteException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
   
   public static void main(String[] args){
      ResultsToXLS x = new ResultsToXLS();
      String[] arr = {"a", "b", "c"};
      x.addResult(new DateTime(), new DateTime(),arr,"", 1, 2.0, 3.0, 4.0, 5.0, 6.0,7);
   }
}
