package fionn.cbr.similarity;
import cbml.cbr.Feature;
/**
 * This class implements the default similarity measure for <code>double</code> feature types.
 * @author: Dónal Doyle
 */
public class DoubleSimilarityMeasure implements cbml.cbr.SimilarityMeasure
{
	private double range;
	/**
	 * Constructs the default similarity measure for use with <code>double</code> Feature types.
	 * @param: minValue the minimum inclusive allowable value for this feature.
	 * @param: maxValue the maximum inclusive value for this feature.
	 */
	public DoubleSimilarityMeasure(Double minValue, Double maxValue)
	{
		range = maxValue.doubleValue() - minValue.doubleValue();
	}
	/**
	 * Constructs the default similarity measure for use with <code>double</code> Feature types.
	 * This constructor assumes that the data is preNormalised and no normalisation will be performed.
	 */
	public DoubleSimilarityMeasure()
	{
		range = 1;
	}
	/**
	 * Returns the similarity between the two specified <code>double</code> features. It uses the following function:<br/> 
	 * @return  the similarity between the two specified <code>date</code> features.
	 * @param feature1 the candidate or base feature.
	 * @param feature2 the test feature.
	 */
	public double calculateSimilarity(Feature feature1, Feature feature2)
	{
		double numericValue1 =
			(new Double((String)feature1.getValue())).doubleValue();
		double numericValue2 =
			(new Double((String)feature2.getValue())).doubleValue();
		double difference = numericValue1 - numericValue2;
		double absDiff = Math.abs(difference);
		
		if (range != 0)
		{
			if (range == 1)
				return 1 - absDiff;
			return 1 - (absDiff / range);
		}
		return 1;
	}
	
	/**
	 * Returns a deep copy of this SimilarityMeasure instance
	 */
	public Object clone()
	{
		DoubleSimilarityMeasure clone = new DoubleSimilarityMeasure();
		clone.range = range;
		return clone;
	}
}
