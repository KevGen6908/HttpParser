package org.example;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;




public class MyHTTPServer {
    private final int port;
    private final String host;

    public MyHTTPServer(int port, String host) {
        this.port = port;
        this.host = host;
    }

    public void start() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.bind(new InetSocketAddress(host, port));
            System.out.println("HTTP Server started on " + host + ":" + port);

            while (true) {
                SocketChannel clientChannel = serverSocketChannel.accept();
                System.out.println("Accepted connection from: " + clientChannel.getRemoteAddress());

                handleRequest(clientChannel);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRequest(SocketChannel clientChannel) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            StringBuilder requestBuilder = new StringBuilder();

            while (clientChannel.read(buffer) > 0) {
                buffer.flip();
                while (buffer.hasRemaining()) {
                    requestBuilder.append((char) buffer.get());
                }
                buffer.clear();

                if (requestBuilder.toString().endsWith("\r\n\r\n")) {
                    break;
                }
            }

            String request = requestBuilder.toString();
            System.out.println("Received request:\n" + request);

            // Parse request
            String[] lines = request.split("\\r?\\n");
            String[] requestLine = lines[0].split("\\s");
            String method = requestLine[0];
            String path = requestLine[1];

            Map<String, String> headers = new HashMap<>();
            for (int i = 1; i < lines.length; i++) {
                String[] headerParts = lines[i].split(":", 2);
                headers.put(headerParts[0].trim(), headerParts[1].trim());
            }

            // Handle request method
            String responseBody;
            switch (method) {
                case "GET":
                    responseBody = "Response to GET request";
                    break;
                case "POST":
                    responseBody = "Response to POST request";
                    break;
                case "PUT":
                    responseBody = "Response to PUT request";
                    break;
                case "PATCH":
                    responseBody = "Response to PATCH request";
                    break;
                case "DELETE":
                    responseBody = "Response to DELETE request";
                    break;
                default:
                    responseBody = "Unsupported HTTP method";
            }

            sendResponse(clientChannel, 200, "OK", responseBody);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendResponse(SocketChannel clientChannel, int statusCode, String statusText, String body) throws IOException {
        String response = "HTTP/1.1 " + statusCode + " " + statusText + "\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                body;

        clientChannel.write(ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8)));
        System.out.println("Sent response to client.");
    }


    public class HTTPClient {

        public static void main(String[] args) {
            String host = "localhost";
            int port = 8080;

            try (SocketChannel socketChannel = SocketChannel.open()) {
                socketChannel.connect(new InetSocketAddress(host, port));

                String request = "GET / HTTP/1.1\r\n" +
                        "Host: " + host + "\r\n" +
                        "Connection: close\r\n\r\n";

                ByteBuffer buffer = ByteBuffer.wrap(request.getBytes(StandardCharsets.UTF_8));
                socketChannel.write(buffer);

                buffer.clear();
                StringBuilder responseBuilder = new StringBuilder();

                while (socketChannel.read(buffer) > 0) {
                    buffer.flip();
                    while (buffer.hasRemaining()) {
                        responseBuilder.append((char) buffer.get());
                    }
                    buffer.clear();
                }

                System.out.println("Response from server:");
                System.out.println(responseBuilder.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        int port = 8080;
        String host = "localhost";

        MyHTTPServer server = new MyHTTPServer(port, host);
        server.start();
    }
}
