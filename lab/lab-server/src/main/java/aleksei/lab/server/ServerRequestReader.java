package aleksei.lab.server;

import request.Request;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ServerRequestReader {
    private static final int BUFFER_SIZE = 8192;
    private static final int IO_TIMEOUT_MS = 5000;

    public Request read(SocketChannel socketChannel) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        long deadline = System.currentTimeMillis() + IO_TIMEOUT_MS;
        while (true) {
            int read = socketChannel.read(buffer);
            if (read == -1) {
                throw new EOFException("Client closed connection");
            }
            if (read == 0) {
                if (System.currentTimeMillis() > deadline) {
                    throw new IOException("Read timeout");
                }
                waitForChannel();
                continue;
            }
            buffer.flip();
            byteArrayOutputStream.write(buffer.array(), buffer.position(), buffer.remaining());
            buffer.clear();
            Request request = tryDeserializeRequest(byteArrayOutputStream.toByteArray());
            if (request != null) {
                return request;
            }
            deadline = System.currentTimeMillis() + IO_TIMEOUT_MS;
        }
    }

    private Request tryDeserializeRequest(byte[] data) throws IOException, ClassNotFoundException {
        Object obj;
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(data))) {
            obj = objectInputStream.readObject();
        } catch (EOFException e) {
            return null;
        }
        if (obj instanceof Request request) {
            return request;
        }
        throw new InvalidObjectException("Invalid request obj");
    }

    private void waitForChannel() throws IOException {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for request", e);
        }
    }
}
