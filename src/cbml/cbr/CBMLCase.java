package cbml.cbr;

import java.io.Serializable;

/**
 * This is a CBML Case Interface. The CBML parsers read Case Documents and convert the case bases into CBML cases. These cases may then be cast by the user into an implementation of this interface.
 * @author Lorcan Coyle
 * @see cbml.cbr.Feature
 * @see cbml.cbr.CaseStruct
 * @version 3.0
 */
public interface CBMLCase extends Cloneable, Serializable {
	/**
	 * Adds the specified feature to this case.
	 * @param newFeature the feature to be added to this case.
	 * @return <code>true</code> if the feature was added successfully, <code>false</code> otherwise
	 */
	boolean addFeature(Feature newFeature);
	/**
	 * Returns a clone of this Case.
	 * @return a clone of this instance.
	 */
	Object clone() throws CloneNotSupportedException;
	/**
	 * Returns the feature at the specified path in this case. 
	 * @return the feature at the specified path in this case or <code>null</code> if there is no feature at the specified path.
	 * @param featurePath path whose associated feature is to be returned.
	 */
	Feature getFeature(String featurePath);
	/**
	* Returns the name of this case.
	* @return the name of this case.
	*/
	String getName();
	/**
	 * Returns the solution <code>Feature</code> associated with this case.
	 * @return the solution <code>Feature</code> associated with this case
	 */
	Feature getSolution();
	/**
	 * Removes the feature at the specified featurePath. Returns the removed feature.
	 * @param featurePath the path of the feature to be removed.
	 * @return the removed feature  or <code>null</code> if there is no feature at the specified path.
	 */
	Feature removeFeature(String featurePath);
	/**
	 * Sets the name for this case.
	 * @param newName the new name of this case.
	 */
	void setName(String newName);
	/**
	 * Sets the solution <code>Feature</code> of this case to the specified value.
	 * @param solution the solution <code>Feature</code> to be associated with this case.
	 */
	void setSolution(Feature solution);

}