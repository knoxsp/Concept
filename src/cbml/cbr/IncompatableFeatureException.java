package cbml.cbr;

/**
 * Thrown to indicate that an incorrect <code>FeatureStruct</code> method is being called. This exception would be thrown if <code>FeatureStruct.setMaxValue</code> was called on a <code>ComplexFeatureStruct</code>.
 * @author Lorcan Coyle
 */
public class IncompatableFeatureException extends Exception {
	/**
	 * Constructs a <code>IncompatableFeatureException</code>.
	 */
	public IncompatableFeatureException() {
		super();
	}
	/**
	 * Constructs an <code>IncompatableFeatureStructException</code> with the specified detail message.
	 * @param message the detail message.
	 */
	public IncompatableFeatureException(String message) {
		super(message);
	}
}
