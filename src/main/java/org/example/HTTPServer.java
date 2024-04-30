package org.example;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

class HTTPServer {
    private final int PORT;
    private final String SERVER_ADDR;
    private String method;
    private String responseBody;


    public HTTPServer(int PORT, String SERVER_ADDR) {
        this.PORT = PORT;
        this.SERVER_ADDR = SERVER_ADDR;
    }

    public void startServer() throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(SERVER_ADDR, PORT));
        System.out.println("The server is running. Waiting for connections on " + SERVER_ADDR + ":" + PORT + " " + Thread.currentThread().getName());

        SocketChannel clientChannel = serverSocketChannel.accept();
        InetSocketAddress clientAddress = (InetSocketAddress) clientChannel.getRemoteAddress();
        while (true) {

            System.out.println("Connection accepted from: " + clientAddress.getHostString() + ":" + clientAddress.getPort() + " " + Thread.currentThread().getName());

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int bytesRead = clientChannel.read(buffer);

            if (bytesRead == -1) {
                System.out.println("Connection closed");
                break;
            }else {
                buffer.flip();
                byte[] data = new byte[buffer.remaining()];
                buffer.get(data);

                String request = new String(data, StandardCharsets.UTF_8);

                System.out.println("Received request:\n" + request);
                handleClientRequest(request);

                System.out.println("Send response to client");
                sendResponse(method, responseBody);
            }
        }
    }

    private String readRequest(SocketChannel clientChannel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        StringBuilder requestBuilder = new StringBuilder();

        int bytesRead;
        while (true) {
            //System.out.println(clientChannel.read(buffer));
            bytesRead = clientChannel.read(buffer);

            if (bytesRead == -1) {
                clientChannel.close();
                break;
            }

            buffer.flip();
            while (buffer.hasRemaining()) {
                requestBuilder.append((char) buffer.get());
            }
            buffer.clear();
        }

        System.out.println(requestBuilder.toString());
        return requestBuilder.toString();
    }


    public void checkSocket(SocketChannel clientChannel) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            int bytesRead = clientChannel.read(buffer);

            buffer.flip();

            if (bytesRead > 0) {
                byte[] data = new byte[buffer.remaining()];
                buffer.get(data);
                String receivedData = new String(data);

                System.out.println("Data received from the client: " + receivedData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClientRequest(String request) {
        try {
            HTTPRequestParser parser = new HTTPRequestParser();
            parser.parserHttpRequest(request);
            method = parser.getMethod();
            String uri = parser.getUri();
            String body = parser.getBody();

            responseBody = "Method: " + method + "\nURI: " + uri + "\nBody: " + body;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendResponse(String method, String responseBody) {
        try(SocketChannel socketChannel = SocketChannel.open()){
            socketChannel.connect(new InetSocketAddress(SERVER_ADDR, PORT));

            String response;
            int codeStatus;
            String statusMessage;

            switch (method) {
                case "GET":
                    codeStatus = 200;
                    statusMessage = "OK";
                    break;
                case "POST":
                    codeStatus = 201;
                    statusMessage = "Created";
                    break;
                case "PUT":
                    codeStatus = 200;
                    statusMessage = "OK";
                    break;
                case "PATCH":
                    codeStatus = 200;
                    statusMessage = "OK";
                    break;
                case "DELETE":
                    codeStatus = 200;
                    statusMessage = "OK";
                    break;
                default:
                    codeStatus = 400;
                    statusMessage = "Bad Request";
                    responseBody = "Unsupported HTTP method";
            }

            response = "HTTP/1.1 " + codeStatus + " " + statusMessage + "\r\n" +
                    "Content-Length: " + responseBody.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n" +
                    responseBody;
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            ByteBuffer buffer = ByteBuffer.wrap(responseBytes);

            while (buffer.hasRemaining()) {
                socketChannel.write(buffer);
            }

            socketChannel.close();

            System.out.println("Server send response and closed socket");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}