package aleksei.lab.server;

import response.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ServerResponseSender {
    private static final int IO_TIMEOUT_MS = 5000;

    public void send(SocketChannel socketChannel, Response response) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(serialize(response));
        long deadline = System.currentTimeMillis() + IO_TIMEOUT_MS;
        while (buffer.hasRemaining()) {
            int written = socketChannel.write(buffer);
            if (written == 0) {
                if (System.currentTimeMillis() > deadline) {
                    throw new IOException("Write timeout");
                }
                waitForChannel();
            } else {
                deadline = System.currentTimeMillis() + IO_TIMEOUT_MS;
            }
        }
    }

    private byte[] serialize(Response response) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(response);
        }
        return byteArrayOutputStream.toByteArray();
    }

    private void waitForChannel() throws IOException {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while sending response", e);
        }
    }
}
