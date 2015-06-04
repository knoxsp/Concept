package cbml.cbr;

import java.io.Serializable;


/**
 * This is the CBML Feature Class. The CBML case content parser reads Case Documents and convert the case bases into cases and features within these cases into <code>Feature<code> objects. 
 * @author Lorcan Coyle
 * @see cbml.cbr.feature.ComplexFeature
 * @see cbml.cbr.feature.StringFeature
 * @see cbml.cbr.feature.IntegerFeature
 * @see cbml.cbr.feature.DoubleFeature
 */
public abstract class Feature implements Cloneable, Serializable {
	protected String name;
	protected boolean complex;
	protected String path;

	/**
	 * Constructs a <code>Feature</code> Object
	 * @param featurePath the path of this feature
	 * @param isComplex <code>true</code> if this feature is complex.
	 */
	protected Feature(String featurePath, boolean isComplex) {
		complex = isComplex;
		this.name = featurePath.substring(featurePath.lastIndexOf("/") + 1);
		path = featurePath;
	}
	/**
	 * Returns the name of this feature.
	 * @return the name of this feature.
	 */
	public String getName() {
		return name;
	}
	/**
	 * Returns the path of this feature within it's parent case.
	 * @return the path of this feature within it's parent case
	 */
	public String getPath() {
		return path;
	}
	/**
	 * Returns the value of this feature. 
		<p>If this feature is complex this value will be a <code>List</code></p>
		<p>If this feature is simple this value will be returned as a <code>String</code> representation of its value.
	 * @return the value of this feature.
	 */
	abstract public Object getValue();
	/**
	 * Returns <code>true</code> if this <code>Feature</code> is a complex type.
	 * @return <code>true</code> if this <code>Feature</code> is a complex type.
	 */
	public boolean isComplex() {
		return complex;
	}
	/**
	 * Sets the value of this feature to the specified value. <code>newValue</code> must either be a <code>Feature</code> or a <code>String</code>.
	 * @param newValue the value to be set (a <code>String</code> if this is a simple feature or a <code>Feature</code> if this is a complex feature.
	 * @throws BadFeatureValueException if <code>newValue</code> is not in the expected Format, i.e. a <code>String</code> for a simple feature or a <code>Feature</code> for a complex feature.	 
	 */
	public abstract void setValue(Object newValue) throws BadFeatureValueException;

	/**
	 * Returns a clone of this Feature.
	 * @return a clone of this instance.
	 */	
	public Object clone() throws CloneNotSupportedException{
	   Feature clone = (Feature) super.clone();
	   clone.name = name;
	   clone.complex = complex;
	   clone.path = path;
	   return clone;
	   
	}
}