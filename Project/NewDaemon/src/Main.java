import java.io.*;
import java.net.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.Scanner;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;

import static java.nio.file.Files.readAllBytes;

// Main class for our Java daemon
public class Main {
    static BufferedReader input;
    static PrintWriter output;
    static InputStream inputStream;
    static OutputStream outputStream;

    public static void main(String[] args) throws IOException {
        // Read the socket port from config file
        String port = readConfigFile();
        int portNumber = Integer.parseInt(port); // Convert port string to integer
        System.out.println("Listening to:"+portNumber);

        // Connect to the server
        Socket socket = new Socket("",portNumber);

        // Create a BufferedReader to read messages from the server
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();

        input = new BufferedReader(new InputStreamReader(inputStream));

        // Create a PrintWriter to send messages to the server
        output = new PrintWriter(outputStream, true);

        // Create a thread to listen for incoming messages from the server
        Thread listenerThread = new Thread(() -> {
            String message;
//            try {
//
//            } catch (IOException e) {
//                System.err.println("Error reading from server: " + e.getMessage());
//            }
        });
        listenerThread.start();

        // Create a thread to read messages from the command-line and send them to the server
        Thread senderThread = new Thread(() -> {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String message;
            try {
                while ((message = reader.readLine()) != null) {
                    processCommand(message);
                }
            } catch (IOException e) {
                System.err.println("Error sending message to server: " + e.getMessage());
            }
        });
        senderThread.start();

        // Wait for both threads to finish
        try {
            listenerThread.join();
            senderThread.join();
        } catch (InterruptedException e) {
            System.err.println("Threads interrupted: " + e.getMessage());
        }

        // Close the socket when done
        socket.close();

    }

    // Reads the config file for the listening port

    private static String readConfigFile()
    {
        BufferedReader br = null;
        String port = null;

        try {
            br = new BufferedReader(new FileReader("config.txt"));
            String currentLine;
            while ((currentLine = br.readLine()) != null) {
                if (currentLine.startsWith("LISTEN_PORT")) {
                    String[] parts = currentLine.split("=");
                    port = parts[1].trim();
                    break; // stop at the first line that starts with LISTEN_PORT
                }
            }
        } catch (IOException e) {
            System.out.println("Couldn't not read Config file...");
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException ex) {
                System.out.println("Couldn't not read Config file...");
            }
        }
        return port;
    }

    // Processes and acts upon a command
    private static void processCommand(String command) {
        // Split the command into two parts (command and argument)
        String[] parts = command.split(" ");

        String cmd = parts[0];
        String arg = parts.length>1?parts[1]:"";

        switch (cmd) {
            case "SEND":
                // Send contents of a file
                try {
                    sendFile(arg);
                } catch (IOException e) {
                    System.out.println("Error Occured while Sending Data...");
                }
                break;
            case "SENDT":
                // Send last 500 lines of a file
                try {
                    sendLastLinesOfFile(arg);
                } catch (IOException e) {
                    System.out.println("Error Occured while Sending Data...");
                }
                break;
            case "STOP":
                // Stop a currently running process
                try {
                    stopProcess(arg);
                } catch (IOException e) {
                    System.out.println("Can not find process...");
                } catch (InterruptedException e) {
                    System.out.println("Can not find process...");
                }
                break;
            case "START":
                // Start a process
                try {
                    startProcess(arg);
                } catch (IOException e) {
                    System.out.println("Can not find process...");
                }
                break;
            case "PS":
                // Send back full list of OS process info in JSON
                sendOSProcessInfoInJson();
                break;
            case "UPDATE":
                // Finds the current node.conf in the local directory and replaces it
                try {
                    updateFile(arg);
                } catch (IOException e) {
                    System.out.println("No Such File exist...");
                }
                break;
            case "LS":
                // Send back file list of files in a subfolder
                sendFileListFromSubFolder(arg);
                break;
            default:
                System.out.println("Unrecognized command");
                break;
        }
    }

    // Implement the actual functionalities here
    private static void sendFile(String filename) throws IOException {
        output.println("***SEND***"+filename);
        System.out.println("SENDING :"+filename);

        BufferedReader br = new BufferedReader(new FileReader(filename));
        String currentLine;
        while ((currentLine = br.readLine()) != null) {
            output.write(currentLine+"\n");
        }

        output.println("***FINISHED***");
        System.out.println("FINISHED SENDING\n");
    }
    private static void sendLastLinesOfFile(String filename) throws IOException {
        output.println("***SENDT***"+filename);
        System.out.println("SENDING :"+filename);

        List<String> data = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String currentLine;
        while ((currentLine = br.readLine()) != null) {
            data.add(currentLine+"\n");
            if(data.size()>500)data.remove(0);
        }

        int co = 0;
        while(co<data.size())
        {
            output.write(data.get(co));
            co++;
        }
        output.println("***FINISHED***");
        System.out.println("FINISHED SENDING\n");

    }
    private static void stopProcess(String processName) throws IOException, InterruptedException {
        System.out.println("STOPPING: "+processName);

        String jarName = processName;
        String pid = "";
        try {
            Process p = Runtime.getRuntime().exec("jps -lV");
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                if (line.contains(jarName)) {
                    pid = line.split("\\s")[0]; // Get the first field (PID)
                    break;
                }
            }
            input.close();
            if (pid.isEmpty()) {
                System.err.println("Could not find PID for " + jarName);
            } else {
                System.out.println("PID for " + jarName + " is " + pid);
            }
        } catch (IOException | SecurityException e) {
            System.out.println("Error Occured killing process...");
            return;
        }

        ProcessBuilder pb = new ProcessBuilder("kill", "-9", pid);
        try {
            Process p = pb.start();
            p.waitFor();
            if (p.exitValue() == 0) {
                System.out.println("Process " + pid + " killed successfully.\n");
            } else {
                System.err.println("Failed to kill process " + pid + ". Exit code: " + p.exitValue());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error Occured killing process...");
        }
    }
    private static void startProcess(String filename) throws IOException {

        System.out.println("STARTING PROCESS: "+filename);
        ProcessBuilder pb = new ProcessBuilder("xterm", "-e", "java", "-jar", filename);
        pb.directory(new File("."));
        Process p = pb.start();

        System.out.println("STARTED: \n");
    }
    private static void sendOSProcessInfoInJson() {

        System.out.println("Sending Process Data...");
        output.println("***PS***");
        try{
            Process p = Runtime.getRuntime().exec("jps -lV");
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                output.write(line+"\n");
            }
            input.close();
        } catch (IOException | SecurityException e) {
            System.out.println("ISSUE Occured while getting Data...");
        }
        output.println("***FINISHED***");
        System.out.println("FINISHED SENDING PS\n");
    }
    private static void updateFile(String filename) throws IOException {
        output.println("***UPDATE***");

        String rece_data;
        FileOutputStream fileOutputStream = new FileOutputStream(filename);

        System.out.println("Update File Receving...");
        while ((rece_data = input.readLine()) != null) {
            if (rece_data.startsWith("***FINISHED***")) break;
            rece_data += "\n";
            fileOutputStream.write(rece_data.getBytes(), 0, rece_data.length());
        }
        System.out.println("Receiving finished...\nSuccessfully Updated\n");
        fileOutputStream.close();

    }
    private static void sendFileListFromSubFolder(String filename) {
        System.out.println("Send List of Subfolder...");
        output.println("***NS***");
        try{
            String folderPath = filename;
            try (java.util.stream.Stream<java.nio.file.Path> files = Files.list(Paths.get(folderPath))) {
                files.forEach(name -> output.println(name.getFileName()));
            }
        } catch (IOException | SecurityException e) {
            System.out.println("No Such Folder exist...");
        }
        output.println("***FINISHED***");
        System.out.println("FINISHED SENDING PS\n");
    }
}