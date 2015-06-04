package fionn.cbr.nn;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;

import cbml.cbr.CaseStruct;
import cbml.cbr.Feature;
import cbml.cbr.FeatureStruct;
import cbml.cbr.IncompatableFeatureException;
import cbml.cbr.SimilarityProfile;
import fionn.Case;

/**
 * NN is an implementation of the Nearest Neighbourhood Algorithm with optomizations based on Case Retrieval Nets. NN uses the <a href="http://www.cs.tcd.ie/Lorcan.Coyle/CBML">CBML</a> specification and all internal objects use CBML interfaces. 
 * @author Lorcan Coyle
 * @see fionn.cbr.nn.Casenode
 * @version 1.0
 */
public class NN implements java.io.Serializable {

   static final private Logger logger = Logger
         .getLogger(NN.class);
	/**
	 * An IE represents an Information Entity and is used in the Nearest Neighbour Algorithm. The notion of an Information Entity comes from the Case Retrieval Net field of CBR.
	 * @author Lorcan Coyle
	 * @version 1.0
	 * @see fionn.cbr.nn.NN
	 */
	protected class IE {
		/**
		 * The activation of this IE in the Nearest Neighbourhood algorithm
		 */
		private double activation;
		/**
		 * The Feature (Feature-value pair) that this IE represents
		 */
		private Feature feature;

		/**
		 * The index of this IE in the Nearest Neighbour algorithm
		 */
		private int index;
		/**
		 * The list of indicies of Casenodes that this IE is connected to.
		 */
		private List relevanceConnections;

		/**
		 * The List of SimArcs contained in this IE
		 */
		private List simArcs;

		/**
		 * Constructs an IE object without any connections.
		 * @param f the Feature (Feature-value pair) that this IE represents
		 */
		public IE(Feature f) {
			feature = f;
			relevanceConnections = new ArrayList();
			simArcs = new ArrayList();
			activation = 0;
			index = -1;
		}
		/**
		 * Sets the activation of this IE to the supplied double value.
		 * @param activation the new Activation of this IE.
		 */
		public void activate(double activation) {
			this.activation = activation;
		}
		/**
		 * Adds a connection to the Casenode with the supplied index.
		 * @param connection the index of the Casenode to which this IE is being connected
		 */
		public void addCaseConnection(int connection) {
			relevanceConnections.add( Integer.valueOf(connection));
		}

		/**
		 * Adds a Similarity Connection to this IE
		 * @param arc the new Similarity Arc Connection
		 */
		public void addSimArc(SimArc arc) {
			simArcs.add(arc);
		}
		/**
		 * Returns the activation of this IE
		 * @return the activation of this IE
		 */
		public double getActivation() {
			return activation;
		}
		/**
		 * Returns the Feature (feature-value pair) contained in this IE
		 * @return the Feature (feature-value pair) contained in this IE
		 */
		public Feature getFeature() {
			return feature;
		}
		/**
		 * Returns the index of this IE in the Nearest Neighbourhood Algorithm
		 * @return the index of this IE in the Nearest Neighbourhood Algorithm
		 */
		public int getIndex() {
			return index;
		}
	
		/**
		 * Returns a List of the relevence indicies connecting this IE to the Casenodes
		 * @return a List of the relevence indicies connecting this IE to the Casenodes
		 */
		public List getRelevanceConnections() {
			return relevanceConnections;
		}
		/**
		 * Returns a List of the similarity Arcs connected to this IE
		 * @return a List of the similarity Arcs connected to this IE
		 */
		public List getSimArcs() {
			return simArcs;
		}
		/**
		 * Sets the index of this IE in the Nearest Neighbourhood Algorithm
		 * @param index the index of this IE in the Nearest Neighbourhood Algorithm
		 */
		public void setIndex(int index) {
			this.index = index;
		}
		/**
		 * Returns a String representation of this IE
		 * @return a String representation of this IE
		 */
		public String toString() {
			StringBuffer output = new StringBuffer();
			output.append("<ie index=\"" + index + "\">");
			output.append("<activation>" + activation + "</activation>");
			// IEs only refer to simple cases
			// we need a seperate feature representation so features of the same name but different paths can be distinguished
			// it would be nice to incorporate this into the feature toString() method, but it conflicts with the way cases are printed out
			// (each feature is printed individually)
			{
				output.append("<feature>");
				String finish = "";
				int pos = 0;
				int endpos;
				String path = feature.getPath();
				String featureName = feature.getName();
				// at the moment this section of code puts in the full path, this may not always be appropriate, although if there are features with the same name at different places in the case hierarchy this will be important, but what happens if there are multiple values?
				while (true) {
					endpos = path.indexOf("/", pos + 1);
					if (endpos == -1) {
						// TEMP - take this out if it doesnt cause problems			
						String name = path.substring(pos + 1);
						if (!featureName.equals(name))
							logger.warn("PROBLEM encountered in IE.toString");
						//output += "<" + name + ">" + feature.getValue() + "</" + name + ">";
						output.append(feature.toString());
						break;
					}
					String name = path.substring(pos + 1, endpos);
					output.append("<" + name + ">");
					finish = "</" + name + ">" + finish;
					pos = endpos;
				}
				output.append(finish + "</feature>");
			}

			if (simArcs.size() != 0) {
				output.append("<similarityArcs>");
				for (int i = 0; i < simArcs.size(); i++) {
					SimArc a = (SimArc) simArcs.get(i);
					output.append(a.toString());
				}
				output.append("</similarityArcs>");
			}

			if (relevanceConnections.size() != 0) {
				output.append("<relevanceArcs>");
				for (int i = 0; i < relevanceConnections.size(); i++) {
					output.append("<arc>");
					output.append("<weight>1</weight>");
					output.append("<connection>" + relevanceConnections.get(i) + "</connection>");
					output.append("</arc>");
				}
				output.append("</relevanceArcs>");
			}
			output.append("</ie>");
			return output.toString();
		}
	}
	/**
	 * SimArc represents a Similarity Arc Connection between two Information Entities
	 * @author Lorcan Coyle
	 * @see fionn.cbr.nn.IE
	 * @see fionn.cbr.nn.NN
	 * @version 1.0
	 */
	protected class SimArc {
		/**
		 * The index of a connecting IE 
		 */
		private int connection;
		/**
		 * The weight of the connection to the IE
		 */
		private double weight;
		/**
		 * Constructs a Similarity Arc Connection with the specifiec weight and connection index
		 * @param weight the weight of this similarity arc
		 * @param connection the connection index of this similarity arc
		 */
		public SimArc(double weight, int connection) {
			this.weight = weight;
			this.connection = connection;
		}
		/**
		 * Returns the connection index of this Similarity Arc
		 * @return the connection index of this Similarity Arc
		 */
		public int getConnection() {
			return connection;
		}
		/**
		 * Takes in the activation of this arcs parent IE and returns the attenuated activation to be passed onto this arc's connecting IE
		 * @param activation the activation of the parent IE
		 * @return the activation after attenuation applied by this arc (attenuation is calculated as  <code>activation * weight</code>
		 */
		public double spreadActivation(double activation) {
			return activation * weight;
		}
		/**
		 * Returns a String representation of this Similarity Arc
		 * @return a String representation of this Similarity Arc
		 */
		public String toString() {
			StringBuffer output = new StringBuffer();
			output.append("<arc><weight>");
			output.append(weight);
			output.append("</weight><connection>");
			output.append(connection);
			output.append("</connection></arc>");
			return output.toString();
		}
	}
	/**
	 * class to speed up the  sorting of the casenodes after their activation
	 * @author johnloughrey
	 *
	 * Returns an int []; where rank[0] holds the index of the highest scoring casenode in the 
	 * NN algorithm; rank[1] is the index of the second highest acrtivation level and so on...
	 * 
	 * algorithm based upon fionn.ga.util.SortPopulation (QuickSort)
	 */
	protected class SortNeighbourhood {

		private int[] rank;
		public SortNeighbourhood(int neighbours) {
			
		}

		protected int partition(List list, int start, int end) {
		   int left, right;
			Casenode partitionElement = (Casenode) list.get(rank[end]);
			left = start - 1;
			right = end;
			for (;;) {
				while (partitionElement.getActivation() < ((Casenode) list.get(rank[++left])).getActivation()) {
					if (left == end)
						break;
				}
				while (partitionElement.getActivation() > ((Casenode) list.get(rank[--right])).getActivation()) {
					if (right == start)
						break;
				}
				if (left >= right)
					break;

				int temp = rank[left];
				rank[left] = rank[right];
				rank[right] = temp;
			}
			int temp = rank[left];
			rank[left] = rank[end];
			rank[end] = temp;
			return left;
		}

		public int[] sort(List list) {
			//reset
		   rank = new int[list.size()];
         for (int i = 0; i < rank.length; i++)
				rank[i] = i;
			//sort values
			sort(list, 0, list.size() - 1);
			return rank;
		}

		protected void sort(List list, int start, int end) {
			int p;
			if (end > start) {
				p = partition(list, start, end);
				sort(list, start, p - 1);
				sort(list, p + 1, end);
			}
		}
	}
	/**
	 * <code>true</code> if a target case has been presented to this NN (i.e. the NN is active), <code>false</code> otherwise
	 */
	//protected boolean activated;
	/**
	 * A List of the Casenodes contained in this NN
	 */
	protected List casenodes;

	/**
	 * The Case Structure Object describing all casenodes in this NN
	 */
	protected CaseStruct caseStruct;

	/**
	 * A Hashtable containing Lists of IEs for each feature in <code>caseStruct</code>. The key for this Hashtable is the feature path.
	 */
	protected Hashtable IELists;

	/**
	 * The profile describing the similarity measures to be used in this NN
	 */
	protected SimilarityProfile profile;

	/**
	 * A Hashtable containing the relevance values for each feature in <code>caseStruct</code>. The key for this Hashtable is the feature path.
	 */
	protected Hashtable relevances;

	//protected int[] ranking;
	protected SortNeighbourhood sort;
	/**
	 * * A Hashtable containing the connection indicies for each feature in the target case. The key for this Hashtable is the feature path.
	 */
	protected Hashtable targetIndices;
	/**
	 * * A boolean flag stating wheter to use Information Entities.
	 */
	protected boolean useIEs;
	
	/**
	 * The number of cases within the case base
	 */
   private int my_casebaseSize = 0;

	/**
	 * Constructs the Nearest Neighbourhood Algorithm, ready to accept the presentation of a target case.
	 * @param casebase a <code>List</code> of <code>nn.Casenodes</code>, comprising the casebase.
	 * @param caseStruct a case structure definition
	 * @param profile a similarity Profile description
	 * @see fionn.cbr.nn.Casenode
	 * @see cbml.cbr.CaseStruct
	 * @see cbml.cbr.SimilarityProfile
	 */
	public NN(List casebase, CaseStruct caseStruct, SimilarityProfile profile) {
		this(casebase, caseStruct, profile, true);
	}

	/**
	 * Constructs the Nearest Neighbourhood Algorithm, ready to accept the presentation of a target case.
	 * @param casebase a <code>List</code> of <code>nn.Casenodes</code>, comprising the casebase.
	 * @param caseStruct a case structure definition
	 * @param profile a similarity Profile description
	 * @param optomisations a boolean to turn on certain optomisations in the similarity retrieval mechanisms
	 * @see fionn.cbr.nn.Casenode
	 * @see cbml.cbr.CaseStruct
	 * @see cbml.cbr.SimilarityProfile
	 */
	public NN(List casebase, CaseStruct caseStruct, SimilarityProfile profile, boolean optomisations) {
		super();

		this.useIEs = optomisations;
		this.profile = profile;
		this.caseStruct = caseStruct;
		targetIndices = new Hashtable();
		int size = casebase.size();
		casenodes = new ArrayList(size);
		initializeNet(size);

		for (int i = 0; i < size; i++) {
			addCase((Casenode) casebase.get(i));
		}
		//System.out.println("Case Base initialised.");
		reset();
		//set up the ranking algorithm
		sort = new SortNeighbourhood(size);
	}

	/**
	 * Adds this casenode to the NN. If this case is already in the NN nothing is done.
	 * @param casenode the new casenode to be added to the NN
	 */
	final public void addCase(Casenode casenode) {
		//Casenode cn = new Casenode(c);
		String caseName = casenode.getName();
		int numberOfCasenodes = casenodes.size();
		for (int i = 0; i < numberOfCasenodes; i++) {
			Case ca = (Case) casenodes.get(i);
			if (caseName.equalsIgnoreCase(ca.getName())) {
				// this case is already in the casebase
				logger.warn("WARNING: Case with name " + caseName + " is already in the system. Case names must be unique.");
				return;
			}
			
		}
		casenode.setIndex(casenodes.size());

		// Sets up the IEs for case cn
		List featurePaths = caseStruct.getFeaturePaths();
		int size = featurePaths.size();
		for (int i = 0; i < size; i++) {
			String featurePath = (String) featurePaths.get(i);
			// only add IEs if there is an IE bank waiting for them...
			if (IELists.containsKey(featurePath)) {
				FeatureStruct thisFeatureStruct = (FeatureStruct) caseStruct.getFeatureStruct(featurePath);
				if (thisFeatureStruct.isDiscriminant()) {
					Feature f = casenode.getFeature(featurePath);
					if (f != null)
						addIE(f, casenode.getIndex());
				}
			}
		}
		my_casebaseSize++;
		casenodes.add(casenode);
	}
	/**
	 * Return the number of cases within the casebase.
	 * @return
	 */
   public int getCasebaseSize(){
      return my_casebaseSize;
   }
   
   public List getCaseNodes(){
      return casenodes;
   }
   
	/**
	 * Adds an IE to the NN for the supplied feature. If this feature already has an IE in the NN then a link to this feature's parent Casenode is added to the preexisting IE.
	 * @param feature the feature (feature-value pair) to be added to the NN
	 * @param parentIndex the index of the parent Casenode of <code>feature</code> 
	 * @return the index of the new (or preexisting) IE
	 */
	final protected int addIE(Feature feature, int parentIndex) {

		String path = feature.getPath();
		List IEList = (List) IELists.get(path);

		// do something...
		int ieIndex = -1;

		int size = IEList.size();
		for (int i = 0; i < size; i++) {
			IE test = (IE) IEList.get(i);
			if (feature.equals(test.getFeature())) {
				ieIndex = i;
				break;
			}
		}
		boolean containsIE = (ieIndex == -1) ? false : true;
		IE current;
		if (!containsIE) {
			current = new IE(feature);
			current.setIndex(size);
		} else {
			current = (IE) IEList.get(ieIndex);
		}

		// parentIndex will be -1 if it refers to a target case
		if (parentIndex > -1) {
			current.addCaseConnection(parentIndex);
		}
		if (!containsIE)
			IEList.add(current);
		return current.getIndex();

	}
	/**
	 * Internal Method for setting up the CRN section of the NN algorithm
	 * @param target the target case
	 */
	private void createSimilarityArcs(Casenode target) {
		// establish similarity arcs for each of the IELists
		// go through relevances to find suitable features
		Enumeration e = relevances.keys();
		while (e.hasMoreElements()) {
			String path = (String) e.nextElement();
			Feature f = target.getFeature(path);
			if (f != null) {
				double relevance = ((Double) relevances.get(path)).doubleValue();
				if (relevance > 0) {
					List IEList = (List) IELists.get(path);
					if (IEList != null) {
						// WE HAVE A CONTENDER
						int index = ((Integer) targetIndices.get(path)).intValue();
						// now we have the list and the target IE index. set up sim arcs
						IE targetIE = (IE) IEList.get(index);
						List simArcs = targetIE.getSimArcs();
						int size = IEList.size();
						boolean[] checklist = new boolean[size];

						for (int i = 0; i < simArcs.size(); i++) {
							SimArc simArc = (SimArc) simArcs.get(i);
							int connection = simArc.getConnection();
							checklist[connection] = true;
						}
						//checklist[index] = true;
						for (int i = 0; i < size; i++) {
							if (!checklist[i]) {
								IE sisterIE = (IE) IEList.get(i);
								// we need a new sim arc for to connect the target IE to its sisters
								double sim = profile.calculateSimilarity(targetIE.getFeature(), sisterIE.getFeature());
								// similarity arc
								if (sim != 0) {
									SimArc simArc = new SimArc(sim, i);
									targetIE.addSimArc(simArc);
								}
							}
						}
					} else {
						// calculate similarity here without looking at IEs - they don't exist
						// add similarity directly to the casenodes
						int size = casenodes.size();
						for (int i = 0; i < size; i++) {
							Casenode c = (Casenode) casenodes.get(i);
							Feature sisterFeature = c.getFeature(path);
							if (sisterFeature != null) {
								double sim = profile.calculateSimilarity(f, sisterFeature);
								if (sim != 0)
								   //System.out.println("Similarity = " + sim);
									c.activate(sim * relevance);
							}
						}
					}
				}
			}
		}
	}
	/**
	 * On presentation of a target case, similarity is calculated for every case in the casebase and the k most similar cases are returned. If the kth most similar case is equal to the k+1th case, this method will return k+1 cases (and as many cases as are similar as the kth case).  N.B. The target case must be of type <code>nn.Casenode</code>. 
	 * @param target the target case to be presented to the algorithm
	 * @param k the number of cases to be returned
	 * @return a <code>List</code> of the k most similar cases to the previously presented target in the casebase.
	 */
	public List getNeighbors(Casenode target, int k) {
		try{
			//This is the "k" value
		   //int numberOfCases = casenodes.size();
		   int numberOfCases = k;
			if (numberOfCases == 0)
				return new ArrayList(0);
			List bestK = new ArrayList(numberOfCases);			
			reset();
			
			// Sets up the IEs for case c
			List featurePaths = caseStruct.getFeaturePaths();
			int size = featurePaths.size();
			for (int i = 0; i < size; i++) {
				String featurePath = (String) featurePaths.get(i);
				// only set up targetIndices and IEs for features that have IELists allocated to them
				if (IELists.containsKey(featurePath)) { //try {
					Feature f = target.getFeature(featurePath);
					if (f != null)
						targetIndices.put(featurePath, Integer.valueOf(addIE(f, -1)));
				}
			}
			createSimilarityArcs(target);
			spreadActivation();
			roundActivations();
			int[] ranking = sort.sort(casenodes);			
			for (int i = 0; i < numberOfCases; i++){
				bestK.add(casenodes.get(ranking[i]));
			}
			return bestK;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private void initializeFeatureInNet(String path, int sizeOfCasebase) {
		try {
			FeatureStruct fs = caseStruct.getFeatureStruct(path);
			if (fs.isDiscriminant()) {
				double relevance = profile.getFeatureWeight(path);
				if (relevance != 0) {
					int type = fs.getType();
					relevances.put(path, new Double(relevance));
					if (useIEs) {
						if (type == FeatureStruct.SYMBOL || type == FeatureStruct.TAXONOMY || type == FeatureStruct.BOOLEAN) { // go with IEs
							//System.out.println(path + " will have IEs");
							IELists.put(path, new ArrayList());
						} else if (type == FeatureStruct.INTEGER) {
							String min = fs.getMinValue();
							String max = fs.getMaxValue();
							if (min != null && max != null) { // range is inclusive - both the max and min values can occur
								int range = Integer.parseInt(max) - Integer.parseInt(min) + 1;
								// we will use IEs if the number of possible values is half that of the number of existing cases in the casebase - NB this is very arbitrary
								if (range < (sizeOfCasebase / 2)) {
									IELists.put(path, new ArrayList());
									//System.out.println(path + " will have IEs");
								}
							}
							//System.out.println(path + " won't have IEs");
						} else if (type == FeatureStruct.DOUBLE || type == FeatureStruct.STRING) {
							// for now don't create IEs at all
							// store references to these features in a list.
							// similarities will be calculated on the fly
							//System.out.println(path + " won't have IEs");
						} else if (type == FeatureStruct.COMPLEX) {
							// Set up IEs for the children features, normalise the feature weights based on this feature's weight
							List childFeatureStructs = fs.getSubFeatureStructs();
							double sumOfThePartsRelevance = 0;
							for (int i = 0; i < childFeatureStructs.size(); i++) {
								FeatureStruct child = (FeatureStruct) childFeatureStructs.get(i);
								sumOfThePartsRelevance += profile.getFeatureWeight(child.getFeaturePath());
							}
							double relevanceNormalizer = relevance / sumOfThePartsRelevance;
							for (int i = 0; i < childFeatureStructs.size(); i++) {
								FeatureStruct child = (FeatureStruct) childFeatureStructs.get(i);
								String childPath = child.getFeaturePath();
								double childRelevance = profile.getFeatureWeight(childPath) * relevanceNormalizer;
								relevances.put(childPath, new Double(childRelevance));
							}
						} else {
						   logger.warn("WARNING: unknown feature type (" + fs.getType() + ") encountered in initializeNet");
						}
					}
				}

			}
		} catch (IncompatableFeatureException e) { // THIS CANNOT HAPPEN
			e.printStackTrace();
		}
	}

	/**
	 * Sets up the CRN section of the NN algorithm.
	 * @param profile the SimilarityProfile describing the Similarity measures to be used.
	 * @param size the number of cases in the casebase
	 */
	private void initializeNet(int sizeOfCasebase) {

		IELists = new Hashtable();
		relevances = new Hashtable();
		List paths = caseStruct.getFeaturePaths();
		for (int i = 0; i < paths.size(); i++) {
			String path = (String) paths.get(i);
			initializeFeatureInNet(path, sizeOfCasebase);
		}
	}

	/**
	 * Resets the CRN section of the NN algorithm, is done on presentation of a new target case.
	 */
	private void reset() {
		//System.out.println("reseting NN");
		// resets all IEs
		Enumeration e = IELists.elements();
		while (e.hasMoreElements()) {
			List IEList = (List) e.nextElement();
			int numberOfIEs = IEList.size();
			for (int i = 0; i < numberOfIEs; i++) {
				IE current = (IE) IEList.get(i);
				current.activate(0);
			}
		}
		// resets all casenodes
		int numberOfCasenodes = casenodes.size();
		for (int i = 0; i < numberOfCasenodes; i++) { // deactivate all Casenodes
			Casenode current = (Casenode) casenodes.get(i);
			current.reset();
		}
		// resets all target indices
		targetIndices.clear();
	}

	private void roundActivations() {
		int size = casenodes.size();
		int precision = 5;
		for (int i = 0; i < size; i++) {
			Casenode c = (Casenode) casenodes.get(i);
			double activation = c.getActivation();
			BigDecimal act = new BigDecimal(activation);
			act = act.setScale(precision, BigDecimal.ROUND_HALF_UP);
			activation = act.doubleValue();
			c.setActivation(activation);
		}
	}
	/**
	 * Spreads Activation across the Case Retrieval Net section of this NN, from the target case to the IEs via relevance arcs, from the IEs
	 * to their sister IEs (those referencing the same feature) via similarity arcs, and from all IEs to their parent cases
	 * via relevance arcs. Each Arc has a relevance which attenuates the activation. 
	 */
	private void spreadActivation() {
		String path = ""; // the activation of the target Case's IEs is done by activating it with the relevance score of that feature
		try { // similarity across sister IEs
			Enumeration e = IELists.keys();
			while (e.hasMoreElements()) {
				path = (String) e.nextElement();
				// gets the target IE with this path
				Integer targetIndex = (Integer) targetIndices.get(path);
				if (targetIndex != null) { // it will be null if target does not have a feature at path (optional feature)
					int index = ((Integer) targetIndex).intValue();
					double relevance = ((Double) relevances.get(path)).doubleValue();
					List IEList = (List) IELists.get(path);
					IE targetIE = (IE) IEList.get(index);
					//targetIE.activate(relevance);
					// the similarity arcs of the target IE - activation is spread to these sister IEs
					List simArcs = targetIE.getSimArcs();
					int size = simArcs.size();
					for (int i = 0; i < size; i++) {
						SimArc a = (SimArc) simArcs.get(i);
						int connection = a.getConnection();
						IE sisterIE = (IE) IEList.get(connection);
						double connectingActivation = a.spreadActivation(1);
						sisterIE.activate(connectingActivation * relevance);
					} // now spread activation vertically to casenodes
					size = IEList.size();
					for (int i = 0; i < size; i++) {
						IE current = (IE) IEList.get(i);
						double activation = current.getActivation(); // don't activate upwards if there is no activation to propogate
						if (activation != 0) {
							List relConnections = current.getRelevanceConnections();
							int relSize = relConnections.size();
							for (int j = 0; j < relSize; j++) {
								int connection = ((Integer) relConnections.get(j)).intValue();
								Casenode motherCasenode = (Casenode) casenodes.get(connection);
								motherCasenode.activate(activation);
							}
						}
					}
				}
			}

		} catch (Exception e) {
		   logger.fatal("ERROR, Exception caught in NN.spreadActivation, path=" + path);
			logger.fatal(e.toString());
		}
	}

	/**
	* @author johnloughrey
	*
	* method to change feature weights on the fly to speed the thing up ...
	* means that we no longer have to rebuild a NN depending on which features we add/remove
	* the can merely adjust the weights (ie. when weight= 1or 0 we are doing feature selection)
	*
	*/
	public void updateWeights(SimilarityProfile simProfile) {
		this.profile = simProfile;
		List paths = caseStruct.getFeaturePaths();
		relevances.clear();
		for (int i = 0; i < paths.size(); i++) {
			String path = (String) paths.get(i);
			double relevance = profile.getFeatureWeight(path);
			relevances.put(path, new Double(relevance));
		}
	}
}
