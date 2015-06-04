package fionn.cbr.similarity;
import java.util.ArrayList;
import java.util.List;

import cbml.cbr.Feature;
/**
 * This class implements an order based similarity measure for <code>symbolic</code> feature types.
 * @author: Dónal Doyle
 */
public class OrderedSymbolSimilarityMeasure
	implements cbml.cbr.SimilarityMeasure
{
	private List possibleValues;
	/**
	 * Constructs a similarity measure for use with <code>symbolic</code> Feature types. 
	 * @param possibleValues the <code>List</code> of possible values this feature can have.
	 */
	public OrderedSymbolSimilarityMeasure(List possibleValues)
	{
		this.possibleValues = possibleValues;
	}
	
	/**
	 * Returns the similarity between the two specified <code>symbolic</code> features. 
	 * @return the similarity between the two specified features. 
	 * @param feature1 the candidate or base feature.
	 * @param feature2 the test feature.
	 */
	public double calculateSimilarity(Feature feature1, Feature feature2)
	{
		int size = possibleValues.size() - 1;
		int numericValue1 = possibleValues.indexOf((String)feature1.getValue());
		int numericValue2 = possibleValues.indexOf((String)feature2.getValue());
		int difference = numericValue1 - numericValue2;
		int diff = Math.abs(difference);
		
		if(numericValue1 == numericValue2)
			return 1;
		else if(size == 0)
			return 0;
		else
			return 1-((double)diff/size);
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
		OrderedSymbolSimilarityMeasure clone = new OrderedSymbolSimilarityMeasure(cloneList);
		return clone;
	}
}
