package cbml.cbr.types;

import java.util.List;


/**
 * @author coylel
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class LocationStruct extends TaxonomyFeatureStruct {

	public LocationStruct(String featurePath, boolean discriminant, boolean solution, boolean manditory) {
		super(featurePath, discriminant, solution, manditory);
	}
	
	/**
	 * Creates and returns a copy of this FeatureStruct.
	 * @return a clone of this instance.
	 */
	public Object clone() {
		LocationStruct copy = new LocationStruct(featurePath, discriminant, solution, manditory);
		int size = tree.size();
		for (int i = 0; i < size; i++) {
			copy.possibleValues.add(possibleValues.get(i));
			copy.tree.add(tree.get(i));
			copy.levels.add(levels.get(i));
		}
		return copy;
	}

	/**
	 * Boosts this city at the cost of its sisters
	 * @param featureValue
	 * @return
	 */
	public boolean boost(String featureValue) {
		int index = possibleValues.indexOf(featureValue);
		String fullFeatureValue = (String) tree.get(index);
		String parentPath = fullFeatureValue.substring(0, fullFeatureValue.lastIndexOf("/"));

		// make sure that these are in the same city!
		int slashCount = 0;
		String countString = parentPath;
		while(true){
			int pos = countString.indexOf("/");
			if(pos == -1)
				break;
			countString = countString.substring(pos + 1) ;
			slashCount ++;
		}
		if(slashCount<4)
			return false;
		
		for (int i = 0; i < tree.size(); i++) {
			if (i != index) {
				String testPath = (String) tree.get(i);
				if (testPath.startsWith(parentPath) && !testPath.equals(parentPath)) {
					String testValue = (String) possibleValues.get(i);
					String newPath = fullFeatureValue + "/" + testValue;
					tree.set(i, newPath);
					int l = ((Integer)levels.get(i)).intValue();
					levels.set(i, new Integer(++l));
				}
			}
		}
		return true;
	}

	public void setValues(List newValues) {
		for (int i = 0; i < newValues.size(); i++) {
			String newPath = (String) newValues.get(i);
			tree.add(newPath);
			possibleValues.add(newPath.substring(newPath.lastIndexOf("/") + 1));
			int level = 1;
			int index;
			while ((index = newPath.indexOf("/", 1)) != -1) {
				level++;
				newPath = newPath.substring(index);
			}
			levels.add(new Integer(level));
		}
	}
	
}
