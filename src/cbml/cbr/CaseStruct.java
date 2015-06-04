package cbml.cbr;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 * This is the Case Structure Class. It is part of the CBML Specification. The CBML Case structure parser reads Case Structure Documents and them into <code>CaseStruct</code> objects. These case
 * structures may be accessed using the methods of this class.
 * 
 * @author Lorcan Coyle
 * @see cbml.cbr.FeatureStruct
 * @see cbml.cbr.CBMLCase
 * @version 3.0
 */

public class CaseStruct implements Serializable, Cloneable {

   public boolean equals(Object o) {
      if (!(o instanceof CaseStruct))
         return false;
      CaseStruct testCaseStruct = (CaseStruct) o;
      if (!testCaseStruct.toString().equals(toString()))
         return false;
      return true;
   }

   // this is the domain that was named in the case structure document
   private String domain;

   // this is a list of all featureStruct paths within this caseStruct
   private List featureStructPaths;

   // this is a hashtable of all featureStructs indexed by feature path
   private Hashtable indexedFeatureStructs;

   /**
    * Constructs an empty Case Structure Object.
    * 
    * @param domainName
    *           the name of the domain of application for this case structure.
    */
   public CaseStruct(String domainName) {
      featureStructPaths = new LinkedList();
      indexedFeatureStructs = new Hashtable();
      domain = domainName;
   }

   /**
    * Adds the specified feature structure to this case structure. If there is already a feature structure at this path it is replaced.
    * 
    * @param featureStruct
    *           the feature structure to be added to this case.
    */
   public void addFeatureStruct(FeatureStruct featureStruct) {
      String path = featureStruct.getFeaturePath();
      if (indexedFeatureStructs.containsKey(path)) {
         Object o = indexedFeatureStructs.remove(path);
         indexedFeatureStructs.put(path, featureStruct);
      } else {
         featureStructPaths.add(path);
         indexedFeatureStructs.put(path, featureStruct);
      }
      if (featureStruct.getType() == FeatureStruct.COMPLEX) {
         try {
            List subFeatureStructs = (List) featureStruct
                  .getSubFeatureStructs();
            int size = subFeatureStructs.size();
            for (int i = 0; i < size; i++) {
               addFeatureStruct((FeatureStruct) subFeatureStructs.get(i));
            }
         } catch (IncompatableFeatureException e) {
            // THIS CAN NEVER HAPPEN - we are dealing exclusively with complex features
         }
      }
   }

   /**
    * Creates and returns a copy of this CaseStruct.
    * 
    * @return a clone of this instance.
    */
   public Object clone() throws CloneNotSupportedException {
      CaseStruct clone = (CaseStruct) super.clone();
      //= new CaseStruct(domain);
      clone.featureStructPaths = (List) ((LinkedList) featureStructPaths)
            .clone();
      clone.indexedFeatureStructs = new Hashtable(20);
      Enumeration keys = indexedFeatureStructs.keys();
      while (keys.hasMoreElements()) {
         String key = (String) keys.nextElement();
         FeatureStruct struct = (FeatureStruct) indexedFeatureStructs.get(key);
         Object clonedStruct = struct.clone();
         clone.indexedFeatureStructs.put(key, clonedStruct);
      }
      return clone;
   }

   /**
    * Returns the domain name for this CaseStruct.
    * 
    * @return a string containing the name of this CaseStruct
    */
   public String getDomain() {
      return domain;
   }

   /**
    * Returns a <code>List</code> of featurepaths corresponding to the feature structures defined within this case structure object.
    * 
    * @return a <code>List</code> of featurepaths corresponding to the feature structures defined within this case structure object.
    */
   public List getFeaturePaths() {
      return featureStructPaths;
   }

   /**
    * Returns the feature structure at the specified path in this case.
    * 
    * @return the feature structure at the specified path in this case or <code>null</code> if there is no feature structure at the specified path.
    * @param featurePath
    *           path whose associated feature structure is to be returned.
    */
   public FeatureStruct getFeatureStruct(String featurePath) {
      return (FeatureStruct) indexedFeatureStructs.get(featurePath);
   }

   /**
    * Returns a string representation of this case structure. This representation is in CBML format.
    * 
    * @return a string representation of this case structure.
    */
   public String toString() {
      StringBuffer sb = new StringBuffer();
      sb
            .append("<case domain=\""
                  + domain
                  + "\" xsi:noNamespaceSchemaLocation=\"../CBML/cbmlv3.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><structure>");
      Enumeration i = indexedFeatureStructs.keys();
      // look for any paths that are in root, print these features out only
      while (i.hasMoreElements()) {
         String path = (String) i.nextElement();
         if (path.indexOf("/", 1) == -1)
            sb.append(((FeatureStruct) indexedFeatureStructs.get(path))
                  .toString());
      }
      sb.append("</structure></case>");
      return sb.toString();
   }

   /**
    * Checks the validity of this case. Returns <code>true</code> if all manditory child features exist and all feature values are valid.
    * 
    * @return <code>true</code> if all manditory child features exist and all feature values are valid.
    * @param testCase
    *           the case to be validated.
    * @param strict
    *           <code>true</code> if the solution is to be validated. Skeletal Cases should not be strictly validated.
    */
   public boolean validate(CBMLCase testCase, boolean strict) {

      Enumeration e = indexedFeatureStructs.keys();
      while (e.hasMoreElements()) {
         String baseFeatureStructPath = (String) e.nextElement();
         FeatureStruct baseFeatureStruct = (FeatureStruct) indexedFeatureStructs
               .get(baseFeatureStructPath);
         boolean manditory = baseFeatureStruct.isManditory();

         if (baseFeatureStruct.isSolution()) {
            // you either have a solution or not.
            Feature solution = testCase.getSolution();
            if (strict && manditory && solution == null){
               return false;
            }else if (solution != null && !baseFeatureStruct.validate(solution)) {
               return false;
            }
         } else {
            Feature f = testCase.getFeature(baseFeatureStructPath);
            if (manditory && f == null) {
               return false;
            }
            if (f != null
                  && (baseFeatureStruct.getType() != FeatureStruct.COMPLEX)
                  && !baseFeatureStruct.validate(f)) {
               return false;
            }
         }
      }
      return true;
   }
}