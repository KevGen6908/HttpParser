package org.example;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HTTPServer extends Thread{
    private final int PORT ;
    private final String SERVER_ADDR;
    private static final Logger logger = Logger.getLogger(Client.class.getName());


    public HTTPServer(int PORT, String SERVER_ADDR ) {
        this.PORT = PORT;
        this.SERVER_ADDR = SERVER_ADDR;
    }

    @Override
    public void run(){
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()){
            serverSocketChannel.bind(new InetSocketAddress(SERVER_ADDR, PORT));
            logger.info("Server started. Listening on " + SERVER_ADDR + ":" + PORT);

            while (true){
                SocketChannel clientChannel = serverSocketChannel.accept();
                logger.info("Accepted connection from: " + clientChannel.getRemoteAddress());

                try {
                    HTTPRequestParser parser = new HTTPRequestParser(clientChannel);
                    parser.parserHttpRequest();

                    String method = parser.getMethod();
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
                    HTTPResponseSender.sendResponse(clientChannel, method, responseBody);
                } catch (IOException e){
                    logger.log(Level.SEVERE, "Error processing HTTP request", e);
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception in server setup", e);
        }
    }
}
