package fionn.cbr.similarity;
import cbml.cbr.Feature;
/**
 * This class implements the default similarity measure for <code>double</code> feature types.
 * @author: Dónal Doyle
 */
public class IntegerSimilarityMeasure implements cbml.cbr.SimilarityMeasure
{
	private int range;
	/**
	 * Constructs the default similarity measure for use with <code>double</code> Feature types.
	 * @param: minValue the minimum inclusive allowable value for this feature.
	 * @param: maxValue the maximum inclusive value for this feature.
	 */
	public IntegerSimilarityMeasure(Integer minValue, Integer maxValue)
	{
		range = maxValue.intValue() - minValue.intValue();
	}

	/**
	 * Returns the similarity between the two specified <code>double</code> features. It uses the following function:<br/> 
	 * @return  the similarity between the two specified <code>date</code> features.
	 * @param feature1 the candidate or base feature.
	 * @param feature2 the test feature.
	 */
	public double calculateSimilarity(Feature feature1, Feature feature2)
	{
		int numericValue1 =
			(new Integer((String)feature1.getValue())).intValue();
		int numericValue2 =
			(new Integer((String)feature2.getValue())).intValue();
		int difference = numericValue1 - numericValue2;
		int absDiff = Math.abs(difference);
		
		if (range != 0)
		{
			return 1 - ((double)absDiff / range);
		}
		return 1;
	}
	
	/**
	 * Returns a deep copy of this SimilarityMeasure instance
	 */
	public Object clone()
	{
		IntegerSimilarityMeasure clone = new IntegerSimilarityMeasure(new Integer(0), new Integer(range));
		return clone;
	}
}
