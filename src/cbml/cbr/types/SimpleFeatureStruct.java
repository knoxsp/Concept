package cbml.cbr.types;

import java.util.List;

import cbml.cbr.BadFeatureValueException;
import cbml.cbr.FeatureStruct;
import cbml.cbr.IncompatableFeatureException;

/**
 * This is a helper class. It defines a simple feature type, all other feature structure definitions inherit from this (including complex).  
 *	This is the parent of all CBML defined feature types: <code>boolean</code>, <code>complex</code>, <code>double</code>, <code>integer</code>, <code>string</code>, <code>symbol</code> and <code>taxonomy</code>.
 * @author Lorcan Coyle
 * @see cbml.cbr.types.BooleanFeatureStruct
 * @see cbml.cbr.types.ComplexFeatureStruct
 * @see cbml.cbr.types.DoubleFeatureStruct
 * @see cbml.cbr.types.IntegerFeatureStruct
 * @see cbml.cbr.types.StringFeatureStruct
 * @see cbml.cbr.types.SymbolFeatureStruct
 * @see cbml.cbr.types.TaxonomyFeatureStruct
 * @version 3.0
 */
public abstract class SimpleFeatureStruct implements FeatureStruct {
	protected boolean discriminant;
	protected String featureName;
	protected String featurePath;
	protected boolean manditory;
	protected boolean solution;
	protected int type;

	/**
	 * Constructs a feature structure of type <code>Simple</code> for the feature with the specified name. 
	 * The feature's cardinality, similarity measure name and whether or not this feature is a discriminating
	 * one are passed into the constructor. 
	 * @param featurePath the name of the feature that this Feature Structure defines.
	 * @param type the type of simple feature structure this is.
	 * @param discriminant <code>true</code> if this feature structure is a discriminating feature.
	 * @param solution <code>true> if this feature structure defines the solution part of a case.
	 * @param manditory <code>true</code> if this feature must occur in a case defined with this feature structure.
	 */
	public SimpleFeatureStruct(String featurePath, int type, boolean discriminant, boolean solution, boolean manditory) {
		this.type = type;
		reset(featurePath, discriminant, solution, manditory);
	}

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * This method is used by the case structure parser to add a child feature structure to this complex feature definition. 
	 * This method is overridden by {<code>ComplexFeatureStruct</code>}. If any other feature structure type tries to call 
	 * this method an <code>IncompatableFeatureException</code> is thrown.
	 * @param childFeatureStruct child feature structure to be added to this feature structure.
	 * @throws cbml.cbr.IncompatableFeatureException if the feature structure type is not <code>complex</code>.
	 */
	public void addSubFeatureStruct(FeatureStruct childFeatureStruct) throws cbml.cbr.IncompatableFeatureException {
		throw new IncompatableFeatureException("Cannot call function addSubFeature on " + types[type] + " type");
	}

	protected String getCBMLRepresentation(String typeRepresentation) {
		StringBuffer sb = new StringBuffer();
		sb.append("<feature name=\"");
		String name = this.getFeaturePath().substring(this.getFeaturePath().lastIndexOf("/") + 1);
		sb.append(name);
		sb.append("\"");
		if (!isDiscriminant()) {
			sb.append(" discriminant=\"false\"");
		}
		if (!isManditory()) {
			sb.append(" manditory=\"false\"");
		}
		if (isSolution()) {
			sb.append(" solution=\"true\"");
		}
		sb.append(">");
		sb.append(typeRepresentation);
		sb.append("</feature>");
		return sb.toString();
	}
	/**
	 * Returns the path of the feature that this feature struct refers to.
	 * @return the path of the feature that this feature struct refers to.
	 */
	public String getFeaturePath() {
		return featurePath;
	}
	/**
	 * Returns the maximum allowable value of the feature specified by this feature structure. This method is overridden by {<code>DoubleFeatureStruct</code>, <code>IntegerFeatureStruct</code>}. If any other Feature Structure type tries to call this method an <code>IncompatableFeatureException</code> is thrown.
	 * @return the maximum allowable value for this feature structure.
	 * @throws cbml.cbr.IncompatableFeatureException if the feature structure type is not one of {<code>double</code>, <code>integer</code>}.
	 */
	public String getMaxValue() throws IncompatableFeatureException {
		throw new IncompatableFeatureException("Cannot call function getMaxValue on " + types[type] + " type");
	}

	/**
	 * Returns the minimum allowable value of the feature specified by this feature structure. This method is overridden by {<code>DoubleFeatureStruct</code>, <code>IntegerFeatureStruct</code>}. If any other Feature Structure type tries to call this method an <code>IncompatableFeatureException</code> is thrown.
	 * @return the minimum allowable value for this feature structure.
	 * @throws cbml.cbr.IncompatableFeatureException if the feature structure type is not one of {<code>double</code>, <code>integer</code>}.
	 */
	public String getMinValue() throws cbml.cbr.IncompatableFeatureException {
		throw new IncompatableFeatureException("Cannot call function getMinValue on " + types[type] + " type");
	}
	/**
	 * This method is used by the case content parser to add features to a case. This method is overridden by <code>ComplexFeatureStruct</code>. If any other Feature Structure type tries to call this method an <code>IncompatableFeatureException</code> is thrown.
	 * @return a <code>List</code> of the child feature structure definitions of this feature structure definition.
	 * @throws cbml.cbr.IncompatableFeatureException if the feature structure type is not <code>complex</code>.
	 */
	public List getSubFeatureStructs() throws IncompatableFeatureException {
		throw new IncompatableFeatureException("Cannot call function getSubFeatures on " + types[type] + " type");
	}
	/**
	 * Returns the feature structure type (one of {<code>FeatureStruct.BOOLEAN</code>, <code>FeatureStruct.COMPLEX</code>, <code>FeatureStruct.DOUBLE</code>, <code>FeatureStruct.INTEGER</code>, <code>FeatureStruct.STRING</code>, <code>FeatureStruct.SYMBOL</code>, <code>FeatureStruct.TAXONOMY</code>}).
	 * @return the feature structure type.
	 */
	public int getType() {
		return type;
	}
	/**
	 * Returns a <code>List</code> of the possible values of this Feature Structure. This method is overridden by {<code>SymbolFeatureStruct</code>, <code>TaxonomyFeatureStruct</code>}. If any other feature structure type tries to call this method an <code>IncompatableFeatureException</code> is thrown.
	 * @return a <code>List</code> of the possible values of this Feature Structure.
	 * @throws cbml.cbr.IncompatableFeatureException if the feature structure type is not <code>symbol</code>, <code>taxonomy</code>.
	 */
	public List getValues() throws cbml.cbr.IncompatableFeatureException {
		throw new IncompatableFeatureException("Cannot call function getValues on " + types[type] + " type");
	}
	/**
	 * Checks whether or not this feature is a discriminating one. Discriminating features are passed into the CBR module, non dizcriminating ones are ignored.
	 * @return <code>true</code> if this feature is discriminating.
	 */
	public boolean isDiscriminant() {
		return discriminant;
	}

	/**
	 * Returns <code>true</code> if the feature defined by this feature structure is manditory in the case.
	 * @return <code>true</code> if the feature defined by this feature structure is manditory in the case.
	 */
	public boolean isManditory() {
		return manditory;
	}
	/**
	 * Returns <code>true</code> if this feature is a solution feature within the case definition.
	 * @return <code>true</code> if this feature is a solution feature within the case definition.
	 */
	public boolean isSolution() {
		return solution;
	}
	/**
	 * Helper method for initialising a <code>FeatureStruct</code>.
	 * @param featurePath the name of the feature that this Feature Structure defines.
	 * @param discriminant <code>true</code> if this feature structure is a discriminating feature.
	 * @param solution <code>true> if this feature structure defines the solution part of a case.
	 * @param manditory <code>true</code> if this feature must occur in a case defined with this feature structure.
	 */
	public void reset(String featurePath, boolean discriminant, boolean solution, boolean manditory) {
		this.featurePath = featurePath;
		featureName = featurePath.substring(featurePath.lastIndexOf("/") + 1);
		this.discriminant = discriminant;
		this.solution = solution;
		this.manditory = manditory;
	}

	/**
	 * This method is used by the case structure parser to set the maximum allowable value for this feature structure. This method is overridden by {<code>DoubleFeatureStruct</code>, <code>IntegerFeatureStruct</code>}. If any other Feature Structure type tries to call this method an <code>IncompatableFeatureException</code> is thrown.
	 * @param newMaxValue the maximum allowable value for this feature structure.
	 * @throws cbml.cbr.IncompatableFeatureException if the feature structure type is not one of {<code>double</code>, <code>integer</code>}.
	 * @throws cbml.cbr.BadFeatureValueException passed value is invalid.
	 */
	public void setMaxValue(String newMaxValue) throws cbml.cbr.IncompatableFeatureException, BadFeatureValueException {
		throw new IncompatableFeatureException("Cannot call function setMaxValue on " + types[type] + " type");
	}
	/**
	 * This method is used by the case structure parser to set the minimum allowable value for this feature structure. This method is overridden by {<code>DoubleFeatureStruct</code>, <code>IntegerFeatureStruct</code>}. If any other Feature Structure type tries to call this method an <code>IncompatableFeatureException</code> is thrown.
	 * @param newMinValue the minimum allowable value for this feature structure.
	 * @throws cbml.cbr.IncompatableFeatureException if the feature structure type is not one of {<code>double</code>, <code>integer</code>}.
	 * @throws cbml.cbr.BadFeatureValueException passed value is invalid.
	 */
	public void setMinValue(String newMinValue) throws cbml.cbr.IncompatableFeatureException, BadFeatureValueException {
		throw new IncompatableFeatureException("Cannot call function setMinValue on " + types[type] + " type");
	}
	/**
	 * This method is used by the case structure parser to generate the symbol list definition of this feature structure. This method is overridden by <code>TaxonomyFeatureStruct</code>. If any other Feature Structure type tries to call this method an <code>IncompatableFeatureException</code> is thrown.
	 * @param value the name of this symbol element.
	 * @throws cbml.cbr.IncompatableFeatureException if the feature structure is not of type <code>symbol</code>.
	 */
	public void setPossVal(String value) throws cbml.cbr.IncompatableFeatureException {
		throw new IncompatableFeatureException("Cannot call function setPossVal on " + types[type] + " type");
	}
	/**
	 * This method is overridden by <code>TaxonomyFeatureStruct</code>. If any other Feature Structure type tries to call this method an <code>IncompatableFeatureException</code> is thrown.
	 * @throws cbml.cbr.IncompatableFeatureException if the feature structure type is not <code>taxonomy</code>
	 */
	public void taxonomyEndElement() throws cbml.cbr.IncompatableFeatureException {
		throw new IncompatableFeatureException("Cannot call function taxonomyEndElement on " + types[type] + " type");
	}
	/**
	 * This method is overridden by <code>TaxonomyFeatureStruct</code>. If any other Feature Structure type tries to call this method an <code>IncompatableFeatureException</code> is thrown.
	 * @param value the name of this taxonomy element.
	 * @throws cbml.cbr.IncompatableFeatureException if the feature structure type is not <code>taxonomy</code>
	 */
	public void taxonomyStartElement(String value) throws cbml.cbr.IncompatableFeatureException {
		throw new IncompatableFeatureException("Cannot call function taxonomyStartElement on " + types[type] + " type");
	}

}
