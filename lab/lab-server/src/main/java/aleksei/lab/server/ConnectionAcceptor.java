package aleksei.lab.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ConnectionAcceptor implements AutoCloseable {
    private final ServerSocketChannel serverSocketChannel;

    public ConnectionAcceptor(int port) throws IOException {
        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.bind(new InetSocketAddress(port));
        this.serverSocketChannel.configureBlocking(false);
    }

    public SocketChannel accept() throws IOException {
        return serverSocketChannel.accept();
    }

    @Override
    public void close() throws IOException {
        serverSocketChannel.close();
    }
}
