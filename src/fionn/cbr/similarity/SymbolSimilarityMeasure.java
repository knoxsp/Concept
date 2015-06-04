package fionn.cbr.similarity;
import java.util.ArrayList;
import java.util.List;

import cbml.cbr.Feature;
/**
 * This class implements the default similarity measure for <code>symbolic</code> feature types.
 * @author: Dónal Doyle
 */
public class SymbolSimilarityMeasure implements cbml.cbr.SimilarityMeasure
{
	private List possibleValues;
	/**
	 * Constructs the default similarity measure for use with <code>symbolic</code> Feature types. 
	 * @param possibleValues the <code>List</code> of possible values this feature can have.
	 */
	public SymbolSimilarityMeasure(List possibleValues)
	{
		this.possibleValues = possibleValues;
	}
	/**
	 * Returns the similarity between the two specified <code>symbol</code> features.
	 * If the two values are equal a similarity of 1 is returned otherwise 0
	 * @return the similarity between the two specified features. 
	 * @param feature1 the candidate or base feature.
	 * @param feature2 the test feature.
	 */
	public double calculateSimilarity(Feature feature1, Feature feature2)
	{
		String value1 = (String)feature1.getValue();
		String value2 = (String)feature2.getValue();
		if (value1.equals(value2))
			return 1;
		return 0;
	}
	
	/**
	 * Returns a deep copy of this SimilarityMeasure instance
	 */
	public Object clone()
	{
		ArrayList cloneList = new ArrayList();
		for (int i = 0; i < possibleValues.size(); i++)
		{
			cloneList.add(possibleValues.get(i));
		}
		SymbolSimilarityMeasure clone = new SymbolSimilarityMeasure(cloneList);
		return clone;
	}
}
