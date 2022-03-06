package Test;

import java.util.*;

public class TestMain{

    // A crude implementation of a unit test engine
    public static void main(String[] args) {
        TestSwitch[] testSwitches = new TestSwitch[] {
            new CommandV2Test()
        };

        ArrayList<String> failedTests = new ArrayList<String>();

        System.out.println();

        for(TestSwitch testSwitch : testSwitches){
            failedTests.addAll(testSwitch.runTests());
        }
        
        System.out.println();

        for (String failedTest : failedTests) {
            System.err.println(failedTest);
        }

        System.out.println();
    }   

    public static void assertThis(boolean test, String message){
        if(!test)
            throw new AssertionError(message);
    }

    public static void assertThis(boolean test){
        TestMain.assertThis(test, "Failed assert");
    }
}

interface TestSwitch {
    public ArrayList<String> runTests();

}