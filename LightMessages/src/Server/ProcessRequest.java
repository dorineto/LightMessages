package Server;

import Commons.*;
import java.net.*;
import java.util.*;
import java.io.*;
import java.nio.*;

public class ProcessRequest extends Thread {
    private Socket processingSocket;
    private String socketUUID;
    private Logger logger;
    private Hashtable<String,Socket> sockets;
    private boolean stopRunning;
    private boolean running;


    public ProcessRequest(Socket processingSocket, String socketUUID, Logger logger, Hashtable<String,Socket> sockets) {
        super("ProcessRequest - " + socketUUID);

        this.processingSocket = processingSocket;
        this.socketUUID = socketUUID;

        this.logger = logger;
        this.sockets = sockets;

        this.running = false;
        this.stopRunning = false;
    }

    public boolean isRunning(){ return this.running; }

    public void stopRunning(){ this.stopRunning = true; }

    public void unstopRunning(){ this.stopRunning = false; }

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

                    if (processingSocket.isClosed() || !processingSocket.isConnected()) 
                    {
                        synchronized(sockets){
                            sockets.remove(socketUUID);
                        }

                        logger.writeLog(LogLevel.DEBUG, "ProcessRequest - UUID=" + socketUUID + " - socket disconnected");
                        this.stopRunning = true;
                        continue;
                    }

                    InputStream inpStream = processingSocket.getInputStream();
                    if (inpStream.available() > 0) 
                    {
                        ArrayList<byte[]> contentChunksList = new ArrayList<byte[]>();

                        int totalBytes = 0;

                        byte[] buffer = new byte[5000];
                        int readLenght = inpStream.read(buffer, 0, buffer.length);

                        contentChunksList.add(Arrays.copyOf(buffer, readLenght));
                        totalBytes += readLenght;

                        while (inpStream.available() > 0) 
                        {
                            readLenght = inpStream.read(buffer, 0, 5000);

                            if (readLenght > -1) 
                            {
                                contentChunksList.add(Arrays.copyOf(buffer, readLenght));
                                totalBytes += readLenght;
                            }
                        }

                        ByteBuffer finalBuffer = ByteBuffer.allocate(totalBytes);

                        for (byte[] contentChunck : contentChunksList)
                            finalBuffer.put(contentChunck);

                        String commandStr = new String(finalBuffer.array(), "UTF-8");

                        String decodedCommandStr = new String(Base64.getDecoder().decode(commandStr), "UTf-8");

                        Command command = Command.parse(decodedCommandStr);

                        if (!command.getContentDict().containsKey("type")) 
                        {
                            logger.writeLog(LogLevel.DEBUG, "ProcessRequest - UUID=" + socketUUID + " - Invalid command "
                                                           +"command=" + decodedCommandStr);
                            continue;
                        }

                        logger.writeLog(LogLevel.DEBUG, "ProcessRequest - UUID=" + socketUUID + " -  Processing Command - "
                                                       +"command=" + decodedCommandStr);

                        int type = Integer.parseInt(command.getContentDict().get("type"));

                        if (type == commandType.CLOSE.ordinal()) 
                        {
                            logger.writeLog(LogLevel.DEBUG, "ProcessRequest - UUID=" + socketUUID + " - Closing Socket");
                            processingSocket.close();

                            synchronized(sockets){
                                sockets.remove(socketUUID);
                            }
                        } 
                        else 
                        {
                            // Verifica se os componentes necessários em comum de
                            // um comando de arquivo ou texto estão presentes
                            if (!command.getContentDict().containsKey("content")
                             || !command.getInfoDict().containsKey("name")
                             || !command.getInfoDict().containsKey("datetime")) 
                            {
                                logger.writeLog(LogLevel.DEBUG, "ProcessRequest - UUID=" + socketUUID + " - Invalid command "
                                                               +"command=" + decodedCommandStr);
                                continue;
                            }

                            OutputStream outAux = null;

                            String otherSocketKey = null;
                            Socket otherSocketAux = null;
                            
                            Enumeration<String> otherSocketsKeys = sockets.keys();

                            while (otherSocketsKeys.hasMoreElements()) 
                            {

                                otherSocketKey = otherSocketsKeys.nextElement();
                                otherSocketAux = sockets.get(otherSocketKey);

                                if (otherSocketAux == null)
                                    continue;

                                try 
                                {
                                    if (!otherSocketKey.equalsIgnoreCase(socketUUID) && (!otherSocketAux.isClosed() && otherSocketAux.isConnected())) 
                                    {

                                        logger.writeLog(LogLevel.DEBUG, "ProcessRequest - UUID= "+ socketUUID + " - Sending UUID=" + otherSocketKey + ",command=" + decodedCommandStr);
                                        
                                        outAux = otherSocketAux.getOutputStream();

                                        synchronized (outAux)
                                        {
                                            outAux.write(finalBuffer.array());
                                            outAux.flush();
                                        }
                                        
                                    }
                                } 
                                catch (IOException ex) 
                                {
                                    logger.writeLog(LogLevel.ERROR,"ProcessRequest - UUID= "+ socketUUID + " - Skiping Socket uuid=" + decodedCommandStr);
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
