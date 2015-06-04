package fionn.cbr.nn;

import java.util.List;

import org.joda.time.DateTime;

import cbml.cbr.Feature;

/**
 * A Casenode is designed to represent a Case in the Nearest Neighbourhood algorithm. It is an implementation of cbml.cbr.Case
 * @author Lorcan Coyle
 * @see cbml.cbr.CBMLCase
 * @see fionn.cbr.nn.NN
 * @version 1.0
 */

public class Casenode extends fionn.Case implements Cloneable{
	/**
	 * The activation of this casenode in the Nearest Neighbourhood algorithm
	 */
	protected double activation;
	
	/**
	 * The creation time of the node
	 * The default is the real time of creation
	 * It should be set to the time appropriate to the simulator for
	 * simulation purposes.
	 */
	private DateTime caseCreationTime = new DateTime();
	
	/**
	 * The index of this casenode in the Nearest Neighbourhood algorithm
	 */
	private int index;

	public Casenode() {
		super();
		// New casenodes are given an activation of 0 and an index of -1. Casenodes with index -1 cannot be assigned relevance connections
		activation = 0;
		index = -1;
	}

	/**
	 * Increases the activation of this casenode by the value specified by newActivation
	 * @param newActivation the new Activation to be added to this Casenode's activation
	 */
	protected void activate(double newActivation) {
		activation += newActivation;
	}

	/**
	 * Returns a clone of this Case.
	 * @return a clone of this instance.
	 */
	public Object clone() throws CloneNotSupportedException{
		Casenode copyOf = (Casenode) super.clone();
		//copyOf.setName(getName());
		//copyOf.setSolution(getSolution());
		/*java.util.Hashtable clonedIndexFeatures = new java.util.Hashtable();
		java.util.Enumeration i = indexedFeatures.keys();
		while (i.hasMoreElements()) {
			String path = (String) i.nextElement();
			clonedIndexFeatures.put(path, ((cbml.cbr.Feature) indexedFeatures.get(path)).clone());
		}
		copyOf.indexedFeatures = clonedIndexFeatures;
		 */
		copyOf.activate(activation);
		copyOf.setIndex(index);
		return copyOf;
	}

	/**
	 * Returns the activation associated with this Casenode.
	 * @return the activation associated with this Casenode.
	 */
	public double getActivation() {
		return activation;
	}
	/**
	 * Returns the index of this casenode in the Nearest Neighbourhood Algorithm. This is -1 if the casenode had not been added to the Algorithm. 
	 * @return the index of this casenode in the Nearest Neighbourhood Algorithm. This is -1 if the casenode had not been added to the Algorithm.
	 */
	protected int getIndex() {
		return index;
	}
	
	protected void setActivation(double newActivation){
		activation = newActivation;
	}

	/**
	 * Sets the index of this Casenode to the specified value. This is called by the Nearest Neighbourhood Algorithm.
	 * @param index the index of this Casenode to the specified value
	 */
	protected void setIndex(int index) {
		this.index = index;
	}

	/**
	 * Resets the activation to 0
	 */
	protected void reset() {
		activation = 0;
	}
	
	public boolean equals(Casenode a_casenode){
      boolean equal = true;
      List<Feature> exhibitA = super.getFeatures();
      List<Feature> exhibitB = a_casenode.getFeatures();
      String currentFeatureA;
      String currentFeatureB;
      if(exhibitA.size() != exhibitB.size()){
         equal = false;
      }else{
         for(int i=0;i<exhibitA.size();i++){
            currentFeatureA = (String)exhibitA.get(i).getValue();
            currentFeatureB = (String)exhibitB.get(i).getValue();
            if(!currentFeatureA.equalsIgnoreCase(currentFeatureB)){
               equal=false;
            }
         }
      }
      return equal;
   }
	
	/**
	 * Knox's code to include a case creation time
	 * which maps to the simulated time it is created.
	 */
	public void setCreationTime(DateTime a_creationTime){
	   caseCreationTime = a_creationTime;
	}
	
	public DateTime getCreationTime(){
	   return caseCreationTime;
	}
	
}
