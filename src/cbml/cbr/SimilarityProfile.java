package cbml.cbr;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;

import cbml.cbr.types.LocationStruct;
import cbml.cbr.types.TaxonomyFeatureStruct;

/**
 * SimilarityProfile describes in complete detail how Similarity should be calculated within the CBML specifications. This included references to feature weights, similarity measures, difference
 * functions and difference-similarity graphs. Different users in a CBR system can have different SimilarityProfiles attached to them.
 * 
 * @author Lorcan Coyle
 * @see cbml.cbr.SimilarityMeasure
 * @version 3.0
 */
public class SimilarityProfile implements Cloneable, Serializable {

   static final private Logger logger = Logger
         .getLogger(SimilarityProfile.class);

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   public boolean equals(Object o) {
      if (!(o instanceof SimilarityProfile))
         return false;
      SimilarityProfile testSimProfile = (SimilarityProfile) o;
      if (!toString().equals(testSimProfile.toString()))
         return false;
      if (!caseStruct.equals(testSimProfile.caseStruct))
         return false;
      return true;
   }

   /*
    * //CONNECTIONS: private class Connections { private List values; private double[][] connections; private boolean symmetric; public Object clone() { List cloneValues = new ArrayList(); for(int i =
    * 0; i < values.size(); i++){ cloneValues.add(values.get(i)); } Connections clone = new Connections(cloneValues, symmetric); int length = connections[0].length; for (int i = 0; i < length; i++) {
    * for (int j = 0; j < length; j++) { clone.connections[i][j] = connections[i][j]; } } return clone; } public Connections(List values, boolean symmetric) { this.values = values; this.symmetric =
    * symmetric; int size = values.size(); connections = new double[size][size]; for (int i = 0; i < size; i++) { for (int j = 0; j < size; j++) { connections[i][j] = j - i; } } } public boolean
    * setConnection(String from, String to, double value) { try { int f = values.indexOf(from); int t = values.indexOf(to); if (Math.abs(f - t) != 1) return false; int length = connections[0].length;
    * double change = value - connections[f][t]; for (int i = 0; i < t; i++) { for (int j = t; j < length; j++) { connections[i][j] = connections[i][j] + change; if (symmetric) { connections[j][i] =
    * connections[i][j]; } } } return true; } catch (Exception e) { return false; } } public double getConnection(String from, String to) { int f = values.indexOf(from); int t = values.indexOf(to);
    * return connections[f][t]; } public void setSymmetric(boolean symmetric) { this.symmetric = symmetric; } public String toString() { StringBuffer b = new StringBuffer(); int length =
    * connections[0].length; for (int i = 0; i < length; i++) { for (int j = 0; j < length; j++) { b.append(connections[i][j] + " "); } b.append("\n"); } return b.toString(); } }
    */
   private class SimilarityArray {

      private double[][] similarityArray;

      private List values;

      public SimilarityArray(List values, double[][] simMatrix) {
         this.values = values;
         similarityArray = simMatrix;
      }

      public Object clone() {
         try {
            final SimilarityArray clone = (SimilarityArray) super.clone();
            clone.values = (List) ((ArrayList) values).clone();
            clone.similarityArray = (double[][]) similarityArray.clone();
            return clone;
         } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
         }
      }

      public double getSimilarity(Feature feature1, Feature feature2) {
         int positionOfFeature1 = values.indexOf((String) feature1.getValue());
         int positionOfFeature2 = values.indexOf((String) feature2.getValue());
         if (positionOfFeature1 == -1 || positionOfFeature2 == -1) {
            logger
                  .warn("ERROR " + feature1 + " or " + feature2 + " is invalid");
            return 0;
         }
         double similarity = similarityArray[positionOfFeature1][positionOfFeature2];
         // If the similarity value was not specified use the exact similarity measure (it will be unspecified if similarity is -1
         if (similarity == -1)
            return (positionOfFeature1 == positionOfFeature2) ? 1 : 0;
         return similarity;
      }
   }

   private class SimilarityGraph implements Serializable, Cloneable {

      private class Point implements Serializable, Cloneable {

         double x, y;

         public Point(double x, double y) {
            this.x = x;
            this.y = y;
         }

         public Object clone() throws CloneNotSupportedException {
            Point clone = (Point) super.clone();
            clone.x = x;
            clone.y = y;
            return clone;
         }

         public double getX() {
            return x;
         }

         public double getY() {
            return y;
         }

         public String toString() {
            return "(" + x + "," + y + ")";
         }
      }

      private boolean allSorted;

      private List points;

      private boolean symmetrical;

      public SimilarityGraph(String graphType) {
         symmetrical = graphType.equals("symmetrical") ? true : false;
         points = new ArrayList();
      }

      public void addPoint(double difference, double similarity) {
         points.add(new Point(difference, similarity));
         allSorted = false;
      }

      public Object clone() throws CloneNotSupportedException {
         SimilarityGraph clone = (SimilarityGraph) super.clone();
         clone.symmetrical = symmetrical;
         int size = points.size();
         for (int i = 0; i < size; i++) {
            clone.points.add(((Point) points.get(i)).clone());
         }
         clone.allSorted = allSorted;
         return clone;
      }

      private void generateGraph() {
         int size = points.size();
         List orderedPoints = new ArrayList(size);
         boolean[] sorted = new boolean[size];
         for (int j = 0; j < size; j++) {
            double minX = Double.POSITIVE_INFINITY;
            int index = -1;
            for (int i = 0; i < size; i++) {
               if (!sorted[i]) {
                  Point point = (Point) points.get(i);
                  double testX = point.getX();
                  if (testX <= minX) {
                     minX = testX;
                     index = i;
                  }
               }
            }

            orderedPoints.add(points.get(index));
            sorted[index] = true;
         }
         // now sort the discontinuities
         for (int j = 0; j < size - 1; j++) {
            // check if the next one is equal
            Point thisPoint = (Point) orderedPoints.get(j);
            double thisX = thisPoint.getX();
            double thisY = thisPoint.getY();
            Point nextPoint = (Point) orderedPoints.get(j + 1);
            double nextX = nextPoint.getX();
            if (nextX == thisX) {
               double nextY = nextPoint.getY();
               if (thisX < 0) {
                  if (nextY < thisY) {
                     // swap
                     Point temp = (Point) orderedPoints.remove(j + 1);
                     orderedPoints.add(j, temp);
                  }
               } else if (thisX > 0) {
                  if (nextY > thisY) {
                     // swap
                     Point temp = (Point) orderedPoints.remove(j + 1);
                     orderedPoints.add(j, temp);
                  }
               } else {
                  // the higher one should be here
                  if (nextY > thisY) {
                     // swap
                     Point temp = (Point) orderedPoints.remove(j + 1);
                     orderedPoints.add(j, temp);
                  }
               }
            }
         }
         points = orderedPoints;
         // now iron out the symmetrical graphs - remove negative points and set a point at value 0;
         if (symmetrical) {
            double lastX = Double.NaN;
            double lastY = Double.NaN;
            for (int i = 0; i < points.size(); i++) {
               Point p = (Point) points.get(i);
               double x = p.getX();
               double y = p.getY();
               if (x == 0)
                  break;
               else if (x > 0) {
                  // Set up the point at x = 0
                  if (Double.isNaN(lastX))
                     points.add(0, new Point(0, 1));
                  else {
                     double newY = lastY
                           + (((lastY - y) / (lastX - x)) * (-lastX));
                     double newX = 0;
                     points.add(0, new Point(newX, newY));
                  }
                  break;
               } else {
                  lastX = x;
                  lastY = y;
                  points.remove(i--);
               }
            }
         }

         allSorted = true;
      }

      public double getSimilarity(final double diff) {
         if (!allSorted) {
            generateGraph();
         }
         final double difference = symmetrical ? Math.abs(diff) : diff;

         int size = points.size();
         double lastX = Double.NaN;
         double lastY = Double.NaN;
         for (int i = 0; i < size; i++) {
            Point point = (Point) points.get(i);
            double nextX = point.getX();
            double nextY = point.getY();
            if (nextX == difference) {
               // check for the next value, they may be the same, if so return the higher of the two (optimistic)
               if (i != size - 1) {
                  Point evenNextPoint = (Point) points.get(i + 1);
                  double evenNextX = evenNextPoint.getX();
                  if (evenNextX == nextX) {
                     //	infinite slope, optomistic approach, return the higher of the two similarities
                     double evenNextY = evenNextPoint.getY();
                     return (evenNextY > nextY) ? evenNextY : nextY;
                  }
               }
               return nextY;
            } else if (nextX > difference) {
               if (i > 0) {
                  if (lastY == nextY)
                     return lastY;

                  double similarity = ((difference - lastX) * (lastY - nextY) / (lastX - nextX))
                        + lastY;
                  return similarity;
               }
               //difference is less than the smallest distance on the graph
               return 0;
            }
            lastX = nextX;
            lastY = nextY;
         }
         //	difference is greater than the largest distance on the graph
         return 0;
      }

      public String toString() {
         StringBuffer sb = new StringBuffer();
         sb.append("[");
         for (int i = 0; i < points.size(); i++) {
            sb.append(((Point) points.get(i)).toString());
         }
         sb.append("]");
         return sb.toString();
      }
   }

   public static final int EXACT = 0;

   public static final int ARRAY = 1;

   public static final int PURE_SIMILARITY = 2;

   public static final int DOUBLE_DIFFERENCE = 3;

   public static final int INTEGER_DIFFERENCE = 4;

   public static final int SYMBOL_DIFFERENCE = 5;

   public static final int TAXONOMY_DIFFERENCE = 6;

   protected CaseStruct caseStruct;

   private String domain;

   private Hashtable graphs;

   private Hashtable relevances;

   //private Hashtable allConnections;
   private Hashtable similarityArrays;

   private Hashtable similarityMeasures;

   private Hashtable similarityTypes;

   private Hashtable symbolValueLists;

   private String username;

   /**
    * Constructs an empty Similarity Profile object. This contains all the information necessary to perform similarity calculations on two cases.
    * 
    * @param caseStruct
    *           the case structure definition for this domain
    * @param username
    *           the owner of this similarity profile (the default value is "default")
    * @param domainName
    *           the name of the domain of application for this similarity profile.
    */
   public SimilarityProfile(CaseStruct caseStruct, String username,
         String domainName) {
      try {
         this.caseStruct = (CaseStruct) caseStruct.clone();
         this.username = username;

         relevances = new Hashtable();
         //allConnections = new Hashtable();
         graphs = new Hashtable();
         similarityArrays = new Hashtable();

         similarityTypes = new Hashtable();
         similarityMeasures = new Hashtable();

         symbolValueLists = new Hashtable();

         domain = domainName;
      } catch (CloneNotSupportedException e) {
         logger.fatal("For some reason CaseStruct is not cloneable: " +e);
      }
   }

   /**
    * LORCAN: Updates the origin and destination feature structs.. this is only for PTA application. If either of thos featurestructures are taxonomy they are changed to locationstructs
    * 
    * @param selectedCase
    */

   public void boostLocation(String path, String valueToBoost) {
      try {
         if (path.equals("/destination") || path.equals("/origin")
               || path.equals("/vias")) {
            Object o = caseStruct.getFeatureStruct(path);
            if (o != null) {
               if (o instanceof LocationStruct) {
                  LocationStruct locationStruct = (LocationStruct) caseStruct
                        .getFeatureStruct(path);
                  locationStruct.boost(valueToBoost);
               } else if (o instanceof TaxonomyFeatureStruct) {
                  FeatureStruct taxStruct = (TaxonomyFeatureStruct) caseStruct
                        .getFeatureStruct(path);
                  LocationStruct locationStruct = new LocationStruct(taxStruct
                        .getFeaturePath(), taxStruct.isDiscriminant(),
                        taxStruct.isSolution(), taxStruct.isManditory());
                  List values = taxStruct.getValues();
                  // this method is safe, values is cloned
                  locationStruct.setValues(values);
                  locationStruct.boost(valueToBoost);
                  caseStruct.addFeatureStruct(locationStruct);
               } else {
                  logger
                        .warn("ERROR: PROBLEM IN SimilarityProfile.boostLocation");
               }
               return;
            }
         }
         logger.warn("Problem in SimilarityProfile.boostOriginDestination!");
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public void addPoint(String featurePath, double difference, double similarity) {
      SimilarityGraph graph = (SimilarityGraph) graphs.get(featurePath);
      graph.addPoint(difference, similarity);
   }

   /**
    * Returns the similarity between the two specified features. It should be noted that some similarity measures may be asymmetrical so the order of the features being passed into this function
    * should be ordered. The returned similarity value is the similarity of the second feature (<code>feature2</code>) in relation to the first feature (<code>feature1</code>).
    * 
    * @return the similarity between the two specified features.
    * @param feature1
    *           the candidate or base feature.
    * @param feature2
    *           the test feature.
    */
   public double calculateSimilarity(Feature feature1, Feature feature2) {
      if (feature1 == null || feature2 == null)
         return 0;
      String featurePath = feature1.getPath();
      double difference = Double.POSITIVE_INFINITY;
      try {
         if (featurePath.equals(feature2.getPath())) {
            int type = ((Integer) similarityTypes.get(featurePath)).intValue();
            if (type == EXACT) {
               return (feature1.getValue().equals(feature2.getValue())) ? 1 : 0;
            } else if (type == ARRAY) {
               SimilarityArray simArray = (SimilarityArray) similarityArrays
                     .get(featurePath);
               return simArray.getSimilarity(feature1, feature2);
            } else if (type == PURE_SIMILARITY) {
               SimilarityMeasure simMeasure = (SimilarityMeasure) similarityMeasures
                     .get(featurePath);
               return simMeasure.calculateSimilarity(feature1, feature2);
            } else if (type == INTEGER_DIFFERENCE) {
               int value1 = Integer.parseInt((String) feature1.getValue());
               int value2 = Integer.parseInt((String) feature2.getValue());
               difference = value1 - value2;
            } else if (type == DOUBLE_DIFFERENCE) {
               double value1 = Double.parseDouble((String) feature1.getValue());
               double value2 = Double.parseDouble((String) feature2.getValue());
               difference = value1 - value2;
            } else if (type == SYMBOL_DIFFERENCE) {
               List values = (List) symbolValueLists.get(featurePath);
               difference = values.indexOf(feature1.getValue())
                     - values.indexOf(feature2.getValue());
               if (feature1.name.indexOf("dayofweek") != -1) {
                  difference = Math.abs(difference);
                  if (difference > 3)
                     difference = 7 - difference;
               }
            } else if (type == TAXONOMY_DIFFERENCE) {
               List values = caseStruct.getFeatureStruct(featurePath)
                     .getValues();
               String value1 = (String) feature1.getValue();
               String value2 = (String) feature2.getValue();
               String address1 = null;
               String address2 = null;
               // find the paths of these values in the tree and calculate similarity from that

               for (int i = 0; i < values.size(); i++) {
                  String testValue = (String) values.get(i);
                  int pos = testValue.lastIndexOf("/");
                  if (pos != -1) {
                     testValue = testValue.substring(pos + 1);
                     if (testValue.equals(value1))
                        address1 = (String) values.get(i);
                     if (testValue.equals(value2))
                        address2 = (String) values.get(i);
                  }
               }
               if (address1 != null && address2 != null) {
                  difference = 0;
                  while (true) {
                     int pos1 = address1.indexOf("/", 1);
                     if (pos1 == -1) {
                        pos1 = address1.length();
                     }
                     int pos2 = address2.indexOf("/", 1);
                     if (pos2 == -1)
                        pos2 = address2.length();

                     String begin1 = address1.substring(0, pos1);
                     String begin2 = address2.substring(0, pos2);

                     if (begin1.equals(begin2)) {
                        address1 = address1.substring(pos1);
                        address2 = address2.substring(pos2);

                        if (address1.length() == 0) {
                           int index;
                           while ((index = address2.indexOf("/", 1)) != -1) {
                              address2 = address2.substring(index);
                              difference++;
                           }
                           break;
                        } else if (address2.length() == 0) {
                           int index;
                           while ((index = address1.indexOf("/", 1)) != -1) {
                              address1 = address1.substring(index);
                              difference++;
                           }
                           break;
                        }
                     } else {
                        int index;
                        difference = 2;
                        while ((index = address2.indexOf("/", 1)) != -1) {
                           address2 = address2.substring(index);
                           difference++;
                        }
                        while ((index = address1.indexOf("/", 1)) != -1) {
                           address1 = address1.substring(index);
                           difference++;
                        }
                        break;
                     }
                  }
               }
            }
            if (difference < Double.POSITIVE_INFINITY) {
               SimilarityGraph graph = (SimilarityGraph) graphs
                     .get(featurePath);
               double sim = graph.getSimilarity(difference);
               return sim;
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return 0.0;
   }

   /**
    * Creates and returns a copy of this SimilarityProfile.
    * 
    * @return a clone of this instance.
    */

   public Object clone() throws CloneNotSupportedException {
      SimilarityProfile clone = (SimilarityProfile) super.clone();
      //new SimilarityProfile((CaseStruct) caseStruct.clone(), username, domain);
      List keys = caseStruct.getFeaturePaths();
      for (int i = 0; i < keys.size(); i++) {
         String key = (String) keys.get(i);
         Double relevance = (Double) relevances.get(key);
         if (relevance != null)
            clone.relevances.put(key, new Double(relevance.doubleValue()));
         SimilarityGraph graph = (SimilarityGraph) graphs.get(key);
         if (graph != null)
            clone.graphs.put(key, graph.clone());
         clone.similarityArrays = (Hashtable) ((Hashtable) similarityArrays)
               .clone();

         Integer similarityType = (Integer) similarityTypes.get(key);
         if (similarityType != null)
            clone.similarityTypes.put(key, Integer.valueOf(similarityType
                  .intValue()));

         List symbolValueList = (List) symbolValueLists.get(key);
         if (symbolValueList != null) {
            List newSymbolValueList = new ArrayList();
            for (int j = 0; j < symbolValueList.size(); j++) {
               // this list is all strings
               newSymbolValueList.add((String) symbolValueList.get(j));
            }
            clone.symbolValueLists.put(key, newSymbolValueList);
         }
         try {
            SimilarityMeasure simMeasure = (SimilarityMeasure) similarityMeasures
                  .get(key);
            if (simMeasure != null)
               clone.similarityMeasures.put(key, simMeasure.clone());
         } catch (CloneNotSupportedException e) {
            e.printStackTrace();
         }
         /*
          * // CONNECTIONS: Connections connections = (Connections) allConnections.get(key); if (connections != null) clone.allConnections.put(key, connections.clone());
          */
      }
      return clone;
   }

   /**
    * Generates a similarity measure to be used for determining the similarity between features defined by this feature structure.
    * 
    * @return <code>true</code> if the similarity measure was generated correctly. If the method <code>calculateSimilarity</code> is called without a generated similarity measure it will always
    *         return 0.
    */
   public boolean generateSimilarityMeasure(String featurePath,
         String similarityMeasureName) {
      try {
         FeatureStruct fs = caseStruct.getFeatureStruct(featurePath);
         SimilarityMeasure simMeasure;
         Class simClass;
         Constructor construct;
         switch (fs.getType()) {
         case FeatureStruct.BOOLEAN:
         case FeatureStruct.STRING:
            simClass = Class.forName(similarityMeasureName);
            construct = simClass.getConstructor(null);
            simMeasure = (SimilarityMeasure) construct.newInstance(null);
            break;
         case FeatureStruct.SYMBOL:
         case FeatureStruct.TAXONOMY: {
            simClass = Class.forName(similarityMeasureName);
            Class cls[] = new Class[1];
            cls[0] = Class.forName("java.util.List");
            construct = simClass.getConstructor(cls);
            Object obj[] = new Object[1];
            obj[0] = fs.getValues();
            simMeasure = (SimilarityMeasure) construct.newInstance(obj);
            break;
         }
         case FeatureStruct.INTEGER: {
            simClass = Class.forName(similarityMeasureName);
            Class cls[] = new Class[2];
            cls[0] = Class.forName("java.lang.Integer");
            cls[1] = Class.forName("java.lang.Integer");
            construct = simClass.getConstructor(cls);
            Object obj[] = new Object[2];
            obj[0] = Integer.valueOf(fs.getMinValue());
            obj[1] = Integer.valueOf(fs.getMaxValue());
            simMeasure = (SimilarityMeasure) construct.newInstance(obj);
            break;
         }
         case FeatureStruct.DOUBLE: {
            simClass = Class.forName(similarityMeasureName);
            if (fs.getMinValue() == null || fs.getMaxValue() == null) {
               construct = simClass.getConstructor(null);
               simMeasure = (SimilarityMeasure) construct.newInstance(null);
            } else {
               Class cls[] = new Class[2];
               cls[0] = Class.forName("java.lang.Double");
               cls[1] = Class.forName("java.lang.Double");
               construct = simClass.getConstructor(cls);
               Object obj[] = new Object[2];
               obj[0] = new Double(fs.getMinValue());
               obj[1] = new Double(fs.getMaxValue());
               simMeasure = (SimilarityMeasure) construct.newInstance(obj);
            }
            break;
         }
         case FeatureStruct.COMPLEX: {
            simClass = Class.forName(similarityMeasureName);
            Class cls[] = new Class[1];
            cls[0] = Class.forName("java.util.List");
            construct = simClass.getConstructor(cls);
            Object obj[] = new Object[1];
            obj[0] = fs.getSubFeatureStructs();
            simMeasure = (SimilarityMeasure) construct.newInstance(obj);
            break;
         }
         default:
            logger.warn("ERROR: unknown feature type (" + fs.getType() + ")");
            return false;
         }
         similarityTypes.put(featurePath, Integer.valueOf(PURE_SIMILARITY));
         similarityMeasures.put(featurePath, simMeasure);
         return true;
      } catch (Exception e) {
         logger.warn("ERROR: problem creating similarity measure for feature "
               + featurePath + " with a measure of type "
               + similarityMeasureName + " in generateSimilarityMeasure." + e);
      }
      return false;
   }

   /**
    * Returns the relevance weight for the feature with the specified feature path.
    * 
    * @param featurePath
    *           the feature path for the feature whose relevance weight we are looking for.
    * @return the relevance weight for the feature with the specified feature path.
    * @throws FeatureNotFoundException
    *            if the specified feature path does not exist.
    */
   public double getFeatureWeight(java.lang.String featurePath) {
      Double rel = ((Double) relevances.get(featurePath));
      if (rel != null) {
         double relevance = rel.doubleValue();
         return relevance;
      }
      logger.warn("WARNING: Profile for user " + username
            + " does not contain a relevance value for feature " + featurePath
            + ". It is assumed that the relevance value is zero.");
      return 0;

   }

   public void setExact(String featurePath) {
      similarityTypes.put(featurePath, Integer.valueOf(EXACT));
   }

   /**
    * Sets the Difference Function and Graph to be used with this Profile
    * 
    * @param featurePath
    *           the feature path of the Feature that uses this difference function.
    * @param graphType
    *           the type of similarity graph to be used with this difference function.
    */
   public void setDifference(String featurePath, String graphType) {
      try {
         FeatureStruct fs = caseStruct.getFeatureStruct(featurePath);
         int type = fs.getType();
         switch (type) {
         case FeatureStruct.DOUBLE:
            similarityTypes
                  .put(featurePath, Integer.valueOf(DOUBLE_DIFFERENCE));
            break;
         case FeatureStruct.INTEGER:
            similarityTypes.put(featurePath, Integer
                  .valueOf(INTEGER_DIFFERENCE));
            break;
         case FeatureStruct.SYMBOL: {
            similarityTypes
                  .put(featurePath, Integer.valueOf(SYMBOL_DIFFERENCE));
            List possibleValues = fs.getValues();
            symbolValueLists.put(featurePath, possibleValues);
            break;
         }
         case FeatureStruct.TAXONOMY: {
            similarityTypes.put(featurePath, Integer
                  .valueOf(TAXONOMY_DIFFERENCE));
            List possibleValues = fs.getValues();
            symbolValueLists.put(featurePath, possibleValues);
            break;
         }
         case FeatureStruct.BOOLEAN:
         case FeatureStruct.COMPLEX:
         case FeatureStruct.STRING:
            throw new IncompatableFeatureException(
                  "ERROR: The symbol distance function cannot be used with type "
                        + FeatureStruct.types[type]
                        + " It only be used with features of type DOUBLE or INTEGER.");
         default:
            throw new IncompatableFeatureException(
                  "ERROR: unknown feature type found in SimilarityProfile.setDifference");
         }
         SimilarityGraph graph = new SimilarityGraph(graphType);
         graphs.put(featurePath, graph);
      } catch (IncompatableFeatureException e) {
         e.printStackTrace();
      }
   }

   /**
    * Sets the relevance weight for the specified feature path. Any previous value stored for this feature is replaced.
    * 
    * @param featurePath
    *           the path of the feature whose relevance weight is to be set.
    * @param weight
    *           the new relevance weight.
    */
   public void setFeatureWeight(java.lang.String featurePath, double weight) {
      relevances.put(featurePath, new Double(weight));
   }

   /**
    * Sets the similarity matrix for the specified feature with the specifiec similarity matrix
    * 
    * @param featurePath
    *           the similarity matrix is set for the feature with this path.
    * @param similarityMatrix
    *           the similarity matrix of this feature structure.
    */
   public void setSimilarityMatrix(String featurePath,
         double[][] similarityMatrix) {
      try {
         List values = caseStruct.getFeatureStruct(featurePath).getValues();
         SimilarityArray simArray = new SimilarityArray(values,
               similarityMatrix);
         similarityArrays.put(featurePath, simArray);
         similarityTypes.put(featurePath, Integer.valueOf(ARRAY));
      } catch (IncompatableFeatureException e) {
      }
   }

   /**
    * Sets the owner of this <code>SimilarityProfile</code> to the specified username.
    * 
    * @param username
    *           the new owner of this <code>SimilarityProfile</code>.
    */
   public void setUser(String username) {
      this.username = username;
   }

   /**
    * Returns the owner of this <code>SimilarityProfile</code>.
    * 
    * @return the owner of this <code>SimilarityProfile</code>.
    */
   public String getUser() {
      return username;
   }

   /**
    * Returns a string representation of this similarity profile. This representation is in CBML format.
    * 
    * @return a string representation of this similarity profile.
    */
   public String toString() {
      StringBuffer sb = new StringBuffer();
      sb
            .append("<case domain=\""
                  + domain
                  + "\" xsi:noNamespaceSchemaLocation=\"../CBML/cbmlv3.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
      sb.append("<similarity username=\"" + username + "\">");
      List featurePaths = caseStruct.getFeaturePaths();
      int size = featurePaths.size();
      for (int i = 0; i < size; i++) {
         String path = (String) featurePaths.get(i);
         FeatureStruct fs = caseStruct.getFeatureStruct(path);
         if (fs.isDiscriminant() && !fs.isSolution()) {
            double weight = ((Double) relevances.get(path)).doubleValue();
            String name = path.substring(path.lastIndexOf("/") + 1);
            sb.append("<feature name=\"" + name + "\" weight=\"" + weight
                  + "\">");
            int type = ((Integer) similarityTypes.get(path)).intValue();
            if (type == EXACT) {
               sb.append("<difference type=\"exact\"/>");
            } else if (type == ARRAY) {
               sb.append("<array>");
               SimilarityArray simArray = (SimilarityArray) similarityArrays
                     .get(path);
               int sizeOfArray = simArray.values.size();
               for (int j = 0; j < sizeOfArray; j++) {
                  boolean gotSecondary = false;
                  for (int k = 0; k < sizeOfArray; k++) {
                     if (j != k && simArray.similarityArray[j][k] != 0) {
                        if (!gotSecondary) {
                           sb.append("<primary name=\""
                                 + ((String) simArray.values.get(j)) + "\">");
                           gotSecondary = true;
                        }
                        sb.append("<secondary name=\""
                              + ((String) simArray.values.get(k))
                              + "\" value=\""
                              + Double.toString(simArray.similarityArray[j][k])
                              + "\"/>");
                     }
                  }
                  if (gotSecondary) {
                     sb.append("</primary>");
                  }
               }
               sb.append("</array>");
            } else if (type == INTEGER_DIFFERENCE || type == DOUBLE_DIFFERENCE
                  || type == SYMBOL_DIFFERENCE || type == TAXONOMY_DIFFERENCE) {
               SimilarityGraph graph = (SimilarityGraph) graphs.get(path);
               String graphType = (graph.symmetrical) ? "symmetrical"
                     : "asymmetrical";
               sb.append("<difference function=\"");
               if (type == SYMBOL_DIFFERENCE || type == TAXONOMY_DIFFERENCE) {
                  sb.append("symboldistance");
               } else {
                  sb.append("numeric");
               }
               sb.append("\" graph=\"" + graphType + "\">");
               int numberOfPoints = graph.points.size();
               for (int j = 0; j < numberOfPoints; j++) {
                  SimilarityGraph.Point p = (SimilarityGraph.Point) graph.points
                        .get(j);
                  sb.append("<point difference=\"" + p.getX()
                        + "\" similarity=\"" + p.getY() + "\"/>");
               }
               sb.append("</difference>");
            } else if (type == PURE_SIMILARITY) {
               SimilarityMeasure simMeasure = (SimilarityMeasure) similarityMeasures
                     .get(path);
               sb.append("<puresimilarity simMeasure=\""
                     + simMeasure.getClass().getName() + "\">");
            } else {
               logger.warn("ERROR: unknown difference type encountered ("
                     + type + ").");
            }
            sb.append("</feature>");
         }
      }
      sb.append("</similarity></case>");
      return sb.toString();

   }

   /**
    * Returns a string representation of a pure similarity profile at featurePath
    * 
    * @param featurePath
    *           the path of the similarity measure being retrieved
    * @return a string representation of the pure similarity measure.
    */
   public String getSimilarityMeasure(String featurePath) {
      try {
         SimilarityMeasure similarityMeasure = (SimilarityMeasure) similarityMeasures
               .get(featurePath);
         String simMeasure = similarityMeasure.toString();
         simMeasure = simMeasure.substring(0, simMeasure.indexOf("@"));
         return simMeasure;
      } catch (Exception e) {
         e.printStackTrace();
         return "";
      }
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
    * @param featurePath
    *           the path of the required feature
    * @author doyledp
    * @return an int representing the similarity type for the feature located at featurepath
    */
   public int getSimilarityType(String featurePath) {
      int type = ((Integer) similarityTypes.get(featurePath)).intValue();
      return type;
   }

   /**
    * @param featurePath
    *           the path of the required feature
    * @author doyledp
    * @return a double array showing the similarity between Symbolic Values. Indexes on the array are the same as in the structure
    */
   public double[][] getSimilarityArray(String featurePath) {
      SimilarityArray array = (SimilarityArray) similarityArrays
            .get(featurePath);
      return array.similarityArray;
   }

   /**
    * @param featurePath
    *           the path of the required feature
    * @author doyledp
    * @return a double array showingpoints in the similarity graph
    */
   public double[][] getSimilarityGraph(String featurePath) {
      SimilarityGraph graph = (SimilarityGraph) graphs.get(featurePath);
      List points = graph.points;
      double[][] array = new double[points.size()][2];
      for (int i = 0; i < points.size(); i++) {
         SimilarityGraph.Point p = (SimilarityGraph.Point) points.get(i);
         array[i][0] = p.getX();
         array[i][1] = p.getY();
      }
      return array;
   }

   /**
    * @param featurePath
    *           the path of the required feature
    * @author doyledp
    * @return boolean if the graph is symmetrical or not.
    */
   public boolean getSimilarityGraphSymmetry(String featurePath) {
      SimilarityGraph graph = (SimilarityGraph) graphs.get(featurePath);
      return graph.symmetrical;
   }

   /*
    * //CONNECTIONS: public Connections getConnections(String featurePath) { return (Connections) allConnections.get(featurePath); }
    */
   /**
    * @return Returns the caseStruct.
    */
   public CaseStruct getCaseStruct() {
      return caseStruct;
   }
}