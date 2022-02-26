package Server;

import java.net.*;
import java.io.*;
import java.util.*;

import Commons.*;

public class ProcessRequest extends ThreadSafeStop {
    private String socketUUID;
    private Socket socket;

    private Hashtable<String, SocketClient> sockets;
    private Logger logger;

    public ProcessRequest(String socketUUID, Socket socket, Hashtable<String, SocketClient> sockets){
        super("ProcessRequest - " + socketUUID);

        this.socketUUID = socketUUID;
        this.socket = socket;

        this.sockets = sockets;
        
        this.logger = Logger.getLogger();
    }

    @Override
    public void run() {
        try 
        {
            
            logger.writeLog(LogLevel.INFO, "ProcessRequest - UUID=" + socketUUID + " - Start thread");
            this.running = true;

            while (!this.stopRunning) 
            {
                try 
                {

                    if (socket.isClosed() || !socket.isConnected()) 
                    {
                        synchronized(sockets){
                            sockets.remove(socketUUID);
                        }

                        logger.writeLog(LogLevel.DEBUG, "ProcessRequest - UUID=" + socketUUID + " - socket disconnected");
                        this.stopRunning = true;
                        continue;
                    }

                    InputStream inpStream = socket.getInputStream();
                    if (inpStream.available() > 0) 
                    {

                        Command command = Command.ProcessesInputStream(inpStream, true);

                        String errorMessage = Command.ValidadeCommand(command);

                        if(errorMessage != null && !errorMessage.trim().equals("")){
                            logger.writeLog(LogLevel.DEBUG, "ProcessRequest - UUID=" + socketUUID + " - Invalid command - " + errorMessage);
                            continue;
                        }

                        logger.writeLog(LogLevel.DEBUG, "ProcessRequest - UUID=" + socketUUID + " -  Processing Command");

                        int type = Integer.parseInt(command.getContentDict().get("type"));

                        if (type == commandType.CLOSE.ordinal()) 
                        {
                            logger.writeLog(LogLevel.DEBUG, "ProcessRequest - UUID=" + socketUUID + " - Closing Socket");
                            socket.close();

                            synchronized(sockets){
                                sockets.remove(socketUUID);
                            }

                            this.stopRunning = true;
                        } 
                        else 
                        {

                            OutputStream outAux = null;

                            String otherSocketKey = null;
                            Socket otherSocketAux = null;
                            
                            Enumeration<String> otherSocketsKeys = sockets.keys();

                            while (otherSocketsKeys.hasMoreElements()) 
                            {

                                otherSocketKey = otherSocketsKeys.nextElement();
                            
                                if (sockets.get(otherSocketKey) == null)
                                    continue;
                                
                                otherSocketAux = sockets.get(otherSocketKey).getSocket();

                                try 
                                {
                                    if (!otherSocketKey.equalsIgnoreCase(socketUUID) && (!otherSocketAux.isClosed() && otherSocketAux.isConnected())) 
                                    {

                                        logger.writeLog(LogLevel.DEBUG, "ProcessRequest - UUID= "+ socketUUID + " - Sending UUID=" + otherSocketKey + ",type=" + type);
                                        
                                        outAux = otherSocketAux.getOutputStream();

                                        synchronized (outAux)
                                        {
                                            outAux.write(command.getBytes());
                                            outAux.flush();
                                        }
                                        
                                    }
                                } 
                                catch (IOException ex) 
                                {
                                    logger.writeLog(LogLevel.ERROR,"ProcessRequest - UUID= "+ socketUUID + " - Skiping Socket");
                                } 
                                catch (Exception ex)
                                {
                                    logger.writeLog(LogLevel.ERROR, "ProcessRequest - UUID=" + socketUUID + " - " + Logger.dumpException(ex));
                                }
                                    
                            }
                        }
                    }
                } 
                catch (Exception ex) 
                {
                    logger.writeLog(LogLevel.ERROR, "ProcessRequest - UUID=" + socketUUID + " - " + Logger.dumpException(ex));
                }

                try 
                {
                    Thread.sleep(2000);
                } 
                catch (Exception ex) {}

            }

            logger.writeLog(LogLevel.INFO, "ProcessRequest - UUID=" + socketUUID + " - End thread");

        } 
        catch (Exception ex) 
        {
            logger.writeLog(LogLevel.ERROR, "ProcessRequest - UUID=" + socketUUID + " - " + Logger.dumpException(ex));
        }

        this.running = false;
    }
}
