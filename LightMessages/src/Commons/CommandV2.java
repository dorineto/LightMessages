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
            getField(inpStream, fieldBytes.array(), FieldBytes.INI);

            // Header size field - HS
            getField(inpStream, fieldBytes.array(), FieldBytes.HS);

            long headerSizeExpected = fieldBytes.getInt(0) & 0xFFFFFFFF;
            
            // Type Field - T
            long headerSize = getField(inpStream, fieldBytes.array(), FieldBytes.T);

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
            String username = null;
            long timestamp = -1L;
            if(type == CommandType.TEXT || type == CommandType.FILE){
                // Username size - US
                headerSize += getField(inpStream, fieldBytes.array(), FieldBytes.US);

                int usernameSize = fieldBytes.getShort(0) & 0xFFFF;

                // Username - U
                headerSize += getField(inpStream, fieldBytes.array(), FieldBytes.U, usernameSize);

                username = new String(fieldBytes.array(), 0, usernameSize, "UTF8");

                // Timestamp - TP
                headerSize += getField(inpStream, fieldBytes.array(), FieldBytes.TP);

                timestamp = fieldBytes.getLong();
            }

            FileInfo fileInfo = null;
            if (type == CommandType.FILE){
                // Filename size - FS
                headerSize += getField(inpStream, fieldBytes.array(), FieldBytes.FS);

                int filenameSize = fieldBytes.getShort(0) & 0xFFFF;

                // Filename - F
                headerSize += getField(inpStream, fieldBytes.array(), FieldBytes.F, filenameSize);

                String filename = new String(fieldBytes.array(), 0, filenameSize, "UTF8");

                fileInfo = new FileInfo(filename);
            }

            // Content size - CS
            headerSize += getField(inpStream, fieldBytes.array(), FieldBytes.CS);

            if(headerSize != headerSizeExpected)
                throw new IllegalArgumentException("The read header size is different from the read expected header size");

            long contentSize = fieldBytes.getInt(0) & 0xFFFFFFFF;

            ByteBuffer[] content = null;
            if(contentSize > 0)
            {
                content =  new ByteBuffer[(int)(contentSize / Integer.MAX_VALUE) + 1];

                long processedContentSize = contentSize;
                int chunckSize;
                byte[] auxBuffer;
                for(int i = 0; i < content.length; i++){
                    chunckSize = Integer.MAX_VALUE > processedContentSize? (int)processedContentSize : Integer.MAX_VALUE;

                    auxBuffer = new byte[chunckSize];

                    inpStream.read(auxBuffer, 0, chunckSize);

                    content[i] = ByteBuffer.wrap(auxBuffer);

                    processedContentSize -= chunckSize;
                }
            }

            // FIN
            getField(inpStream, fieldBytes.array(), FieldBytes.FIN);

            return new CommandV2(type, username, timestamp, fileInfo, content);
        }
        catch(IOException ex){
            //Logger logger = Logger.getLogger();
            ex.printStackTrace();

            return null;
        }
    }

    private static void tryReadStream(InputStream input, byte[] buffer, int len, int off) throws IllegalArgumentException, IOException{
        if(input.read(buffer, off, len) == -1)
            throw new IllegalArgumentException("Unexpected end of packed");
    }

    private static void tryReadStream(InputStream input, byte[] buffer, int len) throws IllegalArgumentException, IOException{
        tryReadStream(input, buffer, len, 0);
    }

    private static long getField(InputStream input, byte[] buffer, FieldBytes currentField, int valueSize) throws IllegalArgumentException, IOException{
        CommandField currentCommandField = currentField.get();

        tryReadStream(input, buffer, currentCommandField.getFieldMarkerBytes().length);

        if(!Arrays.equals(Arrays.copyOf(buffer, currentCommandField.getFieldMarkerBytes().length),currentCommandField.getFieldMarkerBytes()))
            throw new IllegalArgumentException("The package don't contains "+ currentField.name() +" field");

        // If the value size is equals to 0 then the field dont have a value
        if(currentCommandField.getValueSize() == 0)
            return currentCommandField.getFieldMarkerBytes().length;

        // If the value size is less then 0 (size is defined with other field), then valueSize have to be greater then 0 
        if(currentCommandField.getValueSize() < 0 && valueSize < 1)
            throw new IllegalArgumentException("The valueSize have to be greater then 0");

        int currentValueSize = currentCommandField.getValueSize() > 0 ? currentCommandField.getValueSize() : valueSize;

        tryReadStream(input, buffer, currentValueSize);

        return currentCommandField.getFieldMarkerBytes().length + currentValueSize;
    }

    private static long getField(InputStream input, byte[] buffer, FieldBytes currentField) throws IllegalArgumentException, IOException{
        return getField(input, buffer, currentField, 0);
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
        ,US  (new CommandField(new byte[] {(byte)0x55, (byte)0x53}, 2)) // Username size field
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
