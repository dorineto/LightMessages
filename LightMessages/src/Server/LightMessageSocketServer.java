package Server;

import java.net.*;
import java.util.*;
import java.io.*;
import Commons.*;

class LightMessageSocketServer {
	protected ServerSocket servSock = null;
	protected Hashtable<String, SocketClient> sockets = new Hashtable<String, SocketClient>();

	protected Hashtable<String, String> config;
	protected Logger logger;

	protected ServerSocketAccept serverSocketAccept;

	public static void main(String[] args) {
		new LightMessageSocketServer().setup();
	}

	public void setup() {
		try {
			config = ConfigLoader.loadConfig("../config/serverconfig.config", true);

			if (!config.containsKey("logpath"))
				throw new Exception("setup - logpath not configure");

			logger = Logger.getLogger(config.get("logpath"));
			if(logger == null){
				System.out.println("setup - was no possible to create a logger");
				System.exit(1);
			}
				

			if (!config.containsKey("serverport"))
				throw new Exception("setup - serverport not configure");
			else if (!config.containsKey("maxconnectionquantity"))
				throw new Exception("setup - maxconnectionquantity not configure");

			int port = Integer.parseInt(config.get("serverport"));
			int connectionQuantity = Integer.parseInt(config.get("maxconnectionquantity"));

			servSock = new ServerSocket(port, connectionQuantity);
			servSock.setSoTimeout(15000);

			this.serverSocketAccept = new ServerSocketAccept(servSock, sockets);
			this.serverSocketAccept.start();

		} catch (Exception ex) {
			try {
				logger.writeLog(LogLevel.ERROR, "setup - " + Logger.dumpException(ex));

				if (servSock != null && !servSock.isClosed())
					servSock.close();
				
			} catch (Exception ex2) {
				try{
					logger.writeLog(LogLevel.ERROR, "setup - " + Logger.dumpException(ex2));
				}catch(Exception ex3){}
			}
		}

		Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHook()));
	}

	protected class ShutdownHook implements Runnable{
		@Override
		public void run(){
			try {
				logger.writeLog(LogLevel.INFO, "Ending Server...");
				
				serverSocketAccept.stopRunning();
				while(serverSocketAccept.isRunning()){
					try{
						Thread.sleep(2000);
					} catch (Exception ex) {}
				}
					
				if (sockets.size() > 0) {
					OutputStream out = null;

					Iterator<Map.Entry<String, SocketClient>> socketsEntrys = sockets.entrySet().iterator();
					Map.Entry<String, SocketClient> socketEntry = null;
					Socket auxSocket = null;

					while (socketsEntrys.hasNext()) {
						socketEntry = socketsEntrys.next();
						auxSocket = socketEntry.getValue().getSocket();

						try {
							if (!auxSocket.isClosed() && auxSocket.isConnected()) {
								logger.writeLog(LogLevel.DEBUG, "LightMessageSocketServerShutdownHook - Closing uuid=" + socketEntry.getKey());

								String commandStr = "type=" + commandType.CLOSE.ordinal();

								out = auxSocket.getOutputStream();
								out.write(commandStr.getBytes("UTF-8"));
								out.flush();

								auxSocket.close();
							}
						} catch (IOException ex) {} 
						catch (Exception ex) {
							logger.writeLog(LogLevel.ERROR,"LightMessageSocketServerShutdownHook - " + Logger.dumpException(ex));
						}

					}
				}
			} catch (Exception ex) {
				logger.writeLog(LogLevel.ERROR, "LightMessageSocketServerShutdownHook - " + Logger.dumpException(ex));
			} finally {
				try {
					servSock.close();
				} catch (Exception ex) {
					logger.writeLog(LogLevel.ERROR, "LightMessageSocketServerShutdownHook - " + Logger.dumpException(ex));
				}

				logger.close();
			}
		}
	}
}