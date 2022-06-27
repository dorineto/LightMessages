package Server;

import java.net.*;
import java.util.*;
import java.security.*;

import Commons.*;

public class SocketClient {
    private String socketUUID;
    private Socket socket;
    
    private ProcessRequest requestProcessingThread;
    private ProcessSend sendProcessingThread;

    private Hashtable<String, SocketClient> socketGroup;


    public SocketClient(String socketUUID, Socket socket, Hashtable<String, SocketClient> socketGroup) {
        this.socket = socket;
        this.socketUUID = socketUUID;

        this.socketGroup = socketGroup;

        this.requestProcessingThread = new ProcessRequest(this);
        this.sendProcessingThread = new ProcessSend(this);
    }

    public void start(){
        Thread threads[] = new ThreadSafeStop[] { 
            this.requestProcessingThread
            ,this.sendProcessingThread
        }; 

        for(Thread thread : threads)
            thread.start();
    }

    public void stop(){
        ThreadSafeStop threads[] = new ThreadSafeStop[] { 
            this.requestProcessingThread 
            ,this.sendProcessingThread
        };

        for(ThreadSafeStop thread : threads){
            thread.stopRunning();
            
            while(thread.isRunning()){
                try{ 
                    Thread.sleep(2000);
                } catch (Exception ex) {}
            }
        }
    }

    public void sendCommand(CommandV2 command) {
        synchronized(sendProcessingThread){
            sendProcessingThread.enqueueCommandToSend(command);
        }
    }

    //public void 

    public String getSocketUUID() { return this.socketUUID; }

    public Socket getSocket() { return this.socket; }

    public  Hashtable<String, SocketClient> getSocketGroup() { return this.socketGroup; }

    public void exitSocketGroup() {
        synchronized(this.socketGroup){
            this.socketGroup.remove(this.socketUUID);
        }
    }

    protected boolean markAsSendCommand(byte[] sendedChecksumCommand) {
        ProcessSend.commandQueueItem sendedCommand = this.sendProcessingThread.peekCommandToSend();

        boolean isEqualChecksum = MessageDigest.isEqual(sendedChecksumCommand, sendedCommand.getCheckSum());

        if(isEqualChecksum)
            this.sendProcessingThread.dequeueCommandToSend();
        
        return isEqualChecksum;
    }
     
}
