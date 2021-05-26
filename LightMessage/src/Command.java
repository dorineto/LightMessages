import java.util.Hashtable;

class Command{
	private Hashtable<String, String> contentDict, infoDict;
	
	public Command(){
		this.contentDict = new Hashtable<String, String>();
		this.infoDict = new Hashtable<String, String>();
	}
	
	public Hashtable<String, String> getContentDict(){
		return this.contentDict;
	}
	
	public Hashtable<String, String> getInfoDict(){
		return this.infoDict;
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
	
}