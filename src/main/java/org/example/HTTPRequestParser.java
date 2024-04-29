package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HTTPRequestParser {
    private static final Logger logger = Logger.getLogger(HTTPRequestParser.class.getName());
    private final SocketChannel channel;
    private  String method;
    private  String uri;
    private final Map<String, String> headers;
    private  String body;

    public HTTPRequestParser(SocketChannel channel){
        this.headers = new HashMap<>();
        this.channel = channel;
    }

    private  int getContentLength(){
        String contentLengthStr = headers.get("Content-Length");

        if(contentLengthStr != null && !contentLengthStr.isEmpty()){
            try {
                return Integer.parseInt(contentLengthStr);
            } catch (NumberFormatException e){
                logger.log(Level.WARNING, "Invalid Content-Length value: " + contentLengthStr, e);
            }
        }
        return 0;
    }

    public  void parserHttpRequest() throws IOException{
        BufferedReader reader = new BufferedReader(new InputStreamReader(channel.socket().getInputStream()));

        // Read request line
        String requestLine = reader.readLine();
        if(requestLine != null){
            String[] requestLinePath = requestLine.split("\\s");
            if(requestLinePath.length >= 3){
                method = requestLinePath[0];
                uri = requestLinePath[1];
            }
        }

        //Read headers
        String headerLine;
        while ((headerLine = reader.readLine()) != null && !headerLine.isEmpty()){
            String[] headerPath = headerLine.split(":", 2);
            if(headerPath.length == 2){
                String headerName = headerPath[0].trim();
                String headerValue = headerPath[1].trim();
                headers.put(headerName, headerValue);
            }
        }

        //Read body
        StringBuilder bodyBuilder = new StringBuilder();
        int contentLength = getContentLength();
        if(contentLength > 0){
            char[] buffer = new char[contentLength];
            int bytesRead = reader.read(buffer, 0, contentLength);
            if(bytesRead == contentLength){
                bodyBuilder.append(buffer);

            }
        }

        body = bodyBuilder.toString();

        logger.info("HTTP Request parsed:");
        logger.info("Method: " + method);
        logger.info("URI: " + uri);
        logger.info("Headers: " + headers);
        logger.info("Body: " + body);

    }

    public SocketChannel getChannel() {
        return channel;
    }

    public String getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

}
