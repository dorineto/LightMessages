package Commons;

public class TestCommons {
    public static void main(String[] args){
        // 014060ba-05af-4837-b945-59737b4e7d9c
        //a9194fc4-ab48-4a56-afe1-7508c6e25471
        String uuidStr = "a.txt";

        try{
            int i = 0;

            String str = "";
            for(byte val : uuidStr.getBytes("UTF-8")){
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
}
