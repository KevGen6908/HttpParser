package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client extends Thread{
    private static final Logger logger = Logger.getLogger(Client.class.getName());
    private static final int PORT = 9000;
    private static final String SERVER_ADDR = "localhost";
    private final String filePath;

    public Client(String filePath) {
        this.filePath = filePath;
    }


    @Override
    public void run(){
        try (SocketChannel socketChannel = SocketChannel.open()){
            socketChannel.connect(new InetSocketAddress(SERVER_ADDR, PORT));
            logger.info("Connect to server");

            String httpRequest = readRequestFromFile(filePath);

            sendRequest(socketChannel, httpRequest);

            logger.info("HTTP request sent to server");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error occurred while connecting to server", e);
        }
    }

    private void sendRequest(SocketChannel socketChannel, String request) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(request.getBytes());
        while (buffer.hasRemaining()) {
            socketChannel.write(buffer);
        }
    }

    private String readRequestFromFile(String filePath) throws IOException {
        StringBuilder requestBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                requestBuilder.append(line).append("\r\n");
            }
        }
        return requestBuilder.toString();
    }
}
