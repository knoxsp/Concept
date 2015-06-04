package cbml.cbr.types;

import cbml.cbr.Feature;
/**
 * This defines a feature of type <code>string</code>. A string feature can have any string value. 
 *	This is one of a number of CBML defined feature types: <code>boolean</code>, <code>complex</code>, <code>double</code>, <code>integer</code>, <code>string</code>, <code>symbol</code> and <code>taxonomy</code>.
 * @author Lorcan Coyle
 * @see cbml.cbr.types.BooleanFeatureStruct
 * @see cbml.cbr.types.ComplexFeatureStruct
 * @see cbml.cbr.types.DoubleFeatureStruct
 * @see cbml.cbr.types.IntegerFeatureStruct
 * @see cbml.cbr.types.SymbolFeatureStruct
 * @see cbml.cbr.types.TaxonomyFeatureStruct
 * @version 3.0
 */
public class StringFeatureStruct extends SimpleFeatureStruct implements cbml.cbr.FeatureStruct {

	/**
	 * Constructs a feature structure of type <code>String</code> for the feature with the specified name. The feature's cardinality and whether or not this feature is a discriminating one are passed into the constructor. 
	 * @param featurePath the name of the feature that this Feature Structure defines.
	 * @param discriminant <code>true</code> if this feature structure is a discriminating feature.
	 * @param solution <code>true> if this feature structure defines the solution part of a case.
	 * @param manditory <code>true</code> if this feature must occur in a case defined with this feature structure.
	 */
	public StringFeatureStruct(String featurePath, boolean discriminant, boolean solution, boolean manditory) {
		super(featurePath, STRING, discriminant, solution, manditory);
	}
	/**
	 * Returns a string representation of this feature structure. This representation is in CBML format.
	 * @return a string representation of this feature structure.
	 */
	public String toString() {
		return super.getCBMLRepresentation("<string/>");
	}
	/**
	 * Validates the specified feature against this feature structure definition. The feature value is valid if it is a <code>String</code>.
	 * @return <code>true</code> (since all Feature values are Strings).
	 * @param testFeature the feature to be validated.
	 */
	public boolean validate(Feature testFeature) {
		//String value = (String) testFeature.getValue();
		return true;
	}
}
