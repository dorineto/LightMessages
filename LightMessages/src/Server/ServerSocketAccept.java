package Server;

import Commons.*;
import java.net.*;
import java.util.*;
import java.io.*;

public class ServerSocketAccept extends Thread {
    private ServerSocket servSock;
    private Hashtable<String, Socket> sockets;
    //private ArrayList<ProcessRequest> sockets; // TODO: Create a object that will have the socket and the processing thread together
    private Logger logger;
    public boolean stopRunning;
    public boolean running;
    

    public ServerSocketAccept(ServerSocket servSock, Hashtable<String, Socket> sockets, Logger logger){
        super("ServerSocketAccept");

        this.servSock = servSock;
        this.sockets = sockets;
        this.logger = logger;
        this.running = false;
        this.stopRunning = false;
    }

    public boolean isRunning(){ return this.running; }

    public void stopRunning(){ this.stopRunning = true; }

    public void unstopRunning(){ this.stopRunning = false; }

    @Override
    public void run(){
        try {
            this.running = true;

            logger.writeLog(LogLevel.INFO, "ServerSocketAccept - Start thread");
            while (!stopRunning) {
                try {
                    Socket sock = servSock.accept();

                    String uuid = UUID.randomUUID().toString();
                    
                    synchronized(sockets){
                        sockets.put(uuid, sock);
                    }
                    
                    logger.writeLog(LogLevel.DEBUG, "ServerSocketAccept - New Socket Accepted uuid=" + uuid);

                    String commandStr = "type=" + commandType.UUID.ordinal() + "|content=" + uuid;

                    commandStr = Base64.getEncoder().encodeToString(commandStr.getBytes("UTF-8"));

                    OutputStream out = sock.getOutputStream();
                    out.write(commandStr.getBytes("UTF-8"));
                    out.flush();

                    Thread newProcessRequest = new Thread(new ProcessRequest(sock, uuid, logger, sockets), "ProcessRequest - " + uuid);
                    newProcessRequest.start();

                    logger.writeLog(LogLevel.DEBUG, "ServerSocketAccept - New thread setup uuid=" + uuid);

                } catch (SocketTimeoutException ex) {
                }

                try {
                    Thread.sleep(3500);
                } catch (Exception ex) {
                }
            }
            logger.writeLog(LogLevel.INFO, "ServerSocketAccept - End thread");
        } catch (Exception ex) {
            logger.writeLog(LogLevel.ERROR, "ServerSocketAccept - " + ex);

            try {
                if (servSock != null && !servSock.isClosed())
                    servSock.close();
            } catch (Exception ex2) {
            }
        }

        this.running = false;
    }
    
}
