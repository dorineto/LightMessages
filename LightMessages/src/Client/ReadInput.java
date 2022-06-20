package Client;

import java.net.*;
import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.nio.*;

import Commons.*;

public class ReadInput extends ThreadSafeStop {
    private Socket sock;
    private Logger logger;
    private String uniqueID;
    private LightMessageUI ligthUI;

    public ReadInput(Socket sock, LightMessageUI ligthUI){
        super("ReadInput");

        this.sock = sock;
        this.ligthUI = ligthUI;

        this.logger = Logger.getLogger();
    }

    @Override
    public void run(){
        this.running = true;

        logger.writeLog(LogLevel.INFO, "SocketReadInput - Start");
        while(!this.stopRunning){
            try{
                if(!this.stopRunning && !LightMessageSocket.tryConnection(sock)){
                    this.stopRunning = true;
                    ligthUI.closeUI("Não foi possivel estabelecer uma conexão, tente novamente mais tarde!");
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

                    if(type == CommandV2.CommandTypeV2.UUID) // UUID
                    {
                        uniqueID = UUID.nameUUIDFromBytes(command.getContent()[0].array()).toString();
                        logger.writeLog(LogLevel.DEBUG, "SocketReadInput - Recived UUID uuid=" + uniqueID);
                    }
                    else if(type == CommandV2.CommandTypeV2.CLOSE) // CLOSE
                    {
                        logger.writeLog(LogLevel.ERROR, "SocketReadInput - Server Socket closed");
                        this.stopRunning = true;
                        ligthUI.closeUI("SocketServer foi fechada!");			
                    }
                    else if(type == CommandV2.CommandTypeV2.TEXT) // TEXT
                    {
                        String name = command.getUsername();

                            LocalDateTime datetime = LocalDateTime.ofInstant(Instant.ofEpochMilli(command.getTimestamp()), ZoneId.systemDefault());
                            
                            String content = "";
                            for(ByteBuffer chunks : command.getContent())
                                content += new String(chunks.array(), "UTF8");

                            ligthUI.createNewMessage(name, datetime, content, MessageDirection.RECEIVING, CommandV2.CommandTypeV2.TEXT);
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
                            
                            ligthUI.createNewMessage(name, datetime, path + filename, MessageDirection.RECEIVING, CommandV2.CommandTypeV2.FILE);
                    }
                    else
                    {
                        logger.writeLog(LogLevel.ERROR, "SocketReadInput - Unknown command type=" + type);
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

    public String getUniqueID() { return this.uniqueID; }
}
