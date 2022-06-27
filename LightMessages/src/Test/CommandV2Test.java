package Test;

import java.util.*;
import java.nio.*;
import java.io.*;

import Commons.CommandV2;
import Commons.CommandV2.CommandTypeV2;
import Commons.CommandV2.FileInfo;

import static Test.TestMain.assertThis;

public class CommandV2Test implements TestSwitch {
    public ArrayList<String> runTests()
    {
        ArrayList<String> failedTests = new ArrayList<String>();

        try
        {
            this.itShouldprocessesInputStreamAndReturnCommand();
            System.out.println("itShouldprocessesInputStreamAndReturnCommand - Passed");
        }
        catch(AssertionError ex)
        {
            failedTests.add("itShouldprocessesInputStreamAndReturnCommand - " + ex.getMessage());
        }

        try
        {
            this.itShouldProcessesInputCommandAndThrowException();
            System.out.println("itShouldValidateCommandAndPass - Passed");
        }
        catch(AssertionError ex)
        {
            failedTests.add("itShouldValidateCommandAndPass - " + ex.getMessage());
        }

        try
        {
            this.itShouldSerializeCommands();
            System.out.println("itShouldSerializeCommands - Passed");
        }
        catch(AssertionError ex)
        {
            failedTests.add("itShouldSerializeCommands - " + ex.getMessage());
        }

        try
        {
            this.itShouldReadInputAndSerializeOutputBeEquals();
            System.out.println("itShouldReadInputAndSerializeOutputBeEquals - Passed");
        }
        catch(AssertionError ex)
        {
            failedTests.add("itShouldReadInputAndSerializeOutputBeEquals - " + ex.getMessage());
        }

        try
        {
            this.itShouldCreateCommandAndThrowException();
            System.out.println("itShouldCreateCommandAndThrowException - Passed");
        }
        catch(AssertionError ex)
        {
            failedTests.add("itShouldCreateCommandAndThrowException - " + ex.getMessage());
        }

        return failedTests;
    }

    public void itShouldprocessesInputStreamAndReturnCommand()
    {
        //given
        ArrayList<byte[]> inputs = new ArrayList<byte[]>();

        ArrayList<CommandV2> expectedCommands = new ArrayList<CommandV2>();

        // TEXT
        inputs.add(new byte[] {    
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
        });

        byte[] expectedContent = new byte[] {
            (byte)0x61 // a
        };

        expectedCommands.add(new CommandV2(CommandV2.CommandTypeV2.TEXT, "a", 1645994122L, new ByteBuffer[] { ByteBuffer.wrap(expectedContent) }));

        // UUID
        inputs.add(new byte[] {    
            (byte)0xf7, (byte)0xf3,                                                                         // [INI]
            (byte)0x48, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x08,                         // HS      = 8 
            (byte)0x54, (byte)0x01,                                                                         // T       = UUID (0x01)                          (2  B)
            (byte)0x43, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x24,                         // CS      = 36                                   (6  B)
            (byte)0x61, (byte)0x39, (byte)0x31, (byte)0x39, (byte)0x34, (byte)0x66, (byte)0x63, (byte)0x34, // Content = a9194fc4-ab48-4a56-afe1-7508c6e25471 (36 B)
            (byte)0x2d, (byte)0x61, (byte)0x62, (byte)0x34, (byte)0x38, (byte)0x2d, (byte)0x34, (byte)0x61,
            (byte)0x35, (byte)0x36, (byte)0x2d, (byte)0x61, (byte)0x66, (byte)0x65, (byte)0x31, (byte)0x2d,
            (byte)0x37, (byte)0x35, (byte)0x30, (byte)0x38, (byte)0x63, (byte)0x36, (byte)0x65, (byte)0x32,
            (byte)0x35, (byte)0x34, (byte)0x37, (byte)0x31,                                                 
            (byte)0xf3, (byte)0xf7                                                                          // [FIM]
        });

        expectedContent = new byte[] {
            (byte)0x61, (byte)0x39, (byte)0x31, (byte)0x39, (byte)0x34, (byte)0x66, (byte)0x63, (byte)0x34, // a9194fc4-ab48-4a56-afe1-7508c6e25471
            (byte)0x2d, (byte)0x61, (byte)0x62, (byte)0x34, (byte)0x38, (byte)0x2d, (byte)0x34, (byte)0x61,
            (byte)0x35, (byte)0x36, (byte)0x2d, (byte)0x61, (byte)0x66, (byte)0x65, (byte)0x31, (byte)0x2d,
            (byte)0x37, (byte)0x35, (byte)0x30, (byte)0x38, (byte)0x63, (byte)0x36, (byte)0x65, (byte)0x32,
            (byte)0x35, (byte)0x34, (byte)0x37, (byte)0x31
        };

        expectedCommands.add(new CommandV2(CommandV2.CommandTypeV2.UUID, new ByteBuffer[] { ByteBuffer.wrap(expectedContent) }));
        
        // CLOSE
        inputs.add(new byte[] {    
            (byte)0xf7, (byte)0xf3,                                                                         // [INI]
            (byte)0x48, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x08,                         // HS      = 8 
            (byte)0x54, (byte)0xff,                                                                         // T       = CLOSE (0xFF) (2  B)
            (byte)0x43, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,                         // CS      = 0            (6  B)                                                 
            (byte)0xf3, (byte)0xf7                                                                          // [FIM]
        });

        expectedCommands.add(new CommandV2(CommandV2.CommandTypeV2.CLOSE));

        // FILE
        inputs.add(new byte[] {    
            (byte)0xf7, (byte)0xf3,                                                                         // [INI]
            (byte)0x48, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x22,                         // HS      = 34
            (byte)0x54, (byte)0x03,                                                                         // T       = FILE (0x03) (2  B)
            (byte)0x55, (byte)0x53, (byte)0x00, (byte)0x01,                                                 // US      = 1           (4  B)
            (byte)0x55, (byte)0x61,                                                                         // U       = a           (2  B)
            (byte)0x54, (byte)0x50, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x62, (byte)0x1b, // TP      = 1645994122  (10 B)
            (byte)0xe0, (byte)0x8a,
            (byte)0x46, (byte)0x53, (byte)0x00, (byte)0x05,                                                 // FS      = 5           (4  B)
            (byte)0x46, (byte)0x61, (byte)0x2e, (byte)0x74, (byte)0x78, (byte)0x74,                         // F       = a.txt       (6  B)
            (byte)0x43, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01,                         // CS      = 1           (6  B)
            (byte)0x61,                                                                                     // Content = a           (1  B)
            (byte)0xf3, (byte)0xf7                                                                          // [FIM]
        });

        expectedContent = new byte[] {
            (byte)0x61 // a
        };

        CommandV2.FileInfo expectedFileInfo = new CommandV2.FileInfo("a.txt");

        expectedCommands.add(new CommandV2(CommandV2.CommandTypeV2.FILE, "a", 1645994122L, expectedFileInfo, new ByteBuffer[] { ByteBuffer.wrap(expectedContent) }));

        // ACK
        inputs.add(new byte[] {    
            (byte)0xf7, (byte)0xf3,                                                                         // [INI]
            (byte)0x48, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x08,                         // HS      = 8 
            (byte)0x54, (byte)0x04,                                                                         // T       = ACK (0x04)                           (2  B)
            (byte)0x43, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x20,                         // CS      = 32                                   (6  B)
            (byte)0x0c, (byte)0x47, (byte)0x27, (byte)0xa9, (byte)0xdf, (byte)0x6e, (byte)0x7e, (byte)0x53, // Content = 0c4727a9df6e7e538c500cd4ee746e294ae6622a58f63480ec7f4d23e7bd3463 (34 B)
            (byte)0x8c, (byte)0x50, (byte)0x0c, (byte)0xd4, (byte)0xee, (byte)0x74, (byte)0x6e, (byte)0x29, 
            (byte)0x4a, (byte)0xe6, (byte)0x62, (byte)0x2a, (byte)0x58, (byte)0xf6, (byte)0x34, (byte)0x80, 
            (byte)0xec, (byte)0x7f, (byte)0x4d, (byte)0x23, (byte)0xe7, (byte)0xbd, (byte)0x34, (byte)0x63,                                               
            (byte)0xf3, (byte)0xf7                                                                          // [FIM]
        });

        expectedContent = new byte[] {
            (byte)0x0c, (byte)0x47, (byte)0x27, (byte)0xa9, (byte)0xdf, (byte)0x6e, (byte)0x7e, (byte)0x53, // Content = 0c4727a9df6e7e538c500cd4ee746e294ae6622a58f63480ec7f4d23e7bd3463 (34 B)
            (byte)0x8c, (byte)0x50, (byte)0x0c, (byte)0xd4, (byte)0xee, (byte)0x74, (byte)0x6e, (byte)0x29, 
            (byte)0x4a, (byte)0xe6, (byte)0x62, (byte)0x2a, (byte)0x58, (byte)0xf6, (byte)0x34, (byte)0x80, 
            (byte)0xec, (byte)0x7f, (byte)0x4d, (byte)0x23, (byte)0xe7, (byte)0xbd, (byte)0x34, (byte)0x63
        };

        expectedCommands.add(new CommandV2(CommandV2.CommandTypeV2.ACK, new ByteBuffer[] { ByteBuffer.wrap(expectedContent) }));


        // when
        CommandV2 command = null;
        CommandV2 expectedCommand = null;

        for(int i = 0; i < inputs.size(); i++){

            try(InputStream inputStream = new ByteArrayInputStream(inputs.get(i))){
                command = CommandV2.processesInputStream(inputStream);
            }
            catch(IOException ex){}

            // then
            expectedCommand = expectedCommands.get(i);

            String typeName = expectedCommand.getType().name();

            assertThis(command != null, 
                    String.format("[TYPE = %s] - Command is null", typeName));

            assertThis(expectedCommand.getType() == command.getType(), 
                    String.format("[TYPE = %s] - Command type don't match: - Expected: %s, Given: %s", typeName, expectedCommand.getType().name(), command.getType().name()));
            
            assertThis(expectedCommand.getUsername() == command.getUsername() || expectedCommand.getUsername().equals(command.getUsername()),
                    String.format("[TYPE = %s] - Username don't match: - Expected: %s, Given: %s", typeName, expectedCommand.getUsername(), command.getUsername()));
            assertThis(expectedCommand.getTimestamp() == command.getTimestamp() || expectedCommand.getTimestamp().equals(command.getTimestamp()),
                    String.format("[TYPE = %s] - Timestamp don't match: - Expected: %s, Given: %s", typeName, expectedCommand.getTimestamp(), command.getTimestamp()));

            assertThis(expectedCommand.getFileInfo() == command.getFileInfo() || expectedCommand.getFileInfo().equals(command.getFileInfo()),
                    String.format("[TYPE = %s] - Filename don't match: - Expected: %s, Given: %s", typeName, expectedCommand.getFileInfo(), command.getFileInfo()));

            boolean contentCheck = command.getContent() == expectedCommand.getContent() || 
                                   (command.getContent().length == expectedCommand.getContent().length &&
                                   Arrays.equals(expectedCommand.getContent()[0].array(), command.getContent()[0].array()));

            assertThis(contentCheck, 
                    String.format("[TYPE = %s] - Content don't match with the expected value ", typeName)); 
        }

    }

    public void itShouldProcessesInputCommandAndThrowException()
    {
        // given
        ArrayList<byte[]> inputs = new ArrayList<byte[]>();

        ArrayList<String> testCasesLabel = new ArrayList<String>();

        testCasesLabel.add("command package without [INI] mark");
        inputs.add(new byte[] {    
            (byte)0x48, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x18,                         // HS      = 24
            (byte)0x54, (byte)0x02,                                                                         // T       = TEXT (0x02) (2  B)
            (byte)0x55, (byte)0x53, (byte)0x00, (byte)0x01,                                                 // US      = 1           (4  B)
            (byte)0x55, (byte)0x61,                                                                         // U       = a           (2  B)
            (byte)0x54, (byte)0x50, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x62, (byte)0x1b, // TP      = 1645994122  (10 B)
            (byte)0xe0, (byte)0x8a, 
            (byte)0x43, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01,                         // CS      = 1           (6  B)
            (byte)0x61,                                                                                     // Content = a           (1  B)
            (byte)0xf3, (byte)0xf7                                                                          // [FIM]
        });

        testCasesLabel.add("command package without [FIM] mark");
        inputs.add(new byte[] {
            (byte)0xf7, (byte)0xf3,                                                                         // [INI]    
            (byte)0x48, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x18,                         // HS      = 24
            (byte)0x54, (byte)0x02,                                                                         // T       = TEXT (0x02) (2  B)
            (byte)0x55, (byte)0x53, (byte)0x00, (byte)0x01,                                                 // US      = 1           (4  B)
            (byte)0x55, (byte)0x61,                                                                         // U       = a           (2  B)
            (byte)0x54, (byte)0x50, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x62, (byte)0x1b, // TP      = 1645994122  (10 B)
            (byte)0xe0, (byte)0x8a, 
            (byte)0x43, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01,                         // CS      = 1           (6  B)
            (byte)0x61,                                                                                     // Content = a           (1  B)
        });

        testCasesLabel.add("command package without header size");
        inputs.add(new byte[] {    
            (byte)0xf7, (byte)0xf3,                                                                         // [INI]
            (byte)0x54, (byte)0x02,                                                                         // T       = TEXT (0x02) (2  B)
            (byte)0x55, (byte)0x53, (byte)0x00, (byte)0x01,                                                 // US      = 1           (4  B)
            (byte)0x55, (byte)0x61,                                                                         // U       = a           (2  B)
            (byte)0x54, (byte)0x50, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x62, (byte)0x1b, // TP      = 1645994122  (10 B)
            (byte)0xe0, (byte)0x8a, 
            (byte)0x43, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01,                         // CS      = 1           (6  B)
            (byte)0x61,                                                                                     // Content = a           (1  B)
            (byte)0xf3, (byte)0xf7                                                                          // [FIM]
        });

        testCasesLabel.add("command package without type");
        inputs.add(new byte[] {    
            (byte)0xf7, (byte)0xf3,                                                                         // [INI]
            (byte)0x48, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x18,                         // HS      = 24
            (byte)0x55, (byte)0x53, (byte)0x00, (byte)0x01,                                                 // US      = 1           (4  B)
            (byte)0x55, (byte)0x61,                                                                         // U       = a           (2  B)
            (byte)0x54, (byte)0x50, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x62, (byte)0x1b, // TP      = 1645994122  (10 B)
            (byte)0xe0, (byte)0x8a, 
            (byte)0x43, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01,                         // CS      = 1           (6  B)
            (byte)0x61,                                                                                     // Content = a           (1  B)
            (byte)0xf3, (byte)0xf7                                                                          // [FIM]
        });

        testCasesLabel.add("command package without content size");
        inputs.add(new byte[] {    
            (byte)0xf7, (byte)0xf3,                                                                         // [INI]
            (byte)0x48, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x18,                         // HS      = 24
            (byte)0x54, (byte)0x02,                                                                         // T       = TEXT (0x02) (2  B)
            (byte)0x55, (byte)0x53, (byte)0x00, (byte)0x01,                                                 // US      = 1           (4  B)
            (byte)0x55, (byte)0x61,                                                                         // U       = a           (2  B)
            (byte)0x54, (byte)0x50, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x62, (byte)0x1b, // TP      = 1645994122  (10 B)
            (byte)0xe0, (byte)0x8a, 
            (byte)0x61,                                                                                     // Content = a           (1  B)
            (byte)0xf3, (byte)0xf7                                                                          // [FIM]
        });

        testCasesLabel.add("command a text or a file package without username");
        inputs.add(new byte[] {    
            (byte)0xf7, (byte)0xf3,                                                                         // [INI]
            (byte)0x48, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x18,                         // HS      = 24
            (byte)0x54, (byte)0x02,                                                                         // T       = TEXT (0x02) (2  B)
            (byte)0x54, (byte)0x50, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x62, (byte)0x1b, // TP      = 1645994122  (10 B)
            (byte)0xe0, (byte)0x8a, 
            (byte)0x43, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01,                         // CS      = 1           (6  B)
            (byte)0x61,                                                                                     // Content = a           (1  B)
            (byte)0xf3, (byte)0xf7                                                                          // [FIM]
        });

        testCasesLabel.add("command a text or a file package without timestamp");
        inputs.add(new byte[] {    
            (byte)0xf7, (byte)0xf3,                                                                         // [INI]
            (byte)0x48, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x18,                         // HS      = 24
            (byte)0x54, (byte)0x02,                                                                         // T       = TEXT (0x02) (2  B)
            (byte)0x55, (byte)0x53, (byte)0x00, (byte)0x01,                                                 // US      = 1           (4  B)
            (byte)0x55, (byte)0x61,                                                                         // U       = a           (2  B)
            (byte)0xe0, (byte)0x8a, 
            (byte)0x43, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01,                         // CS      = 1           (6  B)
            (byte)0x61,                                                                                     // Content = a           (1  B)
            (byte)0xf3, (byte)0xf7                                                                          // [FIM]
        });

        testCasesLabel.add("command a text or a file package without content with content size grater than 0");
        inputs.add(new byte[] {    
            (byte)0xf7, (byte)0xf3,                                                                         // [INI]
            (byte)0x48, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x18,                         // HS      = 24
            (byte)0x54, (byte)0x02,                                                                         // T       = TEXT (0x02) (2  B)
            (byte)0x55, (byte)0x53, (byte)0x00, (byte)0x01,                                                 // US      = 1           (4  B)
            (byte)0x55, (byte)0x61,                                                                         // U       = a           (2  B)
            (byte)0xe0, (byte)0x8a, 
            (byte)0x43, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01,                         // CS      = 1           (6  B)
            (byte)0xf3, (byte)0xf7                                                                          // [FIM]
        });

        testCasesLabel.add("command a file package without file name");
        inputs.add(new byte[] {    
            (byte)0xf7, (byte)0xf3,                                                                         // [INI]
            (byte)0x48, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x22,                         // HS      = 34
            (byte)0x54, (byte)0x02,                                                                         // T       = FILE (0x03) (2  B)
            (byte)0x55, (byte)0x53, (byte)0x00, (byte)0x01,                                                 // US      = 1           (4  B)
            (byte)0x55, (byte)0x61,                                                                         // U       = a           (2  B)
            (byte)0x54, (byte)0x50, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x62, (byte)0x1b, // TP      = 1645994122  (10 B)
            (byte)0xe0, (byte)0x8a,
            (byte)0x43, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01,                         // CS      = 1           (6  B)
            (byte)0x61,                                                                                     // Content = a           (1  B)
            (byte)0xf3, (byte)0xf7                                                                          // [FIM]
        });

        // then
        for(int i = 0; i < inputs.size() - 1; i++) 
        {
            boolean hasThrown = false;

            try(InputStream inputStream = new ByteArrayInputStream(inputs.get(i))){
                CommandV2.processesInputStream(inputStream);
            }
            catch(IOException ex){}
            catch(IllegalArgumentException ex){ hasThrown = true; }

            assertThis(hasThrown, String.format("For test [%s] wasn't throw an IllegalArgumentException", testCasesLabel.get(i)));
        }
    }
    
    public void itShouldCreateCommandAndThrowException()
    {
        // given
        CommandV2 auxCommand = null;

        ByteBuffer[] content  = new ByteBuffer[]{
            ByteBuffer.wrap(new byte[] {
                (byte)0x61 // a
            })
        };

        // then
        // TEXT tests
        String testCaseDescriprion = "Text command without username";
        try {
            auxCommand = new CommandV2(CommandTypeV2.TEXT, null, 1645994122l, null, content);
        }
        catch(IllegalArgumentException ex){ auxCommand = null; }

        assertThis(auxCommand == null, String.format("For test [%s] wasn't throw an IllegalArgumentException", testCaseDescriprion));

        testCaseDescriprion = "Text command without timestamp";
        try {
            auxCommand = new CommandV2(CommandTypeV2.TEXT, "a", null, null, content);
        }
        catch(IllegalArgumentException ex){ auxCommand = null; }

        assertThis(auxCommand == null, String.format("For test [%s] wasn't throw an IllegalArgumentException", testCaseDescriprion));

        testCaseDescriprion = "Text command without content";
        try {
            auxCommand = new CommandV2(CommandTypeV2.TEXT, "a", 1645994122l, null, null);
        }
        catch(IllegalArgumentException ex){ auxCommand = null; }

        assertThis(auxCommand == null, String.format("For test [%s] wasn't throw an IllegalArgumentException", testCaseDescriprion));


        // UUID tests
        testCaseDescriprion = "UUID command without content";
        try {
            auxCommand = new CommandV2(CommandTypeV2.UUID, null, null, null, null);
        }
        catch(IllegalArgumentException ex){ auxCommand = null; }

        assertThis(auxCommand == null, String.format("For test [%s] wasn't throw an IllegalArgumentException", testCaseDescriprion));


        // FILE tests
        FileInfo fileInfo = new FileInfo("a.txt");

        testCaseDescriprion = "File command without username";
        try {
            auxCommand = new CommandV2(CommandTypeV2.FILE, null, 1645994122l, fileInfo, content);
        }
        catch(IllegalArgumentException ex){ auxCommand = null; }

        assertThis(auxCommand == null, String.format("For test [%s] wasn't throw an IllegalArgumentException", testCaseDescriprion));

        testCaseDescriprion = "File command without timestamp";
        try {
            auxCommand = new CommandV2(CommandTypeV2.FILE, "a", null, fileInfo, content);
        }
        catch(IllegalArgumentException ex){ auxCommand = null; }

        assertThis(auxCommand == null, String.format("For test [%s] wasn't throw an IllegalArgumentException", testCaseDescriprion));

        testCaseDescriprion = "File command without file info";
        try {
            auxCommand = new CommandV2(CommandTypeV2.FILE, "a", 1645994122l, null, content);
        }
        catch(IllegalArgumentException ex){ auxCommand = null; }

        assertThis(auxCommand == null, String.format("For test [%s] wasn't throw an IllegalArgumentException", testCaseDescriprion));

        testCaseDescriprion = "File command without content";
        try {
            auxCommand = new CommandV2(CommandTypeV2.FILE, "a", 1645994122l, fileInfo, null);
        }
        catch(IllegalArgumentException ex){ auxCommand = null; }

        assertThis(auxCommand == null, String.format("For test [%s] wasn't throw an IllegalArgumentException", testCaseDescriprion));

        // ACK tests
        testCaseDescriprion = "ACK command without content";
        try {
            auxCommand = new CommandV2(CommandTypeV2.ACK, null, null, null, null);
        }
        catch(IllegalArgumentException ex){ auxCommand = null; }

        assertThis(auxCommand == null, String.format("For test [%s] wasn't throw an IllegalArgumentException", testCaseDescriprion));
    }

    public void itShouldSerializeCommands()
    {
        // given
        ArrayList<CommandV2> commandsToSerialize = new ArrayList<CommandV2>();

        ArrayList<byte[]> expectedOutputs = new ArrayList<byte[]>();

        // TEXT
        byte[] expectedContent = new byte[] {
            (byte)0x61 // a
        };

        commandsToSerialize.add(new CommandV2(CommandV2.CommandTypeV2.TEXT, "a", 1645994122L, new ByteBuffer[] { ByteBuffer.wrap(expectedContent) }));

        expectedOutputs.add(new byte[] {    
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
        });


        // UUID
        expectedContent = new byte[] {
            (byte)0x61, (byte)0x39, (byte)0x31, (byte)0x39, (byte)0x34, (byte)0x66, (byte)0x63, (byte)0x34, // a9194fc4-ab48-4a56-afe1-7508c6e25471
            (byte)0x2d, (byte)0x61, (byte)0x62, (byte)0x34, (byte)0x38, (byte)0x2d, (byte)0x34, (byte)0x61,
            (byte)0x35, (byte)0x36, (byte)0x2d, (byte)0x61, (byte)0x66, (byte)0x65, (byte)0x31, (byte)0x2d,
            (byte)0x37, (byte)0x35, (byte)0x30, (byte)0x38, (byte)0x63, (byte)0x36, (byte)0x65, (byte)0x32,
            (byte)0x35, (byte)0x34, (byte)0x37, (byte)0x31
        };

        commandsToSerialize.add(new CommandV2(CommandV2.CommandTypeV2.UUID, new ByteBuffer[] { ByteBuffer.wrap(expectedContent) }));

        expectedOutputs.add(new byte[] {    
            (byte)0xf7, (byte)0xf3,                                                                         // [INI]
            (byte)0x48, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x08,                         // HS      = 8 
            (byte)0x54, (byte)0x01,                                                                         // T       = UUID (0x01)                          (2  B)
            (byte)0x43, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x24,                         // CS      = 36                                   (6  B)
            (byte)0x61, (byte)0x39, (byte)0x31, (byte)0x39, (byte)0x34, (byte)0x66, (byte)0x63, (byte)0x34, // Content = a9194fc4-ab48-4a56-afe1-7508c6e25471 (36 B)
            (byte)0x2d, (byte)0x61, (byte)0x62, (byte)0x34, (byte)0x38, (byte)0x2d, (byte)0x34, (byte)0x61,
            (byte)0x35, (byte)0x36, (byte)0x2d, (byte)0x61, (byte)0x66, (byte)0x65, (byte)0x31, (byte)0x2d,
            (byte)0x37, (byte)0x35, (byte)0x30, (byte)0x38, (byte)0x63, (byte)0x36, (byte)0x65, (byte)0x32,
            (byte)0x35, (byte)0x34, (byte)0x37, (byte)0x31,                                                 
            (byte)0xf3, (byte)0xf7                                                                          // [FIM]
        });

        // CLOSE
        commandsToSerialize.add(new CommandV2(CommandV2.CommandTypeV2.CLOSE));

        expectedOutputs.add(new byte[] {    
            (byte)0xf7, (byte)0xf3,                                                                         // [INI]
            (byte)0x48, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x08,                         // HS      = 8 
            (byte)0x54, (byte)0xff,                                                                         // T       = CLOSE (0xFF) (2  B)
            (byte)0x43, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,                         // CS      = 0            (6  B)                                                 
            (byte)0xf3, (byte)0xf7                                                                          // [FIM]
        });

        // FILE
        expectedContent = new byte[] {
            (byte)0x61 // a
        };

        CommandV2.FileInfo expectedFileInfo = new CommandV2.FileInfo("a.txt");

        commandsToSerialize.add(new CommandV2(CommandV2.CommandTypeV2.FILE, "a", 1645994122L, expectedFileInfo, new ByteBuffer[] { ByteBuffer.wrap(expectedContent) }));

        expectedOutputs.add(new byte[] {    
            (byte)0xf7, (byte)0xf3,                                                                         // [INI]
            (byte)0x48, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x22,                         // HS      = 34
            (byte)0x54, (byte)0x03,                                                                         // T       = FILE (0x03) (2  B)
            (byte)0x55, (byte)0x53, (byte)0x00, (byte)0x01,                                                 // US      = 1           (4  B)
            (byte)0x55, (byte)0x61,                                                                         // U       = a           (2  B)
            (byte)0x54, (byte)0x50, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x62, (byte)0x1b, // TP      = 1645994122  (10 B)
            (byte)0xe0, (byte)0x8a,
            (byte)0x46, (byte)0x53, (byte)0x00, (byte)0x05,                                                 // FS      = 5           (4  B)
            (byte)0x46, (byte)0x61, (byte)0x2e, (byte)0x74, (byte)0x78, (byte)0x74,                         // F       = a.txt       (6  B)
            (byte)0x43, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01,                         // CS      = 1           (6  B)
            (byte)0x61,                                                                                     // Content = a           (1  B)
            (byte)0xf3, (byte)0xf7                                                                          // [FIM]
        });

        // ACK
        expectedContent = new byte[] {
            (byte)0x0c, (byte)0x47, (byte)0x27, (byte)0xa9, (byte)0xdf, (byte)0x6e, (byte)0x7e, (byte)0x53, // Content = 0c4727a9df6e7e538c500cd4ee746e294ae6622a58f63480ec7f4d23e7bd3463 (34 B)
            (byte)0x8c, (byte)0x50, (byte)0x0c, (byte)0xd4, (byte)0xee, (byte)0x74, (byte)0x6e, (byte)0x29, 
            (byte)0x4a, (byte)0xe6, (byte)0x62, (byte)0x2a, (byte)0x58, (byte)0xf6, (byte)0x34, (byte)0x80, 
            (byte)0xec, (byte)0x7f, (byte)0x4d, (byte)0x23, (byte)0xe7, (byte)0xbd, (byte)0x34, (byte)0x63
        };

        commandsToSerialize.add(new CommandV2(CommandV2.CommandTypeV2.ACK, new ByteBuffer[] { ByteBuffer.wrap(expectedContent) }));

        expectedOutputs.add(new byte[] {    
            (byte)0xf7, (byte)0xf3,                                                                         // [INI]
            (byte)0x48, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x08,                         // HS      = 8 
            (byte)0x54, (byte)0x04,                                                                         // T       = ACK (0x04)                           (2  B)
            (byte)0x43, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x20,                         // CS      = 32                                   (6  B)
            (byte)0x0c, (byte)0x47, (byte)0x27, (byte)0xa9, (byte)0xdf, (byte)0x6e, (byte)0x7e, (byte)0x53, // Content = 0c4727a9df6e7e538c500cd4ee746e294ae6622a58f63480ec7f4d23e7bd3463 (34 B)
            (byte)0x8c, (byte)0x50, (byte)0x0c, (byte)0xd4, (byte)0xee, (byte)0x74, (byte)0x6e, (byte)0x29, 
            (byte)0x4a, (byte)0xe6, (byte)0x62, (byte)0x2a, (byte)0x58, (byte)0xf6, (byte)0x34, (byte)0x80, 
            (byte)0xec, (byte)0x7f, (byte)0x4d, (byte)0x23, (byte)0xe7, (byte)0xbd, (byte)0x34, (byte)0x63,                                               
            (byte)0xf3, (byte)0xf7                                                                          // [FIM]
        });

        byte[] serizalizedCommand;
        byte[] expectedOutput;
        for(int i = 0; i < commandsToSerialize.size(); i++){
            CommandV2 commandToSerizalize = commandsToSerialize.get(i);

            serizalizedCommand = commandToSerizalize.serialize();

            expectedOutput = expectedOutputs.get(i);

            String typeName = commandToSerizalize.getType().name();

            assertThis(serizalizedCommand != null, String.format("SerializedCommand is null on %s test case", typeName)); 
            assertThis(serizalizedCommand.length == expectedOutput.length, 
                       String.format("SerializedCommand length don't match with the expected value on %s test case", typeName));
            assertThis(Arrays.equals(serizalizedCommand, expectedOutput), 
                       String.format("SerializedCommand value don't match with the expected value on %s test case", typeName));
        }
    }

    public void itShouldReadInputAndSerializeOutputBeEquals()
    {
        //given
        ArrayList<byte[]> inputs = new ArrayList<byte[]>();

        ArrayList<String> testCasesLabel  = new ArrayList<String>();

        testCasesLabel.add("TEXT");
        inputs.add(new byte[] {    
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
        });

        testCasesLabel.add("UUID");
        inputs.add(new byte[] {    
            (byte)0xf7, (byte)0xf3,                                                                         // [INI]
            (byte)0x48, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x08,                         // HS      = 8 
            (byte)0x54, (byte)0x01,                                                                         // T       = UUID (0x01)                          (2  B)
            (byte)0x43, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x24,                         // CS      = 36                                   (6  B)
            (byte)0x61, (byte)0x39, (byte)0x31, (byte)0x39, (byte)0x34, (byte)0x66, (byte)0x63, (byte)0x34, // Content = a9194fc4-ab48-4a56-afe1-7508c6e25471 (36 B)
            (byte)0x2d, (byte)0x61, (byte)0x62, (byte)0x34, (byte)0x38, (byte)0x2d, (byte)0x34, (byte)0x61,
            (byte)0x35, (byte)0x36, (byte)0x2d, (byte)0x61, (byte)0x66, (byte)0x65, (byte)0x31, (byte)0x2d,
            (byte)0x37, (byte)0x35, (byte)0x30, (byte)0x38, (byte)0x63, (byte)0x36, (byte)0x65, (byte)0x32,
            (byte)0x35, (byte)0x34, (byte)0x37, (byte)0x31,                                                 
            (byte)0xf3, (byte)0xf7                                                                          // [FIM]
        });
        
        testCasesLabel.add("CLOSE");
        inputs.add(new byte[] {    
            (byte)0xf7, (byte)0xf3,                                                                         // [INI]
            (byte)0x48, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x08,                         // HS      = 8 
            (byte)0x54, (byte)0xff,                                                                         // T       = CLOSE (0xFF) (2  B)
            (byte)0x43, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,                         // CS      = 0            (6  B)                                                 
            (byte)0xf3, (byte)0xf7                                                                          // [FIM]
        });

        testCasesLabel.add("FILE");
        inputs.add(new byte[] {    
            (byte)0xf7, (byte)0xf3,                                                                         // [INI]
            (byte)0x48, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x22,                         // HS      = 34
            (byte)0x54, (byte)0x03,                                                                         // T       = FILE (0x03) (2  B)
            (byte)0x55, (byte)0x53, (byte)0x00, (byte)0x01,                                                 // US      = 1           (4  B)
            (byte)0x55, (byte)0x61,                                                                         // U       = a           (2  B)
            (byte)0x54, (byte)0x50, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x62, (byte)0x1b, // TP      = 1645994122  (10 B)
            (byte)0xe0, (byte)0x8a,
            (byte)0x46, (byte)0x53, (byte)0x00, (byte)0x05,                                                 // FS      = 5           (4  B)
            (byte)0x46, (byte)0x61, (byte)0x2e, (byte)0x74, (byte)0x78, (byte)0x74,                         // F       = a.txt       (6  B)
            (byte)0x43, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01,                         // CS      = 1           (6  B)
            (byte)0x61,                                                                                     // Content = a           (1  B)
            (byte)0xf3, (byte)0xf7                                                                          // [FIM]
        });

        testCasesLabel.add("ACK");
        inputs.add(new byte[] {    
            (byte)0xf7, (byte)0xf3,                                                                         // [INI]
            (byte)0x48, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x08,                         // HS      = 8 
            (byte)0x54, (byte)0x04,                                                                         // T       = ACK (0x04)                           (2  B)
            (byte)0x43, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x20,                         // CS      = 32                                   (6  B)
            (byte)0x0c, (byte)0x47, (byte)0x27, (byte)0xa9, (byte)0xdf, (byte)0x6e, (byte)0x7e, (byte)0x53, // Content = 0c4727a9df6e7e538c500cd4ee746e294ae6622a58f63480ec7f4d23e7bd3463 (34 B)
            (byte)0x8c, (byte)0x50, (byte)0x0c, (byte)0xd4, (byte)0xee, (byte)0x74, (byte)0x6e, (byte)0x29, 
            (byte)0x4a, (byte)0xe6, (byte)0x62, (byte)0x2a, (byte)0x58, (byte)0xf6, (byte)0x34, (byte)0x80, 
            (byte)0xec, (byte)0x7f, (byte)0x4d, (byte)0x23, (byte)0xe7, (byte)0xbd, (byte)0x34, (byte)0x63,                                               
            (byte)0xf3, (byte)0xf7                                                                          // [FIM]
        });

        // when
        CommandV2 command = null;
        byte[] serializedCommand = null;

        for(int i = 0; i < inputs.size(); i++){

            try(InputStream inputStream = new ByteArrayInputStream(inputs.get(i))){
                command = CommandV2.processesInputStream(inputStream);
            }
            catch(IOException ex){}
            
            // then
            assertThis(command != null, "Command is null");

            serializedCommand = command.serialize();

            assertThis(Arrays.equals(inputs.get(i), serializedCommand), 
                    String.format("[%s] the input bytes and the serialized command don't match", testCasesLabel.get(i)));
            
        }

    }

}

