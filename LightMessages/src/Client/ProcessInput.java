package Client;

import java.io.*;
import java.util.*;
import java.nio.*;

import Commons.*;

public class ProcessInput {

    public static String saveFile(String filename, String content){
        Logger logger = Logger.getLogger();

		String newFilename = "";
		
		try{
			String path = LightMessageSocket.downloadPath; 
			
			newFilename = filename;
			
			File saveFile = new File(path + filename);
			
			saveFile.getParentFile().mkdir();
			
			int quant = 0;
			while(!saveFile.createNewFile()){
				++quant;
				
				newFilename = quant + "_" + filename;
				
				saveFile = new File(path + newFilename);
			}

			ByteArrayInputStream contentBytesStream = new ByteArrayInputStream(Base64.getDecoder().decode(content));

			FileOutputStream fileOut = new FileOutputStream(saveFile);

			byte[] buffer = new byte[5000];

			int readLenght = -1; 
			
			do{

				readLenght = contentBytesStream.read(buffer, 0, buffer.length);

				if(readLenght > -1)
					fileOut.write(buffer, 0, readLenght);

			}while(readLenght > -1);

			fileOut.close();

			contentBytesStream.close();
		
		}catch(Exception ex){
			logger.writeLog(LogLevel.ERROR, "saveFile - " + Logger.dumpException(ex));
			return "";
		}
		
		return newFilename;
	}

	public static String saveFile(String filename, ByteBuffer[] content){
        Logger logger = Logger.getLogger();

		String newFilename = "";
		
		try{
			String path = LightMessageSocket.downloadPath; 
			
			newFilename = filename;
			
			File saveFile = new File(path + filename);
			
			saveFile.getParentFile().mkdir();
			
			int quant = 0;
			while(!saveFile.createNewFile()){
				++quant;
				
				newFilename = quant + "_" + filename;
				
				saveFile = new File(path + newFilename);
			}

			byte[] buffer = new byte[5000];

			try(FileOutputStream fileOut = new FileOutputStream(saveFile))
			{
				for(ByteBuffer chunk : content){
					try(ByteArrayInputStream contentBytesStream = new ByteArrayInputStream(chunk.array())){

						int readLenght = -1; 
						
						do{
	
							readLenght = contentBytesStream.read(buffer, 0, buffer.length);
	
							if(readLenght > -1)
								fileOut.write(buffer, 0, readLenght);
	
						}while(readLenght > -1);

					}
				}
			}
		
		}catch(Exception ex){
			logger.writeLog(LogLevel.ERROR, "saveFile - " + Logger.dumpException(ex));
			return "";
		}
		
		return newFilename;
	}
	
}
