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
    private final Object socketLock = new Object();

    public Client(String filePath) {
        this.filePath = filePath;
    }


    public void startClient(){
        try (SocketChannel socketChannel = SocketChannel.open()){
            socketChannel.connect(new InetSocketAddress(SERVER_ADDR, PORT));
            logger.info("Connect to server " +  Thread.currentThread().getName());

            String httpRequest = readRequestFromFile(filePath);

            sendRequest(socketChannel, httpRequest);

            logger.info("HTTP request sent to server " +  Thread.currentThread().getName());
            receiveResponse();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error occurred while connecting to server " +  Thread.currentThread().getName(), e);
        }
    }

    private void sendRequest(SocketChannel socketChannel, String request) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(request.getBytes());
        synchronized (socketLock) {
            while (buffer.hasRemaining()) {
                socketChannel.write(buffer);
            }
            socketChannel.shutdownOutput();
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

    public void receiveResponse() throws IOException {
        try (SocketChannel socketChannel = SocketChannel.open()) {
            socketChannel.connect(new InetSocketAddress(SERVER_ADDR, PORT));

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            StringBuilder responseBuilder = new StringBuilder();

            System.out.println("Waiting for response. Thread " +  Thread.currentThread().getName());
            System.out.println("socket contents " + socketChannel.read(buffer));

            while (socketChannel.read(buffer) > 0) {
                buffer.flip();
                while (buffer.hasRemaining()) {
                    char c = (char) buffer.get();
                    responseBuilder.append(c);
                }
                buffer.clear();
            }

            System.out.println("A response was received from the server: " + responseBuilder.toString());

            logger.info("Received response from server. " + Thread.currentThread().getName());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error occurred while receiving response from server", e);
            throw e;
        }
    }

}
