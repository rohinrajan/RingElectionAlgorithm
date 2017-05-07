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
/**
 *
 * Rohin Rajan          UTA ID: 1001154037
 * Hiral Trivedi        UTA ID: 1001275299
 * Sayali Mohadikar     UTA ID: 1001246542
 * Ramkrishna Chivukula UTA ID: 1001239737
 */
public class RightConnector implements Runnable {
    BufferedReader reader;
    ServerSocket socket;
    BufferedWriter writer;
    Socket sock;
    
    private Thread thread;
    private String processID;
    private int port;
    
    
    private final String INFORTAG = "INFORMATION";
    private final String ERRORTAG = "ERROR";
    private final String SEPERATE = ";";
    private final String TOKEN = "TOKEN";
    private final String ELECTION = "ELECTION";
    private final String RECONNECTION = "RECONNECTED";
    private final String COORDINATOR = "COORDINATOR";
    private final String CRASHED = "CRASHED";
    private final String STARTELECTION = "STARTELECTION";
    
    private String msg;
    private String[] position;

    private Date timeStamp;
    
    
    public RightConnector(int port, String processID){

        position = new String[10];
        this.port = port;
        this.processID = processID;
        timeStamp = new Date();
    }
    
    
    @Override
    public void run(){
        try{
            //Creating the raad stream and write stream of the sockets
            readAndWriteStreams();
            
            printInfo("RightListener: Right Side Connection Successfully!");
            // Starting the election by sending the election message to the next node
            startElection();
            //Read from the message buffer and perfrom the specified taska accordingly
            readAndPerformTasks();
            
            //Sending token message to ensure that each node is active
            printInfo("RightListener: Done with this thread exiting");
            System.out.println("-----------------------------------------------------------------------------------------");
        }
        catch(Exception ex){
            logError("RightListnener Run()", ex);
        }
    }
    
    private void printInfoFast(String msg){
        try{
            Thread.sleep(1000);//1secs
        }
        catch(Exception ex){
            System.out.println("Error in printInfo: " +ex);
        }
        
        DateFormat format = new SimpleDateFormat("hh:mm:ss");
        String stringTime = format.format(new Date());
        System.out.println(stringTime + " : " + processID +" : " + msg);
    }
    
    
    private void printInfo(String msg){
        try{
            Thread.sleep(5000);//5secs
        }
        catch(Exception ex){
            System.out.println("Error in printInfo: " +ex);
        }
        
        DateFormat format = new SimpleDateFormat("hh:mm:ss");
        String stringTime = format.format(new Date());
        System.out.println(stringTime + " : " + processID +" : " + msg);
    }
    
    private void clearMessageBuffer(){
        RingElection.messageBuffer = null;
        RingElection.isMsgBfrNull = true;
    }
    
    private String readMessageBuffer(){
        String retValue = null;
        try{
            while(RingElection.isMsgBfrNull == true){
            Thread.sleep(2000); //2 secs
            }
            retValue = RingElection.messageBuffer;
            clearMessageBuffer();
        }
        catch(Exception ex){
            System.out.println("RightListener: exception occured in readFromMessageBuffer: "+ex);
        }
        return retValue;
    }

    
   
    //Method API to log info messages in format
    public void logInfo(String message){
        String logmsg = INFORTAG + ": Process" + processID + ": ";
        logmsg += message;
        System.out.println(logmsg);
        //TODO log messages to the log file
    
    }
    
    //Method API to log Error messages in format
    public void logError(String MethodName,Exception ex){
        String logmsg = ERRORTAG + ": " + processID + ": ";
        logmsg += "Exception occured in " + MethodName +": " + ex.getMessage();
        System.out.println(logmsg);
    }
    
    //Method to send write a message to the socket
    public void messageWrite(String msg){
        try{
            writer.write(msg);
            writer.newLine();
            writer.flush();
        }
        catch(Exception ex){
            System.out.println("Exception occurred in rightListener messageWrite :" +ex);
        }
    }
    
    //Method to read the message from the socket
    public String messageRead(){
        String data = null;
        try{
            while(data == null){
                if(reader != null)
                    data = reader.readLine();
                else
                    break;
            }
        }
        catch(Exception ex){
            System.out.println("Exception occurred in rightListener messgaeRead: "+ex);
        }
        return data;
    }
    
    
    
    
    // Method to start the thread of connecting and reading from the socket
    // the process parameters are port and process id
    public void start(){
        try{
            printInfo("Starting Thread RightListener");
            if(thread == null){
                thread = new Thread(this,"RightListener");
                socket = new ServerSocket(port);
                socket.setReuseAddress(true); 
                sock = socket.accept();
                thread.start();
            }
        }
        catch(Exception ex){
            System.out.println("Exception occurred in rightlistener start: " +ex);
        }
    } 

    
    //Method to set the read and write streams
    private void readAndWriteStreams(){
        try{
            //Creating the raad stream and write stream of the sockets
            reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
        }
        catch(Exception ex){
            System.out.println("RightListener: exception occurred in the readAndWriteStream: " +ex);
        }
    }

    //Method to read and perform taks while reading from the buffer
    private void readAndPerformTasks() {
        try{
            while(true){
                String data = readMessageBuffer();
                String header = data.split(SEPERATE)[0];
                
                switch(header){
                    //Read the token message from the buffer
                    case TOKEN:
                        if(RingElection.GetCurrentProcessState() == RingElection.State.Election){
                           printInfo("TOKEN has been recieved but curently performing Election"); 
                        }
                        else {
                            printInfo("TOKEN has been recieved in " + processID + " forwarding to the next node");
                            Thread.sleep(3000);
                            messageWrite(data);
                        }
                        break;
                    //Election message
                    case ELECTION:
                        printInfo("Recieved Election message: " + data);
                        data += SEPERATE + processID + SEPERATE + port;
                        printInfo("Sending Election message: " + data + " to the next node");
                        Thread.sleep(3000);//3 secs
                        messageWrite(data);    
                        break;
                    // Node is back up again message
                    case RECONNECTION:
                        printInfo("The previous node has been brought back alive re-connecting to the previous node");
                        messageWrite(data);
                        port = Integer.parseInt(data.split(SEPERATE)[1]);
                        connectToSocket();
                        break;
                    //Co-ordinator message    
                    case COORDINATOR:
                        printInfo("RightListner: Recieved Co-ordinator message ");
                        printInfo("The current co-ordinator is : " + data.split(SEPERATE)[2]);
                        messageWrite(data);
                        break;
                    //Crashed nodes messaage
                    case CRASHED:
                        printInfo("RightListener: Crashed node recieved!..");
                        printInfo("RightListener: Will check to see if the current processs is related to the previous node");
                        checkAndPerformWithCrashedMessage(data);                    
                        break;
                    //Start Election message 
                    case STARTELECTION:
                        startElection();
                        break;            
                    default:
                        printInfo("RightListener: sending message to next node: " + data);
                        messageWrite(data);
                }
            }
        }
        catch(Exception ex){
            System.out.println("RightListener: exception occurred in the readNPerformTasks " +ex);
        }
        }

    private void connectToSocket() {
        try{
            sock.close();
            sock = RetrievalHelper.socket;
            socket = RetrievalHelper.sock;
            readAndWriteStreams();
        }
        catch(Exception ex){
            System.out.println("RightListener: exception occurred in the connectTOSocket: " +ex);
        }
    }

    
    
    //Method to start election 
    private void startElection() {
        try{
            // set the current process state to election
            RingElection.SetCurrentProcessState(RingElection.State.Election);
            printInfo("Process: " + processID + " is going to start election");
            String elMsg = ELECTION + SEPERATE + processID + SEPERATE + port;
            printInfo("Sending election message: " + elMsg + " to the next node");
            Thread.sleep(3000);//3secs
            messageWrite(elMsg);
        }
        catch(Exception ex){
            System.out.println("RightConnector: exception occurred in the startelection: " +ex);
        }
        
    }

    private void checkAndPerformWithCrashedMessage(String crashedMessage) {
        String newConnectionNode = crashedMessage.split(SEPERATE)[1];
        String newPortNumber = crashedMessage.split(SEPERATE)[2];
        printInfoFast("New Port Number is : " +newPortNumber);
        printInfoFast("Node which needs to be re-connected is " + newConnectionNode + " and current node is " +processID);
        printInfoFast("Checking to see if the node matches the current node");
        //Checking to see if the new process id to connect to is the current process id
        if (newConnectionNode.equals(processID)) {
            printInfoFast("Current node needs to close existing socket and connect to a new socket connect");
            // close exisiting node and connect to new node
            closeExistingAndConnectNewSocketConnection(newPortNumber); 
            printInfoFast("Closed all socket connections and started new socket connection");            
            printInfoFast("Calling Retrieval Helper class to check if the existing node has come back alive");            
            //Call another thread to check to see if the previous node has come alive again
            ReCheckPrevNode(port);
            port = Integer.parseInt(newPortNumber);
        }
        else{
            printInfo("The crashed node is sent to the next node");
            messageWrite(crashedMessage); // Forward the message to next node
        }
    }

    //Method to close the exsiting socket connection
    private void closeExistingAndConnectNewSocketConnection(String newPortNumber) {
        try{
            printInfo("Close existing socket connection");
            sock.close();
            socket.close();
            
           Thread.sleep(2000);//2secs
            if(socket.isClosed() && sock.isClosed()) {
               //Connecting to new socket connection
                printInfo("Starting to connect to the next node using another socket connection");
                connectToNewSocket(Integer.parseInt(newPortNumber));
                printInfo("Connected to socket now sending an intimation to the next node");
                messageWrite("STARTELECTION");               
            }
            
        }
        catch(Exception ex){
            System.out.println("RightListener: Exception occurred in the closeExistingAndConnectNewSocketConnection: " +ex);
        }
    }

    //Method to re-connect to the prevous node which had crashed
    private void ReCheckPrevNode(int port) {
        //Creating an object for the retrievalHelper
        RetrievalHelper helper = new RetrievalHelper(port);
        helper.start();
    }

    private void connectToNewSocket(int newport) {
        try{
            printInfo("Connecting to the new socket with port number: " +newport);
            sock = new Socket("127.0.0.1", newport);
            sock.setReuseAddress(true);
            printInfo("Connected to new node successfully");
            readAndWriteStreams();
        }
        catch(Exception ex){
            printInfo("Exception occurred in connectToNewSocket: " +ex);
        }
    }
}
