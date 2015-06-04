package cbml.cbr.types;

import cbml.cbr.BadFeatureValueException;
import cbml.cbr.Feature;
import cbml.cbr.feature.IntegerFeature;
/**
 * This defines a feature of type <code>integer</code>. An <code>integer</code> must be an <code>int</code> and can have its range restricted. 
 *	This is one of a number of CBML defined feature types: <code>boolean</code>, <code>complex</code>, <code>double</code>, <code>integer</code>, <code>string</code>, <code>symbol</code> and <code>taxonomy</code>.
 * @author Lorcan Coyle
 * @see cbml.cbr.types.BooleanFeatureStruct
 * @see cbml.cbr.types.ComplexFeatureStruct
 * @see cbml.cbr.types.DoubleFeatureStruct
 * @see cbml.cbr.types.StringFeatureStruct
 * @see cbml.cbr.types.SymbolFeatureStruct
 * @see cbml.cbr.types.TaxonomyFeatureStruct
 * @version 3.0
 */
public class IntegerFeatureStruct extends SimpleFeatureStruct implements cbml.cbr.FeatureStruct {
	private boolean maxLimit;
	private int maxValue;
	private boolean minLimit;
	private int minValue;

	/**
	 * Constructs a feature structure of type <code>Integer</code> for the feature with the specified name. The feature's cardinality and whether or not this feature is a discriminating one are passed into the constructor. 
	 * @param featurePath the name of the feature that this Feature Structure defines.
	 * @param discriminant <code>true</code> if this feature structure is a discriminating feature.
	 * @param solution <code>true> if this feature structure defines the solution part of a case.
	 * @param manditory <code>true</code> if this feature must occur in a case defined with this feature structure.
	 */
	public IntegerFeatureStruct(String featurePath, boolean discriminant, boolean solution, boolean manditory) {
		super(featurePath, INTEGER, discriminant, solution, manditory);
		maxLimit = false;
		minLimit = false;

	}
	/**
	 * Creates and returns a copy of this FeatureStruct.
	 * @return a clone of this instance.
	 */
	public Object clone() {
		IntegerFeatureStruct copy = new IntegerFeatureStruct(featurePath, discriminant, solution, manditory);
		try {
			if(maxLimit)
				copy.setMaxValue(getMaxValue());
			if(minLimit)
				copy.setMinValue(getMinValue());
		} catch (BadFeatureValueException e) {e.printStackTrace();}
		return copy;
	}
	/**
	 * Returns the maximum allowable value of the feature specified by this feature structure. The value is returned as a <code>String</code> representation of an <code>Integer</code>
	 * @return the maximum allowable value for this feature structure.
	 */
	public java.lang.String getMaxValue() {
		if (maxLimit)
			return Integer.toString(maxValue);
		return null;
	}
	/**
	 * Returns the minimum allowable value of the feature specified by this feature structure. The value is returned as a <code>String</code> representation of an <code>Integer</code>
	 * @return the minimum allowable value for this feature structure.
	 */
	public java.lang.String getMinValue() {
		if (minLimit)
			return Integer.toString(minValue);
		return null;
	}
	/**
	 * This method is used by the case structure parser to set the maximum allowable value for this feature structure. It should not be used by the user.
	 * @param newMaxValue the maximum allowable value for this feature structure.
	 * @throws cbml.cbr.BadFeatureValueException if the value is not a valid integer.
	 */
	public void setMaxValue(String newMaxValue) throws BadFeatureValueException {
		try {
			maxValue = (new Integer(newMaxValue)).intValue();
			maxLimit = true;
		} catch (NumberFormatException e) {
			throw new BadFeatureValueException("ERROR: cannot set maximum allowable value in IntegerFeatureStruct, " + getFeaturePath() + " to \"" + newMaxValue + "\". \"" + newMaxValue + "\" is not an Integer.");
		}
	}
	/**
	 * This method is used by the case structure parser to set the minimum allowable value for this feature structure. It should not be used by the user.
	 * @param newMinValue the minimum allowable value for this feature structure.
	 * @throws cbml.cbr.BadFeatureValueException if the value is not a valid integer.
	 */
	public void setMinValue(String newMinValue) throws BadFeatureValueException {
		try {
			minValue = (new Integer(newMinValue)).intValue();
			minLimit = true;
		} catch (NumberFormatException e) {
			throw new BadFeatureValueException("ERROR: cannot set minimum allowable value in IntegerFeatureStruct, " + getFeaturePath() + " to \"" + newMinValue + "\". \"" + newMinValue + "\" is not an Integer.");
		}
	}
	/**
	 * Returns a string representation of this feature structure. This representation is in CBML format.
	 * @return a string representation of this feature structure.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("<integer>");
		if (minLimit)
			sb.append("<minInclusive value=\"" + Integer.toString(minValue) + "\"/>");
		if (maxLimit)
			sb.append("<minInclusive value=\"" + Integer.toString(maxValue) + "\"/>");
		sb.append("</integer>");
		return super.getCBMLRepresentation(sb.toString());
	}
	/**
	 * Validates the specified feature against this feature structure definition. The feature value is valid if is an <code>int</code> and resides within the range defined in this feature structure definition. 
	 * @return <code>true</code> it this feature is valid according to this feature structure definition.
	 * @param testFeature the feature to be validated.
	 */
	public boolean validate(Feature testFeature) {
		int v;
		if(testFeature instanceof IntegerFeature){
			v = ((IntegerFeature)testFeature).intValue();
		}else{
			v = new Integer((String) testFeature.getValue()).intValue();
		}
		return (!((maxLimit && v > maxValue) || (minLimit && v < minValue)));
			
	}
}
