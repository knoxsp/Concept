package fionn.cbr.similarity;
/**
 * This class implements the default similarity measure for <code>boolean</code> feature types.
 * @author: Lorcan Coyle
 */
public class BooleanSimilarityMeasure implements cbml.cbr.SimilarityMeasure
{
	/**
	 * Constructs the default similarity measure for use with <code>boolean</code> Feature types.
	 */
	public BooleanSimilarityMeasure()
	{
	}
	/**
	 * Returns the similarity between the two specified <code>boolean</code> features. It returns <code>1</code> if the values are equal otherwise it returns <code>0</code>.
	 * @return the similarity between the two specified <code>boolean</code> features. It returns <code>1</code> if the values are equal otherwise it returns <code>0</code>.
	 * @param feature1 the candidate or base feature.
	 * @param feature2 the test feature.
	 */
	public double calculateSimilarity(
		cbml.cbr.Feature feature1,
		cbml.cbr.Feature feature2)
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
		BooleanSimilarityMeasure clone = new BooleanSimilarityMeasure();
		return clone;
	}
}
