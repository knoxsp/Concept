package scatterbox.classifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;

public class ClassificationSortedSet extends ArrayList<Classification>
      implements SortedSet<Classification> {

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public Comparator<? super Classification> comparator() {
      ClassificationComparator cc = new ClassificationComparator();
      return cc;
      //return null;
   }

   public Classification first() {
      return super.get(0);
   }

   public SortedSet<Classification> headSet(Classification arg0) {
      int indexOfObject = super.lastIndexOf(arg0);
      SortedSet<Classification> tailSet = new ClassificationSortedSet();

      for (int i = indexOfObject; i < super.size(); i++) {
         tailSet.add(super.get(i));
      }

      return tailSet;
   }

   public Classification last() {
      int size = super.size();
      return super.get(size - 1);
   }

   public SortedSet<Classification> subSet(Classification arg0,
         Classification arg1) {
      // TODO Auto-generated method stub
      return null;
   }

   public SortedSet<Classification> tailSet(Classification arg0) {
      int indexOfObject = super.lastIndexOf(arg0);
      SortedSet<Classification> tailSet = new ClassificationSortedSet();

      for (int i = indexOfObject; i >= 0; i--) {
         tailSet.add(super.get(i));
      }

      return tailSet;
   }

   public void update() {
      Classification newClassification;
      Classification[] updatedClassifications = new Classification[this.size()];
      int newConfidence;
      int decay;
      int oldConfidence;
      for (int i = 0; i < this.size(); i++) {
         Classification oldClassification = this.get(i);
         //Retrieve the confidence of the classification to be updated
         oldConfidence = oldClassification.getConfidence();
         decay = oldClassification.getDecay();
         //System.out.println(a_classification.getType() + oldConfidence);
         //If the confidence is 0, ignore it
         if (oldConfidence > 0) {
            //get the new confidence by subtracting the decay value
            newConfidence = oldConfidence - decay;
            //Confidence cannot be less than 0
            if (newConfidence < 0)
               newConfidence = 0;
            //update the confidence
            newClassification = new Classification(oldClassification.getType(),
                  oldClassification.getValue(), newConfidence, decay);
            //add the newly updated classification in place of the old one
            updatedClassifications[i] = newClassification;
         }else{
            updatedClassifications[i] = oldClassification;
         }
      }
      for(Classification c:updatedClassifications){
         this.add(c);
      }
   }

   public boolean add(Classification arg0) {
      Comparator<? super Classification> c = this.comparator();
      boolean inserted = false;

      //If a classification of the same value and type already exists, remove it.
      for (int i = 0; i < this.size(); i++) {
         if (arg0.getValue().equalsIgnoreCase(this.get(i).getValue())) {
            super.remove(i);
            break;
         }
      }
      if (super.size() == 0) {
         //System.out.println("Adding first element");
         super.add(arg0);
         inserted = true;
      } else {
         for (int i = 0; i < this.size(); i++) {
            //System.out.println("Comparison Result = " + c.compare(arg0, super.get(i)));
            //If arg0 is greater than the current element
            if ((c.compare(arg0, this.get(i))) == 1) {
               super.add(i, arg0);
               inserted = true;
               break;
            }
         }
         if (inserted == false) {
            super.add(arg0);
            inserted = true;
         }
      }
      return inserted;
   }

   public boolean addAll(Collection<? extends Classification> arg0) {
      Iterator<? extends Classification> i = arg0.iterator();
      while (i.hasNext()) {
         super.add(i.next());
      }
      return false;
   }

   public void clear() {
      super.clear();

   }

   public boolean contains(Object arg0) {
      return super.contains(arg0);
   }

   public boolean containsAll(Collection<?> arg0) {
      return super.containsAll(arg0);
   }

   public boolean isEmpty() {
      return super.isEmpty();
   }

   public Iterator<Classification> iterator() {
      return super.iterator();
   }

   public boolean remove(Object arg0) {
      return super.remove(arg0);
   }

   public boolean removeAll(Collection<?> arg0) {
      return super.removeAll(arg0);
   }

   public boolean retainAll(Collection<?> arg0) {
      return super.retainAll(arg0);
   }

   public int size() {
      return super.size();
   }

   public Object[] toArray() {
      return super.toArray();
   }

   public <T> T[] toArray(T[] arg0) {
      return super.toArray(arg0);
   }

   public void print() {
      Classification c;
      System.out.println("Printing...");
      for (int i = 0; i < this.size(); i++) {
         c = this.get(i);
         System.out.println(c.getType() + ", " + c.getValue() + ", "
               + c.getConfidence() + ", " + c.getDecay());
      }

   }
}
