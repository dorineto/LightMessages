package Client;

import java.net.*;
import java.io.*;
import java.nio.*;
import java.util.*;
import java.time.LocalDateTime;

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

        try{
            this.logger = Logger.getLogger();
        }
        catch(Exception ex){
            throw new RuntimeException("ReadInput - Ex: " + Logger.dumpException(ex));
        }
    }

    public String getUniqueID() { return this.uniqueID; }

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
                    ArrayList<byte[]> contentChuncks = new ArrayList<byte[]>();
                    int totalBytes = 0;

                    byte[] buffer = new byte[5000];

                    int readLenght = inpStr.read(buffer, 0, buffer.length);
                    
                    contentChuncks.add(Arrays.copyOf(buffer, readLenght));
                    totalBytes += readLenght;

                    while(inpStr.available() > 0){
                        readLenght = inpStr.read(buffer, 0, 5000);

                        if(readLenght > -1){									
                            contentChuncks.add(Arrays.copyOf(buffer, readLenght));
                            totalBytes += readLenght;
                        }
                    }

                    ByteBuffer finalBuffer = ByteBuffer.allocate(totalBytes);

                    for(byte[] contentChunck : contentChuncks)
                        finalBuffer.put(contentChunck);

                    String commandStr = new String(finalBuffer.array(), "UTF-8");

                    commandStr = new String(Base64.getDecoder().decode(commandStr), "UTF-8");
                    
                    Command command = Command.parse(commandStr);
                    
                    if(!command.getContentDict().containsKey("type"))
                    {
                        logger.writeLog(LogLevel.ERROR, "SocketReadInput - Invalid command command="+commandStr);
                        continue;
                    }
                    
                    logger.writeLog(LogLevel.DEBUG, "SocketReadInput - processing command command="+commandStr);
                    
                    int type = Integer.parseInt( command.getContentDict().get("type") );
                    
                    switch(type){
                        case 2: { // UUID
                            if(!command.getContentDict().containsKey("content"))
                            {
                                logger.writeLog(LogLevel.ERROR, "SocketReadInput - Invalid command command="+commandStr);
                                continue;
                            }
                            
                            uniqueID = command.getContentDict().get("content");
                            logger.writeLog(LogLevel.DEBUG, "SocketReadInput - Recived UUID uuid="+uniqueID);
                        }
                        break;
                        case 3: { // CLOSE
                            logger.writeLog(LogLevel.ERROR, "SocketReadInput - Server Socket closed command="+commandStr);
                            this.stopRunning = true;
                            ligthUI.closeUI("SocketServer foi fechada!");									
                        }
                        break;
                        case 0: { // TEXT
                            String name = command.getInfoDict().get("name");
                            LocalDateTime datetime = LocalDateTime.parse(command.getInfoDict().get("datetime"));
                            
                            ligthUI.createNewMessage(name, datetime, command.getContentDict().get("content"), MessageDirection.RECEIVING, commandType.TEXT);
                        }
                        break;
                        case 1: { // FILE
                            if(!command.getContentDict().containsKey("filename"))
                            {
                                logger.writeLog(LogLevel.ERROR, "SocketReadInput - Invalid command command="+commandStr);
                                continue;
                            }
                            
                            String filename = command.getContentDict().get("filename");
                            
                            filename = ProcessInput.saveFile(filename, command.getContentDict().get("content"));
                            
                            if(filename.isEmpty()){
                                logger.writeLog(LogLevel.ERROR, "SocketReadInput - Não foi possivel salvar o arquivo enviado"); 
                                continue;
                            }
                            
                            String name = command.getInfoDict().get("name");
                            LocalDateTime datetime = LocalDateTime.parse(command.getInfoDict().get("datetime"));
                            
                            String path = LightMessageSocket.downloadPath;
                            
                            ligthUI.createNewMessage(name, datetime, path + filename, MessageDirection.RECEIVING, commandType.FILE);
                        }
                        break;
                        default:
                            logger.writeLog(LogLevel.ERROR, "SocketReadInput - Unknown command type command="+commandStr);
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
}
