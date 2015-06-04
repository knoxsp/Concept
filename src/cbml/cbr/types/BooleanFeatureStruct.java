package cbml.cbr.types;

import cbml.cbr.Feature;
/**
 * This defines a feature of type <code>boolean</code>. A <code>boolean</code> feature value must be either true or false.
 *	This is one of a number of CBML defined feature types: <code>boolean</code>, <code>complex</code>, <code>double</code>, <code>integer</code>, <code>string</code>, <code>symbol</code> and <code>taxonomy</code>.
 * @author Lorcan Coyle
 * @see cbml.cbr.types.ComplexFeatureStruct
 * @see cbml.cbr.types.DoubleFeatureStruct
 * @see cbml.cbr.types.IntegerFeatureStruct
 * @see cbml.cbr.types.StringFeatureStruct
 * @see cbml.cbr.types.SymbolFeatureStruct
 * @see cbml.cbr.types.TaxonomyFeatureStruct
 * @version 3.0
 */
public class BooleanFeatureStruct extends SimpleFeatureStruct implements cbml.cbr.FeatureStruct {

	/**
	 * Constructs a feature structure of type <code>Boolean</code> for the feature with the specified name. Whether or not this feature is a discriminating one is passed into the constructor. The minOccurs and maxOccurs on Boolean types are 0 and 1 respectively.
	 * @param featurePath the name of the feature that this Feature Structure defines.
	 * @param discriminant <code>true</code> if this feature structure is a discriminating feature.
	 * @param solution <code>true> if this feature structure defines the solution part of a case.
	 * @param manditory <code>true</code> if this feature must occur in a case defined with this feature structure.
	 */
	public BooleanFeatureStruct(String featurePath, boolean discriminant, boolean solution, boolean manditory) {
		super(featurePath, BOOLEAN, discriminant, solution, manditory);

	}

	/**
	 * Returns a string representation of this feature structure. This representation is in CBML format.
	 * @return a string representation of this feature structure.
	 */
	public String toString() {
		return super.getCBMLRepresentation("<boolean/>");
	}
	/**
	 * Validates the specified feature against this feature structure definition. The feature value is valid if is a <code>boolean</code>. 
	 * @return <code>true</code> it this feature is valid according to this feature structure definition.
	 * @param testFeature the feature to be validated.
	 */
	public boolean validate(Feature testFeature) {
		String value = (String) testFeature.getValue();
		return (value.equals("true") || value.equals("false")) ? true : false;
	}
}
