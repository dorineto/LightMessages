package Commons;

import java.nio.*;
import java.io.*;
import java.util.*;

public class CommandV2 {
    private CommandType type;
    private String username = null;
    private long timestamp  = -1L;

    private FileInfo fileInfo = null;

    private ByteBuffer[] content = null;

    public CommandV2(CommandType type){
        this.type = type;
    }

    public CommandV2(CommandType type, ByteBuffer[] content){
        this.type = type;
        this.content = content;
    }

    public CommandV2(CommandType type, String username, long timestamp, ByteBuffer[] content){
        this.type = type;
        this.username = username;
        this.timestamp = timestamp;
        this.content = content;
    }

    public CommandV2(CommandType type, String username, long timestamp, FileInfo fileInfo, ByteBuffer[] content){
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

            // INI
            CommandField currentCommandField = FieldBytes.INI.get();

            tryReadStream(inpStream, fieldBytes.array(), 0, currentCommandField.getFieldMarkerBytes().length);

            if(!Arrays.equals(Arrays.copyOf(fieldBytes.array(), currentCommandField.getFieldMarkerBytes().length), currentCommandField.getFieldMarkerBytes()))
                throw new IllegalArgumentException("The package don't start with the INI mark");

            // HS
            currentCommandField = FieldBytes.HS.get();

            tryReadStream(inpStream, fieldBytes.array(), 0, currentCommandField.getFieldMarkerBytes().length);

            if(!Arrays.equals(Arrays.copyOf(fieldBytes.array(), currentCommandField.getFieldMarkerBytes().length),currentCommandField.getFieldMarkerBytes()))
                throw new IllegalArgumentException("The package don't contains HS field");

            tryReadStream(inpStream, fieldBytes.array(), 0, currentCommandField.getValueSize());

            long headerSizeExpected = fieldBytes.getInt(0) & 0xFFFFFFFF;

            System.out.println("headerSizeExpected=" + headerSizeExpected);
            
            // HS
            currentCommandField = FieldBytes.T.get();

            long headerSize = 0;

            tryReadStream(inpStream, fieldBytes.array(), 0, currentCommandField.getFieldMarkerBytes().length);

            if(!Arrays.equals(Arrays.copyOf(fieldBytes.array(), currentCommandField.getFieldMarkerBytes().length), currentCommandField.getFieldMarkerBytes()))
                throw new IllegalArgumentException("The package don't contains T field");

            tryReadStream(inpStream, fieldBytes.array(), 0, currentCommandField.getValueSize());

            headerSize += currentCommandField.getFieldMarkerBytes().length + currentCommandField.getValueSize();

            CommandType type;

            switch(fieldBytes.get(0)){
                case (byte)0x01:
                    type = CommandType.UUID;
                    break;
                case (byte)0x02:
                    type = CommandType.TEXT;
                    break;
                case (byte)0x03:
                    type = CommandType.FILE;
                    break;
                case (byte)0xFF:
                default:
                    type = CommandType.CLOSE;
                    break;
            }

            // Decode username, and if its a file decode too the filename
            if(type == CommandType.TEXT || type == CommandType.FILE){
                // US
                currentCommandField = FieldBytes.US.get();

                tryReadStream(inpStream, fieldBytes.array(), 0, currentCommandField.getFieldMarkerBytes().length);

                if(!Arrays.equals(Arrays.copyOf(fieldBytes.array(), currentCommandField.getFieldMarkerBytes().length), currentCommandField.getFieldMarkerBytes()))
                    throw new IllegalArgumentException("The package don't contains US field");

                tryReadStream(inpStream, fieldBytes.array(), 0, currentCommandField.getValueSize());

                headerSize += currentCommandField.getFieldMarkerBytes().length + currentCommandField.getValueSize();

                int usernameSize = fieldBytes.getShort(0) & 0xFFFF;
                
            }

            return new CommandV2(type);
        }
        catch(IOException ex){
            //Logger logger = Logger.getLogger();
            ex.printStackTrace();

            return null;
        }
    }

    private static void tryReadStream(InputStream input, byte[] buffer, int off, int len) throws IllegalArgumentException, IOException{
        if(input.read(buffer, off, len) == -1)
            throw new IllegalArgumentException("Unexpected end of packed");
    }

    public byte[] serialize() {
        return new byte[] {};
    }

    public CommandType getType(){ return this.type; }
    public String getUsername(){ return this.username; }
    public long getTimestamp(){ return this.timestamp; }
    public FileInfo getFileInfo(){ return this.fileInfo; }
    public ByteBuffer[] getContent(){ return this.content; }

    public enum FieldBytes {
        INI  (new CommandField(new byte[] {(byte)0xF7, (byte)0xF3}, 0)) // Mark the start of a package
        ,FIN (new CommandField(new byte[] {(byte)0xF3, (byte)0xF7}, 0)) // Mark the end of a package
        ,HS  (new CommandField(new byte[] {(byte)0x48, (byte)0x53}, 4)) // Header size field
        ,T   (new CommandField(new byte[] {(byte)0x54}, 1))             // Command type field
        ,US  (new CommandField(new byte[] {(byte)0x55, (byte)0x50}, 2)) // Username size field
        ,U   (new CommandField(new byte[] {(byte)0x55}, -1))                      // Username text
        ,TP  (new CommandField(new byte[] {(byte)0x54, (byte)0x50}, 8)) // Timestamp field
        ,FS  (new CommandField(new byte[] {(byte)0x46, (byte)0x53}, 2)) // Filename size field
        ,F   (new CommandField(new byte[] {(byte)0x46}, -1))                       // Filename
        ,CS  (new CommandField(new byte[] {(byte)0x43, (byte)0x53},4)); // Content size

        private final CommandField fieldVal;

        FieldBytes(CommandField fieldVal){
            this.fieldVal = fieldVal;
        }

        public CommandField get(){ return this.fieldVal; }
    }

    public static class CommandField {
        private byte[] fieldMarkerBytes;
        private int valueSize; // In bytes

        public CommandField(byte[] fieldMarkerBytes, int valueSize){
            this.fieldMarkerBytes = fieldMarkerBytes;
            this.valueSize = valueSize;
        }

        public byte[] getFieldMarkerBytes() { return this.fieldMarkerBytes; }
        public int getValueSize() { return this.valueSize; }
    }

    public enum CommandType {
        UUID((byte)0x01)
        ,TEXT((byte)0x02)
        ,FILE((byte)0x03)
        ,CLOSE((byte)0xFF);

        private final byte type;

        CommandType(byte type){
            this.type = type;
        }

        public byte getType(){ return this.type; }
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
