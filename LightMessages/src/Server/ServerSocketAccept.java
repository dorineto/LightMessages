package Server;

import Commons.*;
import java.net.*;
import java.util.*;
import java.io.*;

public class ServerSocketAccept extends ThreadSafeStop {
    private ServerSocket servSock;
    private Hashtable<String, SocketClient> sockets;
    private Logger logger;

    public ServerSocketAccept(ServerSocket servSock, Hashtable<String, SocketClient> sockets){
        super("ServerSocketAccept");

        this.servSock = servSock;
        this.sockets = sockets;

        try{
            this.logger = Logger.getLogger();
        }
        catch(Exception ex){
            throw new RuntimeException("ServerSocketAccept - Ex: " + Logger.dumpException(ex));
        }
    }

    public void run(){
        try {
            this.running = true;

            logger.writeLog(LogLevel.INFO, "ServerSocketAccept - Start thread");
            while (!stopRunning) {
                try {
                    Socket sock = servSock.accept();

                    String uuid = UUID.randomUUID().toString();
                    
                    logger.writeLog(LogLevel.DEBUG, "ServerSocketAccept - New Socket Accepted uuid=" + uuid);

                    String commandStr = "type=" + commandType.UUID.ordinal() + "|content=" + uuid;

                    commandStr = Base64.getEncoder().encodeToString(commandStr.getBytes("UTF-8"));

                    OutputStream out = sock.getOutputStream();
                    out.write(commandStr.getBytes("UTF-8"));
                    out.flush();

                    SocketClient socketClient = new SocketClient(uuid, sock, sockets);
                    socketClient.start();

                    synchronized(sockets){
                        sockets.put(uuid, socketClient);
                    }

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
