package org.example;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        HTTPServer server = new HTTPServer(9000, "localhost");
        Client client = new Client("src/main/java/org/example/HTTPRequest.txt");

        server.start();
        client.start();

        try {
            server.join();
            client.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}