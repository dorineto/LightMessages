package Commons;

import java.nio.*;
import java.io.*;
import java.util.*;

public class TestCommons {
    public static void main(String[] args){
        
    }

    /*
    public static void main(String[] args){
        try
        {
            byte[] byteArr = new byte[]{
                (byte)0x48, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x18,
                (byte)0x12, (byte)0x11, (byte)0x17
            };
            
            InputStream stream = new ByteArrayInputStream(byteArr);
    
            ByteBuffer buffer = ByteBuffer.allocate(255);
    
            stream.read(buffer.array(), 0, 2);
            
            int fiedVal = buffer.getShort(0) & 0xFFFF;

            //System.out.println("fieldVal = " + fiedVal);

            stream.read(buffer.array(), 0, 4);
            
            long value = buffer.getInt(0) & 0xFFFFFFFF;

            //System.out.println("value = " + value);
        }
        catch(IOException ex) 
        {
            System.out.println(ex);
        }
    }

    public static void StringToHexBytes(String strToConvert){
        try{
            int i = 0;

            String str = "";
            for(byte val : strToConvert.getBytes("UTF-8")){
                str += String.format("(byte)0x%x, ", val);
                i++;

                if(i > 0 && i % 8 == 0){
                    System.out.println(str);
                    str = "";
                }
                    
            }

            System.out.println(str);
        }
        catch(Exception ex){}
    }
    */
}
