package scatterbox.utils;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class CounterThread extends Thread{
   Thread t;   
   int Count;
   boolean stop = false;

   public void init()   
   {  
      Count=0;
      t=new Thread(this);
      t.start();
   }

   public void run(){
      final Timer timer = new Timer();

      timer.scheduleAtFixedRate(new TimerTask() {
         public void run(){
            if(stop == false){
               Count++;
            }else{
               return;
            }
         }
      }, new Date(), 1);
   }

   public int getCount(){
      return Count;
   }
   
   public void requestStop() {
      stop = true;
    }
   
}
