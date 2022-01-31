package Commons;

public class TestCommons{
    public static void main(String[] args) {
        try{
            System.out.println("Hello World!");

            throw new Exception("Exception");
        }
        catch(Exception ex){
            System.out.println(Logger.dumpException(ex));
        }
    }    
}