import java.net.*;
import java.io.*;
import java.util.Base64;

import static java.lang.System.exit;
import static java.lang.System.out;

public class Main {

    static BufferedReader in;
    static PrintWriter out;
    static Socket clientSocket;
    static InputStream inputStream;
    static int size;
    public static void main(String[] args) throws IOException {
        String port = readConfigFile();
        int portNumber = port!=null?Integer.parseInt(port):0;
        if(portNumber==0)
        {
            System.out.println("Server can't start...");
            exit(1);
        }

        try {
            ServerSocket server = new ServerSocket(portNumber);
            System.out.println("Server Started on port: "+portNumber);
            while (true) {
                clientSocket = server.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostName());
                // Handle client request
                try {
                    // read/write from/to socket
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    inputStream = clientSocket.getInputStream();
                    in = new BufferedReader(new InputStreamReader(inputStream));
                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println(message);
                        processData(message);
                    }
                } catch(SocketException e) {
                    // Handle disconnection
                    System.out.println("Client disconnected");
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }

    }

    private static String readConfigFile() {
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
            System.out.println("Config File doesn't exist...");
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return port;
    }

    public static void processData(String message) throws IOException {
        if(message.startsWith("***SEND***")) {
            String rece_data;
            FileOutputStream fileOutputStream = new FileOutputStream("received_file.txt");

            System.out.println("File Receving...");
            while ((rece_data = in.readLine()) != null) {
                System.out.println(rece_data);
                if (rece_data.startsWith("***FINISHED***")) break;
                rece_data += "\n";
                fileOutputStream.write(rece_data.getBytes(), 0, rece_data.length());
            }
            System.out.println("Receiving finished...\n");
            fileOutputStream.close();
        }
        else if(message.startsWith("***SENDT***")) {
            String rece_data;
            FileOutputStream fileOutputStream = new FileOutputStream("received_file.txt");

            System.out.println("File Receving...");
            while ((rece_data = in.readLine())!=null) {
                System.out.println(rece_data);
                if(rece_data.startsWith("***FINISHED***"))break;
                rece_data+="\n";
                fileOutputStream.write((rece_data).getBytes(), 0, rece_data.length());
            }
            System.out.println("Receiving finished...\nSaved to received_data.txt\n");

            fileOutputStream.close();
        }
        else if(message.startsWith("***PS***")) {
            String rece_data;
            FileOutputStream fileOutputStream = new FileOutputStream("received_process.txt");

            System.out.println("Process Data Receving...");
            while ((rece_data = in.readLine())!=null) {
                System.out.println(rece_data);
                if(rece_data.startsWith("***FINISHED***"))break;
                rece_data+="\n";
                fileOutputStream.write(rece_data.getBytes(), 0, rece_data.length());
            }
            System.out.println("Receiving finished...\nSaved to received_process.txt\n");
            fileOutputStream.close();
        }
        else if(message.startsWith("***NS***")) {
            String rece_data;
            FileOutputStream fileOutputStream = new FileOutputStream("received_subfolder_data.txt");

            System.out.println("Subfolder Data Receving...");
            while ((rece_data = in.readLine())!=null) {
                System.out.println(rece_data);
                if(rece_data.startsWith("***FINISHED***"))break;
                rece_data+="\n";
                fileOutputStream.write(rece_data.getBytes(), 0, rece_data.length());
            }
            System.out.println("Receiving finished...\nSaved to received_subfolder_data.txt\n");
            fileOutputStream.close();
        }
        else if(message.startsWith("***UPDATE***"))
        {
            System.out.println("SENDING file to update...");

            BufferedReader br = new BufferedReader(new FileReader("file_to_send.txt"));
            String currentLine;
            while ((currentLine = br.readLine()) != null) {
                out.println(currentLine);
            }

            out.println("***FINISHED***");

            System.out.println("SENDING finished...\n");
        }
    }

}