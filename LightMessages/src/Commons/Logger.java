package Commons;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger{
	private static Logger singlogger;

	private BufferedWriter writer;
	
	private Logger(String pathToLog) throws IOException, SecurityException {
		File logFile = new File(pathToLog);
		
		logFile.getParentFile().mkdir();
		logFile.createNewFile();
		
		this.writer = new BufferedWriter( new FileWriter(pathToLog, true) );
		
	}

	public static Logger getLogger(String pathToLog) throws IOException, SecurityException {
		if(Logger.singlogger == null)
			Logger.singlogger = new Logger(pathToLog);

		return Logger.singlogger;
	}

	public static Logger getLogger() throws IOException, SecurityException {
		return Logger.getLogger("../log/default.log");
	}
	
	public void writeLog(LogLevel loglevel, String log) {
		try{
			String datetime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss.SSS"));
			
			String logLine = "\n" + loglevel.name() + " - " + datetime + ": " + log;
			
			writer.append(logLine);
			writer.flush();
		}
		catch(Exception ex){}
	}
	
	public void close(){
		try{
			writer.close();
		}
		catch(Exception ex){}
	}

	public static String dumpException(Exception ex){
        
        StringWriter stringWriter = new StringWriter();

        PrintWriter pw = new PrintWriter(stringWriter);

        ex.printStackTrace(pw);

        pw.close();

        String fullExceptionStr = stringWriter.toString();

        try {
            stringWriter.close();
        }
        catch(Exception ex1){}

        return fullExceptionStr;
	}
}
