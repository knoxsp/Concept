package cbml.cbr.feature;

import java.util.List;

import org.apache.log4j.Logger;

import cbml.cbr.BadFeatureValueException;
import cbml.cbr.Feature;
/**
 * A Complex Feature is a CBML Feature of type <code>complex</code>. It has a number of sub features.
 * @author Lorcan Coyle
 * @see cbml.cbr.feature.IntegerFeature
 * @see cbml.cbr.feature.DoubleFeature 
 * @see cbml.cbr.feature.StringFeature
 */
public class ComplexFeature extends Feature {
   
   static final private Logger logger = Logger.getLogger(ComplexFeature.class);

   private List value;
	/**
	 * Constructs an empty Complex Feature Object
	 * @param featurePath the path of this feature
	 */
	public ComplexFeature(String featurePath) {
		super(featurePath, true);
		value = new java.util.LinkedList();
	}

	/**
	 * Compares the specified object with this Feature for equality. Returns true if and only if the specified object is also a <code>ComplexFeature</code>, both Features have the same path, and both have the same sub values in the same order.
	 * @param o the reference object with which to compare.
	 * @return <code>true</code> if the specified object is equal to this <code>ComplexFeature</code>.
	 */
	public boolean equals(Object o) {
		Feature f = (Feature) o;
		if (!f.isComplex())
			return false;
		else if (!f.getPath().equals(path))
			return false;
		else {
			List features = (List) this.value;
			List testFeatures = (List) f.getValue();
			int size = features.size();
			if (size != testFeatures.size())
				return false;

			for (int i = 0; i < size; i++) {
				Feature testSubFeature = (Feature) testFeatures.get(i);
				Feature thisSubFeature = (Feature) features.get(i);
				if (!testSubFeature.equals(thisSubFeature))
					return false;
			}
		}
		return true;
	}
	/**
	 * Returns the value of this feature. This is a list of the sub features of this Complex Feature.
	 * @return a list of the sub features of this Complex Feature.
	 */
	public Object getValue() {
		return value;
	}
	/**
	 * Sets the value of this feature to the specified value. Value must be a <code>Feature</code>.
	 * @param newValue the value to be set (a <code>Feature</code>).
	 * @throws BadFeatureValueException if <code>newValue</code> is not a <code>Feature</code>.
	 */
	public void setValue(java.lang.Object newValue) throws BadFeatureValueException {
		try {
			Feature child = (Feature) newValue;
			// only add it if it's not already there.
			// should duplicates be allowed?
			int pos = value.indexOf(child);
			if (pos == -1)
				value.add(child);
		} catch (ClassCastException e) {
			logger.error("ERROR: in ComplexFeature.setValue. newValue should be a Feature.");
			throw new BadFeatureValueException(e.getMessage());
		}
	}
	/**
	 * Returns a string representation of this ComplexFeature. This representation is in CBML format.
	 * @return a string representation of this ComplexFeature.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("<" + name + ">");
		for (int i = 0; i < value.size(); i++) {
			sb.append(((Feature) value.get(i)).toString());
		}
		sb.append("</" + name + ">");
		return sb.toString();
	}
	/**
	 * Creates and returns a copy of this ComplexFeature.
	 * @return a clone of this instance.
	 */
	public Object clone() throws CloneNotSupportedException{
		ComplexFeature clone = (ComplexFeature) super.clone();
	   try {
			int size = value.size();
			for (int i = 0; i < size; i++) {
				clone.setValue(((Feature) value.get(i)).clone());
			}
		} catch (BadFeatureValueException e) {
			// This should never happen
			e.printStackTrace();
		}
		return clone;
	}
}