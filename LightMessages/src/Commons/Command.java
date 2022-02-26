package Commons;

import java.io.*;
import java.nio.*;
import java.util.*;

public class Command{
	private Hashtable<String, String> contentDict, infoDict;
	private String decodedString;
	private byte[] bytes;
	
	public Command(){
		this.contentDict = new Hashtable<String, String>();
		this.infoDict = new Hashtable<String, String>();

		this.decodedString = "";
		this.bytes = null;
	}
	
	public Hashtable<String, String> getContentDict(){ return this.contentDict; }
	public Hashtable<String, String> getInfoDict(){ return this.infoDict; }

	public String getDecodedString() { return this.decodedString; }

	public byte[] getBytes() {return this.bytes; }
	
	public String Serialize(){
		String serCommandInfo = "";

		Iterator<Map.Entry<String,String>> iter = this.infoDict.entrySet().iterator();

		Map.Entry<String,String> entry;
		while(iter.hasNext()){
			entry = iter.next();
			serCommandInfo += entry.getKey() + "=" + entry.getValue() + "|";
		}

		if(!serCommandInfo.isEmpty())
			serCommandInfo = serCommandInfo.substring(0, serCommandInfo.length() - 1);
		
		String serCommandContent = "";

		iter = this.contentDict.entrySet().iterator();
		while(iter.hasNext()){
			entry = iter.next();
			serCommandContent += entry.getKey() + "=" + entry.getValue() + "|";
		}

		if(!serCommandContent.isEmpty())
			serCommandContent = serCommandContent.substring(0, serCommandContent.length() - 1);

		return serCommandContent + (!serCommandContent.isEmpty() && !serCommandInfo.isEmpty()? "&|" : "") + serCommandInfo;
	}

	public static Command ProcessesInputStream(InputStream inpStream, boolean fillBytes, boolean fillDecodedString) throws IOException{
		ArrayList<byte[]> contentChunksList = new ArrayList<byte[]>();

		int totalBytes = 0;

		byte[] buffer = new byte[5000];
		int readLenght = inpStream.read(buffer, 0, buffer.length);

		contentChunksList.add(Arrays.copyOf(buffer, readLenght));
		totalBytes += readLenght;

		while (inpStream.available() > 0) 
		{
			readLenght = inpStream.read(buffer, 0, 5000);

			if (readLenght > -1) 
			{
				contentChunksList.add(Arrays.copyOf(buffer, readLenght));
				totalBytes += readLenght;
			}
		}

		ByteBuffer finalBuffer = ByteBuffer.allocate(totalBytes);

		for (byte[] contentChunck : contentChunksList)
			finalBuffer.put(contentChunck);

		String commandStr = new String(finalBuffer.array(), "UTF-8");

		String decodedCommandStr = new String(Base64.getDecoder().decode(commandStr), "UTf-8");

		Command command = Command.parse(decodedCommandStr);

		if(fillBytes)
			command.bytes = finalBuffer.array();

		finalBuffer.clear();

		if(fillDecodedString)
			command.decodedString = decodedCommandStr;

		return command;
	}

	public static Command ProcessesInputStream(InputStream inpStream, boolean fillBytes) throws IOException{
		return ProcessesInputStream(inpStream, fillBytes, false);
	}

	public static Command ProcessesInputStream(InputStream inpStream) throws IOException{
		return ProcessesInputStream(inpStream, false, false);
	}

	public static Command parse(String commandStr){
		Command returnCmd = new Command();

		if(!commandStr.trim().isEmpty()){
			String[] contentInfoStr = new String[2];
			
			int sepIndex = commandStr.lastIndexOf("&|");
						
			contentInfoStr[0] = sepIndex != -1? commandStr.substring(0, sepIndex) : commandStr;
			contentInfoStr[1] = sepIndex != -1? commandStr.substring(sepIndex + 2, commandStr.length()) : "";
			
			String[] splitAux, keyValueSplit;
			Hashtable<String,String> dict;
			for(int i = 0; i < contentInfoStr.length; i++){
				dict = i == 0? returnCmd.getContentDict() : returnCmd.getInfoDict();
				
				if(!contentInfoStr[i].isEmpty()){
					splitAux = contentInfoStr[i].split("\\|");
					
					for(String keyValueStr : splitAux){
						keyValueSplit = keyValueStr.split("=", 2);
						
						dict.put(keyValueSplit[0], keyValueSplit[1]);
					}
				}
			}
		}
		
		return returnCmd;
	}

	public static String ValidadeCommand(Command command) {

		if (!command.getContentDict().containsKey("type")) 
			return "Command without type";

		int type = Integer.parseInt(command.getContentDict().get("type"));

		if(type != commandType.CLOSE.ordinal() && !command.getContentDict().containsKey("content"))
			return "Command without content";

		// If the command type text or a file it was to have content, 
		// name and datetime
		if(type == commandType.TEXT.ordinal() || type == commandType.FILE.ordinal()){
			if (!command.getInfoDict().containsKey("name")
			 || !command.getInfoDict().containsKey("datetime")) 
			{
				String withoutValue = "name";

				if(!command.getInfoDict().containsKey("datetime"))
					withoutValue = "datetime";

				return "Command without " + withoutValue;
			}

			if(type == commandType.FILE.ordinal() && !command.getContentDict().containsKey("filename"))
				return "Command without filename";	
		}

		return "";
	}

}