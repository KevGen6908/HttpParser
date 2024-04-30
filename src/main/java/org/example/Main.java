package org.example;

import java.io.IOException;

public class Main {
    private static final int PORT = 9000;
    private static final String SERVER_ADDR = "localhost";
    private static final String REQUEST_FILE_PATH = "src/main/resources/HTTPRequest.txt";

    public static void main(String[] args) {
        try {
            Thread serverThread = new Thread(() -> {
                try {
                    HTTPServer httpServer = new HTTPServer(PORT, SERVER_ADDR);
                    httpServer.startServer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            serverThread.start();

            Thread.sleep(1000);
            Client client = new Client(REQUEST_FILE_PATH);
            client.startClient();

            serverThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}