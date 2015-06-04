package cbml.cbr;


/**
 * This is an interface for a similarity measure. It must be implemented by all CBML similarity measures.
 * @author Lorcan Coyle
 * @see cbml.cbr.SimilarityProfile
 */
public interface SimilarityMeasure extends java.io.Serializable {
	/**
	 * Returns the similarity between the two specified features. It should be noted that some similarity measures may be asymmetrical so the order of the features being passed into this function should be ordered. The returned similarity value is the similarity of the second feature (<code>feature2</code>) in relation to the first feature (<code>feature1</code>).
	 * @return  the similarity between the two specified features. This value should be normalised (i.e. similarity => [0..1])
	 * @param feature1 the candidate or base feature.
	 * @param feature2 the test feature.
	 */
	double calculateSimilarity(Feature feature1, Feature feature2);
	
	/**
	 * Creates and returns a copy of this Similarity Measure.
	 * @return a clone of this instance.
	 */
	Object clone() throws CloneNotSupportedException;
}
