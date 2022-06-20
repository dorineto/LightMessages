package Server;

import Commons.*;

import java.net.*;
import java.nio.ByteBuffer;
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

        this.logger = Logger.getLogger();
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

                    ByteBuffer[] content = new ByteBuffer[] { 
                        ByteBuffer.wrap(uuid.getBytes("UTF8"))
                    };
                    
                    CommandV2 command = new CommandV2(CommandV2.CommandTypeV2.UUID,  content);

                    OutputStream out = sock.getOutputStream();
                    out.write(command.serialize());
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
            } catch (Exception ex2) {}
        }

        this.running = false;
    }
    
}
