package Commons;

import java.util.*;
import java.time.*;

public class TestCommons{
    public static void main(String[] args) {
        /*
        Hashtable<String,String> table = new  Hashtable<String,String>();

        table.put("name", "A");
        table.put("textcolor", "green");
        table.put("likes", "green,bananas");

        Iterator<Map.Entry<String,String>> entrys = table.entrySet().iterator();

        Map.Entry<String,String> entry;
        while(entrys.hasNext()){
            entry = entrys.next();

            System.out.println(entry.getKey() + "=" + entry.getValue());
        }
        */

        System.out.println("Start - First Test - Text");
        Command command = new Command();

        Hashtable<String,String> info = command.getInfoDict();
        
        info.put("name", "a");
        info.put("datetime", LocalDateTime.parse("2022-02-26T01:55:42.488").toString());
        
        Hashtable<String,String> content = command.getContentDict();

        content.put("type", "0");
        content.put("content", "a");
        
        System.out.println(command.Serialize());
        //type=0|content=a&|name=a|datetime=2022-02-26T01:55:42.488
        //type=0|content=a&|name=a|datetime=2022-02-26T01:55:42.488
        if(!command.Serialize().equals( "type=0|content=a&|name=a|datetime=2022-02-26T01:55:42.488"))
            throw new RuntimeException("Faild");
        
        System.out.println("End - First Test - Text");

        System.out.println("Start - Second Test - UUID");
        command = new Command();
        
        content = command.getContentDict();

        content.put("type", "2");
        content.put("content", "a76ab33b-316f-4f33-bf9f-b547a892897e");
        
        System.out.println(command.Serialize());
        //type=2|content=a76ab33b-316f-4f33-bf9f-b547a892897e
        if(!command.Serialize().equals("type=2|content=a76ab33b-316f-4f33-bf9f-b547a892897e"))
            throw new RuntimeException("Faild");

        System.out.println("End - Second Test - UUID");

        System.out.println("Start - Third Test - File");
        command = new Command();

        info = command.getInfoDict();
        
        info.put("name", "a");
        info.put("datetime", LocalDateTime.parse("2022-02-26T02:44:42.489").toString());
        
        content = command.getContentDict();

        content.put("type", "1");
        content.put("content", "iVBORw0KGgoAAAANSUhEUgAAADkAAAAxCAIAAADvKZa/AAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAK7SURBVGhD7ZY7azJBFIa/n5nKUksLJdikEBFiI4GgUbzfTbzFW7ygoKIxCUZFGzEhoqSw0cpa88KebnUzG74JBOapds4Ou8+cOXP5d/g7CFc+CFc+CFc+CFc+qHPN5/O73Y4avw6r683Njd/v93g88/mcQr8Oq+v9/X0mkykUCp+fnxRSZL/fr1Yr9K/Vav1+v9vtVqvV2Wy2XC6ph3pYXYvFYjqdlv5HodN8fHyMx+Nms4nZCAQCuVwum826XK5KpZJMJvGK+qmE1fXh4SEUCuHf+B+FTrBYLMrlciQSgR+Gd319jeymUinUD3QvLy99Ph/6UG81sLp2Op1gMBgOh1utFoVOMBwOY7EYzDA2ZFEKwh6DTCQS0EXdv76+rtdr6RU7rK4ArldXV8gKtY8BUewVt7e31JaBoscAotHo4+MjhZhR4SpNKBJGbRnIvUajOTs7MxgMFJKB1Mbjca/X++38yGF1RTIws0iYwj+QVJPJpNVq9Xo9hY6Bj6CWBoMBtZlhdcWiluZOeW253e5vXe/u7lC+P9gNWF2RBiQDc1cqlSh0DIvFotPpzs/PqS3j4uLC6XRiSDCmEDOsrlgxqDOUrPKawPxarVZUArVlYO/D8sKnONar0Wi02WwOh0M5r6PRCCpYgtSWgXMBHVAD7+/vFGKG1dVsNkv3AZQshY6BUw2ngML8Yn/FR2CME5hCzLC61ut1XAlQA41Gg0InwG4gTTTqEgN7eXnBVKA2sDTtdju253a7PZlMqLcaWF2x/HFmompxEaHQCbbbba/XgxPyh7FhNnCAoUDxgAMWG9/b2xt1VQmrqypwfsISlwdcsiAHS+QVux4uQD++uAAurhLT6RQpfHp6wpJ/fn7ebDb04qdwdP3vCFc+CFc+CFc+CFc+CFc+CFc+CFc+CFc+CFc+CFc+/B3Xw+EL2Y7bsWLPxQgAAAAASUVORK5CYII=");
        content.put("filename", "teste_img.png");
        
        System.out.println(command.Serialize());
        //type=1|content=iVBORw0KGgoAAAANSUhEUgAAADkAAAAxCAIAAADvKZa/AAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAK7SURBVGhD7ZY7azJBFIa/n5nKUksLJdikEBFiI4GgUbzfTbzFW7ygoKIxCUZFGzEhoqSw0cpa88KebnUzG74JBOapds4Ou8+cOXP5d/g7CFc+CFc+CFc+CFc+qHPN5/O73Y4avw6r683Njd/v93g88/mcQr8Oq+v9/X0mkykUCp+fnxRSZL/fr1Yr9K/Vav1+v9vtVqvV2Wy2XC6ph3pYXYvFYjqdlv5HodN8fHyMx+Nms4nZCAQCuVwum826XK5KpZJMJvGK+qmE1fXh4SEUCuHf+B+FTrBYLMrlciQSgR+Gd319jeymUinUD3QvLy99Ph/6UG81sLp2Op1gMBgOh1utFoVOMBwOY7EYzDA2ZFEKwh6DTCQS0EXdv76+rtdr6RU7rK4ArldXV8gKtY8BUewVt7e31JaBoscAotHo4+MjhZhR4SpNKBJGbRnIvUajOTs7MxgMFJKB1Mbjca/X++38yGF1RTIws0iYwj+QVJPJpNVq9Xo9hY6Bj6CWBoMBtZlhdcWiluZOeW253e5vXe/u7lC+P9gNWF2RBiQDc1cqlSh0DIvFotPpzs/PqS3j4uLC6XRiSDCmEDOsrlgxqDOUrPKawPxarVZUArVlYO/D8sKnONar0Wi02WwOh0M5r6PRCCpYgtSWgXMBHVAD7+/vFGKG1dVsNkv3AZQshY6BUw2ngML8Yn/FR2CME5hCzLC61ut1XAlQA41Gg0InwG4gTTTqEgN7eXnBVKA2sDTtdju253a7PZlMqLcaWF2x/HFmompxEaHQCbbbba/XgxPyh7FhNnCAoUDxgAMWG9/b2xt1VQmrqypwfsISlwdcsiAHS+QVux4uQD++uAAurhLT6RQpfHp6wpJ/fn7ebDb04qdwdP3vCFc+CFc+CFc+CFc+CFc+CFc+CFc+CFc+CFc+CFc+/B3Xw+EL2Y7bsWLPxQgAAAAASUVORK5CYII=|filename=teste_img.png&|name=a|datetime=2022-02-26T02:44:42.489
        if(!command.Serialize().equals("type=1|content=iVBORw0KGgoAAAANSUhEUgAAADkAAAAxCAIAAADvKZa/AAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAK7SURBVGhD7ZY7azJBFIa/n5nKUksLJdikEBFiI4GgUbzfTbzFW7ygoKIxCUZFGzEhoqSw0cpa88KebnUzG74JBOapds4Ou8+cOXP5d/g7CFc+CFc+CFc+CFc+qHPN5/O73Y4avw6r683Njd/v93g88/mcQr8Oq+v9/X0mkykUCp+fnxRSZL/fr1Yr9K/Vav1+v9vtVqvV2Wy2XC6ph3pYXYvFYjqdlv5HodN8fHyMx+Nms4nZCAQCuVwum826XK5KpZJMJvGK+qmE1fXh4SEUCuHf+B+FTrBYLMrlciQSgR+Gd319jeymUinUD3QvLy99Ph/6UG81sLp2Op1gMBgOh1utFoVOMBwOY7EYzDA2ZFEKwh6DTCQS0EXdv76+rtdr6RU7rK4ArldXV8gKtY8BUewVt7e31JaBoscAotHo4+MjhZhR4SpNKBJGbRnIvUajOTs7MxgMFJKB1Mbjca/X++38yGF1RTIws0iYwj+QVJPJpNVq9Xo9hY6Bj6CWBoMBtZlhdcWiluZOeW253e5vXe/u7lC+P9gNWF2RBiQDc1cqlSh0DIvFotPpzs/PqS3j4uLC6XRiSDCmEDOsrlgxqDOUrPKawPxarVZUArVlYO/D8sKnONar0Wi02WwOh0M5r6PRCCpYgtSWgXMBHVAD7+/vFGKG1dVsNkv3AZQshY6BUw2ngML8Yn/FR2CME5hCzLC61ut1XAlQA41Gg0InwG4gTTTqEgN7eXnBVKA2sDTtdju253a7PZlMqLcaWF2x/HFmompxEaHQCbbbba/XgxPyh7FhNnCAoUDxgAMWG9/b2xt1VQmrqypwfsISlwdcsiAHS+QVux4uQD++uAAurhLT6RQpfHp6wpJ/fn7ebDb04qdwdP3vCFc+CFc+CFc+CFc+CFc+CFc+CFc+CFc+CFc+CFc+/B3Xw+EL2Y7bsWLPxQgAAAAASUVORK5CYII=|filename=teste_img.png&|name=a|datetime=2022-02-26T02:44:42.489"))
            throw new RuntimeException("Faild");

        System.out.println("End - Third Test - File");

    }    
}