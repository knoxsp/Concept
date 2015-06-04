package cbml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import cbml.cbr.CaseStruct;
import cbml.cbr.FeatureStruct;
import cbml.cbr.IncompatableFeatureException;
import cbml.cbr.SimilarityProfile;
import cbml.cbr.types.BooleanFeatureStruct;
import cbml.cbr.types.ComplexFeatureStruct;
import cbml.cbr.types.DoubleFeatureStruct;
import cbml.cbr.types.IntegerFeatureStruct;
import cbml.cbr.types.StringFeatureStruct;
import cbml.cbr.types.SymbolFeatureStruct;
import cbml.cbr.types.TaxonomyFeatureStruct;

/**
 * This class implements a CBML Case Structure and Similarity Measure document parser. The only methods that the user should use are the constructor and the readCBMLDocument methods.
 * @author Lorcan Coyle
 * @version 3.0
 */

public class CBMLReader extends DefaultHandler {
	//protected static final String READER = "org.apache.xerces.parsers.SAXParser";

   static final private Logger logger = Logger
   .getLogger(CBMLReader.class);

   
	/**
	 * The URL of the default cbml schema document. Currently this is located <a href='http://www.cs.tcd.ie/research_groups/mlg/CBML/Schema/cbmlv3.xsd'>here</a>.
	 */
	public static final String schemaURL = "http://www.cs.tcd.ie/research_groups/mlg/CBML/Schema/cbmlv3.xsd";

	protected CaseStruct caseStruct;
	private List complexFeatureTree;

	private boolean currentFeatureDiscriminant;
	private boolean currentFeatureManditory;
	private boolean currentFeatureSolution;
	private FeatureStruct currentFeatureStruct;
	protected SimilarityProfile currentSimProfile;
	protected String domainName;
	private String genealogy;

	// A Case Structure object is allowed only one solution feature
	private boolean gotSolutionFeature;
	protected boolean ignore;
	protected boolean parsingArray;
	protected boolean parsingDocument;
	protected boolean parsingGraph;
	private int primary;
	private boolean processingFeature;
	private boolean processingTaxonomy;
	private boolean processingType;

	// for similarity 
	protected Hashtable profiles;

	// for reading the document	
	protected XMLReader reader;
	private boolean readingSimilarity;

	// for structure
	private boolean readingStructure;
	// contains a list of all features that refer to other features
	protected List referees;

	// this tells us if the current featurestruct is a reference. Very important in the case of complex features
	// without it the current featurestruct (taken from the references hashmap) is overwritten with the parent feature struct (from complexFeatureTree)
	protected Hashtable references;
	private double[][] similarities;
	private List values;

	/**
	 * Constructs a Case Structure document parser. Uses the default CBML schema document (<code>http://www.cs.tcd.ie/research_groups/mlg/CBML/Schema/cbmlv3.xsd</code>) to validate the structure document it parses.
	 */
	public CBMLReader() {
		super();
		references = new Hashtable();
		referees = new ArrayList();
		profiles = new Hashtable();
		try {
			reader = XMLReaderFactory.createXMLReader();
			reader.setContentHandler(this);
			reader.setFeature("http://xml.org/sax/features/validation", true);
			reader.setFeature("http://apache.org/xml/features/validation/schema", true);
			reader.setEntityResolver(new EntityResolver() {
				public InputSource resolveEntity(String arg0, String arg1) throws SAXException, IOException {
					InputStream stream;
					if (arg1.equals(schemaURL))
						stream = getClass().getResourceAsStream("cbmlv3.xsd");
					else
						stream = getClass().getResourceAsStream("cbmlv3.xsd");

					return new InputSource(stream);
				}
			});
			//reader.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
		} catch (SAXException e) {
			logger.fatal("ERROR: Problem encountered while initializing the CBMLReader.\n" + e);
		}
	}

	/**
	 * Receive notification of the end of an element. This method should only be called internally.
	 * @param nameSpaceURI - The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being performed.
	 * @param localName - The local name (without prefix), or the empty string if Namespace processing is not being performed.
	 * @param fullName - The qualified XML 1.0 name (with prefix), or the empty string if qualified names are not available.
	 * @throws SAXException - Any SAX exception, possibly wrapping another exception.
	 */
	public void endElement(String nameSpaceURI, String localName, String fullName) throws org.xml.sax.SAXException {
		if (!parsingDocument)
			logger.warn("The method endElement should only be called internally.");

		if (localName.equals("case")) {
			// do nothing
			ignore = false;
			return;
		} else if (ignore)
			return;

		try
		{
			if (readingSimilarity) {
				if (localName.equals("similarity")) {
					readingSimilarity = false;
				} else
					endSimilarityElement(nameSpaceURI,localName,fullName); 
			} else if (readingStructure) {
				if (localName.equals("structure")) {
					readingStructure = false;
				} else
					endStructureElement(nameSpaceURI,localName,fullName);
			}
		}
		catch (IncompatableFeatureException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Receive notification of the end of an element in similarity section of document. This method should only be called internally.
	 * @param nameSpaceURI - The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being performed.
	 * @param localName - The local name (without prefix), or the empty string if Namespace processing is not being performed.
	 * @param fullName - The qualified XML 1.0 name (with prefix), or the empty string if qualified names are not available.
	 * @throws SAXException - Any SAX exception, possibly wrapping another exception.
	 * @author doyledp
	 */	
	protected void endSimilarityElement(String nameSpaceURI, String localName, String fullName) throws org.xml.sax.SAXException {
		
		if (localName.equals("array")) {
			currentSimProfile.setSimilarityMatrix(genealogy, similarities);
			values = null;
			similarities = null;
			parsingArray = false;
		} else if (localName.equals("feature")) {
			genealogy = genealogy.substring(0, genealogy.lastIndexOf("/"));
		} else if (localName.equals("primary")) {
			primary = -1;
		} else if (localName.equals("similarity")) {
			readingSimilarity = false;
		} else if (localName.equals("graph")) {
			parsingGraph = false;
		}		
	}
	
	/**
	 * Receive notification of the end of an element in structure section of document. This method should only be called internally.
	 * @param nameSpaceURI - The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being performed.
	 * @param localName - The local name (without prefix), or the empty string if Namespace processing is not being performed.
	 * @param fullName - The qualified XML 1.0 name (with prefix), or the empty string if qualified names are not available.
	 * @throws SAXException - Any SAX exception, possibly wrapping another exception.
	 * @author doyledp
	 */	
	protected void endStructureElement(String nameSpaceURI, String localName, String fullName) throws org.xml.sax.SAXException, IncompatableFeatureException {
		if (processingTaxonomy) {
			// We are processing a taxonomy. The only escape tag is the "<\taxonomy>" tag
			if (localName.equals("taxonomy")) {
				processingTaxonomy = false;
				processingType = false;
			} else {
				currentFeatureStruct.taxonomyEndElement();
			}
		} else if (processingType) {
			// We are reading a simple feature, waiting for a feature type tag. The only available tags are {boolean,double,integer,string,symbol}
			if (localName.equals("symbol") || localName.equals("double") || localName.equals("integer") || localName.equals("boolean") || localName.equals("string")) {
				processingType = false;
			}
		} else if (localName.equals("feature")) {
			genealogy = genealogy.substring(0, genealogy.lastIndexOf("/"));
			// checks to see if we are in the definition of a complex feature already (nested)
			if (complexFeatureTree.isEmpty()) {
				caseStruct.addFeatureStruct(currentFeatureStruct);
			} else {
				FeatureStruct parent = (FeatureStruct) complexFeatureTree.get(complexFeatureTree.size() - 1);
				parent.addSubFeatureStruct(currentFeatureStruct);
			}
		} else if (localName.equals("complex")) {
			// We are at the end of reading a complex feature
			if (complexFeatureTree.isEmpty()) {
				throw new SAXException("ERROR: Unexpected tag \"" + localName + "\" encountered.");
			}
			for (int i = 0; i < referees.size(); i++) {
				String refereePath = (String) referees.get(i);
				if (refereePath.equals(genealogy)) {
					referees.remove(i);
					break;
				}
			}
			currentFeatureStruct = (FeatureStruct) complexFeatureTree.remove(complexFeatureTree.size() - 1);
			/*
						else if (!currentFeatureReference) {
							// must check if we have referenced this feature?
							// if we have then the value stored in currentFeatureStruct is correct and should not be overwritten
							currentFeatureStruct = (FeatureStruct) complexFeatureTree.remove(complexFeatureTree.size() - 1);
						}*/
		} else {
			logger.warn("WARNING: unexpected tag encountered (" + localName + ").");
		}
	}
	/**
	 * Returns the CaseStruct that has been read by this Reader
	 * @return the CaseStruct that has been read by this Reader. This will be empty if no Case structure document has been read by this parser.
	 */
	public CaseStruct getCaseStruct() {
		return caseStruct;
	}

	/**
	 * THIS IS TEMPORARY AND IS ONLY USED BY LORCAN COYLE
	 * @param newCaseStruct
	 * @return
	 */
	public void setCaseStruct(CaseStruct newCaseStruct) {
		caseStruct = newCaseStruct;
	}
	/**
	 * Returns a Hashtable of SimilarityProfile objects referenced by username that has been read by this reader.
	 * @return a Hashtable of SimilarityProfile objects referenced by username that has been read by this reader. This will be empty if no Similarity Profile document has been read by this parser.
	 */
	public Hashtable getSimilarities() {
		return profiles;
	}
	/**
	 * Takes the InputSource of a case structure document and parses it into a CaseStruct object. 
	 * @param source the <code>InputSource</code> of the case structure document to be parsed.
	 * @return CaseStruct The generated Case Structure object.
	 * @throws CBMLException if the case structure document is invalid. Some further information should be printed to the command line.
	 */
	public boolean readCBMLDocument(InputSource source) throws CBMLException {
		try {
			parsingDocument = true;
			reader.parse(source);
			parsingDocument = false;
			return true;
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	/**
	 * Receive notification of the beginning of a document. This method should only be called internally.
	 * @throws SAXException - Any SAX exception, possibly wrapping another exception.
	 */
	public void startDocument() throws org.xml.sax.SAXException {
		if (!parsingDocument)
			logger.warn("The method startDocument should only be called internally.");

		complexFeatureTree = new ArrayList();
		ignore = false;
		processingTaxonomy = false;
		readingSimilarity = false;
		readingStructure = false;
		parsingArray = false;
		parsingGraph = false;
		currentFeatureSolution = false;
		gotSolutionFeature = false;
		genealogy = "";
	}
	/**
	 * Receive notification of the start of an element. This method should only be called internally.
	 * @param nameSpaceURI - The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being performed.
	 * @param localName - The local name (without prefix), or the empty string if Namespace processing is not being performed.
	 * @param fullname - The qualified name (with prefix), or the empty string if qualified names are not available.
	 * @param attributes - The attributes attached to the element. If there are no attributes, it shall be an empty Attributes object.
	 * @throws SAXException - Any SAX exception, possibly wrapping another exception.
	 */
	public void startElement(String nameSpaceURI, String localName, String fullname, org.xml.sax.Attributes attributes) throws org.xml.sax.SAXException {
		if (!parsingDocument)
			logger.warn("The method startElement should only be called internally.");
		if (ignore)
			return;

		try {
			if (readingSimilarity) {
				startSimilarityElement(nameSpaceURI, localName, fullname, attributes);
			} else if (readingStructure) {
				startStructureElement(nameSpaceURI, localName, fullname, attributes);
			} else if (localName.equals("case")) {
				domainName = attributes.getValue("domain");
			} else if (localName.equals("structure")) {
				readingStructure = true;
				caseStruct = new CaseStruct(domainName);
			} else if (localName.equals("similarity")) {
				String username = attributes.getValue("username");
				if (username == null) {
					ignore = true;
					return;
				}
				// this user extends his/her profile from this parentUser
				String parentUsername = attributes.getValue("extends");
				if (!(parentUsername == null || parentUsername.equals(""))) {
					SimilarityProfile parentProfile = ((SimilarityProfile) profiles.get(parentUsername));
					if (parentProfile != null) {
						currentSimProfile = (SimilarityProfile) parentProfile.clone();
						currentSimProfile.setUser(username);
					}
				}
				if (currentSimProfile == null)
					currentSimProfile = new SimilarityProfile(caseStruct, username, domainName);
				profiles.put(username, currentSimProfile);
				readingSimilarity = true;
			} else {
				logger.warn("WARNING: Unrecognised Tag \"" + localName + "\".");
			}
		} catch (IncompatableFeatureException e) {
			e.printStackTrace();
			throw new SAXException("ERROR: Unrecognised Tag \"" + localName + "\" found in type " + FeatureStruct.types[currentFeatureStruct.getType()]);
		} /* catch (FeatureNotFoundException e) {
																			throw new SAXException("ERROR READING PROFILE FILE: feature " + genealogy + " could not be found in the caseStruct. Check the struct file to see if it has been defined.");
																		} */
		catch (Exception e) {
			e.printStackTrace();
			throw new SAXException(e.getMessage() + "(genealogy was " + genealogy + ")");
		}
	}
	
	/**
	 * Receive notification of the start of an element in similarity section of document. This method should only be called internally.
	 * @param nameSpaceURI - The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being performed.
	 * @param localName - The local name (without prefix), or the empty string if Namespace processing is not being performed.
	 * @param fullName - The qualified XML 1.0 name (with prefix), or the empty string if qualified names are not available.
	 * @throws SAXException - Any SAX exception, possibly wrapping another exception.
	 * @author doyledp
	 */	
	protected void startSimilarityElement(String nameSpaceURI, String localName, String fullname, org.xml.sax.Attributes attributes) throws org.xml.sax.SAXException, IncompatableFeatureException {
		// implement similarity code here
		if (parsingArray) {
			if (localName.equals("primary")) {
				String primaryName = attributes.getValue("name");
				primary = values.indexOf(primaryName);
				if (primary == -1)
					logger.fatal("ERROR READING SIMILARITY FILE, primary name " + primaryName + " is not a valid value");

			} else if (localName.equals("secondary")) {
				String secondaryName = attributes.getValue("name");
				int secondary = values.indexOf(secondaryName);
				if (secondary == -1)
					logger.fatal("ERROR READING SIMILARITY FILE, secondary name " + secondaryName + " is not a valid value");
				String similarity = attributes.getValue("similarity");
				double sim = new Double(similarity).doubleValue();
				similarities[primary][secondary] = sim;
			}
		} else if (parsingGraph) {
			if (localName.equals("point")) {
				String difference = attributes.getValue("difference");
				String similarity = attributes.getValue("similarity");
				try {
					double diff = Double.parseDouble(difference);
					double sim = Double.parseDouble(similarity);
					currentSimProfile.addPoint(genealogy, diff, sim);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				logger.warn("WARNING: Unrecognised Tag: " + localName);
			}
		} else if (localName.equals("feature")) {
			String featureName = attributes.getValue("name");
			if (featureName.equals("case")) {
				throw new SAXException("ERROR: \"case\" is not a valid feature name.");
			}
			genealogy += "/" + featureName;
			currentFeatureStruct = caseStruct.getFeatureStruct(genealogy);
			double weight = Double.parseDouble(attributes.getValue("weight"));
			if (featureName == null || featureName.equals(""))
				logger.warn("ERROR READING PROFILE FILE, name must have a value");

			// this will replace existing details
			currentSimProfile.setFeatureWeight(genealogy, weight);
		} else if (localName.equals("array")) {
			int type = currentFeatureStruct.getType();
			if (!(type == FeatureStruct.SYMBOL || type == FeatureStruct.TAXONOMY)) {
				throw new IncompatableFeatureException(
					"ERROR READING PROFILE FILE, feature "
						+ genealogy
						+ " is of an incorrect type ("
						+ FeatureStruct.types[type]
						+ ") All array similarity measures are only definable for features of type taxonomy or symbol. Please change the case similarity document");
			}
			values = currentFeatureStruct.getValues();
			int size = values.size();
			similarities = new double[size][size];
			for (int i = 0; i < size; i++)
				for (int j = 0; j < size; j++)
					similarities[i][j] = -1;
			parsingArray = true;
		} else if (localName.equals("measure")) {
			String similarityMeasureName = attributes.getValue("name");
			currentSimProfile.generateSimilarityMeasure(genealogy, similarityMeasureName);
		} else if (localName.equals("graph")) {
			String graphType = attributes.getValue("type");
			currentSimProfile.setDifference(genealogy, graphType);
			parsingGraph = true;
		} else if (localName.equals("exact")) {
			currentSimProfile.setExact(genealogy);
		} else {
			logger.warn("WARNING: Unrecognised Tag: " + localName);
		}		
	}
	
	/**
	 * Receive notification of the start of an element in structure section of document. This method should only be called internally.
	 * @param nameSpaceURI - The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing is not being performed.
	 * @param localName - The local name (without prefix), or the empty string if Namespace processing is not being performed.
	 * @param fullName - The qualified XML 1.0 name (with prefix), or the empty string if qualified names are not available.
	 * @throws SAXException - Any SAX exception, possibly wrapping another exception.
	 * @author doyledp
	 */	
	protected void startStructureElement(String nameSpaceURI, String localName, String fullname, org.xml.sax.Attributes attributes) throws org.xml.sax.SAXException, IncompatableFeatureException {
		if (processingTaxonomy) {
			// We are processing a taxonomy
			String name = attributes.getValue("name");
			if (name == null || name.equals(""))
				throw new SAXException("ERROR: Problem occured while reading a taxonomy tag (" + localName + "). Every taxonomy tags must have a name attribute.");
			currentFeatureStruct.taxonomyStartElement(name);
		} else if (processingFeature) {
			int currentFeatureType;
			if (localName.equals("symbol"))
				currentFeatureType = FeatureStruct.SYMBOL;
			else if (localName.equals("double"))
				currentFeatureType = FeatureStruct.DOUBLE;
			else if (localName.equals("integer"))
				currentFeatureType = FeatureStruct.INTEGER;
			else if (localName.equals("taxonomy"))
				currentFeatureType = FeatureStruct.TAXONOMY;
			else if (localName.equals("boolean"))
				currentFeatureType = FeatureStruct.BOOLEAN;
			else if (localName.equals("complex"))
				currentFeatureType = FeatureStruct.COMPLEX;
			else if (localName.equals("string"))
				currentFeatureType = FeatureStruct.STRING;
			else
				throw new SAXException("ERROR: \"" + localName + "\" is not a valid Feature Type. Available Types are {complex,symbol,double,integer,taxonomy,boolean,string}");

			String currentFeatureReferenceName = attributes.getValue("name");
			String ref = attributes.getValue("ref");
			//currentFeatureReference = false;
			if (ref != null) {
				//currentFeatureReference = true;
				currentFeatureStruct = (FeatureStruct) ((FeatureStruct) references.get(ref)).clone();
				if (currentFeatureStruct.getType() != currentFeatureType) {
					throw new SAXException(
						"ERROR: A Feature type " + currentFeatureStruct.getType() + " with reference name \"" + ref + "\" has been referenced but it is not compatable with its referee type (" + currentFeatureType + "). Check your Case Structure document.");
				}
				referees.add(genealogy);
				currentFeatureStruct.reset(genealogy, currentFeatureDiscriminant, currentFeatureSolution, currentFeatureManditory);
				if (currentFeatureType == FeatureStruct.TAXONOMY) {
					processingTaxonomy = true;
				} else if (currentFeatureType == FeatureStruct.COMPLEX) {
					complexFeatureTree.add(currentFeatureStruct);
				}
			} else {
				processingType = true;
				if (currentFeatureType == FeatureStruct.COMPLEX) {
					currentFeatureStruct = new ComplexFeatureStruct(genealogy, currentFeatureDiscriminant, currentFeatureSolution, currentFeatureManditory);
					complexFeatureTree.add(currentFeatureStruct);
					processingType = false;
				} else if (currentFeatureType == FeatureStruct.SYMBOL) {
					currentFeatureStruct = new SymbolFeatureStruct(genealogy, currentFeatureDiscriminant, currentFeatureSolution, currentFeatureManditory);
				} else if (currentFeatureType == FeatureStruct.DOUBLE) {
					currentFeatureStruct = new DoubleFeatureStruct(genealogy, currentFeatureDiscriminant, currentFeatureSolution, currentFeatureManditory);
				} else if (currentFeatureType == FeatureStruct.INTEGER) {
					currentFeatureStruct = new IntegerFeatureStruct(genealogy, currentFeatureDiscriminant, currentFeatureSolution, currentFeatureManditory);
				} else if (currentFeatureType == FeatureStruct.TAXONOMY) {
					processingTaxonomy = true;
					currentFeatureStruct = new TaxonomyFeatureStruct(genealogy, currentFeatureDiscriminant, currentFeatureSolution, currentFeatureManditory);
				} else if (currentFeatureType == FeatureStruct.BOOLEAN) {
					currentFeatureStruct = new BooleanFeatureStruct(genealogy, currentFeatureDiscriminant, currentFeatureSolution, currentFeatureManditory);
				} else if (currentFeatureType == FeatureStruct.STRING) {
					currentFeatureStruct = new StringFeatureStruct(genealogy, currentFeatureDiscriminant, currentFeatureSolution, currentFeatureManditory);
				}
				// Stores the currentFeatureStruct in the references HashTable so it can be copied later by calling 
				// if using the "ref" keyword in the startElement method
				if (currentFeatureReferenceName != null) {
					references.put(currentFeatureReferenceName, currentFeatureStruct);
				}
			}
			processingFeature = false;
		} else if (processingType) {
			String value = attributes.getValue("value");
			try {
				if (localName.equals("enumeration")) {
					currentFeatureStruct.setPossVal(value);
				} else if (localName.equals("minInclusive")) {
					currentFeatureStruct.setMinValue(value);
				} else if (localName.equals("maxInclusive")) {
					currentFeatureStruct.setMaxValue(value);
				}
			} catch (cbml.cbr.BadFeatureValueException e) {
				throw new SAXException("ERROR: while reading tag \"" + localName + "\" attribute value is invalid (" + value + ").");
			}
		} else if (localName.equals("feature")) {
			String currentFeatureName = attributes.getValue("name");
			if (currentFeatureName.equals("case")) {
				throw new SAXException("ERROR: \"case\" is not a valid feature name.");
			}
			if (genealogy.equals("")) {
				currentFeatureDiscriminant = true;
				if (attributes.getValue("discriminant") != null && attributes.getValue("discriminant").equals("false"))
					currentFeatureDiscriminant = false;
			} else {
				// only root features can be discriminating
				currentFeatureDiscriminant = false;
			}
			genealogy += "/" + currentFeatureName;
			if (attributes.getValue("solution") != null && attributes.getValue("solution").equals("true")) {
				if (gotSolutionFeature) {
					throw new SAXException("ERROR: Feature \"" + currentFeatureName + "\" cannot be a solution, a solution has already been defined in this case structure.");
				} else {
					currentFeatureSolution = true;
					gotSolutionFeature = true;
				}
			} else {
				currentFeatureSolution = false;
			}
			currentFeatureManditory = true;
			String stringManditory = attributes.getValue("manditory");
			if (stringManditory != null && stringManditory.equals("false")) {
				currentFeatureManditory = false;
			}
			processingFeature = true;
		}
	}
}