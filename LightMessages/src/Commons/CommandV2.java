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

    public enum CommandType {
        UUID(1),TEXT(2),FILE(3),CLOSE(255);

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
