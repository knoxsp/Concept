package fionn;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import cbml.cbr.BadFeatureValueException;
import cbml.cbr.CBMLCase;
import cbml.cbr.Feature;

/**
 * fionn.Case is the minimum implementation of cbml.cbr.Case. 
 * @author Lorcan Coyle
 * @see fionn.cbr.nn.Casenode
 * @version 1.0
 */
public class Case implements CBMLCase {
	/**
	 * The name of this case
	 */
	protected String caseName;
	/**
	 * The solution Feature associated with this Casenode
	 */
	protected Feature solution;
	/**
	 * A Hashtable storing the features of this case indexed by featurePaths
	 */
	protected Hashtable indexedFeatures;
	/**
	 * Constructs a Case object.
	 */
	public Case() {
		indexedFeatures = new Hashtable();
	}
	/**
	 * Adds the specified feature to this case.
	 * @param newFeature the feature to be added to this case.
	 * @return <code>true</code> if the feature was added succesfully, <code>false</code> otherwise
	 */
	public boolean addFeature(Feature newFeature) {
		String path = newFeature.getPath();
		// start by adding this feature to its parent (which must already in the indexedFeatures Hashtable)
		int pos2 = path.lastIndexOf("/");
		if (pos2 > 0) {
			try {
				String parentFeaturePath = path.substring(0, pos2);
				Feature parent = (Feature) indexedFeatures.get(parentFeaturePath);
				parent.setValue(newFeature);
			} catch (BadFeatureValueException e) {
				// will this ever happen?	
				return false;
			}
		}
		indexedFeatures.put(path, newFeature);
		if (newFeature.isComplex()) {
			List childFeatures = (List) newFeature.getValue();
			int size = childFeatures.size();
			for (int i = 0; i < size; i++) {
				addFeature((Feature) childFeatures.get(i));
			}
		}
		return true;
	}

	/**
	 * Returns a clone of this Case.
	 * @return a clone of this instance.
	 */
	public Object clone() throws CloneNotSupportedException{
		Case copyOf = (Case) super.clone();
		copyOf.setName(getName());
		copyOf.setSolution((Feature) solution.clone());
		Hashtable clonedIndexFeatures = new Hashtable();
		Enumeration i = indexedFeatures.keys();
		while (i.hasMoreElements()) {
			String path = (String) i.nextElement();
			clonedIndexFeatures.put(path, ((Feature) indexedFeatures.get(path)).clone());
		}
		copyOf.indexedFeatures = clonedIndexFeatures;
		return copyOf;
	}
	/**
	 * Returns the feature at the specified path in this case. 
	 * @return the feature at the specified path in this case or null if there is no feature at the specified path.
	 * @param featurePath path whose associated feature is to be returned.
	 */
	public Feature getFeature(String featurePath) {
		return (Feature) indexedFeatures.get(featurePath);
	}
	/**
	* Returns the name of this case.
	* @return the name of this case.
	*/
	public String getName() {
		return caseName;
	}
	/**
	 * Returns the solution <code>Feature</code> associated with this case.
	 * @return the solution <code>Feature</code> associated with this case
	 */
	public Feature getSolution() {
		return solution;
	}
	/**
	 * Removes the feature at the specified featurePath. Returns the removed feature.
	 * @param featurePath the path of the feature to be removed or if there was no Feature at the specified path.
	 * @return the removed feature.
	 */
	public Feature removeFeature(String featurePath) {
		return (Feature) indexedFeatures.remove(featurePath);
	}
	/**
	 * Sets the name for this case.
	 * @param name the new name of this case.
	 */
	public void setName(String name) {
		caseName = name;
	}
	/**
	 * Sets the solution <code>Feature</code> of this case to the specified value.
	 * @param solution the solution <code>Feature</code> to be associated with this case.
	 */
	public void setSolution(cbml.cbr.Feature solution) {
		this.solution = solution;
	}
	/**
	 * Returns a String representation of this Casenode object.
	 * @return a String representation of this Casenode object.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("<case name=\"" + caseName + "\">");
		//sb.append("<score>" + activation + "</score>");
		Enumeration i = indexedFeatures.keys();
		// look for any paths that are in root, print these features out only
		while (i.hasMoreElements()) {
			String path = (String) i.nextElement();
			if (path.indexOf("/", 1) == -1)
				sb.append(((Feature) indexedFeatures.get(path)).toString());
		}
		if (solution != null)
			sb.append(solution.toString());
		sb.append("</case>");
		return sb.toString();
	}
	
	// SJ added getFeatures() to Case for NaiveBayes 
	/** 
	 * Get a list of the features in the case
	 * @return a list of the case features as Feature objects
	 */
	public List getFeatures() {
		// have to get the features from the hashtable here...
		List features = new ArrayList();
	
		Enumeration e = indexedFeatures.elements();
		while (e.hasMoreElements()) {
			features.add(e.nextElement());
		}
	
		return features;
	}

	
}
