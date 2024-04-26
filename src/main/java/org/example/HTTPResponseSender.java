package org.example;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HTTPResponseSender {
    private static final Logger logger = Logger.getLogger(HTTPResponseSender.class.getName());

    public static void sendResponse(SocketChannel socketChannel, String method, String responseBody){
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

            logger.info("HTTP response sent to client");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error occurred while sending HTTP response", e);
        }

    }
}
