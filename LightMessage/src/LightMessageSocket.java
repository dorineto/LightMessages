import java.net.*;
import java.io.*;
import java.util.Base64;
import java.util.Hashtable;
import java.time.LocalDateTime;

public class LightMessageSocket{
	protected LightMessageUI ligthUI;
	
	protected Socket sock = null;
	protected boolean running = true;
	protected InetSocketAddress address = null;
	protected String uniqueID = "";
	
	protected Hashtable<String, String> config;
	protected Logger logger;
	
	public LightMessageSocket(LightMessageUI ligthUI){
		this.ligthUI = ligthUI;
	}
	
	public static boolean tryConnection(Socket sock, InetSocketAddress address, Logger logger){
		int trys = 0;
		while(trys < 3 && !sock.isConnected()){
			try{
				sock.connect(address, 15000);
			}
			catch(SocketTimeoutException ex){
				logger.writeLog(LogLevel.ERROR, "tryConnection - Socket timout connection - try="+(trys + 1));
				trys++;
			}
			catch(Exception ex){
				logger.writeLog(LogLevel.ERROR, "tryConnection - " + ex);
				break;
			}
		}
		
		return sock.isConnected();
	}
	
	public void setup() throws Exception {
		try{
			config = ConfigLoader.loadConfig("../config/config.config", false);
			
			if(!config.containsKey("logpath"))
				throw new Exception("Socket Setup - logpath não está configurado");
			
			logger = new Logger(config.get("logpath"));
			
			if(!config.containsKey("serveraddress"))
				throw new Exception("Socket Setup - serveraddress não está configurado");
			else if(!config.containsKey("serveraddressport"))
				throw new Exception("Socket Setup - serveraddressport não está configurado");
			
			sock = new Socket();
			
			int port = Integer.parseInt(config.get("serveraddressport"));
			
			address = new InetSocketAddress(InetAddress.getByName(config.get("serveraddress")), port);
			
			if(!LightMessageSocket.tryConnection(sock, address, logger))
				throw new Exception("\nSocket Setup - Não foi possivel estabelecer uma conexão, tente novamente mais tarde!\n");
			
			new Thread( () -> {
				logger.writeLog(LogLevel.INFO, "SocketReadInput - Start");
				while(running){
					try{
						if(running && !LightMessageSocket.tryConnection(sock, address, logger)){
							running = false;
							ligthUI.closeUI("Não foi possivel estabelecer uma conexão, tente novamente mais tarde!");
							continue;
						}
						
						InputStream inpStr = sock.getInputStream();
						if(inpStr.available() > 0){
							byte[] buffer = new byte[inpStr.available()];
							
							inpStr.read(buffer);
							
							String commandStr = new String(buffer, "UTF-8");

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
									running = false;
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
									
									filename = saveFile(filename, command.getContentDict().get("content"));
									
									if(filename.isEmpty()){
										logger.writeLog(LogLevel.ERROR, "SocketReadInput - Não foi possivel salvar o arquivo enviado"); 
										continue;
									}
									
									String name = command.getInfoDict().get("name");
									LocalDateTime datetime = LocalDateTime.parse(command.getInfoDict().get("datetime"));
									
									String path = config.containsKey("downloadfilepath")? config.get("downloadfilepath") : "./downloads/";
									
									ligthUI.createNewMessage(name, datetime, path + filename, MessageDirection.RECEIVING, commandType.FILE);
								}
								break;
								default:
									logger.writeLog(LogLevel.ERROR, "SocketReadInput - Unknown command type command="+commandStr);
							}
						}
					}
					catch(Exception ex){
						logger.writeLog(LogLevel.ERROR, "SocketReadInput - "+ex);
					}
						
					try{
						Thread.sleep(5000);
					}
					catch(Exception ex){}
				}
			}, "SocketReadInput").start();
			
		}
		catch(Exception ex){
			if(logger != null)
				logger.writeLog(LogLevel.ERROR, "setup - " + ex);
				
			throw new Exception(ex);
		}
		
	}
	
	public void closeSocket(){
		try
		{
			if(logger != null)
				logger.writeLog(LogLevel.INFO, "Closing Socket");
			
			if(running){
				OutputStream out = sock.getOutputStream();
				
				String commandStr = "type="+commandType.CLOSE.ordinal();

				commandStr = Base64.getEncoder().encodeToString(commandStr.getBytes("UTF-8"));

				out.write(commandStr.getBytes("UTF-8"));
				out.flush();
			}
		}
		catch(Exception ex)
		{
			if(logger != null)
				logger.writeLog(LogLevel.ERROR, "closeSocket - "+ex);
		}
		finally
		{
			try
			{
				sock.close();
			}
			catch(Exception ex){}
			
			if(logger != null)
				logger.close();
		}
	}
	
	protected String saveFile(String filename, String content){
		String newFilename = "";
		
		try{
			String path = config.containsKey("downloadfilepath")? config.get("downloadfilepath") : "./downloads/"; 
			
			newFilename = filename;
			
			File saveFile = new File(path + filename);
			
			saveFile.getParentFile().mkdir();
			
			int quant = 0;
			while(!saveFile.createNewFile()){
				++quant;
				
				newFilename = quant + "_" + filename;
				
				saveFile = new File(path + newFilename);
			}
			
			BufferedWriter bw = new BufferedWriter( new FileWriter(saveFile) );
			
			//String originalContent = content.replaceAll("<PIPE>", "|").replaceAll("<ECOM_PIPE>", "&|");
			
			String originalContent = new String(Base64.getDecoder().decode(content), "UTF-8");

			bw.write(originalContent, 0, originalContent.length());
			bw.close();
		
		}catch(Exception ex){
			logger.writeLog(LogLevel.ERROR, "saveFile - "+ex);
			return "";
		}
		
		return newFilename;
	}
	
	public String processesSending(String userName, String content, commandType type){
		try{
			if(running && !LightMessageSocket.tryConnection(sock, address, logger)){
				running = false;
				return "\nNão foi possivel estabelecer uma conexão, tente novamente mais tarde!\n";
			}
			
			String command = "&|name=" + userName + "|datetime=" + LocalDateTime.now();
			
			String commandContent = "type=" + type.ordinal() + "|content=";
			
			if(type == commandType.TEXT){
				commandContent += content;
			}
			else if(type == commandType.FILE){
				File contentFile = new File(content);
				
				if(!contentFile.exists() || !contentFile.isFile())
					return "Arquivo não encontrado! Favor verifique se o cominho está correto.";
					
				if(!contentFile.canRead())
					return "Sem permição para leitura desse arquivo!";
				
				try{
					BufferedReader br = new BufferedReader( new FileReader(contentFile) );
					
					String fileContent = "";
					int aux = br.read();
					
					while(aux != -1){
						fileContent += (char)aux;
						aux = br.read();
					}
					
					//fileContent = fileContent.replaceAll("\\|", "<PIPE>").replaceAll("&\\|", "<ECOM_PIPE>");
					
					fileContent = Base64.getEncoder().encodeToString(fileContent.getBytes("UTF-8"));

					br.close();
					
					commandContent += fileContent + "|filename=" + contentFile.getName();
				}
				catch(Exception ex){
					logger.writeLog(LogLevel.ERROR, "processesSending - "+ex);
					return ex.getMessage();
				}
				
			}
			else{
				return "Tipo de envio desconhecido!";
			}
			
			command = Base64.getEncoder().encodeToString((commandContent + command).getBytes("UTF-8"));
			
			OutputStream out = sock.getOutputStream();
			out.write(command.getBytes("UTF-8"));
			out.flush();
			
			logger.writeLog(LogLevel.DEBUG, "processesSending - Sended - command=" + command);
		}
		catch(Exception ex){
			logger.writeLog(LogLevel.ERROR, "processesSending - "+ex);
			return ex.getMessage();
		}
		
		return "";
	}
	
}