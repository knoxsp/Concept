package scatterbox.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;

public class SecretSantaMatchMaker {

   String[] names = {"Deirdre", "Seamus", "Ljiljana", "Paul", "Susan", "Simon", "Stephen", "Rachel"};
   
   String[] couples = {"Ljiljana,Stephen", "Simon,Susan", "Deirdre,Seamus"};
   
   public static void main(String[] args) {
      SecretSantaMatchMaker matchMaker = new SecretSantaMatchMaker();
      matchMaker.getMatch();

   }
   
   /**
    * Match non-partners for secret santa.
    */
   private void getMatch(){
      List<String> taken = new LinkedList<String>();
      
      for(String name : names){
         String randomName; 
         do{
         //Get the random index
         int randomIndex = getRandomIndex();
         //Retrieve the name at that random index
         randomName = names[randomIndex];
         //A person cannot get themselves, and partners cannot get each other.
         //A person cannot get someone who has already been picked.
         }while(name.equalsIgnoreCase(randomName) || 
               (isPartnerOf(name, randomName))||
               (taken.contains(randomName))
               );
         taken.add(randomName);
         System.out.println(name + "  gets to buy for:  " + getMD5(randomName));
      }
   }
   
   /**
    * Checks the couples array to see if personA is the partner of personB
    * @param personA
    * @param personB
    * @return
    */
   private boolean isPartnerOf(String personA, String personB){
      boolean partner = false;
      for(String couple : couples){
          if(couple.contains(personA) && couple.contains(personB)){
             return true;
          }
      }
      return partner;
   }
   
   /**
   *Get a random index between 0 and the number 
   *of people in the secret santa pool.
   */
   private int getRandomIndex(){
      double rand = Math.random()*names.length;
      int i = (int) rand;
      return i;
   }
   
   /**
    * This method takes a string and returns the MD5 hash of that string. 
    * The purpose of this method is to create a uniquie identifier for a person 
    * by hashing their email address.
    */
   public String getMD5(String data){
      StringBuffer md5 = new StringBuffer();

      MessageDigest digest;
      try {
         digest = java.security.MessageDigest.getInstance("MD5");
         digest.update(data.getBytes(),0,data.length());
         return new BigInteger(1, digest.digest()).toString(16);
      } catch (NoSuchAlgorithmException e) {
         System.err.println("Error with md5 transformation"+e.getLocalizedMessage());
      }

      return md5.toString();
   }

}
