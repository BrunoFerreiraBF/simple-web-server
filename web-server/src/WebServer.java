

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Bruno Ferreira on 29/06/2017.
 */
public class WebServer {


    public static void main(String[] args) {

        WebServer server = new WebServer();

        try {

            server.init();

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }

    /**
     * initializes the server and waits connection and initiates a ClientHandler Thread
     * @throws IOException
     */
    public void init() throws IOException {

        Socket clientSocket;
        ServerSocket serverSocket = new ServerSocket(1991);
        ExecutorService cachedPool = Executors.newCachedThreadPool();

        while (true) {

            System.out.println("* Waiting for connection...*");
            clientSocket = serverSocket.accept();

            // Allocate a Pool of an undefined number of threads
            cachedPool.submit(new ClientHandler(clientSocket));

        }
    }


    /**
     * receives request gets filepath see getFilePath
     * creates File
     * generates Header see generateHeader
     *
     * @param toClient an DataOutputStream
     * @param fromClient an BufferedReader
     * @throws IOException
     */
    private void start(DataOutputStream toClient, BufferedReader fromClient) throws IOException {

        //find file path and create a File with it
        String filePath = getFilePath(fromClient);


        File file = new File("www" + filePath);
        file = file.exists() ? file : new File("www/file_not_found");


        //generate header
        System.out.println(generateHeader(file));


        //send response to client
        System.out.println("i will send");

        toClient.write(generateHeader(file).getBytes());
        toClient.write(readFile(file));

        System.out.println("sending");

        toClient.flush();

        System.out.println("done");

    }


    /**
     * reads 1st line with BufferedReader fromClient
     *
     * @param fromClient an BufferedReader
     * @return String file Extension
     * @throws IOException
     */
    private String getFilePath(BufferedReader fromClient) throws IOException {

        String head1 = fromClient.readLine();

        if (head1.equals("null") || head1.split(" ")[1].equals("/")) {

            return "/index.html";
        }

        return head1.split(" ")[1];
    }


    /**
     * Reads all file bytes
     *
     * @param file
     * @return a byte[]
     * @throws IOException
     */
    private byte[] readFile(File file) throws IOException {

        return Files.readAllBytes(file.toPath());

        //TODO: write byte to byte to be faster
    }


    /**
     * generates Header according to client request
     *
     * @param file
     * @return String response Header
     */
    public String generateHeader(File file) {

        // Specific Headers
        if (getCode(file) == 404) {
            return "HTTP/1.0 " + 404 + " Not Found" + "\r\n"
                    + "Content-Type: " + getFileExtension(file) + "\r\n"
                    + "Content-Length: " + (new File("resources/file_not_found").length()) + " \r\n\r\n";
        }


        // Standard header
        return "HTTP/1.0 " + getCode(file) + " Document Follows" + "\r\n"
                + "Content-Type: " + getFileExtension(file) + "\r\n"
                + "Content-Length: " + file.length() + " \r\n\r\n";
    }


    /**
     * get file extension mime type
     *
     * @param file
     * @return String file extension
     */
    private String getFileExtension(File file) {

        String fileExtension;

        //if there is no extension
        if ((file.getName().indexOf(".")) == -1) {

            return "text/html; charset=UTF-8";

        } else {

            fileExtension = file.getName().substring(file.getName().indexOf("."));
        }


        // Specific file types
        if (fileExtension.equals(".jpg")) {
            return "image/png";
        }


        if (fileExtension.equals(".txt") || fileExtension.equals(".html")) {
            return "text/html; charset=UTF-8";
        }

        if (fileExtension.equals(".png")) {
            return "image/png";
        }


        return fileExtension;
    }


    /**
     * handles codes
     *
     * @param file
     * @return int code
     */
    private int getCode(File file) {

        return !file.exists() ? 404 : 200;
    }


    /**
     * inner class ClientHandler
     * Handles client request and responds
     */
    public class ClientHandler implements Runnable {

        private Socket clientSocket;

        /**
         * ClientHandler constructor
         *
         * @param clientSocket
         */
        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {

            System.out.println("\n***********************************\n" +
                    "* Client :  " + clientSocket.getRemoteSocketAddress().toString() +
                    " *\n***********************************\n");


            try {

                DataOutputStream toClient = new DataOutputStream(clientSocket.getOutputStream());

                BufferedReader fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));



                start(toClient, fromClient);

                clientSocket.close();

            } catch (IOException e) {

                System.err.println(e.getMessage());

            }

        }

    }



}