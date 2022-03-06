package Test;

import java.util.*;
import java.nio.*;
import java.io.*;

import Commons.CommandV2;
import static Test.TestMain.assertThis;

public class CommandV2Test implements TestSwitch {
    public ArrayList<String> runTests()
    {
        ArrayList<String> failedTests = new ArrayList<String>();

        try
        {
            this.itShouldprocessesInputStreamAndReturnATextCommand();
            System.out.println("itShouldprocessesInputStreamAndReturnATextCommand - Passed");
        }
        catch(AssertionError ex)
        {
            failedTests.add("itShouldprocessesInputStreamAndReturnATextCommand - " + ex.getMessage());
        }

        try
        {
            this.itShouldprocessesInputStreamAndReturnAUUIDCommand();
            System.out.println("itShouldprocessesInputStreamAndReturnAUUIDCommand - Passed");
        }
        catch(AssertionError ex)
        {
            failedTests.add("itShouldprocessesInputStreamAndReturnAUUIDCommand - " + ex.getMessage());
        }

        try
        {
            this.itShouldprocessesInputStreamAndReturnACloseCommand();
            System.out.println("itShouldprocessesInputStreamAndReturnACloseCommand - Passed");
        }
        catch(AssertionError ex)
        {
            failedTests.add("itShouldprocessesInputStreamAndReturnACloseCommand - " + ex.getMessage());
        }

        try
        {
            this.itShouldprocessesInputStreamAndReturnAFileCommand();
            System.out.println("itShouldprocessesInputStreamAndReturnAFileCommand - Passed");
        }
        catch(AssertionError ex)
        {
            failedTests.add("itShouldprocessesInputStreamAndReturnAFileCommand - " + ex.getMessage());
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

        return failedTests;
    }

    public void itShouldprocessesInputStreamAndReturnATextCommand()
    {

        // given
        byte[] input  = new byte[] {    
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

        // when
        CommandV2 command = null;

        try(InputStream inputStream = new ByteArrayInputStream(input))
        {
            command = CommandV2.processesInputStream(inputStream);
        }
        catch(IOException ex){}

        // then
        byte[] expectedContent = new byte[] {
            (byte)0x61 // a
        };

        CommandV2 expectedCommand = new CommandV2(CommandV2.CommandType.TEXT, "a", 1645994122L, ByteBuffer.wrap(expectedContent));

        assertThis(command != null, "Command is null");

        assertThis(expectedCommand.getType() == command.getType(), 
                   String.format("Command type don't match: - Expected: %s, Given: %s", expectedCommand.getType().name(), command.getType().name()));
        assertThis(expectedCommand.getUsername() == command.getUsername(),
                   String.format("Username don't match: - Expected: %s, Given: %s", expectedCommand.getUsername(), command.getUsername()));
        assertThis(expectedCommand.getTimestamp() == command.getTimestamp(),
                   String.format("Timestamp don't match: - Expected: %d, Given: %d", expectedCommand.getTimestamp(), command.getTimestamp()));
        assertThis(expectedCommand.getFileInfo() == command.getFileInfo(),
                   String.format("Fileinfo don't match: - Expected: %s, Given: %s", expectedCommand.getFileInfo(), command.getFileInfo()));
        
        assertThis(command.getContent() != null, "Content is null"); 
        assertThis(expectedCommand.getContent().array().length == command.getContent().array().length, "Content length don't match with the expected value");
        assertThis(Arrays.equals(expectedCommand.getContent().array(), command.getContent().array()), "Content value don't match with the expected value");
    }

    public void itShouldprocessesInputStreamAndReturnAUUIDCommand()
    {
        
        // given
        byte[] input  = new byte[] {    
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
        };

        // when
        CommandV2 command = null;

        try(InputStream inputStream = new ByteArrayInputStream(input)){
            command = CommandV2.processesInputStream(inputStream);
        }
        catch(IOException ex){}

        // then
        byte[] expectedContent = new byte[] {
            (byte)0x61, (byte)0x39, (byte)0x31, (byte)0x39, (byte)0x34, (byte)0x66, (byte)0x63, (byte)0x34, // a9194fc4-ab48-4a56-afe1-7508c6e25471
            (byte)0x2d, (byte)0x61, (byte)0x62, (byte)0x34, (byte)0x38, (byte)0x2d, (byte)0x34, (byte)0x61,
            (byte)0x35, (byte)0x36, (byte)0x2d, (byte)0x61, (byte)0x66, (byte)0x65, (byte)0x31, (byte)0x2d,
            (byte)0x37, (byte)0x35, (byte)0x30, (byte)0x38, (byte)0x63, (byte)0x36, (byte)0x65, (byte)0x32,
            (byte)0x35, (byte)0x34, (byte)0x37, (byte)0x31
        };

        CommandV2 expectedCommand = new CommandV2(CommandV2.CommandType.UUID, ByteBuffer.wrap(expectedContent));

        assertThis(command != null, "Command is null");

        assertThis(expectedCommand.getType() == command.getType(), 
                   String.format("Command type don't match: - Expected: %s, Given: %s", expectedCommand.getType().name(), command.getType().name()));
        assertThis(expectedCommand.getUsername() == command.getUsername(),
                   String.format("Username don't match: - Expected: %s, Given: %s", expectedCommand.getUsername(), command.getUsername()));
        assertThis(expectedCommand.getTimestamp() == command.getTimestamp(),
                   String.format("Timestamp don't match: - Expected: %d, Given: %d", expectedCommand.getTimestamp(), command.getTimestamp()));
        assertThis(expectedCommand.getFileInfo() == command.getFileInfo(),
                   String.format("Fileinfo don't match: - Expected: %s, Given: %s", expectedCommand.getFileInfo(), command.getFileInfo()));
        
        assertThis(command.getContent() != null, "Content is null"); 
        assertThis(expectedCommand.getContent().array().length == command.getContent().array().length, "Content length don't match with the expected value");
        assertThis(Arrays.equals(expectedCommand.getContent().array(), command.getContent().array()), "Content value don't match with the expected value");
    }

    public void itShouldprocessesInputStreamAndReturnACloseCommand()
    {
        // given
        byte[] input  = new byte[] {    
            (byte)0xf7, (byte)0xf3,                                                                         // [INI]
            (byte)0x48, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x08,                         // HS      = 8 
            (byte)0x54, (byte)0xff,                                                                         // T       = CLOSE (0xFF) (2  B)
            (byte)0x43, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,                         // CS      = 0            (6  B)                                                 
            (byte)0xf3, (byte)0xf7                                                                          // [FIM]
        };

        // when
        CommandV2 command = null;

        try(InputStream inputStream = new ByteArrayInputStream(input)){
            command = CommandV2.processesInputStream(inputStream);
        }
        catch(IOException ex){}

        // then
        CommandV2 expectedCommand = new CommandV2(CommandV2.CommandType.CLOSE);

        assertThis(command != null, "Command is null");

        assertThis(expectedCommand.getType() == command.getType(), 
                   String.format("Command type don't match: - Expected: %s, Given: %s", expectedCommand.getType().name(), command.getType().name()));
        assertThis(expectedCommand.getUsername() == command.getUsername(),
                   String.format("Username don't match: - Expected: %s, Given: %s", expectedCommand.getUsername(), command.getUsername()));
        assertThis(expectedCommand.getTimestamp() == command.getTimestamp(),
                   String.format("Timestamp don't match: - Expected: %d, Given: %d", expectedCommand.getTimestamp(), command.getTimestamp()));
        assertThis(expectedCommand.getFileInfo() == command.getFileInfo(),
                   String.format("Fileinfo don't match: - Expected: %s, Given: %s", expectedCommand.getFileInfo(), command.getFileInfo()));
        assertThis(expectedCommand.getContent() == command.getContent(), 
                   String.format("Content don't match: - Expected: %s, Given: %s", expectedCommand.getContent(), command.getContent()));
    }

    public void itShouldprocessesInputStreamAndReturnAFileCommand()
    {
        // given
        byte[] input  = new byte[] {    
            (byte)0xf7, (byte)0xf3,                                                                         // [INI]
            (byte)0x48, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x22,                         // HS      = 34
            (byte)0x54, (byte)0x02,                                                                         // T       = FILE (0x03) (2  B)
            (byte)0x55, (byte)0x53, (byte)0x00, (byte)0x01,                                                 // US      = 1           (4  B)
            (byte)0x55, (byte)0x61,                                                                         // U       = a           (2  B)
            (byte)0x54, (byte)0x50, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x62, (byte)0x1b, // TP      = 1645994122  (10 B)
            (byte)0xe0, (byte)0x8a,
            (byte)0x46, (byte)0x53, (byte)0x00, (byte)0x05,                                                 // FS      = 5           (4  B)
            (byte)0x46, (byte)0x61, (byte)0x2e, (byte)0x74, (byte)0x78, (byte)0x74,                         // F       = a.txt       (6  B)
            (byte)0x43, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01,                         // CS      = 1           (6  B)
            (byte)0x61,                                                                                     // Content = a           (1  B)
            (byte)0xf3, (byte)0xf7                                                                          // [FIM]
        };

         // when
        CommandV2 command = null;

        try(InputStream inputStream = new ByteArrayInputStream(input)){
            command = CommandV2.processesInputStream(inputStream);
        }
        catch(IOException ex){}

        // then
        byte[] expectedContent = new byte[] {
            (byte)0x61 // a
        };

        CommandV2.FileInfo expectedFileInfo = new CommandV2.FileInfo("a.txt");

        CommandV2 expectedCommand = new CommandV2(CommandV2.CommandType.FILE, "a", 1645994122L, expectedFileInfo, ByteBuffer.wrap(expectedContent));

        assertThis(command != null, "Command is null");

        assertThis(expectedCommand.getType() == command.getType(), 
                   String.format("Command type don't match: - Expected: %s, Given: %s", expectedCommand.getType().name(), command.getType().name()));
        assertThis(expectedCommand.getUsername() == command.getUsername(),
                   String.format("Username don't match: - Expected: %s, Given: %s", expectedCommand.getUsername(), command.getUsername()));
        assertThis(expectedCommand.getTimestamp() == command.getTimestamp(),
                   String.format("Timestamp don't match: - Expected: %d, Given: %d", expectedCommand.getTimestamp(), command.getTimestamp()));
        
        assertThis(command.getFileInfo() != null, "FileInfo is null"); 
        assertThis(expectedCommand.getFileInfo().getFilename() == command.getFileInfo().getFilename(),
                   String.format("Filename don't match: - Expected: %s, Given: %s", expectedCommand.getFileInfo().getFilename(), command.getFileInfo().getFilename()));
        
        assertThis(command.getContent() != null, "Content is null"); 
        assertThis(expectedCommand.getContent().array().length == command.getContent().array().length, "Content length length don't match with the expected value");
        assertThis(Arrays.equals(expectedCommand.getContent().array(), command.getContent().array()), "Content value don't match with the expected value");
    }

    public void itShouldProcessesInputCommandAndThrowException()
    {
        // given
        ArrayList<byte[]> inputs = new ArrayList<byte[]>();

        // command package without [INI] mark
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

        // command package without [FIM] mark
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

        // command package without header size
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

        // command package without type
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

        // command package without content size
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

        // command a text or a file package without username 
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

        // command a text or a file package without timestamp 
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

        // command a text or a file package without content with content size grater than 0
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

        // command a file package without file name
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

        for(int i = 0; i < inputs.size() - 1; i++) 
        {
            boolean hasThrown = false;

            try(InputStream inputStream = new ByteArrayInputStream(inputs.get(i))){
                CommandV2.processesInputStream(inputStream);
            }
            catch(IOException ex){}
            catch(IllegalArgumentException ex){ hasThrown = true; }

            assertThis(hasThrown, String.format("For test %d wasn't throw an IllegalArgumentException", i + 1));
        }
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

        commandsToSerialize.add(new CommandV2(CommandV2.CommandType.TEXT, "a", 1645994122L, ByteBuffer.wrap(expectedContent)));

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

        commandsToSerialize.add(new CommandV2(CommandV2.CommandType.UUID, ByteBuffer.wrap(expectedContent)));

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
        commandsToSerialize.add(new CommandV2(CommandV2.CommandType.CLOSE));

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

        commandsToSerialize.add(new CommandV2(CommandV2.CommandType.TEXT, "a", 1645994122L, expectedFileInfo, ByteBuffer.wrap(expectedContent)));

        expectedOutputs.add(new byte[] {    
            (byte)0xf7, (byte)0xf3,                                                                         // [INI]
            (byte)0x48, (byte)0x53, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x22,                         // HS      = 34
            (byte)0x54, (byte)0x02,                                                                         // T       = FILE (0x03) (2  B)
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


        byte[] serizalizedCommand;
        byte[] expectedOutput;
        for(int i = 0; i < commandsToSerialize.size(); i++){
            CommandV2 commandToSerizalize = commandsToSerialize.get(i);

            serizalizedCommand = commandToSerizalize.serialize();

            expectedOutput = expectedOutputs.get(i);

            assertThis(serizalizedCommand != null, String.format("SerializedCommand is null on %d test case", i + 1)); 
            assertThis(serizalizedCommand.length == expectedOutput.length, 
                       String.format("SerializedCommand length don't match with the expected value on %d test case", i + 1));
            assertThis(Arrays.equals(serizalizedCommand, expectedOutput), 
                       String.format("SerializedCommand value don't match with the expected value on %d test case", i + 1));
        }
    }
}

