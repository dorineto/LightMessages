package Server;

import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.io.*;
import Commons.*;

class LightMessageSocketServer{
	protected ServerSocket servSock = null;
	protected Hashtable<String, Socket> sockets = new Hashtable<String, Socket>();
	
	protected boolean running = true;
	
	protected Hashtable<String, String> config;
	protected Logger logger;
	
	public static void main(String[] args){
		new LightMessageSocketServer().setup();
	}
	
	public void setup(){
		try{
			config = ConfigLoader.loadConfig("../config/serverconfig.config", true);
			
			if(!config.containsKey("logpath"))
				throw new Exception("setup - logpath not configure");
			
			logger = new Logger(config.get("logpath"));
			
			/*
			config.put("serverport", "4860");
			config.put("maxconnectionquantity", "1024");
			*/
			
			if(!config.containsKey("serverport"))
				throw new Exception("setup - serverport not configure");
			else if(!config.containsKey("maxconnectionquantity"))
				throw new Exception("setup - maxconnectionquantity not configure");
				
			int port = Integer.parseInt(config.get("serverport"));
			int connectionQuantity = Integer.parseInt(config.get("maxconnectionquantity"));
			
			servSock = new ServerSocket(port, connectionQuantity);
			servSock.setSoTimeout(15000);
			
			new Thread(() -> {
				try
				{
					logger.writeLog(LogLevel.INFO, "ThreadServerSocketAcept - Start thread");
					while(running)
					{
						try
						{
							Socket sock = servSock.accept();
							
							String uuid = UUID.randomUUID().toString();
							
							sockets.put(uuid, sock);
							
							logger.writeLog(LogLevel.DEBUG, "ThreadServerSocketAcept - New Socket Accepted uuid="+uuid);
							
							String commandStr = "type="+commandType.UUID.ordinal()+"|content="+uuid;
							
							commandStr = Base64.getEncoder().encodeToString(commandStr.getBytes("UTF-8"));

							OutputStream out = sock.getOutputStream();
							out.write(commandStr.getBytes("UTF-8"));
							out.flush();
						}
						catch(SocketTimeoutException ex){}
						
						try
						{
							Thread.sleep(3500);
						}
						catch(Exception ex){}
					}
					logger.writeLog(LogLevel.INFO, "ThreadServerSocketAcept - End thread");
				}
				catch(Exception ex)
				{
					logger.writeLog(LogLevel.ERROR, "ThreadServerSocketAccept - " + ex);
					
					running = false;
					
					try{
						if(servSock != null && !servSock.isClosed())
							servSock.close();
					}
					catch(Exception ex2){}
				}
			}, "ThreadServerSocketAccept").start();
			
			new Thread( () -> {
				try{
					logger.writeLog(LogLevel.INFO, "ThreadServerSocketProcessInput - Start thread");
					while(running){
						
						Socket auxSock = null;
						
						Enumeration<String> socketsKeys = sockets.keys();
						while(socketsKeys.hasMoreElements())
						{
							String socketKey = socketsKeys.nextElement();
							
							auxSock = sockets.get(socketKey);
							if(auxSock == null)
								continue;
							
							try
							{
								
								if(auxSock.isClosed() || !auxSock.isConnected())
								{
									sockets.remove(socketKey);
									logger.writeLog(LogLevel.DEBUG, "ThreadServerSocketProcessInput - Socket removed uiid="+socketKey);
									continue;
								}
														
								InputStream inpStream = auxSock.getInputStream();
								if(inpStream.available() > 0)
								{
									ArrayList<byte[]> contentChunksList = new ArrayList<byte[]>();

									int totalBytes = 0;

									byte[] buffer = new byte[5000];
									int readLenght = inpStream.read(buffer, 0, buffer.length);

									contentChunksList.add(Arrays.copyOf(buffer, readLenght));
									totalBytes += readLenght;

									while( inpStream.available() > 0 )
									{
										readLenght = inpStream.read(buffer, 0, 5000);

										if(readLenght > -1){
											contentChunksList.add(Arrays.copyOf(buffer, readLenght));
											totalBytes += readLenght;
										}
									}

									ByteBuffer finalBuffer = ByteBuffer.allocate(totalBytes);

									for (byte[] contentChunck :  contentChunksList)
										finalBuffer.put(contentChunck);

									String commandStr = new String(finalBuffer.array(), "UTF-8");
									
									String decodedCommandStr = new String(Base64.getDecoder().decode(commandStr), "UTf-8");

									Command command = Command.parse(decodedCommandStr);
									
									if(!command.getContentDict().containsKey("type"))
									{
										logger.writeLog(LogLevel.DEBUG, "ThreadServerSocketProcessInput - Invalid command uiid="+socketKey+",command="+decodedCommandStr);
										continue;
									}
									
									logger.writeLog(LogLevel.DEBUG, "ThreadServerSocketProcessInput - Processing uiid="+socketKey+",command="+decodedCommandStr);
									
									int type = Integer.parseInt(command.getContentDict().get("type"));
									
									if(type == commandType.CLOSE.ordinal())
									{
										logger.writeLog(LogLevel.DEBUG, "ThreadServerSocketProcessInput - Closing uuid="+socketKey);
										auxSock.close();
										sockets.remove(socketKey);
									}
									else
									{
										// Verifica se os componentes necessários em comum de 
										// um comando de arquivo ou texto estão presentes  
										if(!command.getContentDict().containsKey("content") 
										 ||!command.getInfoDict().containsKey("name")
										 ||!command.getInfoDict().containsKey("datetime")){
											logger.writeLog(LogLevel.DEBUG, "ThreadServerSocketProcessInput - Invalid command uiid="+socketKey+",command="+decodedCommandStr);
											continue;
										 }
										
										OutputStream outAux = null;
									
										String otherSocketKey = null;
										Socket otherSocketAux = null;
										
										Enumeration<String> otherSocketsKeys = sockets.keys();
										while(otherSocketsKeys.hasMoreElements())
										{
											otherSocketKey = otherSocketsKeys.nextElement();
											otherSocketAux = sockets.get(otherSocketKey);
											
											if(otherSocketAux == null)
												continue;
											
											try
											{
												if(!otherSocketKey.equalsIgnoreCase(socketKey)
												 && (!otherSocketAux.isClosed() && otherSocketAux.isConnected()))
												{
													logger.writeLog(LogLevel.DEBUG, "ThreadServerSocketProcessInput - Sending uuid="+ otherSocketKey+",command="+decodedCommandStr);
													 
													outAux = otherSocketAux.getOutputStream();
													outAux.write(finalBuffer.array());
													outAux.flush();
												}
											}
											catch(IOException ex){
												logger.writeLog(LogLevel.ERROR, "ThreadServerSocketProcessInput - Removing Socket uuid="+decodedCommandStr);
												sockets.remove(otherSocketKey);
											}
											catch(Exception ex)
											{
												logger.writeLog(LogLevel.ERROR, "ThreadServerSocketProcessInput - " + Logger.dumpException(ex));
												ex.printStackTrace();
											}
										}
									}
								}
							}
							catch(Exception ex)
							{
								logger.writeLog(LogLevel.ERROR, "ThreadServerSocketProcessInput - " + Logger.dumpException(ex));
								ex.printStackTrace();
							}
							
						}
						
						try
						{
							Thread.sleep(2500);
						}
						catch(Exception ex){}
					}
					logger.writeLog(LogLevel.INFO, "ThreadServerSocketProcessInput - End thread");
				}
				catch(Exception ex)
				{
					logger.writeLog(LogLevel.ERROR, "ThreadServerSocketProcessInput - " + Logger.dumpException(ex));
					ex.printStackTrace();
				}
			}, "ThreadServerSocketProcessInput").start();
			
		}
		catch(Exception ex){
			if(logger == null)
				System.out.println("setup - " + ex);
			else
				logger.writeLog(LogLevel.ERROR, "setup - " + Logger.dumpException(ex));
				
			try
			{
				if(servSock != null && !servSock.isClosed())
					servSock.close();
			}
			catch(Exception ex2)
			{
				if(logger == null)
					System.out.println("setup - " + Logger.dumpException(ex2));
				else
					logger.writeLog(LogLevel.ERROR, "setup - " + Logger.dumpException(ex2));
			}
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try
			{
				if(logger == null)
					System.out.println("\n\nEnding Server...\n");
				else
					logger.writeLog(LogLevel.INFO, "Ending Server...");
				
				if(sockets.size() > 0){
					OutputStream out = null;
					
					Iterator<Map.Entry<String, Socket>> socketsEntrys = sockets.entrySet().iterator();
					Map.Entry<String, Socket> socketEntry = null;
					
					while(socketsEntrys.hasNext())
					{
						socketEntry = socketsEntrys.next();
						
						try
						{								
							if(!socketEntry.getValue().isClosed() && socketEntry.getValue().isConnected())
							{
								if(logger == null)
									System.out.println("LightMessageSocketServerShutdownHook - Closing uuid="+socketEntry.getKey());
								else
									logger.writeLog(LogLevel.DEBUG, "LightMessageSocketServerShutdownHook - Closing uuid="+socketEntry.getKey());
								
								String commandStr = "type="+commandType.CLOSE.ordinal();
								
								out = socketEntry.getValue().getOutputStream();
								out.write(commandStr.getBytes("UTF-8"));
								out.flush();
								
								socketEntry.getValue().close();
							}
						}
						catch(IOException ex){ }
						catch(Exception ex)
						{
							if(logger == null)
								System.out.println("LightMessageSocketServerShutdownHook - " + Logger.dumpException(ex));
							else
								logger.writeLog(LogLevel.ERROR, "LightMessageSocketServerShutdownHook - " + Logger.dumpException(ex));
						}
						
					}
				}
			}
			catch(Exception ex)
			{
				if(logger == null)
					System.out.println("LightMessageSocketServerShutdownHook - " + Logger.dumpException(ex));
				else
					logger.writeLog(LogLevel.ERROR, "LightMessageSocketServerShutdownHook - " + Logger.dumpException(ex));
			}
			finally
			{
				try
				{
					servSock.close();
				}
				catch(Exception ex)
				{
					if(logger == null)
						System.out.println("LightMessageSocketServerShutdownHook - "+ex);
					else
						logger.writeLog(LogLevel.ERROR, "LightMessageSocketServerShutdownHook - " + Logger.dumpException(ex));
				}
				
				if(logger != null)
					logger.close();
				
			}
		}));
	}
}