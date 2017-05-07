/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ringelection;

import java.io.*;
import org.apache.commons.io.output.TeeOutputStream;





/**
 *
 * Rohin Rajan          UTA ID: 1001154037
 * Hiral Trivedi        UTA ID: 1001275299
 * Sayali Mohadikar     UTA ID: 1001246542
 * Ramkrishna Chivukula UTA ID: 1001239737
 */



public class RingElection {

   static String processID;
   static int lftPort, rgtPort;
   private static final String LOCALHOST = "127.0.0.1";
   
   public static String messageBuffer;
   public static boolean isMsgBfrNull = true;
   public static boolean isRoutingReceived = false;
   public static boolean recievedProcessMessage = false;

   public static final String CRASHED = "CRASHED";
   public static final String UPDATEUSEDPORTS = "UPDATEUSEDPORTS";
   public static final int BASEPORT = 9010;
   public static int[] availablePorts = new int[] {9007,9008,9010,9011,9012,9013};
   
   //Method to validate the prcess based on the process ID
    private static boolean isValidProcessID(String processID) {
        boolean retValue=true;
        try{
            //check to see if the process is null or not
        if(processID != null)
            if(Integer.parseInt(processID) > 6 || Integer.parseInt(processID) <= 0)
                retValue = false;
        }
        catch(Exception ex){
            System.out.println("Exception occurred in the isValidProcess(): " + ex.getLocalizedMessage());
            retValue = false;
        }
        return retValue;    
    }
   
   public enum State{
       Election,
       Coordinator,
       Process
   };
   
   private static State CurrentState; 
   
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
       try{
           //Ask user to decide the procees id
           System.out.println("Enter the process ID: ");
           BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
           processID = br.readLine();
           if(isValidProcessID(processID)){
               getPortNumber(processID);
               //Call the Left and Right threads
               LeftConnector lftlistener = new LeftConnector(LOCALHOST,lftPort,processID);
               lftlistener.start();
               RightConnector rgtListener = new RightConnector(rgtPort,processID);
               rgtListener.start();
           }
           else
               System.out.println("Not a valid process ID, please enter between 1 to 6");
       }
       catch(Exception ex) {
           System.out.println("Exceptions occurred in: " +ex);
       }
    }

    //Method to get the port  numbers based on the process id given by the user
    private static void getPortNumber(String processID) {
        try{
            
            switch(processID){
                case "1":
                    LogIntoFile(processID);
                    lftPort = 9006;
                    rgtPort = 9001;
                    break;
                case "2":
                    LogIntoFile(processID);
                    lftPort = 9001;
                    rgtPort = 9002;
                    break;
                case "3":
                    LogIntoFile(processID);
                    lftPort = 9002;
                    rgtPort = 9003;
                    break;
                case "4":
                    LogIntoFile(processID);
                    lftPort = 9003;
                    rgtPort = 9004;
                    break;
                case "5":
                    LogIntoFile(processID);
                    lftPort = 9004;
                    rgtPort = 9005;
                    break;
                case "6":
                    LogIntoFile(processID);
                    lftPort = 9005;
                    rgtPort = 9006;
                    break;
            }
        }
        catch(Exception ex){
            System.out.println("Exception occurred in getPortNumber: "+ex);
        }
    }
    //Method to set the current process state to a new state given
    public static void SetCurrentProcessState(State newState){
        CurrentState = newState;
    }
    
    public static State GetCurrentProcessState(){
        return CurrentState;
    }
    
     public static void LogIntoFile(String pID){
         try{
            String currentDirectory = System.getProperty("user.dir");
            FileOutputStream fileName = new FileOutputStream(currentDirectory +processID+".txt");
            TeeOutputStream myOut = new TeeOutputStream(System.out, fileName);
            PrintStream ps = new PrintStream(myOut);
            System.setOut(ps);
         }
         catch(Exception e){
             System.out.println("File not found");
         }
    }
        }
    
                
