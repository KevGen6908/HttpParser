package org.example;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

public class HttpServer implements Server{

    private final InetSocketAddress address;
    private final Router router;

    private ServerSocketChannel server;

    public HttpServer(int port, Router router) {
        this(new InetSocketAddress(port), router);
    }

    public HttpServer(InetSocketAddress address, Router router) {
        this.router = router;
        this.address = address;
    }

    @Override
    public void start() throws IOException {
        Selector selector = SelectorProvider.provider().openSelector();
        server = ServerSocketChannel.open()
                .bind(address);
        server.configureBlocking(false);
        server.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            selector.select(key -> {
                if (!key.isValid()) {
                    return;
                }
                SocketChannel sc = null;
                try {
                    if (key.isAcceptable()) {
                        sc = server.accept();
                        sc.configureBlocking(false);
                        sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        return;
                    }
                    if (key.isReadable() && key.isWritable()) {
                        sc = (SocketChannel) key.channel();
                        try {
                            HttpRequest request = new HttpRequest(sc);
                            request.parse();
                            HttpResponse response = router.getHandler(request).handle(request);
                            response.write(sc);
                        } catch (HttpRequest.BadRequestException e) {
                            throw new RuntimeException(e);
                        } finally {
                            sc.close();
                        }
                    }
                } catch (IOException e) {
                    key.cancel();
                    try {
                        if (sc != null) {
                            sc.close();
                        }
                    } catch (IOException ex) {

                    }
                }
            });
        }
    }

    @Override
    public void close() throws IOException {
        if (server != null) {
            server.close();
        }
    }
}
