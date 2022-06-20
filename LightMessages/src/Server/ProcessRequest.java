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
                        int availableBytesRead = inpStream.available();

                        CommandV2 command;
                        try
                        {
                            command = CommandV2.processesInputStream(inpStream);
                        }
                        catch(IllegalArgumentException ex){
                            logger.writeLog(LogLevel.DEBUG, "ProcessRequest - UUID=" + socketUUID + " - Invalid command - " + ex.getMessage());
                            inpStream.skip(availableBytesRead);
                            continue;
                        }

                        if(command == null){
                            logger.writeLog(LogLevel.DEBUG, "ProcessRequest - UUID=" + socketUUID + " - Invalid command");
                            inpStream.skip(availableBytesRead);
                            continue;
                        }

                        logger.writeLog(LogLevel.DEBUG, "ProcessRequest - UUID=" + socketUUID + " -  Processing Command");

                        CommandV2.CommandTypeV2 type = command.getType();

                        if (type == CommandV2.CommandTypeV2.CLOSE) 
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

                            byte[] serializedCommand = command.serialize();

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
                                            outAux.write(serializedCommand);
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

                        if(!socket.isClosed())
                        {
                            availableBytesRead = inpStream.available();
                            if(availableBytesRead > 0)
                                inpStream.skip(availableBytesRead);
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
