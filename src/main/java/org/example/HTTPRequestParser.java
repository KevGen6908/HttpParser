package org.example;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HTTPRequestParser {
    private static final Logger logger = Logger.getLogger(HTTPRequestParser.class.getName());
    private  String method;
    private  String uri;
    private final Map<String, String> headers;
    private  String body;

    public HTTPRequestParser(){
        this.headers = new HashMap<>();
    }

    private int getContentLength(){
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

    public void parserHttpRequest(String request) throws IOException {

        logger.info("Starting to parse HTTP request");

        String[] lines = request.split("\\r?\\n");
        String[] requestLinePath = lines[0].split("\\s");
        if (requestLinePath.length >= 3) {
            method = requestLinePath[0];
            uri = requestLinePath[1];
        }

        for (int i = 1; i < lines.length; i++) {
            String[] headerPath = lines[i].split(":", 2);
            if (headerPath.length == 2) {
                String headerName = headerPath[0].trim();
                String headerValue = headerPath[1].trim();
                headers.put(headerName, headerValue);
            }
        }

        int contentLength = getContentLength();
        if (contentLength > 0) {
            body = lines[lines.length - 1];
        }

        logger.info("HTTP request parsed successfully");

        logger.info("Method: " + method);
        logger.info("URI: " + uri);
        logger.info("Headers: " + headers);
        logger.info("Body: " + body);
    }


    public String getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public String getBody() {
        return body;
    }

}
