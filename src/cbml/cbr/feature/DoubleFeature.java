package cbml.cbr.feature;

/**
 * A Double Feature is a <code>double</code> CBML Feature. It is a simple attribute value pair.
 * @see cbml.cbr.feature.ComplexFeature
 * @see cbml.cbr.feature.IntegerFeature
 * @see cbml.cbr.feature.StringFeature
 * @author Lorcan Coyle
 */
public class DoubleFeature extends StringFeature {
	private Double doubleObj;
	/**
	 * Constructs an empty Double Feature Object
	 * @param featurePath the path of this feature
	 * @param value the value of this simple Feature
	 */
	public DoubleFeature(String featurePath, Double value) {
		super(featurePath, value.toString());
		doubleObj = value;
	}
	/**
	 * Compares the specified object with this Feature for equality. Returns true if and only if the specified object is also a <code>DoubleFeature</code>, both Features have the same path, and both have the same value.
	 * @param o the reference object with which to compare.
	 * @return <code>true</code> if the specified object is equal to this <code>DoubleFeature</code>. 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		DoubleFeature f = (DoubleFeature) o;
		if (path.equals(f.getPath()))
			return f.doubleValue() == doubleValue();
		return false;
	}
	/**
	 * Sets the value of this feature to the specified value. <code>newValue<code> must be a <code>String</code>.
	 * @param newValue the value to be set (a <code>String</code>).
	 */
	public void setDoubleValue(Double newValue) {
		doubleObj = newValue;
	}
	/**
	 * Returns a string representation of this Feature. This representation is in CBML format.
	 * @return a string representation of this Feature.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("<");
		sb.append(name);
		sb.append(">");
		sb.append(doubleValue());
		sb.append("</");
		sb.append(name);
		sb.append(">");
		return sb.toString();
	}
	/**
	 * Returns a clone of this Feature.
	 * @return a clone of this instance.
	 */
	public Object clone() throws CloneNotSupportedException{
	   DoubleFeature clone = (DoubleFeature) super.clone();
	   clone.doubleObj = Double.valueOf(doubleObj.doubleValue());
		return clone;
	}

	/**
	 * Returns the <code>double</code> value of this feature.
	 * @return a double value of this feature
	 * @author johnloughrey
	 */
	public double doubleValue() {
		return doubleObj.doubleValue();
	}

}
