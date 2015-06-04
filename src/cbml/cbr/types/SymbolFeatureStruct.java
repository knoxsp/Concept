package cbml.cbr.types;

import java.util.ArrayList;
import java.util.List;

import cbml.cbr.Feature;
/**
 * This defines a feature of type <code>symbol</code>. A <code>symbol</code> feature has a value within a predefined list of possible values. 
 *	This is one of a number of CBML defined feature types: <code>boolean</code>, <code>complex</code>, <code>double</code>, <code>integer</code>, <code>string</code>, <code>symbol</code> and <code>taxonomy</code>.
 * @author Lorcan Coyle
 * @see cbml.cbr.types.BooleanFeatureStruct
 * @see cbml.cbr.types.ComplexFeatureStruct
 * @see cbml.cbr.types.DoubleFeatureStruct
 * @see cbml.cbr.types.IntegerFeatureStruct
 * @see cbml.cbr.types.StringFeatureStruct
 * @see cbml.cbr.types.TaxonomyFeatureStruct
 * @version 3.0
 */
public class SymbolFeatureStruct extends SimpleFeatureStruct implements cbml.cbr.FeatureStruct {

	private List possibleValues;
	/**
	 * Constructs a feature structure of type <code>Symbol</code> for the feature with the specified name. The feature's cardinality and whether or not this feature is a discriminating one are passed into the constructor. 
	 * @param featurePath the name of the feature that this Feature Structure defines.
	 * @param discriminant <code>true</code> if this feature structure is a discriminating feature.
	 * @param solution <code>true> if this feature structure defines the solution part of a case.
	 * @param manditory <code>true</code> if this feature must occur in a case defined with this feature structure.
	 */
	public SymbolFeatureStruct(String featurePath, boolean discriminant, boolean solution, boolean manditory) {
		super(featurePath, SYMBOL, discriminant, solution, manditory);
		possibleValues = new ArrayList();

	}
	/**
	 * Creates and returns a copy of this FeatureStruct.
	 * @return a clone of this instance.
	 */
	public Object clone() {
		SymbolFeatureStruct copy = new SymbolFeatureStruct(featurePath, discriminant, solution, manditory);
		int size = possibleValues.size();
		for (int i = 0; i < size; i++) {
			copy.setPossVal((String) possibleValues.get(i));
		}
		return copy;
	}
	/**
	 * Returns a <code>List</code> of the possible values of this symbol Element.
	 * @return returns a <code>List</code> of the possible values of this symbol element.
	 */
	public List getValues() {
		return possibleValues;
	}
	/**
	 * This method is used by the case structure parser to generate the symbol list definition of this feature structure. It should not be used by the user.
	 * @param newEnumeratedValue the name of this symbol element.
	 */
	public void setPossVal(String newEnumeratedValue) {
		possibleValues.add(newEnumeratedValue);
	}
	/**
	 * Returns a string representation of this feature structure. This representation is in CBML format.
	 * @return a string representation of this feature structure.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("<symbol>");
		for (int i = 0; i < possibleValues.size(); i++) {
			sb.append("<enumeration value=\"" + (String) possibleValues.get(i) + "\"/>");
		}
		sb.append("</symbol>");
		return super.getCBMLRepresentation(sb.toString());
	}
	/**
	 * Validates the specified feature against this feature structure definition. The feature value is valid if it exists in the list of possible values.
	 * @return <code>true</code> it this feature is valid according to this feature structure definition.
	 * @param testFeature the feature to be validated.
	 */
	public boolean validate(Feature testFeature) {
		String value = (String) testFeature.getValue();
		for (int i = 0; i < possibleValues.size(); i++) {
			if (((String) possibleValues.get(i)).equals(value))
				return true;
		}
		return false;
	}
}