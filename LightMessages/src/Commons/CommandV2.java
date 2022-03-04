package Commons;

import java.nio.*;
import java.io.*;

public class CommandV2 {
    private CommandType type;
    private String username;
    private long timestamp;

    private FileInfo fileInfo;

    private ByteBuffer content;

    public CommandV2(CommandType type, String username, long timestamp, FileInfo fileInfo, ByteBuffer content){
        this.type = type;
        this.username = username;
        this.timestamp = timestamp;
        this.fileInfo = fileInfo;
        this.content = content;
    }

    public CommandV2(CommandType type, String username, long timestamp, ByteBuffer content){
        this.type = type;
        this.username = username;
        this.timestamp = timestamp;
        this.fileInfo = null;
        this.content = content;
    }

	public static CommandV2 ProcessesInputStream(InputStream inpStream, boolean fillBytes, boolean fillDecodedString) throws IOException{
        return new CommandV2(CommandType.CLOSE, "a", 0, ByteBuffer.wrap(new byte[] {}));
    }

    public enum FieldBytes {
        INI  (new int[] {0xF7, 0xF3})
        ,FIN (new int[] {0xF3, 0xF7})
        ,HS  (new int[] {0x48, 0x53})
        ,US  (new int[] {0x55, 0x50})
        ,U   (new int[] {0x55})
        ,TP  (new int[] {0x54, 0x50})
        ,FS  (new int[] {0x46, 0x53})
        ,F   (new int[] {0x46})
        ,CS  (new int[] {0x43, 0x53});

        private final int[] fieldVal;

        FieldBytes(int[] fieldVal){
            this.fieldVal = fieldVal;
        }

        public int[] getFieldVal(){ return this.fieldVal; }
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

    public class FileInfo{
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
