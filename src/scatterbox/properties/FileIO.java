//
// $Revision: 14412 $
// $Date: 2009-05-14 20:14:02 +0100 (Thu, 14 May 2009) $
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * A utility class to load in data.
 * @author Graeme Stevenson (graeme.stevenson@ucd.ie).
 */
public final class FileIO {

   /**
    * Exists to defeat instantiation.
    */
   private FileIO() {
      // Exists to defeat instantiation
   }

   /**
    * Fetch data from an file on disk.
    * @param a_file the file to load from disk.
    * @return a string containing the contents of the specified file.
    * @throws IOException if there is a problem opening or closing the file.
    */
   public static String load(final File a_file) throws IOException {
      final StringBuilder result = new StringBuilder();
      // read in one line at a time
      final BufferedReader input = new BufferedReader(new FileReader(a_file));
      try {
         String line = null;
         while ((line = input.readLine()) != null) {
            result.append(line);
            result.append(System.getProperty("line.separator"));
         }
      } finally {
         input.close();
      }
      return result.toString();
   }
}
