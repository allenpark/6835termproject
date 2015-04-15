package handsfree;

public class HandsFree {
   private static Thread server;
   public static double[] data;
   
   public static void main(String[] args) {
      data = new double[7];
      server = new FaceTrackNoIRListener();
      server.start();
      try {
    	  String oldOut = "";
    	  while (true) {
    		  Thread.sleep(100);
    		  synchronized(data) {
    			  String out = "";
    			  for (int i = 0; i < data.length; i++) {
    				  out += String.format("%3.1f", data[i]) + " ";
    			  }
    			  if (!oldOut.equals(out)) {
    				  System.out.println(out);
    				  oldOut = out;
    			  }
    		  }
    	  }   
      } catch(Exception e) {
         System.err.println("Main: " + e);
      }   
   }
}