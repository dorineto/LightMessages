import java.net.*;
import java.io.*;
import java.util.Arrays;
import java.util.UUID;

public class TesteAPS{
	protected Socket sock = null;
	protected boolean running = true;
	protected InetSocketAddress address = null;
	protected String uniqueID = "";
	
	public static void main(String[] args){
		new TesteAPS().setup();
	}
	
	public static boolean tryConnection(Socket sock, InetSocketAddress address){
		int trys = 0;
		while(trys < 3 && !sock.isConnected()){
			try{
				sock.connect(address, 15000);
			}
			catch(SocketTimeoutException ex){
				System.out.println("tryConnection - Socket timout connection - try="+(trys + 1));
				trys++;
			}
			catch(Exception ex){
				System.out.println("tryConnection - " + ex);
				break;
			}
		}
		
		System.out.println("tryConnection - sock connection " + sock.isConnected());
		return sock.isConnected();
	}
	
	public void setup(){
		try{
			sock = new Socket();
			address = new InetSocketAddress(InetAddress.getByName("localhost"), 4860);
			
			if(!TesteAPS.tryConnection(sock, address)){
				System.out.println("\nImpossible to connect, try again later!\n");
				System.exit(1);
			}
			
			new Thread( () -> {
				System.out.println("TerminalInputRead - Start");
				while(running){
					try{
						if(System.in.available() > 1){
							byte[] buffer = new byte[System.in.available()];
							
							System.in.read(buffer);
							
							byte lastByte = buffer[buffer.length - 1];
							
							// Checks if the last byte is a CR or LF or CRLF or NET
							if(lastByte == 10 || lastByte == 13 || lastByte == 133){
								int bufferEnd = lastByte == 13 && buffer[buffer.length - 2] == 10? 2 : 1;
								
								String strInp = (new String(buffer, "UTF-8")).substring(0, buffer.length - bufferEnd);
								
								System.out.println("Sended: " + strInp + "\n");
								
								if(!running && !TesteAPS.tryConnection(sock, address)){
									System.out.println("\nImpossible to connect, try again later!\n");
									running = false;
								}
																
								OutputStream out = sock.getOutputStream();
								out.write(("message|"+strInp).getBytes("UTF-8"));
								out.flush();
							}
							
							System.in.read(buffer);
						}
					}
					catch(Exception ex){
						System.out.println("TerminalInputRead - "+ex);
					}
						
					try{
						Thread.sleep(1000);
					}
					catch(Exception ex){}
				}
				
				System.out.println("TerminalInputRead - End");
			}, "TerminalInputRead").start();
			
			new Thread( () -> {
				System.out.println("SocketReadInput - Start");
				while(running){
					try{
						if(!running && !TesteAPS.tryConnection(sock, address)){
							System.out.println("\nImpossible to connect, try again later!\n");
							running = false;
						}
						
						InputStream inpStr = sock.getInputStream();
						if(inpStr.available() > 0){
							byte[] buffer = new byte[inpStr.available()];
							
							inpStr.read(buffer);
							
							String[] command = (new String(buffer, "UTF-8")).split("\\|", 2);
							
							// Alterar para depois ser tratado por uma classe separada
							if(command[0].equalsIgnoreCase("uuid"))
							{
								uniqueID = command[1];
								System.out.println("UUID - "+uniqueID);
							}
							else if(command[0].equalsIgnoreCase("close")) // Quando enviado por enquanto para o programa
							{
								running = false;
								System.exit(0);
							}
							else
							{
								System.out.println("Recived - "+command[1]);
							}
							
						}
						
					}
					catch(Exception ex){
						System.out.println("SocketReadInput - "+ex);
					}
						
					try{
						Thread.sleep(5000);
					}
					catch(Exception ex){}
				}
			}, "SocketReadInput").start();
			
		}
		catch(Exception ex){
			System.out.println("main - " + ex);
			
			try{
				if(sock != null && !sock.isConnected())
					sock.close();
			}
			catch(Exception ex2){
					System.out.println("main - " + ex2);
			}
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try
			{
				System.out.println("\n\nEnding...\n");
				
				OutputStream out = sock.getOutputStream();
				out.write("close".getBytes("UTF-8"));
				out.flush();
			}
			catch(Exception ex)
			{
				System.out.println("TesteAPSShutdownHook - "+ex);
			}
			finally
			{
				try
				{
					sock.close();
				}
				catch(Exception ex)
				{
					System.out.println("TesteAPSShutdownHook - "+ex);
				}
				
			}
		}));
	}
}