/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ringelection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
/**
 *
 * Rohin Rajan          UTA ID: 1001154037
 * Hiral Trivedi        UTA ID: 1001275299
 * Sayali Mohadikar     UTA ID: 1001246542
 * Ramkrishna Chivukula UTA ID: 1001239737
 */
public class LeftConnector implements Runnable {
    Socket socket;
    BufferedReader reader;
    BufferedWriter writer;
    private Thread thread;
    private int port;
    private String address;
    private String processID;
    
    private ServerSocket gen;
    private String[] position;
    private HashSet<Integer> usedPorts;
    // This is a boolean flag which is used to create the token and pass it to the next node
    private boolean TokenPassed = false;
    private static int increment =1;
    
    private final String SEPERATOR = ";";
    private final String TOKEN = "TOKEN";
    private final String ELECTION = "ELECTION";
    private final String RECONNECTION = "RECONNECTED";
    private final String COORDINATOR = "COORDINATOR";
    private final String CRASHED = "CRASHED";
    private final String STARTELECTION = "STARTELECTION";
    private final String UPDATEPOSITION = "UPDATEPOSITION";
    
    
    
    public LeftConnector(String address, int port, String processID){
        this.address = address;
        this.port = port;
        this.processID = processID;
    }
    
    @Override
    public void run(){
        try{
            //Initating the connection to the socket
            connectSocketToLeftProcess();
            // setting the array list of used ports 
            usedPorts = new HashSet<>();
            //create read and write streams to the socket
            generateReadNWriteStreams();
                   
            //Reading and perform tasks based on the message sent through the socket
            readAndPerformTasks();
            
            System.out.println("LefttListener: Done with this thread exiting");
            System.out.println("-----------------------------------------------------------------------------------------");
        }
        catch(Exception ex){
            System.out.println("Exception occurred in LeftListener: "+ex);
        }
    }
    
    
    
    //Method to write message to buffer 
    private void WriteToMessageBuffer(String message){
        try{
            if (RingElection.isMsgBfrNull) {
                RingElection.messageBuffer = message;
                RingElection.isMsgBfrNull = false;
            }
            else {
                Thread.sleep(2000);//2 secs
                WriteToMessageBuffer(message);
            }
        }
        catch(Exception ex){
            System.out.println("LeftListener exception occurred in WriteToMessageBuffer: " +ex);
        }
    }
    
        
    
    //Method to start the thread, we are passing the address and the port number
    public void start() {
        try{
            printInfo("Starting the left Listnener thread");
            thread = new Thread(this,"LeftListener");
            thread.start();
        }
        catch(Exception ex){
            printException(ex, "start");
        }
    }
    
    //Method to write the message to the socket
    public void messageWrite(String message){
        try{
            if(writer != null){
                writer.write(message);
                writer.newLine();
                writer.flush();
            }    
        }
        catch(Exception ex){
            System.out.println("Exception :" +ex);
        }
    }
    
    //Method to Read the message from the socket
    public String messageRead() throws Exception{
        String data = null;
        try{
            while(data == null){
               data = reader.readLine();
            }
        }
        catch(Exception ex){
            System.out.println("Exception occurred in: "+ex);
            throw ex;
        }
        return data;
    }

    //Method to connect to the left process or node
    private void connectSocketToLeftProcess() {
        int retryCounter=1; 
        //Setting the nu,ber of attempts to be made by this thread to try to connect to the left process or node
        while(retryCounter <=10){
            try{
                printInfo("Initiating a connection to the left connection");
                printInfo("No. of attempts made: " + retryCounter);
                //incrementing the retry counter
                retryCounter++;
                socket = new Socket(address,port); //connecting to the socker
                socket.setReuseAddress(true);
                printInfo("LeftProcess is connected successfully"); //enters this line if the socket is connected successfully
                break;
            }
            catch(Exception ex){
                System.out.println("Unable to connect to left process reason: "+ex);
            }
            try{
                Thread.sleep(2000); //setting sleep for 2 secs before checking again
            }
            catch(Exception ex){ //not optimal code will remove this later on
                //empty catch 
            }
        }
    }

    //Method to read and perform the tasks based on the messages recieved by the next node
    private void readAndPerformTasks() {
        try{
            while(true){
                //Read the socket for any messages
                String data = messageRead();
                //split the msg based on the seperator and get the header to determine the tasks
                String[] arry = data.split(SEPERATOR);
                switch(arry[0]){
                    //TOken message recieved
                    case TOKEN:
                        WriteToMessageBuffer(data);
                        break;
                        //Election message recieved 
                    case ELECTION:
                        //check to see if this process is the one who sent the election message
                        if (arry[1].equals(processID)) {
                            printInfo("Election message recieved: " + data);
                            printInfo("Election message has been recieved now will elect coordinator"); 
                            //calling the election algorithm
                            runElection(arry);
                        }
                        else {
                            WriteToMessageBuffer(data);
                        }
                        break;
                    case RECONNECTION:
                        printInfo("LeftListener: Closing the existing socket connection");
                        gen.close();
                        printInfo("LeftListener: Reconnecting to the original process");
                        connectSocketToLeftProcess();
                        generateReadNWriteStreams();
                        break;
                    case COORDINATOR:
                        if (arry[2].equals(processID)) {
                            RingElection.SetCurrentProcessState(RingElection.State.Coordinator);
                            printInfo("Recieved Co-ordinator message this process is the co-ordinator");
                        }
                        if (!arry[1].equals(processID)) {
                            if(!RingElection.GetCurrentProcessState().equals(RingElection.State.Coordinator))
                                RingElection.SetCurrentProcessState(RingElection.State.Process);
                            WriteToMessageBuffer(data);
                        }
                        else {
                            if(!RingElection.GetCurrentProcessState().equals(RingElection.State.Coordinator))
                                RingElection.SetCurrentProcessState(RingElection.State.Process);
                        }
                        if((RingElection.GetCurrentProcessState() == RingElection.State.Coordinator)
                                &&(!TokenPassed)){
                            WriteToMessageBuffer(TOKEN);
                            TokenPassed = true;
                        }
                        break;
                    case CRASHED:
                        TokenPassed = false; // if this process is coordinator then need to resend the message
                        WriteToMessageBuffer(data);
                        break;
                    case STARTELECTION:
                        WriteToMessageBuffer(data);
                        break;
                    case UPDATEPOSITION:
                        printInfo("LeftConnector: Recieved update position message");
                        if (!arry[1].equals(processID)) {
                            WriteToMessageBuffer(data);
                        }
                        updatePositionLocation(arry);
                        break;
                    case RingElection.UPDATEUSEDPORTS:
                        printInfo("Recieved updateUsedPorts will now update the port numbers");
                        updateUsedPorts(arry);
                        if(!arry[1].equals(processID)) //Stop Mechanism
                            WriteToMessageBuffer(data);
                        break;
                }
            }
        }
        catch(Exception ex){
            System.out.println("LeftListner: One node might have crashed reconnecting to next node");
            System.out.println("LeftListner: Determine the new node and send the crashed message");
            String newNode = getNewNode();
            int newPort = getNewPort();
            if (newPort != RingElection.BASEPORT) {
                System.out.println("LeftListener: Found new node " + newNode + " Will start to connect to the new node");
                // This would get the new node and write the message to buffer
                generateCrashedMessage(newNode, newPort);
                createNewSocket(newPort);
                readAndPerformTasks();
            }
            else
                System.out.println("Exception has occurred exiting application");
        }
    }

    private void printInfo(String msg){
        DateFormat format = new SimpleDateFormat("hh:mm:ss");
        String stringTime = format.format(new Date());
        System.out.println(stringTime + " : " + processID +" : " + msg);
    }
    
    //Method to determine and send the coordinator message 
    //Election Algorithm
    private void runElection(String[] arry) {
        printInfo("Starting election algorithm in " +processID + " to select the coordinator");
        int maxProcessID =0, iterator =0;
        position = new String[(arry.length -1)/2];
        
        for(int pos = 1; pos < arry.length ; pos+=2){
            int pID = Integer.parseInt(arry[pos]);
            //appending the processIDs
            position[iterator] = arry[pos];
            //appending port numbers 
            usedPorts.add(Integer.parseInt(arry[pos+1]));
            iterator++;
            //finding the co-ordinator processID
            if (maxProcessID < pID) {
                maxProcessID = pID;
            }    
        }
        //Sending the Update Position Message to the next node
        generateUpdatePositionMessage();
        //Sending the update Used ports to the next node
        generateUpdateUsedPortsMessage();
        WriteToMessageBuffer("COORDINATOR" + SEPERATOR + processID  + SEPERATOR +  maxProcessID);
    }


    private void generateSocketStreams(Socket socket) {
        try{
            this.socket = socket;
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        }
        catch(Exception ex){
            System.out.println("LeftListener: exception occurred in generateSocketStreams " + ex);
        }
    }

    //Method to get the new node
    private String getNewNode() {
        String newNode= position[position.length-2];
        return newNode;
    }

    //Method to generate the crashed message with the new node
    private void generateCrashedMessage(String newNode, int newPort) {
            //Generate the crashed message
            String msg = RingElection.CRASHED + SEPERATOR + newNode + SEPERATOR + newPort;
            System.out.println("LeftConnector : " + processID + " : " + msg);
            //Write message to the budder
        WriteToMessageBuffer(msg);
        }
        
    private void createNewSocket(int port) {
        try{
            gen = new ServerSocket(port);
            gen.setReuseAddress(true); 
            Socket s = gen.accept();
            generateSocketStreams(s);
        }
        catch(Exception ex){
            printException(ex, "createNewSocket");
        }
    }

    //Method to overwrite the existing Input n output streams for the socket connection
    private void generateReadNWriteStreams() {
        try{
            //Reading the process information from the previous node
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));   
        }
        catch(Exception ex){
            printException(ex, "generateReadNWriteStreams");
        }
    }

    //Method to get the new port number from the available ports present
    private int getNewPort() {
        int newPort = RingElection.BASEPORT;
        boolean isAvailabe = false;
        try{
            do{
                newPort += Integer.parseInt(processID);
                if(!usedPorts.contains(newPort))
                    isAvailabe = true;
            }
            while(!isAvailabe);
        }
        catch(Exception ex){
            printException(ex,"getNewPort");
        }
        return newPort;
    }

    
    private void printException(Exception ex, String MethodName){
        String msg = "LeftConnector " + SEPERATOR;
        msg += processID + SEPERATOR;
        msg += "Exception occurred in the " + MethodName;
        msg+= SEPERATOR + " with exception : " +ex.getMessage();
        System.out.println(msg);
    }
    
    //Method to update the position location 
    private void updatePositionLocation(String[] newpostions) {
        try{
            String temp;
            position = new String[newpostions.length-1];
            System.arraycopy(newpostions, 1, position,0,newpostions.length-1);

            while(!position[0].equals(processID)){
                temp = position[0];
                for(int newpos =0;newpos < position.length-1;newpos++){
                    position[newpos] = position[newpos+1];
                }
                position[position.length-1] = temp;
            }
                
        }
        catch(Exception ex){
            System.out.println("LeftConnector: Exception occurred in the updatePositionLocation: " +ex);
        }
        
    }

    //Method to generate and send a message to next node for the updated position
    private void generateUpdatePositionMessage() {
        StringBuilder strmsg = new StringBuilder(UPDATEPOSITION + SEPERATOR);
        for(String pos: position){
            strmsg.append(pos);
            strmsg.append(SEPERATOR);
        }
        WriteToMessageBuffer(strmsg.toString());
    }
    
    //Method to generate and send a message to next node for the updated position
    private void generateUpdateUsedPortsMessage() {
        StringBuilder strmsg = new StringBuilder(RingElection.UPDATEUSEDPORTS + SEPERATOR);
        strmsg.append(processID);
        strmsg.append(SEPERATOR);
        for(int usedport: usedPorts){
            strmsg.append(usedport);
            strmsg.append(SEPERATOR);
        }
        WriteToMessageBuffer(strmsg.toString());
    }
    

    //Method to update the used port numbers from the election messages
    private void updateUsedPorts(String[] arry) {
        try{
            for (int pos = 2; pos < arry.length; pos++) {
                int value =  Integer.parseInt(arry[pos]);
                if (!usedPorts.contains(value)) {
                    usedPorts.add(value);
                }
            }
            for(int ports: usedPorts)
                System.out.println("The current updated ports are : " + ports);
        }
        catch(Exception ex){
            printInfo("Exception occurred in the updateUsedPorts: " + ex);
        }
    }
}
