package Server;

import java.net.*;
import java.io.*;
import java.util.*;

import Commons.*;

public class ProcessRequest extends ThreadSafeStop {
    private SocketClient socketCliente;

    private Logger logger;

    public ProcessRequest(SocketClient socket){
        super("ProcessRequest - " + socket.getSocketUUID());

        this.socketCliente = socket;
        
        this.logger = Logger.getLogger();
    }

    @Override
    public void run() {
        String socketUUID = this.socketCliente.getSocketUUID();

        try 
        {
            
            logger.writeLog(LogLevel.INFO, "ProcessRequest - UUID=" + socketUUID + " - Start thread");
            this.running = true;

            Socket socket = this.socketCliente.getSocket();

            while (!this.stopRunning) 
            {
                try 
                {

                    if (socket.isClosed() || !socket.isConnected()) 
                    {
                        this.socketCliente.exitSocketGroup();

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

                            socketCliente.exitSocketGroup();

                            this.stopRunning = true;
                        }
                        else if (type == CommandV2.CommandTypeV2.ACK) {
                            byte[] sendedChecksum = command.getContent()[0].array();

                            boolean markedSend = socketCliente.markAsSendCommand(sendedChecksum);

                            String logMessage = "Acknowledged command";

                            if(!markedSend)
                                logMessage = "Not acknowledged command - checksum don't match";

                            logger.writeLog(LogLevel.DEBUG, "ProcessRequest - UUID=" + socketUUID + " - " + logMessage);
                        }
                        else 
                        {

                            String otherSocketKey = null;
                            SocketClient otherSocketAux = null;
                            
                            Enumeration<String> otherSocketsKeys = this.socketCliente.getSocketGroup().keys();

                            Hashtable<String, SocketClient> sockets = this.socketCliente.getSocketGroup();

                            while (otherSocketsKeys.hasMoreElements()) 
                            {

                                otherSocketKey = otherSocketsKeys.nextElement();
                            
                                if (sockets.get(otherSocketKey) == null)
                                    continue;
                                
                                otherSocketAux = sockets.get(otherSocketKey);

                                try 
                                {
                                    if (!otherSocketKey.equalsIgnoreCase(socketUUID) && (!otherSocketAux.getSocket().isClosed() && otherSocketAux.getSocket().isConnected())) 
                                    {

                                        logger.writeLog(LogLevel.DEBUG, "ProcessRequest - UUID= "+ socketUUID + " - Sending UUID=" + otherSocketKey + ",type=" + type);
                                        
                                        otherSocketAux.sendCommand(command);
                                        
                                    }
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
