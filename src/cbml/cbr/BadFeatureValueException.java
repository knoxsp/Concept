package cbml.cbr;

/**
 * Thrown to indicate that the value being passed into a <code>FeatureStruct</code> method was invalid.
 * @author Lorcan Coyle
 */
public class BadFeatureValueException extends Exception {
	/**
	 * Constructs a <code>BadFeatureValueException</code>.
	 */
	public BadFeatureValueException() {
		super();
	}
	/**
	 * Constructs a <code>BadFeatureValueException</code> with the specified detail message.
	 * @param message the detail message.
	 */
	public BadFeatureValueException(String message) {
		super(message);
	}
}
