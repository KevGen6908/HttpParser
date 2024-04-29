package org.example;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        HTTPServer httpServer = new HTTPServer(9000, "localhost");
        Thread serverThread = new Thread(() -> httpServer.startServer());
        serverThread.start();

        Client client = new Client("path/to/request.txt");
        client.sendRequest();

    }

}