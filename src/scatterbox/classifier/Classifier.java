package scatterbox.classifier;

import java.io.IOException;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import scatterbox.event.BluetoothEvent;
import scatterbox.event.CalendarEvent;
import scatterbox.event.IMEvent;
import scatterbox.event.UbisenseEvent;
import scatterbox.properties.Properties;
import cbml.cbr.feature.StringFeature;

public class Classifier {
   /**
    * Logger for the class
    */
   private Logger my_logger = Logger.getLogger(getClass().getName());

   /**
    * Confidence of a reading being within 4 metres squared
    * Ex: a person's desk
    */
   int smallAreaConfidence;
   /**
    * Confidence of a reading being within 25 metres squared
    * Ex: A cubicle
    */
   int mediumAreaConfidence;

   /**
    * Confidence of a reading being within a floor
    */
   int floorConfidence;

   public static JSONObject decayProperties;

   public Classifier(){
      getClassifierDetails();
      getDecayDetails();
   }

   public Classification classifyLocation(
         ClassificationSortedSet a_setOfLocations) {
      Classification location = null;
      int size = a_setOfLocations.size();
      int confidenceOfFirst = a_setOfLocations.get(0).getConfidence();
      StringFeature[] locationFeatures;
      int[] confidences;
      int numFeatures = 0;
      //If there are no locations defined or none are currently classified
      if ((size == 0) || (confidenceOfFirst == 0)) {
         location = null;
      } else {
         /*
          * Make a feature of each classification if its confidence > 0
          * Then use the location similarity measure as well as the confidence to decide
          * on the best candidate.
          */
         for (int i = 0; i < size; i++) {
            int confidence = a_setOfLocations.get(i).getConfidence();
            if (confidence > 0) {
               numFeatures++;
            }
         }
         /*
          *TODO Is there a situation where the first location in the list is NOT the
          *location we want to pick?
          */
         //         if (numFeatures == 1) {
         //            System.out.println(a_setOfLocations.get(0).getValue() + "("
         //                  + a_setOfLocations.get(0).getConfidence() + ")");
         location = new Classification(a_setOfLocations.get(0).getType(),
               a_setOfLocations.get(0).getValue(), 100);
         //         } else {
         //            locationFeatures = new StringFeature[numFeatures];
         //            confidences = new int[numFeatures];
         //            for (int i = 0; i < numFeatures; i++) {
         //               String type = a_setOfLocations.get(i).getType();
         //               String value = a_setOfLocations.get(i).getValue();
         //               confidences[i] = a_setOfLocations.get(i).getConfidence();
         //               locationFeatures[i] = new StringFeature(type, value);
         //            }
         //
         //            for (int i = 0; i < numFeatures - 1; i++) {
         //               double similarity = simMeasure.calculateSimilarity(
         //                     locationFeatures[i], locationFeatures[i + 1]);
         //               if (similarity > 0) {
         //                  System.out.println(locationFeatures[i] + "(" + confidences[i]
         //                        + ")" + "VERSUS" + locationFeatures[i + 1] + "("
         //                        + confidences[i + 1] + ") -- Similarity = " + similarity);
         //               }
         //            }
         //         }
      }
      return location;
   }

   /**
    * Bluetooth classification currently only classifies location. 
    * Decay of location occurrs on a case-by-case basis.
    * The decay gets set in the constructor of the classification. 
    */
   public Classification classifyBluetooth(BluetoothEvent a_bluetoothEvent) {
      String spotterID = a_bluetoothEvent.getSpotterID();
      int bluetoothAccuracyTax = 20;
      Classification feature = null;
         if (spotterID.equalsIgnoreCase("000D931AE13D")) {
            feature = new Classification("/location", "CASLFourthFloorCommonArea",
                  mediumAreaConfidence - bluetoothAccuracyTax);
         } else {
            feature = new Classification("/location", "CASLThirdFloor", floorConfidence - bluetoothAccuracyTax);
         }
      return feature;
   }

   /**
    * Decay of location occurs on a case-by-case basis.
    * The decay gets set in the constructor of the classification
    * Ideally, this will be replaced, along with the bluetooth sensor
    * with an ontology-based location reasoning system.
    * @param a_ubisenseEvent 
    */
   public Classification classifyUbisense(UbisenseEvent a_ubisenseEvent) {
      double x = a_ubisenseEvent.getX();
      double y = a_ubisenseEvent.getY();
      double z = a_ubisenseEvent.getZ();
      Classification location = null;

      //On the 3rd floor
      if (z < 3) {
         //In Cubicle f
         if (x > 25 && x < 30 && y < 5.5) {
            if (x <= 27 && y <= 1.9) {
               location = new Classification("/location", "LorcanDesk", smallAreaConfidence);
            } else if (x >= 28 && y < 1.9) {
               location = new Classification("/location", "AdoDesk", smallAreaConfidence);
            } else if (x >= 28 && y > 3) {
               location = new Classification("/location", "KnoxDesk", smallAreaConfidence);
            } else {
               location = new Classification("/location",
                     "CASLThirdFloorCubeF", mediumAreaConfidence);
            }
         }else if(x > 20 && x<25 && y < 5.5){
            location = new Classification("/location",
                  "CASLThirdFloorCubeG", mediumAreaConfidence);
            //InternArea -- open space outside offices
         } else if (x > 4 && x < 10) {
            location = new Classification("/location", "internArea", mediumAreaConfidence);
         } else if (x > 10 && x < 20 && y > 8) {
            //Where the lifts are
            location = new Classification("/location", "ThirdFloorFoyer", mediumAreaConfidence);
         } else if (x > 10 && y > 5 && y <= 8) {
            //corridor between cubicles
            location = new Classification("/location", "ThirdFloorCorridor",
                  mediumAreaConfidence);
         } else {
            location = new Classification("/location", "CASLThirdFloor", floorConfidence);
         }
         //On the 4th floor
      } else {
         //In the common area
         if (x < 10.2) {
            //The "comfy seating area"
            if (y > 11.5) {
               location = new Classification("/location",
                     "CASLCommonRoomComfySeatingArea", mediumAreaConfidence);
            } else if (y < 3) {
               location = new Classification("/location", "RocketFoods", smallAreaConfidence);
            } else {
               location = new Classification("/location", "EatingArea", mediumAreaConfidence);
            }
            // Outside the elevators on the fourth floor
         } else {
            location = new Classification("/location", "FourthFloorFoyer", mediumAreaConfidence);
         }

      }
      return location;
   }

   public Classification classifyIM(IMEvent an_IMEvent) {
      String availability = an_IMEvent.getAvailability();
      Classification status;

      if (availability.equalsIgnoreCase("available")) {
         status = new Classification("/IM", "available", 100);
      } else if (availability.equalsIgnoreCase("idle")) {
         status = new Classification("/IM", "idle", 100);
      } else if (availability.equalsIgnoreCase("dnd")) {
         status = new Classification("/IM", "dnd", 100);
      } else {
         status = new Classification("/IM", "away", 100);
      }
      return status;
   }

   public Classification classifyActivity(Long a_timeDifference) {
      Classification activity;
      if (a_timeDifference <= 10000) {
         activity = new Classification("/activity", "tenseconds", 100);
      } else if (a_timeDifference > 10000 && a_timeDifference <= 60000) {
         activity = new Classification("/activity", "minute", 100);
      } else if (a_timeDifference > 60000 && a_timeDifference <= 3600000) {
         activity = new Classification("/activity", "hour", 100);
      } else {
         activity = new Classification("/activity", "overhour", 100);
      }
      return activity;
   }

   /**
    * Classify a calendar event based on start time, end, title, location and description.
    * Currently, a naive title and description classification is used.
    * @param a_calendarEvent
    * @return
    */
   public Classification classifyCalendar(CalendarEvent a_calendarEvent) {
      //TODO Make the classifiation smarter by incorporating the location
      Classification calendarClassification;
      String eventDetails = a_calendarEvent.my_title + a_calendarEvent.my_description;
      String[] meetingKeywords = {"meeting", "meet", "discuss", "review"};
      if (matchPresent(meetingKeywords, eventDetails)) {
         calendarClassification = new Classification("/calendar", "meeting",
               100);
      } else if (eventDetails.contains("break")) {
         calendarClassification = new Classification("/calendar", "break", 100);
      } else if (eventDetails.contains("lunch")) {
         calendarClassification = new Classification("/calendar", "lunch", 100);
      } else {
         //The default classification of a calendar entry which cannot be classified
         //is "busy". The assumption behind this is that if a calendar entry is made, the user must
         //be doing *something* planned in advance, hence may be busy.
         calendarClassification = new Classification("/calendar", "busy", 100);
      }
      return calendarClassification;
   }

   /**
    * Checks whether the string in question matches any of the 
    * words contained in a string array.
    * @param a_stringArray
    * @param a_string
    * @return
    */
   private boolean matchPresent(String[] a_stringArray, String a_string){
      boolean match = false;
      for(String t:a_stringArray){
         if(a_string.contains(t)) match = true;
      }
      return match;
   }

   public Classification classifyTime(DateTime a_time){
      StringFeature timeFeature = getTimeframe(a_time);
      Classification timeClassification = new Classification("/time", (String) timeFeature.getValue(), 100,10);
      return timeClassification;
   }
   
   /**
    * Split a day into a set of classified time frames.
    * This currently splits a day into half-hour time slots.
    * @param a_time
    * @return
    */
   public StringFeature getTimeframe(DateTime a_time) {
      StringFeature timeframe = null;
      int hour = a_time.getHourOfDay();
      int minute = a_time.getMinuteOfHour();
      int day = a_time.getDayOfWeek();
      
      String startTime;
      String endTime;
      
      if(minute >= 30){
         startTime = ""+hour+"30";
         int endHour = (hour+1);
         if(endHour == 25) endHour=0;
         endTime = ""+(hour+1)+"00";
      }else{
         startTime= ""+hour+"00";
         endTime=""+hour+"30";
      }
      
      timeframe = new StringFeature("/time", startTime+"-"+endTime);
 
      
      return timeframe;
   }

   public void getClassifierDetails(){
      try {
         Properties.load("properties/classifier.properties");
         JSONObject properties = Properties.getProperties();
         JSONObject locationProperties = (JSONObject) properties.get("location");
         smallAreaConfidence = (Integer) locationProperties.get("level3");
         mediumAreaConfidence = (Integer) locationProperties.get("level2");
         floorConfidence = (Integer) locationProperties.get("level1");
      } catch (JSONException e) {
         my_logger.severe("JSON error when loading properties" + e.getLocalizedMessage());
      } catch (IOException e) {
         my_logger.severe("IO error when loading properties" + e.getLocalizedMessage());
      }
   }
   
   public void getDecayDetails(){
      try {
         Properties.load("properties/decay.properties");
          decayProperties = Properties.getProperties();
      } catch (JSONException e) {
         my_logger.severe("JSON error when loading properties" + e.getLocalizedMessage());
      } catch (IOException e) {
         my_logger.severe("IO error when loading properties" + e.getLocalizedMessage());
      }
   }
}
