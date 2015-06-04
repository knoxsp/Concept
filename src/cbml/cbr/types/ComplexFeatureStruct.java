package cbml.cbr.types;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import cbml.cbr.Feature;
import cbml.cbr.FeatureStruct;

/**
 * This defines a feature of type <code>complex</code>. A <code>complex</code> feature value is a container for child features.
 *	This is one of a number of CBML defined feature types: <code>boolean</code>, <code>complex</code>, <code>double</code>, <code>integer</code>, <code>string</code>, <code>symbol</code> and <code>taxonomy</code>.
 * @author Lorcan Coyle
 * @see cbml.cbr.types.BooleanFeatureStruct
 * @see cbml.cbr.types.DoubleFeatureStruct
 * @see cbml.cbr.types.IntegerFeatureStruct
 * @see cbml.cbr.types.StringFeatureStruct
 * @see cbml.cbr.types.SymbolFeatureStruct
 * @see cbml.cbr.types.TaxonomyFeatureStruct
 * @version 3.0
 */
public class ComplexFeatureStruct extends SimpleFeatureStruct implements cbml.cbr.FeatureStruct {
	
   static final private Logger logger = Logger.getLogger(ComplexFeatureStruct.class);

   private List subFeatureStructs;

	/**
	 * Constructs a feature structure of type <code>Complex</code> for the feature with the specified name. The feature's cardinality and whether or not this feature is a discriminating one are passed into the constructor. 
	 * @param featurePath the path of the feature that this Feature Structure defines.
	 * @param discriminant <code>true</code> if this feature structure is a discriminating feature.
	 * @param solution <code>true> if this feature structure defines the solution part of a case.
	 * @param manditory <code>true</code> if this feature must occur in a case defined with this feature structure.
	 */
	public ComplexFeatureStruct(String featurePath, boolean discriminant, boolean solution, boolean manditory) {
		super(featurePath, COMPLEX, discriminant, solution, manditory);
		subFeatureStructs = new ArrayList();

	}
	/**
	 * This method is used by the case structure parser to add a child feature structure to this complex feature definition. It should not be used by the user.
	 * @param childFeatureStruct child feature structure to be added to this feature structure.
	 */
	public void addSubFeatureStruct(FeatureStruct childFeatureStruct) {
		subFeatureStructs.add(childFeatureStruct);
	}
	/**
	 * Creates and returns a copy of this FeatureStruct.
	 * @return a clone of this instance.
	 */
	public Object clone() {
			ComplexFeatureStruct copy = (ComplexFeatureStruct) super.clone();
			int size = subFeatureStructs.size();
			copy.subFeatureStructs = new ArrayList(size);
			for (int i = 0; i < size; i++) {
				FeatureStruct childCopy = (FeatureStruct) ((FeatureStruct) subFeatureStructs.get(i)).clone();
				copy.subFeatureStructs.add(childCopy);
			}
			return copy;
	}
	/**
	 * This method is used by the case content parser to add features to a case. It should not be used by the user.
	 * @return a <code>List</code> of the child feature structure definitions of this feature structure definition.
	 */
	public List getSubFeatureStructs() {
		if (subFeatureStructs == null)
			return new ArrayList();
		return subFeatureStructs;
	}
	/**
	 * Helper method for initialising a <code>ComplexFeatureStruct</code>. 
	 * All the sub feature structures must have their featurepaths changed to reflect 
	 * the new featurepath for this feature structure.
	 * @param featurePath the name of the feature that this Feature Structure defines.
	 * @param discriminant <code>true</code> if this feature structure is a discriminating feature.
	 * @param solution <code>true> if this feature structure defines the solution part of a case.
	 * @param manditory <code>true</code> if this feature must occur in a case defined with this feature structure.
	 */
	public void reset(String featurePath, boolean discriminant, boolean solution, boolean manditory) {
		super.reset(featurePath, discriminant, solution, manditory);
		if (subFeatureStructs == null)
			return;
		int size = subFeatureStructs.size();
		for (int i = 0; i < size; i++) {
			FeatureStruct child = (FeatureStruct) subFeatureStructs.get(i);
			String childPath = child.getFeaturePath();
			String childName = childPath.substring(childPath.lastIndexOf("/"));
			// Children features take the mandatory values from their parent.
			child.reset(featurePath + childName, child.isDiscriminant(), child.isSolution(), manditory);
		}
	}
	/**
	 * Returns a <code>String</code> representation of this feature structure. The string representation consists of a list of the structure's children, enclosed in square brackets ("[]"). Adjacent elements are separated by the characters ", " (comma and space).
	 * @return a <code>String</code> representation of this feature structure.
	 */
	public String toString() {
		int size = subFeatureStructs.size();
		StringBuffer sb = new StringBuffer();
		sb.append("<complex>");
		for (int i = 0; i < size; i++) {
			sb.append(((FeatureStruct) subFeatureStructs.get(i)).toString());
		}
		sb.append("</complex>");
		return super.getCBMLRepresentation(sb.toString());
	}
	/**
	 * Validates the specified feature against this feature structure definition. The feature value is valid if its child features are within their cardinality constraints as defined in the structure definition. 
	 * @return <code>true</code> it this feature is valid according to this feature structure definition.
	 * @param complexFeature the feature to be validated.
	 */
	public boolean validate(Feature complexFeature) {

		List childFeatures = (List) complexFeature.getValue();
		for (int i = 0; i < subFeatureStructs.size(); i++) {
			// test if the feature was manditory, if so, mark it as such
			FeatureStruct baseFeatureStruct = (FeatureStruct) subFeatureStructs.get(i);
			String baseFeaturePath = baseFeatureStruct.getFeaturePath();
			if (baseFeatureStruct.isManditory()) {
				boolean found = false;
				for (int j = 0; j < childFeatures.size(); j++) {
					Feature f = (Feature) childFeatures.get(j);
					if (f.getPath().equals(baseFeaturePath)) {
						found = true;
						break;
					}
				}
				if (!found) {
					logger.error("VALIDATION ERROR: Feature " + getFeaturePath() + " is missing a manditory feature (" + baseFeaturePath + ").");
					return false;
				}
			}
		}
		return true;
	}
}
