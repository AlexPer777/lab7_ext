package aleksei.lab.server;

import manager.Reader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import request.Request;
import response.Response;
import response.ResponseStatus;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ForkJoinPool;

public class ClientRequestHandler {
    private final Reader commandReader;
    private final ServerRequestReader requestReader;
    private final ServerResponseSender responseSender;
    private final ForkJoinPool readPool = new ForkJoinPool();
    private final ForkJoinPool sendPool = new ForkJoinPool();
    private static final Logger logger = LogManager.getLogger(ClientRequestHandler.class);

    public ClientRequestHandler(Reader commandReader) {
        this.commandReader = commandReader;
        this.requestReader = new ServerRequestReader();
        this.responseSender = new ServerResponseSender();
    }

    public void handle(SocketChannel socketChannel) {
        readPool.execute(() -> readRequest(socketChannel));
    }

    private void readRequest(SocketChannel clientChannel) {
        try {
            clientChannel.configureBlocking(false);
            Request request = requestReader.read(clientChannel);
            new Thread(() -> processRequest(clientChannel, request)).start();
        } catch (IOException | ClassNotFoundException e) {
            sendResponse(clientChannel, new Response("Client error: " + e.getMessage(), ResponseStatus.ERROR, null));
            logger.error("Client request read error", e);
        }
    }

    private void processRequest(SocketChannel clientChannel, Request request) {
        Response response = commandReader.toCommand(request);
        sendResponse(clientChannel, response);
    }

    private void sendResponse(SocketChannel clientChannel, Response response) {
        sendPool.execute(() -> {
            try (SocketChannel ignored = clientChannel) {
                responseSender.send(clientChannel, response);
            } catch (IOException e) {
                logger.error("Response send error", e);
            }
        });
    }
}
