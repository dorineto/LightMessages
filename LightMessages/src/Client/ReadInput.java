package Client;

import java.net.*;
import java.io.*;
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
                    
                    Command command = Command.ProcessesInputStream(inpStr);
                    
                    String errorMessage = Command.ValidadeCommand(command);

                    if(errorMessage != null && !errorMessage.trim().equals("")){
                        logger.writeLog(LogLevel.ERROR, "SocketReadInput - Invalid command - " + errorMessage);
                        continue;
                    }

                    int type = Integer.parseInt( command.getContentDict().get("type") );

                    logger.writeLog(LogLevel.DEBUG, "SocketReadInput - processing command - type = " + type);
                    
                    switch(type){
                        case 2: { // UUID
                            uniqueID = command.getContentDict().get("content");
                            logger.writeLog(LogLevel.DEBUG, "SocketReadInput - Recived UUID uuid=" + uniqueID);
                        }
                        break;
                        case 3: { // CLOSE
                            logger.writeLog(LogLevel.ERROR, "SocketReadInput - Server Socket closed");
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
                            String filename = command.getContentDict().get("filename");
                            
                            filename = ProcessInput.saveFile(filename, command.getContentDict().get("content"));
                            
                            if(errorMessage != null && !errorMessage.trim().equals("")){
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
