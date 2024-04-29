package org.example;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {
    private static final Logger logger = Logger.getLogger(Client.class.getName());
    private static final int PORT = 9000;
    private static final String SERVER_ADDR = "localhost";
    private final String filePath;
    private final Lock fileLock = new ReentrantLock();

    public Client(String filePath) {
        this.filePath = filePath;
    }


    public void startClient(){
        try (SocketChannel socketChannel = SocketChannel.open()){
            socketChannel.connect(new InetSocketAddress(SERVER_ADDR, PORT));
            logger.info("Connect to server");

            String httpRequest = readRequestFromFile(filePath);

            sendRequest(socketChannel, httpRequest);

            logger.info("HTTP request sent to server");
            receiveResponse(socketChannel);
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

    public void receiveResponse(SocketChannel socketChannel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        StringBuilder responseBuilder = new StringBuilder();
        while (socketChannel.read(buffer) > 0) {
            buffer.flip();
            while (buffer.hasRemaining()) {
                char c = (char) buffer.get();
                responseBuilder.append(c);
            }
            buffer.clear();
        }

        String response = responseBuilder.toString();
        try {
            fileLock.lock(); // Захватываем мьютекс
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("response.txt"))) {
                writer.write("response:\n");
                writer.write(response);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                fileLock.unlock(); // Освобождаем мьютекс
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(responseBuilder.toString());

        logger.info("Received response from server and saved it to 'response.txt'.");
    }

}
