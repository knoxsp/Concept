//
// $Revision: 14418 $
// $Date: 2009-05-15 15:39:32 +0100 (Fri, 15 May 2009) $
// $Author: graeme $
//
// This file is part of SenseStar, a tool for recording and and managing access
// to sensor data. Copyright (c) 2009. This work is jointly owned by Graeme
// Stevenson and University College Dublin.
//
// SenseStar is free software: you can redistribute it and/or modify it under
// the terms of the GNU Lesser General Public License as published by the Free
// Software Foundation, either version 3 of the License, or (at your option) any
// later version.
//
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
// FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
// details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>.
package scatterbox.properties;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class loads the JSON properties file from disk.
 * @author Graeme Stevenson (graeme.stevenson@ucd.ie)
 */
public final class Properties {

   /**
    * The properties in JSON format.
    */
   private static JSONObject my_properties;

   /**
    * Exists to defeat instantiation.
    */
   private Properties() {
      // Exists to defeat instantiation.
   }

   /**
    * Loads a JSON object from the properties file.
    * @return the JSON object from the properties file, or null if it doesn't exist or is
    *         incorrect.
    */
   public static synchronized JSONObject getProperties() {
      return my_properties;
   }

   /**
    * Loads a JSON object from the properties file.
    * @param a_fileName the name of the file to load. Should be relative to the root directory
    *           of the sensor.
    * @throws JSONException if the properties file contains errors.
    * @throws IOException if an error occurs loading the property file.
    */
   public static synchronized void load(final String a_fileName) throws JSONException, IOException {
      final File file = new File(a_fileName);
      my_properties = new JSONObject(FileIO.load(file));
   }

   /**
    * Converts a JSON array into a java array of Strings.
    * @param a_jsonArray a JSONArray object.
    * @return an array of Java Strings that match the contents of the JSONArray.
    * @throws JSONException if an error occurs accessing the JSON object.
    */
   public static String[] toArray(final JSONArray a_jsonArray) throws JSONException {
      final String[] result = new String[a_jsonArray.length()];
      for (int i = 0; i < result.length; i++) {
         result[i] = a_jsonArray.getString(i);
      }
      return result;
   }

   /**
    * Turns the properties of a JSONObject into a map. It expects all attributes and values to
    * be Strings.
    * @param an_object the JSON object whose parameter's should be transformed into a map.
    * @throws JSONException if an error occurs accessing the JSON object.
    * @return a map of the object's properties.
    */
   public static Map<String, String> toMap(final JSONObject an_object) throws JSONException {
      final Map<String, String> map = new Hashtable<String, String>();
      final Iterator<String> keyIterator = an_object.keys();
      while (keyIterator.hasNext()) {
         final String key = keyIterator.next();
         map.put(key, an_object.getString(key));
      }

      return map;
   }
}
