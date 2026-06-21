package aleksei.lab.client;

import request.Request;
import response.Response;
import response.ResponseStatus;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class Client {
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 9876;
    private static final int RECONNECT_ATTEMPTS = 5;
    private static final int RECONNECT_DELAY_MS = 1000;
    private static final int CONNECT_TIMEOUT_MS = 1000;
    private static final int IO_TIMEOUT_MS = 5000;
    private static final int INITIAL_BUFFER_SIZE = 1024;
    private static final int MAX_BUFFER_SIZE = 1024 * 1024;

    public static void main(String[] args) {
        String host = parseHost(args);
        int port = parsePort(args);

        Scanner scanner = new Scanner(System.in);
        ClientRequestParser parser = new ClientRequestParser(scanner);
        ClientResponsePrinter printer = new ClientResponsePrinter();
        System.out.print("Login: ");
        String login = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        System.out.println("Client started. 'exit' to stop client");
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line == null || line.trim().isEmpty()) {
                continue;
            }
            if ("exit".equalsIgnoreCase(line.trim())) {
                System.out.println("Client closed");
                break;
            }
            try {
                Long updateId = extractInteractiveUpdateId(line);
                if (updateId != null && !canModify(host, port, parser, printer, login, password, updateId)) {
                    continue;
                }
                Request request = parser.buildRequest(line.trim());
                request.setCredentials(login, password);
                Response response = sendRequest(host, port, request);
                if (response == null) {
                    System.out.println("Server unavailable. Command was not executed.");
                    continue;
                }
                printer.print(response);
                if (response.getStatus() == ResponseStatus.EXIT) {
                    break;
                }
            } catch (RuntimeException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static Long extractInteractiveUpdateId(String line) {
        String trimmed = line.trim();
        if (trimmed.contains("{")) {
            return null;
        }
        String[] parts = trimmed.split("\\s+");
        if (parts.length != 2 || !"update".equals(parts[0])) {
            return null;
        }
        try {
            return Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean canModify(String host, int port, ClientRequestParser parser, ClientResponsePrinter printer,
                                     String login, String password, long id) {
        Request request = parser.buildRequest("can_modify " + id);
        request.setCredentials(login, password);
        Response response = sendRequest(host, port, request);
        if (response == null) {
            System.out.println("Server unavailable. Command was not executed.");
            return false;
        }
        if (response.getStatus() != ResponseStatus.SUCCESS) {
            printer.print(response);
            return false;
        }
        return true;
    }

    private static String parseHost(String[] args) {
        if (args.length < 1 || args[0] == null || args[0].isBlank()) {
            return DEFAULT_HOST;
        }
        return args[0];
    }

    private static int parsePort(String[] args) {
        if (args.length < 2) {
            return DEFAULT_PORT;
        }
        try {
            return Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid port '" + args[1] + "', using default " + DEFAULT_PORT);
            return DEFAULT_PORT;
        }
    }

    private static Response sendRequest(String host, int port, Request request) {
        for (int attempt = 1; attempt <= RECONNECT_ATTEMPTS; attempt++) {
            Response response = trySendRequest(host, port, request);
            if (response != null) {
                return response;
            }
            if (attempt < RECONNECT_ATTEMPTS) {
                System.out.println("Server unavailable. Reconnect attempt "
                        + attempt + "/" + RECONNECT_ATTEMPTS);
                waitBeforeReconnect();
            }
        }
        return null;
    }

    private static Response trySendRequest(String host, int port, Request request) {
        try (SocketChannel channel = SocketChannel.open()) {
            channel.configureBlocking(false);
            connect(channel, host, port);
            writeFully(channel, serialize(request));
            Object obj = deserialize(readResponse(channel));
            if (obj instanceof Response response) {
                return response;
            }
            return new Response("Invalid server response", ResponseStatus.ERROR, null);
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }

    private static void connect(SocketChannel channel, String host, int port) throws IOException {
        channel.connect(new InetSocketAddress(host, port));
        long deadline = System.currentTimeMillis() + CONNECT_TIMEOUT_MS;
        while (!channel.finishConnect()) {
            if (System.currentTimeMillis() > deadline) {
                throw new IOException("Connection timeout");
            }
            waitForChannel();
        }
    }

    private static byte[] serialize(Request request) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(request);
        }
        return byteArrayOutputStream.toByteArray();
    }

    private static void writeFully(SocketChannel channel, byte[] data) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        long deadline = System.currentTimeMillis() + IO_TIMEOUT_MS;
        while (buffer.hasRemaining()) {
            int written = channel.write(buffer);
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

    private static byte[] readResponse(SocketChannel channel) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(INITIAL_BUFFER_SIZE);
        int currentBufferSize = INITIAL_BUFFER_SIZE;
        ByteBuffer buffer = ByteBuffer.allocate(currentBufferSize); // динамический размер буффера
        long deadline = System.currentTimeMillis() + IO_TIMEOUT_MS;
        while (true) {
            int read = channel.read(buffer);
            if (read == -1) {
                return byteArrayOutputStream.toByteArray();
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
            if (byteArrayOutputStream.size() >= currentBufferSize && currentBufferSize < MAX_BUFFER_SIZE) {
                currentBufferSize = Math.min(currentBufferSize * 2, MAX_BUFFER_SIZE);
                buffer = ByteBuffer.allocate(currentBufferSize);
            }
            deadline = System.currentTimeMillis() + IO_TIMEOUT_MS;
        }
    }

    private static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return objectInputStream.readObject();
        }
    }

    private static void waitBeforeReconnect() {
        try {
            Thread.sleep(RECONNECT_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void waitForChannel() throws IOException {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for channel", e);
        }
    }
}
