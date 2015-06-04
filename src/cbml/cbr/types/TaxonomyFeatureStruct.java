package cbml.cbr.types;

import java.util.ArrayList;
import java.util.List;

import cbml.cbr.Feature;

/**
 * This defines a feature of type <code>taxonomy</code>. A taxonomy is a tree of possible values. 
 *	This is one of a number of CBML defined feature types: <code>boolean</code>, <code>complex</code>, <code>double</code>, <code>integer</code>, <code>string</code>, <code>symbol</code> and <code>taxonomy</code>.
 * @see cbml.cbr.types.BooleanFeatureStruct
 * @see cbml.cbr.types.ComplexFeatureStruct
 * @see cbml.cbr.types.DoubleFeatureStruct
 * @see cbml.cbr.types.IntegerFeatureStruct
 * @see cbml.cbr.types.StringFeatureStruct
 * @see cbml.cbr.types.SymbolFeatureStruct
 * @version 3.0
 */
public class TaxonomyFeatureStruct extends SimpleFeatureStruct implements cbml.cbr.FeatureStruct {
	private String genealogy;
	protected List possibleValues;
	protected List tree;
	protected List levels;
	private int level;

	/** 
	 * Constructs a feature structure of type <code>Taxonomy</code> for the feature with the specified name. The feature's cardinality and whether or not this feature is a discriminating one are passed into the constructor. 
	 * @param featurePath the name of the feature that this Feature Structure defines.
	 * @param discriminant <code>true</code> if this feature structure is a discriminating feature.
	 * @param solution <code>true> if this feature structure defines the solution part of a case.
	 * @param manditory <code>true</code> if this feature must occur in a case defined with this feature structure.
	 */
	public TaxonomyFeatureStruct(String featurePath, boolean discriminant, boolean solution, boolean manditory) {
		super(featurePath, TAXONOMY, discriminant, solution, manditory);
		possibleValues = new ArrayList();
		tree = new ArrayList();
		levels = new ArrayList();
		genealogy = "";
		level = 0;
	}
	/**
	 * Creates and returns a copy of this FeatureStruct.
	 * @return a clone of this instance.
	 */
	public Object clone() {
		TaxonomyFeatureStruct copy = new TaxonomyFeatureStruct(featurePath, discriminant, solution, manditory);
		int size = tree.size();
		for (int i = 0; i < size; i++) {
			copy.possibleValues.add(possibleValues.get(i));
			copy.tree.add(tree.get(i));
			copy.levels.add(levels.get(i));
		}
		return copy;
	}
	/**
	 * Returns a <code>List</code> of the possible values of this Taxonomy Element.
	 * @return returns a <code>List</code> of the possible values of this taxonomy element.
	 */
	public List getValues() {
		return tree;
	}
	/**
	 * This method is used by the case structure parser to generate the taxomony definition of this feature structure. It should not be used by the user.
	 */
	public void taxonomyEndElement() {
		level--;
		genealogy = genealogy.substring(0, genealogy.lastIndexOf("/"));
	}
	/**
	 * This method is used by the case structure parser to generate the taxomony definition of this feature structure. It should not be used by the user.
	 * @param newTaxonomyValue the name of this taxonomy element.
	 */
	public void taxonomyStartElement(String newTaxonomyValue) {
		genealogy += "/" + newTaxonomyValue;
		possibleValues.add(newTaxonomyValue);
		tree.add(genealogy);
		levels.add(new Integer(++level));
	}
	/**
	 * Returns a string representation of this feature structure. This representation is in CBML format.
	 * @return a string representation of this feature structure.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("<taxonomy>");
		int position = 1;
		int size = tree.size();
		if (size == 1) {
			sb.append("<node value=\"" + possibleValues.get(0) + "\"/>");
		} else {
			sb.append("<node value=\"" + possibleValues.get(0) + "\"");
			for (int i = 1; i < size; i++) {
				String path = (String) tree.get(i);
				int pos = path.lastIndexOf("/");
				String value = (String) possibleValues.get(i);
				if (pos > position) {
					position = pos;
					// We go up a level
					sb.append("><node value=\"" + value + "\"");
				} else if (pos == position) {
					// we stay at the same level
					sb.append("/><node value=\"" + value + "\"");
				} else {
					// we go down x levels
					int numberoflevels = ((Integer) levels.get(i)).intValue();
					int lastNumberoflevels = ((Integer) levels.get(i-1)).intValue();
					int drop = lastNumberoflevels - numberoflevels;
					sb.append("/>");
					for(int j = 0; j < drop; j++){
						sb.append("</node>");
					}
					position = pos;
					sb.append("<node value=\"" + value + "\"");
				}
			}
			sb.append("/>");
			for(int i = 0; i < ((Integer) levels.get(levels.size() -1)).intValue()-1; i++){
				sb.append("</node>");
			}
		}
		sb.append("</taxonomy>");
		return super.getCBMLRepresentation(sb.toString());
	}
	/**
	 * Validates the specified feature against this feature structure definition. The feature value is valid if it exists in the taxonomy tree.
	 * @return <code>true</code> it this feature is valid according to this feature structure definition.
	 * @param testFeature the feature to be validated.
	 */
	public boolean validate(Feature testFeature) {
		String value = (String) testFeature.getValue();
		for (int i = 0; i < possibleValues.size(); i++) {
			String testValue = (String) possibleValues.get(i);
			if (value.equals(testValue))
				return true;
		}
		return false;
	}
}
