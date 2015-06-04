package fionn.cbr.similarity;

import cbml.cbr.Feature;
/**
 * This class implements a numeric similarity measure that uses a <i>less is better</i> measure for <code>double</code> and <code>integer</code> feature types.
 * @author: Lorcan Coyle
 */
public class LessIsBetterNumericSimilarityMeasure implements cbml.cbr.SimilarityMeasure {

/**
 * Constructs a less is better similarity measure for use with <code>double</code> Feature types.
 * @param: min the minimum allowable value for this feature.
 * @param: max the maximum allowable value for this feature.
 */
public LessIsBetterNumericSimilarityMeasure() {

}

/**
 * Returns the similarity between the two specified <code>integer</code> or <code>double</code> features. It uses the following function:<br/> 
 	<p>
 	<code>
 	&nbsp;&nbsp;&nbsp;if(feature1.getValue() >= feature2.getValue())<br/>
	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return 1.0;<br/>
	&nbsp;&nbsp;&nbsp;else<br/>
	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return 0;<br/>
	</code>
	</p>
	<p/>
 * @return  the similarity between the two specified <code>integer</code> or <code>double</code> features.
 * @param feature1 the candidate or base feature.
 * @param feature2 the test feature.
 */
public double calculateSimilarity(Feature feature1, Feature feature2) {

	double numericValue1 = (new Double((String) feature1.getValue())).doubleValue();
	double numericValue2 = (new Double((String) feature2.getValue())).doubleValue();

	// we will score higher on better or equal candidate feature values
	if(numericValue1 >= numericValue2)
		return 1;
	else
		return 0;
}

/**
 * Returns a deep copy of this SimilarityMeasure instance
 */
public Object clone()
{
	LessIsBetterNumericSimilarityMeasure clone = new LessIsBetterNumericSimilarityMeasure();
	return clone;
}
}
