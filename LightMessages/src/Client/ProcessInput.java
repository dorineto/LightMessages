package Client;

import java.io.*;
import java.net.*;
import java.awt.image.*;
import javax.imageio.*;
import java.util.*;

import Commons.*;

public class ProcessInput {
    public static String saveFile(String filename, String content){
        Logger logger = null;

        try{
            logger = Logger.getLogger();
        }
        catch(Exception ex){
            throw new RuntimeException("saveFile - Ex: " + Logger.dumpException(ex));
        }

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
			
			logger.writeLog(LogLevel.INFO, "content=" + content);

			ByteArrayInputStream contentBytesStream = new ByteArrayInputStream(Base64.getDecoder().decode(content));

			String mimeType = URLConnection.guessContentTypeFromName(filename);

			if(mimeType != null && !mimeType.isEmpty() && mimeType.toLowerCase().startsWith("image/"))
			{ 

				BufferedImage contentImgBuff = ImageIO.read(contentBytesStream);

				String format = mimeType.toLowerCase().split("/")[1];
				
				ImageIO.write(contentImgBuff, format, saveFile);

			}
			else
			{

				FileOutputStream fileOut = new FileOutputStream(saveFile);

				byte[] buffer = new byte[5000];

				int readLenght = -1; 
				
				do{

					readLenght = contentBytesStream.read(buffer, 0, buffer.length);

					if(readLenght > -1)
						fileOut.write(buffer, 0, readLenght);

				}while(readLenght > -1);

				fileOut.close();
			}

			contentBytesStream.close();
		
		}catch(Exception ex){
			logger.writeLog(LogLevel.ERROR, "saveFile - " + Logger.dumpException(ex));
			return "";
		}
		
		return newFilename;
	}
}
