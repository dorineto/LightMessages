package Server;

import java.util.*;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.security.*;

import Commons.*;
import Commons.CommandV2.CommandTypeV2;

public class ProcessSend extends ThreadSafeStop {
    private final long acknolageTimeoutSeconds = 45;

    private SocketClient socket;

    private Logger logger;

    private ArrayDeque<commandQueueItem> commandToSendQueue;

    public ProcessSend(SocketClient socket){
        super("ProcessSend - " + socket.getSocketUUID());

        this.socket = socket;

        this.commandToSendQueue = new ArrayDeque<commandQueueItem>();
        this.logger = Logger.getLogger();
    }

    public boolean enqueueCommandToSend(ICommand commandToSend){
        try {
            commandQueueItem queueItem =  new commandQueueItem(commandToSend);
            return this.commandToSendQueue.offer(queueItem);
        }
        catch(NoSuchAlgorithmException ex){
            return false;
        }
    }

    public void dequeueCommandToSend(){
        this.commandToSendQueue.poll();
    }

    public commandQueueItem peekCommandToSend(){
        return this.commandToSendQueue.peek();
    }
    
    @Override
    public void run() {
        String socketUUID = this.socket.getSocketUUID();

        try 
        {

            logger.writeLog(LogLevel.INFO, "ProcessRequest - UUID=" + socketUUID + " - Start thread");
            this.running = true;

            while (!this.stopRunning) 
            {
                try 
                {

                    Socket socketClient = this.socket.getSocket();
                    if (socketClient.isClosed() || !socketClient.isConnected()) 
                    {
                        this.socket.exitSocketGroup();

                        logger.writeLog(LogLevel.DEBUG, "ProcessRequest - UUID=" + socketUUID + " - socket disconnected");
                        this.stopRunning = true;
                        continue;
                    }

                    commandQueueItem command = this.commandToSendQueue.peek();

                    if(command != null && command.getSendedTime() == null){
                        
                        byte[] serializedCommand = command.getCommand().serialize();

                        try 
                        {
                            if ((!socketClient.isClosed() && socketClient.isConnected())) 
                            {

                                CommandTypeV2 type = ((CommandV2)command.getCommand()).getType();

                                logger.writeLog(LogLevel.DEBUG, "ProcessRequest - UUID= "+ socketUUID + " - type=" + type.name());
                                
                                OutputStream outAux = socketClient.getOutputStream();

                                outAux.write(serializedCommand);
                                outAux.flush();

                                command.markSend();

                            }
                        } 
                        catch (Exception ex)
                        {
                            logger.writeLog(LogLevel.ERROR, "ProcessRequest - UUID=" + socketUUID + " - " + Logger.dumpException(ex));
                        }
                                
                    }
                    else if(command != null){
                        LocalDateTime sendedTime = command.getSendedTime();

                        LocalDateTime timeoutTime = LocalDateTime.now().minusSeconds(this.acknolageTimeoutSeconds);

                        if(!timeoutTime.isBefore(sendedTime))
                            this.dequeueCommandToSend();
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

    public static class commandQueueItem {
        private ICommand command;
        private LocalDateTime sendedTime;

        private byte[] checkSum;

        public commandQueueItem(ICommand command) throws NoSuchAlgorithmException 
        {
            this.command = command;
            this.sendedTime = null;

            this.checkSum = MessageDigest.getInstance("SHA-256").digest(command.serialize());
        }

        public ICommand getCommand() { return this.command; }

        public LocalDateTime getSendedTime() { return this.sendedTime; }
        public void markSend() { this.sendedTime = LocalDateTime.now(); }

        public byte[] getCheckSum() { return this.checkSum; }
    }
}
