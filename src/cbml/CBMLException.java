package cbml;

/**
 * Thrown to indicate that there was a problem encountered while parsing a CBML document. The message included with this Exception should be used to correct the document.
 * @author Lorcan Coyle
 */
public class CBMLException extends Exception {
	/**
	 * Constructs a <code>CBMLException</code>.
	 */
	public CBMLException() {
		super();
	}
	/**
	 * Constructs a <code>CBMLException</code> with the specified detail message.
	 * @param message the detail message.
	 */
	public CBMLException(String message) {
		super(message);
	}
}
