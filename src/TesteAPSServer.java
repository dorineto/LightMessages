import java.net.*;
import java.util.*;
import java.io.*;

class TesteAPSServer{
	protected ServerSocket servSock = null;
	protected Hashtable<String, Socket> sockets = new Hashtable<String, Socket>();
	
	protected boolean running = true;
	
	public static void main(String[] args){
		new TesteAPSServer().setup();
	}
	
	public void setup(){
		try{
			servSock = new ServerSocket(4860, 1024 * 5); // Rever valores do backlog depois
			servSock.setSoTimeout(15000);
			
			new Thread(() -> {
				try
				{
					System.out.println("ThreadServerSocketAcept - Start thread");
					while(running)
					{
						try
						{
							Socket sock = servSock.accept();
							
							String uuid = UUID.randomUUID().toString();
							
							sockets.put(uuid, sock);
							
							OutputStream out = sock.getOutputStream();
							out.write(("uuid|"+uuid).getBytes("UTF-8"));
							out.flush();
						}
						catch(SocketTimeoutException ex){}
						
						try
						{
							Thread.sleep(3500);
						}
						catch(Exception ex){}
					}
					System.out.println("ThreadServerSocketAcept - End thread");
				}
				catch(Exception ex)
				{
					System.out.println("ThreadServerSocketAccept - " + ex);
					
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
					System.out.println("ThreadServerSocketProcessInput - Start thread");
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
									System.out.println("ThreadServerSocketProcessInput - Socket removed");
									continue;
								}
														
								InputStream inpStream = auxSock.getInputStream();
								if(inpStream.available() > 0)
								{
									byte[] buffer = new byte[inpStream.available()];
									
									inpStream.read(buffer);
									
									// Por enquanto deixa enviar para todos conectados, 
									// depois enviar para integrantes do chat e terá uma 
									// classe separada para processar os comandos
									String[] command = (new String(buffer, "UTF-8")).split("\\|", 2);
									if(command[0].equalsIgnoreCase("close"))
									{
										System.out.println("ThreadServerSocketProcessInput - Closing uuid="+socketKey);
										auxSock.close();
										sockets.remove(socketKey);
									}
									else
									{
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
													System.out.println("ThreadServerSocketProcessInput - "
																	  +"Sending msg="+command[1]+" uuid="+otherSocketKey);
													 
													outAux = otherSocketAux.getOutputStream();
													outAux.write(("message|"+command[1]).getBytes("UTF-8"));
													outAux.flush();
												}
											}
											catch(Exception ex)
											{
												System.out.println("ThreadServerSocketProcessInput - "+ex);
											}
										}
									}
								}
							}
							catch(Exception ex)
							{
								System.out.println("ThreadServerSocketProcessInput - "+ex);
							}
							
						}
						
						try
						{
							Thread.sleep(2500);
						}
						catch(Exception ex){}
					}
					System.out.println("ThreadServerSocketProcessInput - End thread");
				}
				catch(Exception ex)
				{
					System.out.println("ThreadServerSocketProcessInput - " + ex);
				}
			}, "ThreadServerSocketProcessInput").start();
			
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				try
				{
					System.out.println("\n\nEnding Server...\n");
					
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
									System.out.println("TesteAPSServerShutdownHook - Closing uuid="+socketEntry.getKey());
									out = socketEntry.getValue().getOutputStream();
									out.write("close".getBytes("UTF-8"));
									out.flush();
									
									socketEntry.getValue().close();
								}
							}
							catch(Exception ex)
							{
								System.out.println("TesteAPSServerShutdownHook - "+ex);
							}
							
						}
					}
				}
				catch(Exception ex)
				{
					System.out.println("TesteAPSServerShutdownHook - "+ex);
				}
				finally
				{
					try
					{
						servSock.close();
					}
					catch(Exception ex)
					{
						System.out.println("TesteAPSServerShutdownHook - "+ex);
					}
				}
			}));
			
		}
		catch(Exception ex){
			System.out.println("setup - " + ex);
			
			try
			{
				if(servSock != null && !servSock.isClosed())
					servSock.close();
			}
			catch(Exception ex2)
			{
				System.out.println("ThreadServerSocketAccept - " + ex2);
			}
		}
	}
}