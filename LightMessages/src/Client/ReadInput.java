package Client;

import java.net.*;
import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.nio.*;
import java.security.MessageDigest;

import javax.xml.bind.DatatypeConverter;

import Commons.*;
import Commons.CommandV2.CommandTypeV2;

public class ReadInput extends ThreadSafeStop {
    private LightMessageSocket clientSocket;

    private Logger logger;

    public ReadInput(LightMessageSocket clientSocket){
        super("ReadInput");

        this.clientSocket = clientSocket;

        this.logger = Logger.getLogger();
    }

    @Override
    public void run(){
        this.running = true;

        Socket sock = this.clientSocket.getSocket();

        logger.writeLog(LogLevel.INFO, "SocketReadInput - Start");
        while(!this.stopRunning){
            try{
                if(!this.stopRunning && !LightMessageSocket.tryConnection(sock)){
                    this.stopRunning = true;
                    this.clientSocket.getLigthUI().closeUI("Não foi possivel estabelecer uma conexão, tente novamente mais tarde!");
                    continue;
                }
                
                InputStream inpStr = sock.getInputStream();
                if(inpStr.available() > 0){
                    
                    CommandV2 command = null;
                    try
                    {
                        command = CommandV2.processesInputStream(inpStr);
                    }
                    catch(IllegalArgumentException ex){
                        logger.writeLog(LogLevel.ERROR, "SocketReadInput - Invalid command - " + ex.getMessage());
                        continue;
                    }

                    if(command == null){
                        logger.writeLog(LogLevel.ERROR, "SocketReadInput - Invalid command");
                        continue;
                    }   

                    CommandV2.CommandTypeV2 type = command.getType();

                    logger.writeLog(LogLevel.DEBUG, "SocketReadInput - processing command - type = " + type.name());

                    boolean sendAcknowledge = false;
                    if(type == CommandV2.CommandTypeV2.UUID) // UUID
                    {
                        
                        String uniqueID = UUID.nameUUIDFromBytes(command.getContent()[0].array()).toString();

                        this.clientSocket.setUniqueID(uniqueID);

                        logger.writeLog(LogLevel.DEBUG, "SocketReadInput - Recived UUID uuid=" + uniqueID);
                    }
                    else if(type == CommandV2.CommandTypeV2.CLOSE) // CLOSE
                    {
                        logger.writeLog(LogLevel.ERROR, "SocketReadInput - Server Socket closed");
                        this.stopRunning = true;
                        this.clientSocket.getLigthUI().closeUI("SocketServer foi fechada!");			
                    }
                    else if(type == CommandV2.CommandTypeV2.TEXT) // TEXT
                    {
                        String name = command.getUsername();

                        LocalDateTime datetime = LocalDateTime.ofInstant(Instant.ofEpochMilli(command.getTimestamp()), ZoneId.systemDefault());
                        
                        String content = "";
                        for(ByteBuffer chunks : command.getContent())
                            content += new String(chunks.array(), "UTF8");

                        this.clientSocket.getLigthUI().createNewMessage(name, datetime, content, MessageDirection.RECEIVING, CommandV2.CommandTypeV2.TEXT);

                        sendAcknowledge = true;
                    }
                    else if(type == CommandV2.CommandTypeV2.FILE) // FILE
                    {
                        String filename = command.getFileInfo().getFilename();
                            
                        filename = ProcessInput.saveFile(filename, command.getContent());
                        
                        if(filename.trim().equals("")){
                            logger.writeLog(LogLevel.ERROR, "SocketReadInput - Não foi possivel salvar o arquivo enviado"); 
                            continue;
                        }
                        
                        String name = command.getUsername();
                        LocalDateTime datetime = LocalDateTime.ofInstant(Instant.ofEpochMilli(command.getTimestamp()), ZoneId.systemDefault());
                        
                        String path = LightMessageSocket.downloadPath;
                        
                        this.clientSocket.getLigthUI().createNewMessage(name, datetime, path + filename, MessageDirection.RECEIVING, CommandV2.CommandTypeV2.FILE);

                        sendAcknowledge = true;
                    }
                    else
                    {
                        logger.writeLog(LogLevel.ERROR, "SocketReadInput - Unknown command type=" + type);
                    }

                    if(sendAcknowledge){
                        ByteBuffer[] recivedCommandChecksum = new ByteBuffer[] {
                            ByteBuffer.wrap(MessageDigest.getInstance("SHA-256").digest(command.serialize()))
                        };

                        this.clientSocket.processesSending(null, recivedCommandChecksum, CommandTypeV2.ACK);
                    }
                }
            }
            catch(Exception ex){
                logger.writeLog(LogLevel.ERROR, "SocketReadInput - " + Logger.dumpException(ex));
            }
                
            try{
                Thread.sleep(2000);
            }
            catch(Exception ex){}
        }

		this.running = false;
    }

    //public String getUniqueID() { return this.uniqueID; }
}
