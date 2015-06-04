package cbml.cbr.feature;

/**
 * A Simple Feature is a CBML Feature of one of the following types: <code>array</code>, <code>boolean</code>, <code>double</code>, <code>integer</code>, <code>string</code>, <code>symbol</code>, <code>taxonomy</code>. It is a simple attribute value pair.
 * @see cbml.cbr.feature.ComplexFeature
 * @see cbml.cbr.feature.DoubleFeature
 * @see cbml.cbr.feature.StringFeature
 * @author Lorcan Coyle
 */
public class IntegerFeature extends StringFeature {

	private Integer intObj;
	/**
	 * Constructs an empty Simple Feature Object
	 * @param featurePath the path of this feature
	 * @param value the value of this simple Feature
	 */
	public IntegerFeature(String featurePath, Integer value){
		super(featurePath, value.toString());
		intObj = value;
	}
	/**
	 * Compares the specified object with this Feature for equality. Returns true if and only if the specified object is also an <code>IntegerFeature</code>, both Features have the same path, and both have the same value.
	 * @param o the reference object with which to compare.
	 * @return <code>true</code> if the specified object is equal to this <code>StringFeature</code>.
	 */
	public boolean equals(Object o) {
		IntegerFeature f = (IntegerFeature) o;
		if (path.equals(f.getPath()))
			return f.intValue() == intValue();
		return false;
	}
	
	/**
	 * Sets the value of this feature to the specified value. <code>newValue<code> must be a <code>String</code>.
	 * @param newValue the value to be set (a <code>String</code>).
	 */
	public void setIntValue(Integer newValue){
		intObj = newValue;
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
		sb.append(intValue());	
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
		IntegerFeature clone =(IntegerFeature ) super.clone();
		clone.intObj = Integer.valueOf(intObj.intValue());
		return clone;
	}
	
	/**
	 * Returns the in value of this feature, only if the value is of type <code>java.lang.Integer</code>
	 * @return an int value of this feature
	 * @author johnloughrey
	 */
	public int intValue()
	{
		return intObj.intValue();
	}
}
