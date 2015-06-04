package cbml;

import org.apache.log4j.Logger;

/**
 * This class implements an Error Handler that deals with the various errors that may occur during the XML parsing process. This handler is used by the Case Structure parser (cbml.StructReader), Case parser (cbml.CaseReader) and the Profile parser (cbml.ProfileReader).
 * @author Lorcan Coyle
 */
public class CBMLErrorHandler implements org.xml.sax.ErrorHandler {
	
   static final private Logger logger = Logger.getLogger(CBMLErrorHandler.class);

   private String documentName;
	private boolean isDocument;
	/**
	 * Constructs the CBMLErrorHandler.
	 */
	public CBMLErrorHandler() {
		isDocument = false;
	}
	/**
	 * Constructs the CBMLErrorHandler.
	 */
	public CBMLErrorHandler(String documentName) {
		super();
		this.documentName = documentName;
		isDocument = true;
	}
	/**
	 * Is called if the parser encounters a non-fatal error. The SAX parser continues parsing after invoking this method. 
	 * @param exception the Exception that was thrown
	 */
	public void error(org.xml.sax.SAXParseException exception) throws org.xml.sax.SAXException {
		StringBuffer sb = new StringBuffer();
		if (isDocument)
			sb.append("-----ERROR found in document '" + documentName + "'-----");
		else
			sb.append("-----ERROR found-----");
		sb.append("\n\tLine Number:\t" + exception.getLineNumber());
		sb.append("\tColumn Number:\t" + exception.getColumnNumber());
		sb.append("\n\tMessage:\t" + exception.getMessage());
		if (isDocument) {
			sb.append("\n------------------------------------");
			for (int i = 0; i < documentName.length(); i++)
				sb.append("-");
			sb.append("\n");
		} else
			sb.append("\n-------------------\n");
		logger.error(sb.toString());
	}
	/**
	 * Is called if the parser encounters a fatal error. The SAX parser assumes that the document is unstable at this point and ceases parsing the document. 
	 * @param exception the Exception that was thrown
	 */
	public void fatalError(org.xml.sax.SAXParseException exception) throws org.xml.sax.SAXException {
		StringBuffer sb = new StringBuffer();
		if (isDocument)
			sb.append("--FATAL ERROR found in document '" + documentName + "'--");
		else
			sb.append("--FATAL ERROR found---");
		sb.append("\n\tLine Number:\t" + exception.getLineNumber());
		sb.append("\tColumn Number:\t" + exception.getColumnNumber());
		sb.append("\n\tMessage:\t" + exception.getMessage());
		if (isDocument) {
			sb.append("\n------------------------------------");
			for (int i = 0; i < documentName.length(); i++)
				sb.append("-");
			sb.append("\n");
		} else
			sb.append("----------------------");
		logger.fatal(sb.toString());
	}
	/**
	 * Is called if the parser encounters a condition that is not an error. The SAX parser continues parsing after invoking this method. 
	 * @param exception the Exception that was thrown
	 */
	public void warning(org.xml.sax.SAXParseException exception) throws org.xml.sax.SAXException {
		StringBuffer sb = new StringBuffer();
		if (isDocument)
			sb.append("----WARNING found in document '" + documentName + "'----");
		else
			sb.append("----WARNING found----");
		sb.append("\n\tLine Number:\t" + exception.getLineNumber());
		sb.append("\tColumn Number:\t" + exception.getColumnNumber());
		sb.append("\n\tMessage:\t" + exception.getMessage());
		if (isDocument) {
			sb.append("\n------------------------------------");
			for (int i = 0; i < documentName.length(); i++)
				sb.append("-");
			sb.append("\n");
		} else
			sb.append("---------------------");
		logger.warn(sb.toString());
	}
}
