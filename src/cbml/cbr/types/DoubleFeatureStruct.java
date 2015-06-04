package cbml.cbr.types;

import cbml.cbr.BadFeatureValueException;
import cbml.cbr.Feature;
import cbml.cbr.feature.DoubleFeature;
/**
 * This defines a feature of type <code>double</code>. A <code>double</code> feature value must be a <code>double</code> and can have its range restricted. 
 *	This is one of a number of CBML defined feature types: <code>boolean</code>, <code>complex</code>, <code>double</code>, <code>integer</code>, <code>string</code>, <code>symbol</code> and <code>taxonomy</code>.
 * @author Lorcan Coyle
 * @see cbml.cbr.types.BooleanFeatureStruct
 * @see cbml.cbr.types.ComplexFeatureStruct
 * @see cbml.cbr.types.IntegerFeatureStruct
 * @see cbml.cbr.types.StringFeatureStruct
 * @see cbml.cbr.types.SymbolFeatureStruct
 * @see cbml.cbr.types.TaxonomyFeatureStruct
 * @version 3.0
 */
public class DoubleFeatureStruct extends SimpleFeatureStruct implements cbml.cbr.FeatureStruct {
	private boolean maxLimit;
	private double maxValue;
	private boolean minLimit;
	private double minValue;

	/**
	 * Constructs a feature structure of type <code>Double</code> for the feature with the specified name. The feature's cardinality and whether or not this feature is a discriminating one are passed into the constructor. 
	 * @param featurePath the name of the feature that this Feature Structure defines.
	 * @param discriminant <code>true</code> if this feature structure is a discriminating feature.
	 * @param solution <code>true> if this feature structure defines the solution part of a case.
	 * @param manditory <code>true</code> if this feature must occur in a case defined with this feature structure.
	 */
	public DoubleFeatureStruct(String featurePath, boolean discriminant, boolean solution, boolean manditory) {
		super(featurePath, DOUBLE, discriminant, solution, manditory);
		maxLimit = false;
		minLimit = false;

	}
	/**
	 * Creates and returns a copy of this FeatureStruct.
	 * @return a clone of this instance.
	 */
	public Object clone() {
		DoubleFeatureStruct copy = new DoubleFeatureStruct(featurePath, discriminant, solution, manditory);
		try {
			if(maxLimit)
				copy.setMaxValue(getMaxValue());
			if(minLimit)
				copy.setMinValue(getMinValue());
		} catch (BadFeatureValueException e) {e.printStackTrace();}
		return copy;
	}
	/**
	 * Returns the maximum allowable value of the feature specified by this feature structure. The value is returned as a <code>String</code> representation of a <code>Double</code>
	 * @return the maximum allowable value for this feature structure.
	 */
	public String getMaxValue() {
		if (maxLimit)
			return Double.toString(maxValue);
		return null;
	}
	/**
	 * Returns the minimum allowable value of the feature specified by this feature structure. The value is returned as a <code>String</code> representation of a <code>Double</code>
	 * @return the minimum allowable value for this feature structure.
	 */
	public String getMinValue() {
		if (minLimit)
			return Double.toString(minValue);
		return null;
	}
	/**
	 * This method is used by the case structure parser to set the maximum allowable value for this feature structure. It should not be used by the user.
	 * @param newMaxValue maximum allowable value for this feature structure
	 * @throws cbml.cbr.BadFeatureValueException if the value is not a valid double. 
	 */
	public void setMaxValue(String newMaxValue) throws BadFeatureValueException {
		try {
			maxValue = (new Double(newMaxValue)).doubleValue();
			maxLimit = true;
		} catch (NumberFormatException e) {
			throw new BadFeatureValueException("ERROR: cannot set maximum allowable value in DoubleFeatureStruct, " + getFeaturePath() + " to \"" + newMaxValue + "\". \"" + newMaxValue + "\" is not a Double.");
		}

	}
	/**
	 * This method is used by the case structure parser to set the minimum allowable value for this feature structure. It should not be used by the user.
	 * @param newMinValue the minimum allowable value for this feature structure.
	 * @throws cbml.cbr.BadFeatureValueException if the value is not a valid double.
	 */
	public void setMinValue(String newMinValue) throws BadFeatureValueException {
		try {
			minValue = (new Double(newMinValue)).doubleValue();
			minLimit = true;
		} catch (NumberFormatException e) {
			throw new BadFeatureValueException("ERROR: cannot set minimum allowable value in DoubleFeatureStruct, " + getFeaturePath() + " to \"" + newMinValue + "\". \"" + newMinValue + "\" is not a Double.");
		}
	}
	/**
	 * Returns a string representation of this feature structure. This representation is in CBML format.
	 * @return a string representation of this feature structure.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("<double>");
		if (minLimit)
			sb.append("<minInclusive value=\"" + Double.toString(minValue) + "\"/>");
		if (maxLimit)
			sb.append("<minInclusive value=\"" + Double.toString(maxValue) + "\"/>");
		sb.append("</double>");
		return super.getCBMLRepresentation(sb.toString());
	}
	/**
	 * Validates the specified feature against this feature structure definition. The feature value is valid if is a <code>double</code> and resides within the range defined in this feature structure definition. 
	 * @return <code>true</code> it this feature is valid according to this feature structure definition.
	 * @param testFeature the feature to be validated.
	 */
	public boolean validate(Feature testFeature) {		
		double v;
		if(testFeature instanceof DoubleFeature){
			v = ((DoubleFeature)testFeature).doubleValue();
		}else{
			v = new Double((String) testFeature.getValue()).doubleValue();
		}
		return (!((maxLimit && v > maxValue) || (minLimit && v < minValue)));
	}
}
