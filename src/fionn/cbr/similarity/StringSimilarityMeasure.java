package fionn.cbr.similarity;
import cbml.cbr.Feature;
/**
 * This class implements the default similarity measure for <code>string</code> feature types.
 * @author: Dónal Doyle
 */
public class StringSimilarityMeasure implements cbml.cbr.SimilarityMeasure
{
	/**
	 * Constructs the default similarity measure for use with <code>string</code> Feature types. 
	 */
	public StringSimilarityMeasure()
	{
	}
	/**
	 * Returns the similarity between the two specified features. 
	 * If the two values in Feature.getValue are equal 1 is returned else 0
	 * @return the similarity between the two specified features. It returns <code>1</code> if the <code>String</code> values are equal otherwise it returns <code>0</code>.
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
		StringSimilarityMeasure clone = new StringSimilarityMeasure();
		return clone;
	}
}
