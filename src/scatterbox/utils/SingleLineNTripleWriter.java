//
// $Revision:$
// $Date:$
// $Author:$
//
// This file is part of Construct, a context-aware systems platform.
// Copyright (c) 2006, 2007, 2008 UCD Dublin. All rights reserved.
//
// Construct is free software; you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as
// published by the Free Software Foundation; either version 2.1 of
// the License, or (at your option) any later version.
//
// Construct is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with Construct; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
// USA
//
// Further information about Construct can be obtained from
// http://www.construct-infrastructure.org
package scatterbox.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.NTripleWriter;

/**
 * Writes out an XML serialization of a model. Overwrites the write methods from
 * the original Jena implementation to remove newline characters.
 * @author Graeme Stevenson (graeme.stevenson@ucd.ie)
 */
public class SingleLineNTripleWriter extends NTripleWriter {

   /**
    * Write out a model using the provided base uri to a Writer.
    * @param a_baseModel the model to write.
    * @param a_writer the Writer to write to.
    * @param the_baseUri the base URI of the model.
    */
   public final void write(final Model a_baseModel, final Writer a_writer,
         final String the_baseUri) {
      final Model model = ModelFactory.withHiddenStatements(a_baseModel);
      final PrintWriter printWriter;
      if (a_writer instanceof PrintWriter) {
         printWriter = (PrintWriter) a_writer;
      } else {
         printWriter = new PrintWriter(a_writer);
      }

      final StmtIterator iter = model.listStatements();
      Statement stmt = null;

      while (iter.hasNext()) {
         stmt = iter.nextStatement();
         writeResource(stmt.getSubject(), printWriter);
         printWriter.print(" ");
         writeResource(stmt.getPredicate(), printWriter);
         printWriter.print(" ");
         writeNode(stmt.getObject(), printWriter);
         printWriter.print(" . ");
      }
      printWriter.flush();
   }

   /**
    * Write out a model to a PrintWriter.
    * @param a_model the model to write.
    * @param a_writer the PrintWriter to write to.
    * @throws IOException if an error occurs writing to the writer.
    */
   public static final void write(final Model a_model, final PrintWriter a_writer)
      throws IOException {
      final StmtIterator iter = a_model.listStatements();
      Statement stmt = null;
      while (iter.hasNext()) {
         stmt = iter.nextStatement();
         writeResource(stmt.getSubject(), a_writer);
         a_writer.print(" ");
         writeResource(stmt.getPredicate(), a_writer);
         a_writer.print(" ");
         writeNode(stmt.getObject(), a_writer);
         a_writer.print(" . ");
      }
   }

}
