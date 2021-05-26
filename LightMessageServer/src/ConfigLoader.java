import java.util.Hashtable;
import java.io.*;


class ConfigLoader{	
	public static Hashtable<String, String> loadConfig(String pathConfigFile, boolean serverConfig) throws IOException, SecurityException {
		File configFile = new File(pathConfigFile);
		
		Hashtable<String, String> config;
		
		if(!configFile.exists()){
			config = ConfigLoader.defaultConfigs(pathConfigFile, serverConfig);
			return config;
		}
			
		config = new Hashtable<String, String>();
		
		BufferedReader br = new BufferedReader( new FileReader(configFile) );
		
		String[] configSplit;
		String configLine = br.readLine();
		
		while(configLine != null){
			configSplit = configLine.split(":", 2);
			
			config.put(configSplit[0], configSplit[1]);
			
			configLine = br.readLine();
		}
		
		br.close();
		
		return config;
	}
	
	public static Hashtable<String, String> defaultConfigs(String pathConfigFile, boolean serverConfig) throws IOException, SecurityException {
		Hashtable<String, String> config = new Hashtable<String, String>();
		
		String configStr = "";
		
		if(!serverConfig)
		{
			config.put("logpath", "../log/default.log");
			config.put("serveraddress", "localhost");
			config.put("serveraddressport", "4860");
			config.put("downloadfilepath", "../downloads/");
			
			configStr = "logpath:../log/default.log\n"
					  + "serveraddress:localhost\n"
					  + "serveraddressport:4860\n"
					  + "downloadfilepath:../downloads/\n";
		}
		else
		{
			config.put("logpath", "../log/serverdefault.log");
			config.put("serverport", "4860");
			config.put("maxconnectionquantity", "1024");
			
			configStr = "logpath:../log/serverdefault.log\n"
					  + "serverport:4860\n"
					  + "maxconnectionquantity:1024\n";
		}
		
		File configFile = new File(pathConfigFile);
		
		configFile.getParentFile().mkdir();
		
		BufferedWriter bw = new BufferedWriter( new FileWriter(configFile) );
		bw.write(configStr, 0, configStr.length());
		bw.close();
		
		return config;
	}
	
}