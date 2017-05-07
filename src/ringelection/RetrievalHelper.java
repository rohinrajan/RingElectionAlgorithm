/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ringelection;

import java.net.*;

/**
 *
 * Rohin Rajan          UTA ID: 1001154037
 * Hiral Trivedi        UTA ID: 1001275299
 * Sayali Mohadikar     UTA ID: 1001246542
 * Ramkrishna Chivukula UTA ID: 1001239737
 */
//This is Retrival class which helps in checking and detecting if the previous node is come back alive
public class RetrievalHelper implements Runnable {

    int port;
    Thread thread;
    public static Socket socket;
    public static ServerSocket sock;
    
    public RetrievalHelper(int port){
        this.port = port;
    }
    
    @Override
    public void run() {
        try{
            Thread.sleep(1000);//1 sec
            sock = new ServerSocket(port);
            sock.setReuseAddress(true);
            socket = sock.accept();
            socket.setReuseAddress(true);
            WriteToMessageBuffer("RECONNECTED" + ";" + port);
        }
        catch(Exception ex){
            System.out.println("RetrievalHelperException: Exception occured in the run(): "+ex);
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
    //Starting the new thread in order to detect the crashed process
    public void start(){
        System.out.println("Starting the RetrievalHelper");
        System.out.println("Will start continous pings on the port: " +port + "in order to detect if the process has come back online");
        thread = new Thread(this, "RetrivalThreadForPort_" + port);
        thread.start();
    }
        
}
