package Commons;

import java.nio.*;
import java.io.*;
import java.util.*;

public class CommandV2 {
    private CommandType type;
    private String username = null;
    private long timestamp  = -1L;

    private FileInfo fileInfo = null;

    private ByteBuffer content = null;

    public CommandV2(CommandType type){
        this.type = type;
    }

    public CommandV2(CommandType type, ByteBuffer content){
        this.type = type;
        this.content = content;
    }

    public CommandV2(CommandType type, String username, long timestamp, ByteBuffer content){
        this.type = type;
        this.username = username;
        this.timestamp = timestamp;
        this.content = content;
    }

    public CommandV2(CommandType type, String username, long timestamp, FileInfo fileInfo, ByteBuffer content){
        this.type = type;
        this.username = username;
        this.timestamp = timestamp;
        this.fileInfo = fileInfo;
        this.content = content;
    }

	public static CommandV2 processesInputStream(InputStream inpStream) {
        try 
        {
            ByteBuffer fieldBytes = ByteBuffer.allocate(255);

            if(inpStream.read(fieldBytes.array(), 0, 2) < -1)
                throw new IllegalArgumentException("Unexpected end of packed");
            
            if(!Arrays.equals(Arrays.copyOf(fieldBytes.array(), 2), FieldBytes.INI.getFieldVal()))
                throw new IllegalArgumentException("The package don't start with the INI mark");

            if(inpStream.read(fieldBytes.array(), 0, 2) < -1)
                throw new IllegalArgumentException("Unexpected end of packed");

            if(!Arrays.equals(Arrays.copyOf(fieldBytes.array(), 2), FieldBytes.HS.getFieldVal()))
                throw new IllegalArgumentException("The package don't contains HS field");

            inpStream.read(fieldBytes.array(), 0, 4);

            long headerSize = fieldBytes.getInt(0) & 0xFFFFFFFF;

            System.out.println(headerSize);

            return new CommandV2(CommandType.CLOSE);
        }
        catch(IOException ex){
            //Logger logger = Logger.getLogger();
            ex.printStackTrace();

            return null;
        }
    }

    public byte[] serialize() {
        return new byte[] {};
    }

    public CommandType getType(){ return this.type; }
    public String getUsername(){ return this.username; }
    public long getTimestamp(){ return this.timestamp; }
    public FileInfo getFileInfo(){ return this.fileInfo; }
    public ByteBuffer getContent(){ return this.content; }

    public enum FieldBytes {
        INI  (new byte[] {(byte)0xF7, (byte)0xF3})
        ,FIN (new byte[] {(byte)0xF3, (byte)0xF7})
        ,HS  (new byte[] {(byte)0x48, (byte)0x53})
        ,US  (new byte[] {(byte)0x55, (byte)0x50})
        ,U   (new byte[] {(byte)0x55})
        ,TP  (new byte[] {(byte)0x54, (byte)0x50})
        ,FS  (new byte[] {(byte)0x46, (byte)0x53})
        ,F   (new byte[] {(byte)0x46})
        ,CS  (new byte[] {(byte)0x43, (byte)0x53});

        private final byte[] fieldVal;

        FieldBytes(byte[] fieldVal){
            this.fieldVal = fieldVal;
        }

        public byte[] getFieldVal(){ return this.fieldVal; }
    }

    public enum CommandType {
        UUID(0x01)
        ,TEXT(0x02)
        ,FILE(0x03)
        ,CLOSE(0xFF);

        private final int type;

        CommandType(int type){
            this.type = type;
        }

        public int getType(){ return this.type; }
    }

    public static class FileInfo{
        private String filename;

        public FileInfo(){
            this.filename = "";
        }

        public FileInfo(String filename){
            this.filename = filename;
        }

        public String getFilename() { return this.filename; }
        public void setFilename(String filename) { this.filename = filename; }
    }
}
