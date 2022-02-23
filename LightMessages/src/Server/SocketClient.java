package Server;

import java.net.*;
import java.util.*;

import Commons.*;

public class SocketClient {
    private String socketUUID;
    private Socket socket;
    
    private ProcessRequest processingThread;


    public SocketClient(String socketUUID, Socket socket, Hashtable<String, SocketClient> sockets) {
        this.socket = socket;
        this.socketUUID = socketUUID;

        this.processingThread = new ProcessRequest(socketUUID, socket, sockets);
    }

    public void start(){
        Thread threads[] = new ThreadSafeStop[] { 
            this.processingThread
        }; 

        for(Thread thread : threads)
            thread.start();
    }

    public void stop(){
        ThreadSafeStop threads[] = new ThreadSafeStop[] { 
            this.processingThread 
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

    public String getSocketUUID() { return this.socketUUID; }

    public Socket getSocket() { return this.socket; }
        
}
