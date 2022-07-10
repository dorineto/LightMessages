package Commons;

import java.nio.*;
import java.io.*;
import java.util.*;
import java.util.stream.*;
import java.time.LocalDateTime;
import java.security.*;
import javax.xml.bind.DatatypeConverter;

public class TestCommons {
    public static void main(String[] args){
        byte[] input = new byte[] {    
            (byte)0xf7, (byte)0xf3,                                                                         // [INI]
            (byte)0x48, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x18,                         // HS      = 24
            (byte)0x54, (byte)0x02,                                                                         // T       = TEXT (0x02) (2  B)
            (byte)0x55, (byte)0x53, (byte)0x00, (byte)0x01,                                                 // US      = 1           (4  B)
            (byte)0x55, (byte)0x61,                                                                         // U       = a           (2  B)
            (byte)0x54, (byte)0x50, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x62, (byte)0x1b, // TP      = 1645994122  (10 B)
            (byte)0xe0, (byte)0x8a, 
            (byte)0x43, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01,                         // CS      = 1           (6  B)
            (byte)0x61,                                                                                     // Content = a           (1  B)
            (byte)0xf3, (byte)0xf7                                                                          // [FIM]
        };

        String hexOutput = "";

        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            hexOutput = byteArrayToHex(md.digest(input)).toLowerCase();
        }
        catch(Exception ex) {}
        
        System.out.println(String.format("hex = %s", hexOutput));
    }

    public static String byteArrayToHex(byte[] a) {
        String wholeHexString = DatatypeConverter.printHexBinary(a).toUpperCase();

        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < Math.ceil(wholeHexString.length() / 2); i++)
            sb.append(String.format("%c%c ", wholeHexString.charAt(i * 2), wholeHexString.charAt(i * 2 + 1)));

        return sb.toString();
     }

    /*
    public static void main(String[] args){
        
        byte[] arrayA = new byte[] { 1, 2, 3 };
        byte[] arrayB = new byte[] { 4, 5, 6 };


        byte[] arrayC = null;
        byte[] arrayD = null;
        try(ByteArrayOutputStream output = new ByteArrayOutputStream()){
            output.write(arrayA, 0, arrayA.length);
            output.write(arrayB, 0, arrayB.length);

            System.out.println("size=" + output.size());

            arrayC = output.toByteArray();

            for(byte val : arrayC)
                System.out.print(val + " ");

            output.reset();

            output.write(arrayB, 0, arrayB.length);
            output.write(arrayA, 0, arrayA.length);

            arrayD = output.toByteArray();

            System.out.println("\n");

            for(byte val : arrayC)
                System.out.print(val + " ");

                System.out.println("");

            for(byte val : arrayD)
                System.out.print(val + " ");
        }
        catch(Exception ex){}

    }
    */
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

