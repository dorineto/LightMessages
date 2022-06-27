package Client;

import java.net.*;
import java.io.*;
import java.util.*;

import Commons.*;

public class ProcessSend extends ThreadSafeStop {
    private Logger logger;
    
    private LightMessageSocket clientSocket;

    private ArrayDeque<ICommand> commandToSend;

    public ProcessSend(LightMessageSocket clientSocket){
        super("ProcessSend");

        this.clientSocket = clientSocket;

        this.logger = Logger.getLogger();

        this.commandToSend = new ArrayDeque<ICommand>();
    }

    public boolean enqueueCommandToSend(ICommand command) {
        synchronized(this.commandToSend){
            return this.commandToSend.offer(command);
        }
    }

    public void dequeueCommandToSend() {
        synchronized(this.commandToSend){
            this.commandToSend.poll();
        }
    }

    public ICommand peekCommandToSend() {
        return this.commandToSend.peek();
    }

    @Override
    public void run(){
        this.running = true;

        Socket sock = this.clientSocket.getSocket();

        logger.writeLog(LogLevel.INFO, "SocketProcessSend - Start");
        while(!this.stopRunning){
            try{
                if(!this.stopRunning && !LightMessageSocket.tryConnection(sock)){
                    this.stopRunning = true;
                    this.clientSocket.getLigthUI().closeUI("Não foi possivel estabelecer uma conexão, tente novamente mais tarde!");
                    continue;
                }
                
                CommandV2 command = (CommandV2)this.commandToSend.peek();
                if(command != null){
                        
                    byte[] serializedCommand = command.serialize();

                    try 
                    {
                        if ((!sock.isClosed() && sock.isConnected())) 
                        {

                            CommandV2.CommandTypeV2 type = command.getType();

                            logger.writeLog(LogLevel.DEBUG, "SocketProcessSend - UUID= "+ this.clientSocket.getUniqueID() + " - type=" + type.name());
                            
                            OutputStream outAux = sock.getOutputStream();

                            outAux.write(serializedCommand);
                            outAux.flush();
                            
                            this.dequeueCommandToSend();
                        }
                    } 
                    catch (Exception ex)
                    {
                        logger.writeLog(LogLevel.ERROR, "SocketProcessSend - UUID=" + this.clientSocket.getUniqueID() + " - " + Logger.dumpException(ex));
                    }
                            
                }

            }
            catch(Exception ex){
                logger.writeLog(LogLevel.ERROR, "SocketProcessSend - " + Logger.dumpException(ex));
            }
                
            try{
                Thread.sleep(2000);
            }
            catch(Exception ex){}
        }

		this.running = false;
    }

}
