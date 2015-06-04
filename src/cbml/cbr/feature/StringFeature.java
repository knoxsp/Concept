package cbml.cbr.feature;

import org.apache.log4j.Logger;

import cbml.cbr.BadFeatureValueException;
import cbml.cbr.Feature;

/**
 * A Simple Feature is a CBML Feature of one of the following types: <code>array</code>,<code>boolean</code>,<code>double</code>,<code>integer</code>,<code>string</code>,
 * <code>symbol</code>,<code>taxonomy</code>. It is a simple attribute value pair.
 * 
 * @see cbml.cbr.feature.ComplexFeature
 * @see cbml.cbr.feature.DoubleFeature
 * @see cbml.cbr.feature.IntegerFeature
 * @author Lorcan Coyle
 */
public class StringFeature extends Feature {

   static final private Logger logger = Logger.getLogger(StringFeature.class);

   protected String value;

   /**
    * Constructs an empty Simple Feature Object
    * 
    * @param featurePath
    *           the path of this feature
    * @param value
    *           the value of this simple Feature
    */
   public StringFeature(String featurePath, String value) {
      super(featurePath, false);
      this.value = value;
   }

   /**
    * Compares the specified object with this Feature for equality. Returns true if and only if the specified object is also a <code>StringFeature</code>, both Features have the same path, and both
    * have the same value.
    * 
    * @param o
    *           the reference object with which to compare.
    * @return <code>true</code> if the specified object is equal to this <code>StringFeature</code>.
    */
   public boolean equals(Object o) {
      StringFeature f = (StringFeature) o;
      if (path.equals(f.getPath()))
         return f.getValue().equals(getValue());
      return false;
   }

   /**
    * Sets the value of this feature to the specified value. <code>newValue<code> must be a <code>String</code>.
    * @param newValue the value to be set (a <code>String</code>).
    * @throws BadFeatureValueException if <code>newValue</code> is not a <code>String</code>.
    */
   public void setValue(Object newValue) throws BadFeatureValueException {
      try {
         if (newValue instanceof String) {
            value = (String) newValue;
         }
      } catch (ClassCastException e) {
         logger
               .error("ERROR: in StringFeature.setValue. newValue should be a String.");
         throw new BadFeatureValueException(e.getMessage());
      }
   }

   /**
    * Returns a string representation of this Feature. This representation is in CBML format.
    * 
    * @return a string representation of this Feature.
    */
   public String toString() {
      StringBuffer stringbuffer = new StringBuffer();
      stringbuffer.append('<');
      stringbuffer.append(name);
      stringbuffer.append('>');
      stringbuffer.append(value);
      stringbuffer.append("</");
      stringbuffer.append(name);
      stringbuffer.append('>');
      return stringbuffer.toString();
   }

   /**
    * Returns a clone of this Feature.
    * 
    * @return a clone of this instance.
    */
   public Object clone() throws CloneNotSupportedException {
      StringFeature clone = (StringFeature) super.clone();
      clone.value = value;
      return clone;
   }

   /**
    * Returns an Object representation of the Feature
    * 
    * @return Object value of this feature
    */
   public Object getValue() {
      return value;
   }
}