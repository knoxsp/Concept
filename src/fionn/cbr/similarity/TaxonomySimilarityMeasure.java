package fionn.cbr.similarity;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import scatterbox.utils.SimpleCaseBaseTest;
import cbml.cbr.Feature;

/**
 * This class implements the default similarity measure for <code>taxonomy</code> feature types.
 * @author: Lorcan Coyle
 */
public class TaxonomySimilarityMeasure implements cbml.cbr.SimilarityMeasure {
	
   static final private Logger logger = Logger
   .getLogger(SimpleCaseBaseTest.class);

   private List possibleValues;
	private boolean debug = true;
	/**
	 * Constructs the default similarity measure for use with <code>taxonomy</code> Features types. 
	 * @param possibleValues the <code>Collection</code> of possible values this feature can have.
	 */
	public TaxonomySimilarityMeasure(List possibleValues) {
		this.possibleValues = possibleValues;
	}
	/**
	 * Returns the similarity between the two specified <code>taxonomy</code> features.
	 * @return the similarity between the two specified features. 
	 * @param feature1 the candidate or base feature.
	 * @param feature2 the test feature.
	 */
	public double calculateSimilarity(Feature feature1, Feature feature2) {
		if (debug) {
			logger.warn("WARNING: calculateSimilarity in TaxonomySimilarityMeasure is under trial. Currently being used with " + feature1.getName() + ", (" + feature1.getPath() + ").");
			debug = false;
		}
		String value1 = (String) feature1.getValue();
		String value2 = (String) feature2.getValue();

		String address1 = null;
		String address2 = null;
		// find the paths of these values in the tree and calculate similarity from that

		for (int i = 0; i < possibleValues.size(); i++) {
			String testValue = (String) possibleValues.get(i);
			int pos = testValue.lastIndexOf("/");
			if (pos != -1) {
				testValue = testValue.substring(pos + 1);
				if (testValue.equals(value1))
					address1 = (String) possibleValues.get(i);
				if (testValue.equals(value2))
					address2 = (String) possibleValues.get(i);
			}
		}
		if (address1 == null || address2 == null)
			return 0;

		while (true) {
			int pos1 = address1.indexOf("/", 1);
			if (pos1 == -1) {
				pos1 = address1.length();
			}

			int pos2 = address2.indexOf("/", 1);
			// The candidate address is not specific enough
			if (pos2 == -1)
				pos2 = address2.length();

			String begin1 = address1.substring(0, pos1);
			String begin2 = address2.substring(0, pos2);

			if (begin1.equals(begin2)) {
				address1 = address1.substring(pos1);
				address2 = address2.substring(pos2);
				if (address1.length() == 0)
					return 1;
				else if (address2.length() == 0)
					break;
			} else
				break;
		}
		int layers = 3;
		int pos = 0;
		while (true) {
			pos = address1.indexOf("/", pos + 1);
			if (pos == -1)
				break;
			else
				layers++;
		}
		pos = 0;
		while (true) {
			pos = address2.indexOf("/", pos + 1);
			if (pos == -1)
				break;
			else
				layers++;
		}

		// same city
		if (layers <= 3)
			return .9;
		// same country
		else if (layers <= 5)
			return .5;
		//same continent
		else if (layers <= 7)
			return 0.1;

		return 0.0;

	}

	/**
	 * Returns a deep copy of this SimilarityMeasure instance
	 */
	public Object clone() {
		ArrayList cloneList = new ArrayList();
		for (int i = 0; i < possibleValues.size(); i++) {
			cloneList.add(possibleValues.get(i));
		}
		TaxonomySimilarityMeasure clone = new TaxonomySimilarityMeasure(cloneList);
		return clone;
	}
}
