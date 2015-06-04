package cbml.cbr;

import java.util.List;
/**
 * This defines a CBML feature. There are a number of <i>CBML</i> defined types: <code>boolean</code>, <code>complex</code>, <code>double</code>, <code>integer</code>, <code>string</code>, <code>symbol</code> and <code>taxonomy</code>. These all follow this interface.
 * @author Lorcan Coyle
 * @see cbml.cbr.types.BooleanFeatureStruct
 * @see cbml.cbr.types.ComplexFeatureStruct
 * @see cbml.cbr.types.DoubleFeatureStruct
 * @see cbml.cbr.types.IntegerFeatureStruct
 * @see cbml.cbr.types.SimpleFeatureStruct
 * @see cbml.cbr.types.StringFeatureStruct
 * @see cbml.cbr.types.SymbolFeatureStruct
 * @see cbml.cbr.types.TaxonomyFeatureStruct
 * @version 3.0
 */
public interface FeatureStruct extends java.io.Serializable, Cloneable{
	//final static int ARRAY = 0;
	final static int BOOLEAN = 0;
	final static int COMPLEX = 1;
	//final static int DATE = 3;
	final static int DOUBLE = 2;
	final static int INTEGER = 3;
	final static int STRING = 4;
	final static int SYMBOL = 5;
	final static int TAXONOMY = 6;
	final static String[] types = { "boolean", "complex", "double", "integer", "string", "symbol", "taxonomy" };
	/**
	 * Adds the specified feature structure to this <code>complex</code> feature structure.
	 * @param childFeatureStruct the feature structure to be added to this <code>complex</code> feature structure.
	 * @exception IncompatableFeatureException <i>This exception should never be thrown. It could only be thrown if the parser misread the case structure document. This will be removed in the next version.</i>.
	 */
	void addSubFeatureStruct(FeatureStruct childFeatureStruct) throws IncompatableFeatureException;
	/**
	 * Creates and returns a copy of this FeatureStruct.
	 * @return a clone of this instance.
	 */
	Object clone();
	/**
	 * Returns the path of the feature that this feature struct refers to.
	 * @return the path of the feature that this feature struct refers to.
	 */
	String getFeaturePath();
	/**
	 * Returns the maximum allowable value of the feature specified by this feature structure. This method is overridden by {<code>DoubleFeatureStruct</code>, <code>IntegerFeatureStruct</code>}. If any other Feature Structure type tries to call this method an <code>IncompatableFeatureException</code> is thrown.
	 * @return the maximum allowable value for this feature structure.
	 * @throws cbml.cbr.IncompatableFeatureException if the feature structure type is not one of {<code>double</code>, <code>integer</code>}.
	 */
	String getMaxValue() throws IncompatableFeatureException;
	/**
	 * Returns the minimum allowable value of the feature specified by this feature structure. This method is overridden by {<code>DoubleFeatureStruct</code>, <code>IntegerFeatureStruct</code>}. If any other Feature Structure type tries to call this method an <code>IncompatableFeatureException</code> is thrown.
	 * @return the minimum allowable value for this feature structure.
	 * @throws cbml.cbr.IncompatableFeatureException if the feature structure type is not one of {<code>double</code>, <code>integer</code>}.
	 */
	String getMinValue() throws IncompatableFeatureException;
	/**
	 * This method is used by the case content parser to add features to a case. This method is implemented by <code>ComplexFeatureStruct</code>. If any other Feature Structure type tries to call this method an <code>IncompatableFeatureException</code> is thrown.
	 * @return a <code>List</code> of the child feature structure definitions of this feature structure definition.
	 * @throws cbml.cbr.IncompatableFeatureException if the feature structure type is not <code>complex</code>.
	 */
	List getSubFeatureStructs() throws IncompatableFeatureException;
	/**
	 * Returns the feature structure type (one of {<code>FeatureStruct.BOOLEAN</code>, <code>FeatureStruct.COMPLEX</code>, <code>FeatureStruct.DOUBLE</code>, <code>FeatureStruct.INTEGER</code>, <code>FeatureStruct.STRING</code>, <code>FeatureStruct.SYMBOL</code>, <code>FeatureStruct.TAXONOMY</code>}).
	 * @return the feature structure type.
	 */
	int getType();
	/**
	 * Returns a <code>List</code> of the possible values of this Feature Structure.  This method is overridden by <code>TaxonomyFeatureStruct</code>,<code>SymbolicFeatureStruct</code>. If any other Feature Structure type tries to call this method an <code>IncompatableFeatureException</code> is thrown.
	 * @return returns a <code>List</code> of the possible values of this Feature Structure.
	 * @throws cbml.cbr.IncompatableFeatureException if the feature structure type is not one of {<code>symbol</code>, <code>taxonomy</code>}.
	 */
	List getValues() throws IncompatableFeatureException;
	/**
	 * Checks whether or not this feature is a discriminating one. Discriminating features are passed into the CBR module, non dizcriminating ones are ignored.
	 * @return <code>true</code> if this feature is discriminating.
	 */
	boolean isDiscriminant();

	/**
	 * Returns <code>true</code> if the feature defined by this feature structure is manditory in the case.
	 * @return <code>true</code> if the feature defined by this feature structure is manditory in the case.
	 */
	boolean isManditory();
	/**
	 * Returns <code>true</code> if this feature is a solution feature within the case definition.
	 * @return <code>true</code> if this feature is a solution feature within the case definition.
	 */
	boolean isSolution();
	/**
	 * Helper method for initialising a <code>FeatureStruct</code>.
	 * @param featurePath the path of the feature that this Feature Structure defines.
	 * @param discriminant <code>true</code> if this feature structure is a discriminating feature.
	 * @param solution <code>true> if this feature structure defines the solution part of a case.
	 * @param manditory <code>true</code> if this feature must occur in a case defined with this feature structure.
	 */
	void reset(String featurePath, boolean discriminant, boolean solution, boolean manditory);

	/**
	 * This method is used by the case structure parser to set the maximum allowable value for this feature structure. This method is overridden by {<code>DoubleFeatureStruct</code>, <code>IntegerFeatureStruct</code>}. If any other Feature Structure type tries to call this method an <code>IncompatableFeatureException</code> is thrown.
	 * @param newMaxValue the maximum allowable value for this feature structure.
	 * @throws cbml.cbr.IncompatableFeatureException if the feature structure type is not one of {<code>double</code>, <code>integer</code>}.
	 * @throws cbml.cbr.BadFeatureValueException if <code>newMaxValue</code> is invalid.
	 */
	void setMaxValue(String newMaxValue) throws IncompatableFeatureException, BadFeatureValueException;
	/**
	 * This method is used by the case structure parser to set the minimum allowable value for this feature structure. This method is overridden by {<code>DoubleFeatureStruct</code>, <code>IntegerFeatureStruct</code>}. If any other Feature Structure type tries to call this method an <code>IncompatableFeatureException</code> is thrown.
	 * @param newMinValue the minimum allowable value for this feature structure.
	 * @throws cbml.cbr.IncompatableFeatureException if the feature structure type is not one of {<code>double</code>, <code>integer</code>}.
	 * @throws cbml.cbr.BadFeatureValueException if <code>newMinValue</code> is invalid.
	 */
	void setMinValue(String newMinValue) throws IncompatableFeatureException, BadFeatureValueException;
	/**
	 * This method is used by the case structure parser to generate the symbol list definition of this feature structure. This method is overridden by <code>TaxonomyFeatureStruct</code>. If any other Feature Structure type tries to call this method an <code>IncompatableFeatureException</code> is thrown.
	 * @param newEnumeratedValue the name of this symbol element.
	 * @throws cbml.cbr.IncompatableFeatureException if the feature structure is not of type <code>symbol</code>.
	 */
	void setPossVal(String newEnumeratedValue) throws IncompatableFeatureException;
	/**
	 * This method is overridden by <code>TaxonomyFeatureStruct</code>. If any other Feature Structure type tries to call this method an <code>IncompatableFeatureException</code> is thrown.
	 * @throws cbml.cbr.IncompatableFeatureException if the feature structure type is not <code>taxonomy</code>
	 */
	void taxonomyEndElement() throws IncompatableFeatureException;
	/**
	 * This method is overridden by <code>TaxonomyFeatureStruct</code>. If any other Feature Structure type tries to call this method an <code>IncompatableFeatureException</code> is thrown.
	 * @param newTaxonomyValue the name of this taxonomy element.
	 * @throws cbml.cbr.IncompatableFeatureException if the feature structure type is not <code>taxonomy</code>
	 */
	void taxonomyStartElement(String newTaxonomyValue) throws IncompatableFeatureException;
	/**
	 * Returns a string representation of this feature structure. This representation is in CBML format.
	 * @return a string representation of this feature structure.
	 */
	String toString();
	/**
	 * Supports on-the-fly validation. Validates the specified feature against this feature structure definition. The feature value is valid if is within the constraints defined in this feature structure definition. 
	 * @return <code>true</code> it this feature is valid according to this feature structure definition.
	 * @param testFeature the feature to be validated.
	 */
	boolean validate(Feature testFeature);
}
