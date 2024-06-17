package org.example;

public interface HttpHandler {
    HttpResponse handle(HttpRequest request);
}
