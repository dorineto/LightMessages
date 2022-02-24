package Client;

import java.net.*;
import java.io.*;
import java.util.Base64;
import java.util.Hashtable;
import java.time.LocalDateTime;

import java.awt.image.*;
import javax.imageio.*;

import java.util.*;
import java.nio.*;

import Commons.*;

public class LightMessageSocket{
	protected LightMessageUI ligthUI;
	
	protected Socket sock = null;
	
	protected Logger logger;

	private ReadInput readInput;
	
	protected static InetSocketAddress connectionAddress = null;
	protected static Hashtable<String, String> config;
	protected static String downloadPath;

	public LightMessageSocket(LightMessageUI ligthUI){
		this.ligthUI = ligthUI;
	}
	
	public static InetSocketAddress getConnectionAddress() { return connectionAddress; }
	public static Hashtable<String, String> getConfig () { return config; }
	public static String getDownloadPath() { return downloadPath; }

	public static boolean tryConnection(Socket sock){
		Logger logger = null;

		try{
			logger = Logger.getLogger();
		}
		catch(Exception ex){
			throw new RuntimeException("tryConnection - Ex: " + Logger.dumpException(ex));
		}

		int trys = 0;
		while(trys < 3 && !sock.isConnected()){
			try{
				sock.connect(connectionAddress, 15000);
			}
			catch(SocketTimeoutException ex){
				logger.writeLog(LogLevel.ERROR, "tryConnection - Socket timout connection - try="+(trys + 1));
				trys++;
			}
			catch(Exception ex){
				logger.writeLog(LogLevel.ERROR, "tryConnection - " + Logger.dumpException(ex));
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
			
			logger = Logger.getLogger(config.get("logpath"));
			
			if(!config.containsKey("serveraddress"))
				throw new Exception("Socket Setup - serveraddress não está configurado");
			else if(!config.containsKey("serveraddressport"))
				throw new Exception("Socket Setup - serveraddressport não está configurado");
			
			sock = new Socket();
			
			int port = Integer.parseInt(config.get("serveraddressport"));
			
			connectionAddress = new InetSocketAddress(InetAddress.getByName(config.get("serveraddress")), port);
			
			downloadPath = config.containsKey("downloadfilepath")? config.get("downloadfilepath") : "./downloads/";

			if(!LightMessageSocket.tryConnection(sock))
				throw new Exception("\nSocket Setup - Não foi possivel estabelecer uma conexão, tente novamente mais tarde!\n");
			
			readInput = new ReadInput(sock, ligthUI);
			readInput.start();

		}
		catch(Exception ex){
			if(logger != null)
				logger.writeLog(LogLevel.ERROR, "setup - " + Logger.dumpException(ex));
				
			throw new Exception(ex);
		}
		
	}
	
	public void closeSocket(){
		try
		{
			if(logger != null)
				logger.writeLog(LogLevel.INFO, "Closing Socket");
			
			if(readInput.isRunning()){
				OutputStream out = sock.getOutputStream();
				
				String commandStr = "type="+commandType.CLOSE.ordinal();

				commandStr = Base64.getEncoder().encodeToString(commandStr.getBytes("UTF-8"));

				out.write(commandStr.getBytes("UTF-8"));
				out.flush();

				readInput.stopRunning();
				while(readInput.isRunning()){
					try{
						Thread.sleep(2000);
					}catch(Exception ex) {}
				}
			}
		}
		catch(Exception ex)
		{
			if(logger != null)
				logger.writeLog(LogLevel.ERROR, "closeSocket - "+ Logger.dumpException(ex));
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
	
	public String processesSending(String userName, String content, commandType type){
		try{
			if(readInput.isRunning() && !LightMessageSocket.tryConnection(sock)){
				closeSocket();
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
					String fileContent = "";

					String mimeType = URLConnection.guessContentTypeFromName(contentFile.getName());

					if(mimeType != null && !mimeType.isEmpty() && mimeType.toLowerCase().startsWith("image/"))
					{

						BufferedImage sendBuffImg = ImageIO.read(contentFile);

						ByteArrayOutputStream imgByteArr = new ByteArrayOutputStream();

						String format = mimeType.toLowerCase().split("/")[1];
						ImageIO.write(sendBuffImg, format, imgByteArr);

						fileContent = Base64.getEncoder().encodeToString(imgByteArr.toByteArray());
						
						imgByteArr.close();
					}
					else
					{

						FileInputStream inputFile = new FileInputStream(contentFile);

						ArrayList<byte[]> contentChunks = new ArrayList<>();

						byte[] buffer = new byte[5000];

						int readLenght = -1;

						int totalBytes = 0;
						
						do{
							readLenght = inputFile.read(buffer, 0, buffer.length);

							if (readLenght > -1){
								contentChunks.add(Arrays.copyOf(buffer, readLenght));
								totalBytes += readLenght;
							}
						}while(readLenght > -1);

						inputFile.close();

						ByteBuffer finalBuffer = ByteBuffer.allocate(totalBytes);

						for(byte[] contentChunk : contentChunks)
							finalBuffer.put(contentChunk);

						fileContent += Base64.getEncoder().encodeToString(finalBuffer.array());

					}

					commandContent += fileContent + "|filename=" + contentFile.getName();
				}
				catch(Exception ex){
					logger.writeLog(LogLevel.ERROR, "processesSending - "+ Logger.dumpException(ex));
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
			logger.writeLog(LogLevel.ERROR, "processesSending - " + Logger.dumpException(ex));
			return ex.getMessage();
		}
		
		return "";
	}
	
}