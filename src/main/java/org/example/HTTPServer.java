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
    private static final Logger logger = Logger.getLogger(HTTPServer.class.getName());

    public HTTPServer(int PORT, String SERVER_ADDR) {
        this.PORT = PORT;
        this.SERVER_ADDR = SERVER_ADDR;
    }

    public void startServer() throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(SERVER_ADDR, PORT));
        logger.info("The server is running. Waiting for connections on " + SERVER_ADDR + ":" + PORT + " " + Thread.currentThread().getName());

        SocketChannel clientChannel = serverSocketChannel.accept();
        InetSocketAddress clientAddress = (InetSocketAddress) clientChannel.getRemoteAddress();
        while (true) {
            logger.info("Connection accepted from: " + clientAddress.getHostString() + ":" + clientAddress.getPort() + " " + Thread.currentThread().getName());

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int bytesRead = clientChannel.read(buffer);

            if (bytesRead == -1) {
                logger.info("Connection closed");
                break;
            } else {
                buffer.flip();
                byte[] data = new byte[buffer.remaining()];
                buffer.get(data);

                String request = new String(data, StandardCharsets.UTF_8);
                logger.info("Received request:");
                System.out.println(request);
                handleClientRequest(request);

                logger.info("Send response to client");
                sendResponse(clientChannel, method, responseBody);
            }
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
            logger.log(Level.SEVERE, "Error occurred while handling client request", e);
        }
    }

    public void sendResponse(SocketChannel channel, String method, String responseBody) {
        try {
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
                case "PATCH":
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
                channel.write(buffer);
            }

            //channel.shutdownOutput();
            logger.info("Server sent response and closed socket");

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error occurred while sending response", e);
            throw new RuntimeException(e);
        }
    }
}
