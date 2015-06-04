package cbml;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import cbml.cbr.CBMLCase;
import cbml.cbr.CaseStruct;
import cbml.cbr.Feature;
import cbml.cbr.FeatureStruct;
import cbml.cbr.feature.ComplexFeature;
import cbml.cbr.feature.DoubleFeature;
import cbml.cbr.feature.IntegerFeature;
import cbml.cbr.feature.StringFeature;

/**
 * This class implements a CBML case content document parser. The only methods that the user should use are the constructor, the readCasebase(InputSource CaseXML) and the readSkeletalCase(InputSource CaseXML) methods.
 * @author Lorcan Coyle
 * @version 3.0
 */
public class CaseReader extends DefaultHandler {
   //private static final String READER = "org.apache.xerces.parsers.SAXParser";

   static final private Logger logger = Logger.getLogger(CaseReader.class);

   private StringBuffer accumulator;

   private List casebase;

   private Class caseClass;

   private CaseStruct caseStruct;

   private CBMLCase currentCase;

   private String domain;

   private String genealogy;

   private boolean invalidCase;

   private boolean parsingCase;

   private boolean parsingDocument;

   private XMLReader reader;

   private boolean readingCasebase;

   private boolean skeletalCase;

   private boolean strict;

   /**
    * Constructs a Case Content document parser. This parses all case content documents into cases of type <code>caseClass</code> (which must implement <code>cbml.cbr.Case</code>). Validates all future case content documents by the <code>CaseStruct</code> that is passed into this constructor. The level of strictness of this validation can be reduced by passing false into the <code>strict</code> variable (this is not recommended).
    * @param caseClass the <code>Case</code> class that is to be generated.
    * @param caseStruct the case structure definition that all parsed cases must conform to.
    * @param strict <code>true</code> by default. Only change to false if the case or case base are known and expected not to conform exactly to the case structure document.
    */

   // passes in the level of validation - (strict = true) means that numbers of features will be checked
   //												(strict = false) means that they will not be checked
   // NB: (strict = true) will fail most cases in the sponge casebase
   public CaseReader(Class caseClass, CaseStruct caseStruct, Boolean strict) {
      super();
      this.strict = strict.booleanValue();
      this.caseClass = caseClass;
      this.caseStruct = caseStruct;
      skeletalCase = false;
      domain = null;

      try {
         reader = XMLReaderFactory.createXMLReader();
         reader.setContentHandler(this);
         reader.setErrorHandler(new CBMLErrorHandler());
      } catch (SAXException e) {
         logger
               .fatal("ERROR: Problem encountered while initializing the CaseReader.\n"
                     + e);
      }
   }

   /**
    * Receive notification of character data.  This method should only be called internally.
    * @param buffer The characters from the XML document.
    * @param start - The start position in the array.
    * @param length - The number of characters to read from the array.
    */
   public void characters(char[] buffer, int start, int length) {
      accumulator.append(buffer, start, length);
   }

   /**
    * Receive notification of the end of an element.  This method should only be called internally.
    * @param namespaceURI - The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being performed.
    * @param localName - The local name (without prefix), or the empty string if Namespace processing is not being performed.
    * @param fullName - The qualified XML 1.0 name (with prefix), or the empty string if qualified names are not available.
    * @throws SAXException - Any SAX exception, possibly wrapping another exception.
    */
   public void endElement(String namespaceURI, String localName, String fullName)
         throws SAXException {
      if (!parsingDocument)
         logger.warn("The method endElement should only be called internally.");
      String charData = accumulator.toString().trim();
      if (localName.equals("case")) {
         if (!caseStruct.validate(currentCase, true)) {
            invalidCase = true;
            logger.warn("ERROR: base cardinality of features in case: "
                  + currentCase.getName()
                  + " does not match the case structure definition.");
         }
         // add the case to the casebase here
         if (invalidCase)
            logger.warn("Case " + currentCase.getName() + " is invalid");
         else if (!skeletalCase)
            casebase.add(currentCase);
         // anything outside case tags is invalid data, ignore it
         parsingCase = false;
      } else if (invalidCase) {
         // don't process the remainder of the case
         // the error has been reported
      } else if (!parsingCase && localName.equals("casebase")) {
         // do  nothing! maybe check the domain or something
         domain = null;
      } else {
         // the localname is the feature name
         // validifying charData against the constraints of the featureStruct
         try {
            FeatureStruct currentFeatureStruct = (FeatureStruct) caseStruct
                  .getFeatureStruct(genealogy);
            if (currentFeatureStruct != null) {
               //System.out.println(genealogy + " is valid");
               // if the feature is a simple type add it, complex types are already stored
               if (currentFeatureStruct.getType() != FeatureStruct.COMPLEX) {
                  //Added by knox
                  boolean alreadyChecked = false;
                  Feature f = null;
                  /**
                   * new implementation of the SimpleFeature Object...
                   */
                  if (currentFeatureStruct.getType() == FeatureStruct.DOUBLE) {
                     f = new DoubleFeature(genealogy, new Double(charData));
                  } else if (currentFeatureStruct.getType() == FeatureStruct.INTEGER) {
                     f = new IntegerFeature(genealogy, Integer.valueOf(charData));
                  } else {
                     //f = new StringFeature(genealogy, charData);
                     //A case may have a bunch of features stuck together with ands.
                     //Added by knox
                     String[] features = charData.split("AND");
                     for(String s:features){
                        f = new StringFeature(genealogy, s);
                        if(!currentFeatureStruct.validate(f)){
                           invalidCase = true;
                           logger.warn("WARNING Feature " + genealogy
                                 + " with value \"" + charData + "\" is invalid");
                        }
                     }
                     alreadyChecked = true;
                  }
                  
                  /**
                   * ...end
                   */
                  if (!currentFeatureStruct.validate(f) && alreadyChecked == false) {
                     invalidCase = true;
                     logger.warn("WARNING Feature " + genealogy
                           + " with value \"" + charData + "\" is invalid");
                  } else {
                     if (currentFeatureStruct.isSolution()) {
                        currentCase.setSolution(f);
                     } else {
                        currentCase.addFeature(f);
                     }
                  }
               } else if (strict && !skeletalCase) {
                  Feature currentComplexFeature = currentCase
                        .getFeature(genealogy);
                  if (!currentFeatureStruct.validate(currentComplexFeature)) {
                     invalidCase = true;
                     logger.warn("ERROR: wrong number of tags in "
                           + genealogy + " in case: " + currentCase.getName()
                           + ".");
                  }
               }
            }
            genealogy = genealogy.substring(0, genealogy.lastIndexOf("/"));
         } catch (Exception e) {
            // feature is not valid if an exception has been thrown
            // this needs to be more specific! more information needs to be given
            invalidCase = true;
            throw new SAXException("Validification Error: " + e);
         }
      }
   }

   /**
    * Takes a CBML case document in the form of an <code>InputSource</code> and parses it into a case base in the form of a <code>List</code>.
    * @param CaseXML the case content document.
    * @return the generated case base.
    * @throws CBMLException if there was a problem reading the case document. Some further information should be printed to the command line.
    */
   public List readCasebase(InputSource CaseXML) throws CBMLException {
      readingCasebase = true;
      try {
         casebase = new LinkedList();
         parsingDocument = true;
         reader.parse(CaseXML);
         parsingDocument = false;
         return casebase;
      } catch (Exception e) {
         logger.error(e);
         String message = "ERROR READING CASEBASE " + e;
         throw new CBMLException(message);
      }
   }

   /**
    * Takes a CBML case document (containing a single case) and parses it into a single <code>Case</code> object. Because this case is considered skeletal (or partial) the parser will not be checked that all mantidory features are present.
    * @param CaseXML the case content document.
    * @return the generated case.
    * @throws CBMLException if there was a problem reading the case document. Some further information should be printed to the command line.
    */
   public CBMLCase readSkeletalCase(InputSource CaseXML) throws CBMLException {
      readingCasebase = false;
      try {
         skeletalCase = true;
         parsingDocument = true;
         reader.parse(CaseXML);
         parsingDocument = false;
         skeletalCase = false;
         return currentCase;
      } catch (SAXException e) {
         String message = "ERROR READING Case: " + CaseXML;
         throw new CBMLException(message);
      } catch (IOException e) {
         String message = "ERROR READING Case: " + CaseXML;
         throw new CBMLException(message);
      }
   }

   /**
    * Receive notification of the beginning of a document.  This method should only be called internally.
    * @throws SAXException - Any SAX exception, possibly wrapping another exception.
    */
   public void startDocument() {
      if (!parsingDocument)
         logger.warn("The method startDocument should only be called internally.");

      // we initialize our string buffer
      accumulator = new StringBuffer();
      genealogy = "";
   }

   /**
    * Receive notification of the start of an element.  This method should only be called internally.
    * @param nameSpaceURI - The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being performed.
    * @param localName - The local name (without prefix), or the empty string if Namespace processing is not being performed.
    * @param fullName - The qualified name (with prefix), or the empty string if qualified names are not available.
    * @param attributes - The attributes attached to the element. If there are no attributes, it shall be an empty Attributes object.
    * @throws SAXException - Any SAX exception, possibly wrapping another exception.
    */
   public void startElement(String nameSpaceURI, String localName,
         String fullName, Attributes attributes)
         throws org.xml.sax.SAXException {
      if (!parsingDocument)
         logger.warn("The method startElement should only be called internally.");
      accumulator.setLength(0);
      if (localName.equals("case")) {
         // reset everything.
         genealogy = "";
         invalidCase = false;
         parsingCase = true;
         String casename = attributes.getValue("name");
         try {
            Constructor construct = caseClass.getConstructor(null);
            currentCase = (CBMLCase) construct.newInstance(null);
            currentCase.setName(casename);
         } catch (Exception e) {
            e.printStackTrace();
         }
      } else if (invalidCase) {
         // don't process the remainder of the case
         // the error has been reported
         logger.debug("-");
      } else if (!parsingCase && localName.equals("casebase")) {
         domain = attributes.getValue("name");
         // IT MIGHT BE NICE TO PUT THE CAPABILITY TO BUNDLE ALL CASE CONTENT DOCUMENTS INTO ONE AND USE THE DOMAIN ATTRIBUTE ON THE CASEBASE TAG TO REFER TO THE CORRECT STRUCTURE DOCUMENT.
         /*if(domain.equals(caseStruct.getDomain())){
          System.out.println("WARNING The domain refered to in the case content document is not the same as that in the case structure document.");
          }*/
         // do  nothing! maybe check the domain or something
      } else {
         // genealogy is a way to maintain a sense of position in the caseStruct.
         // This allows validation of features with the same name possible
         genealogy += "/" + localName;
         //checkList.add(genealogy);
         FeatureStruct currentFeatureStruct = (FeatureStruct) caseStruct
               .getFeatureStruct(genealogy);
         if (currentFeatureStruct == null) {
            logger.warn("WARNING Feature "
                        + genealogy
                        + " found in case "
                        + currentCase.getName()
                        + " is not specified in the caseStruct. Therefore it cannot be validated and will be ignored");
         } else {
            // can complex types be validated? should be able to do this
            if (currentFeatureStruct.getType() == FeatureStruct.COMPLEX) {
               Feature f = new ComplexFeature(genealogy);
               if (currentFeatureStruct.isSolution()) {
                  currentCase.setSolution(f);
               } else {
                  if (!currentCase.addFeature(f))
                     logger.warn("WARNING: feature " + genealogy
                           + " could not be added to case "
                           + currentCase.getName());
               }

            }
         }
      }
   }
}
