package Commons;

import java.nio.*;
import java.io.*;
import java.util.*;

import javax.xml.bind.DatatypeConverter;

public class CommandV2 {
    private CommandType type;
    private String username = null;
    private Long timestamp  = null;

    private FileInfo fileInfo = null;

    private ByteBuffer[] content = null;

    public CommandV2(CommandType type){
        this.type = type;
    }

    public CommandV2(CommandType type, ByteBuffer[] content){
        this.type = type;
        this.content = content;
    }

    public CommandV2(CommandType type, String username, Long timestamp, ByteBuffer[] content){
        this.type = type;
        this.username = username;
        this.timestamp = timestamp;
        this.content = content;
    }

    public CommandV2(CommandType type, String username, Long timestamp, FileInfo fileInfo, ByteBuffer[] content){
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
            Long timestamp = null;
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
        try (ByteArrayOutputStream serializedHeaderBytes = new ByteArrayOutputStream()) {
            ByteBuffer auxBuffer = ByteBuffer.allocate(8);

            // Type - T
            CommandField currentField = FieldBytes.T.get();

            serializedHeaderBytes.write(currentField.getFieldMarkerBytes());
            serializedHeaderBytes.write(this.type.getType());
            
            if(this.username != null && !this.username.trim().isEmpty())
            {
                // Username size - US
                currentField = FieldBytes.US.get();
                serializedHeaderBytes.write(currentField.getFieldMarkerBytes());

                byte[] usernameBytes = this.username.getBytes("UTF8");

                auxBuffer.putShort(0, (short)usernameBytes.length);
                serializedHeaderBytes.write(Arrays.copyOf(auxBuffer.array(), Short.BYTES));

                // Username - U
                currentField = FieldBytes.U.get();
                serializedHeaderBytes.write(currentField.getFieldMarkerBytes());
                serializedHeaderBytes.write(usernameBytes);
            }
           
            if(this.timestamp != null)
            {
                // Timestamp - TP
                currentField = FieldBytes.TP.get();
                serializedHeaderBytes.write(currentField.getFieldMarkerBytes());

                auxBuffer.putLong(0, this.timestamp);
                serializedHeaderBytes.write(Arrays.copyOf(auxBuffer.array(), Long.BYTES));
            }

            if(this.fileInfo != null)
            {
                // Filename size - FS
                currentField = FieldBytes.FS.get();
                serializedHeaderBytes.write(currentField.getFieldMarkerBytes());

                byte[] filenameBytes = this.fileInfo.getFilename().getBytes("UTF8");

                auxBuffer.putShort(0, (short)filenameBytes.length);
                serializedHeaderBytes.write(Arrays.copyOf(auxBuffer.array(), Short.BYTES));

                // Filename - F
                currentField = FieldBytes.F.get();
                serializedHeaderBytes.write(currentField.getFieldMarkerBytes());
                serializedHeaderBytes.write(filenameBytes);
            }

            // Content size - CS
            currentField = FieldBytes.CS.get();
            serializedHeaderBytes.write(currentField.getFieldMarkerBytes());

            long contentSize = 0;
            if(this.content != null)
            {
                for (ByteBuffer contentChunck : this.content)
                    contentSize += contentChunck.limit();
            }

            auxBuffer.putInt(0, (int)contentSize);
            serializedHeaderBytes.write(Arrays.copyOf(auxBuffer.array(), Integer.BYTES));

            byte[] headerContentBytes = serializedHeaderBytes.toByteArray();

            serializedHeaderBytes.reset();

            // INI
            currentField = FieldBytes.INI.get();
            serializedHeaderBytes.write(currentField.getFieldMarkerBytes());

            // Header size = HS
            currentField = FieldBytes.HS.get();
            serializedHeaderBytes.write(currentField.getFieldMarkerBytes());

            auxBuffer.putInt(0, headerContentBytes.length);
            serializedHeaderBytes.write(Arrays.copyOf(auxBuffer.array(), Integer.BYTES));

            // Join the package beging with the header content to complete the header
            serializedHeaderBytes.write(headerContentBytes);

            // Content
            if(this.content != null)
            {
                for (ByteBuffer contentChunck : this.content)
                    serializedHeaderBytes.write(contentChunck.array());
            }

            // FIN
            currentField = FieldBytes.FIN.get();
            serializedHeaderBytes.write(currentField.getFieldMarkerBytes());

            return serializedHeaderBytes.toByteArray();
        }
        catch(UnsupportedEncodingException ex){
            //Logger logger = Logger.getLogger();
            ex.printStackTrace();

            return null;
        }
        catch(IOException ex){
            //Logger logger = Logger.getLogger();
            ex.printStackTrace();

            return null;
        }
    }

    public CommandType getType(){ return this.type; }
    public String getUsername(){ return this.username; }
    public Long getTimestamp(){ return this.timestamp; }
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

        @Override
        public boolean equals(Object obj){
            if(!(obj instanceof FileInfo))
                return false;

            FileInfo castedObject = (FileInfo)obj;

            return this.filename == castedObject.getFilename() || this.filename.equals(castedObject.getFilename());
        }

        @Override
        public String toString(){
            return this.filename;
        }
    }
}
