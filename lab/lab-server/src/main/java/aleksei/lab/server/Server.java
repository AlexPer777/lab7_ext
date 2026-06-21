package aleksei.lab.server;

import auth.AuthService;
import auth.PasswordHasher;
import database.DatabaseInitializer;
import database.DatabaseManager;
import database.HumanBeingRepository;
import database.UserRepository;
import executor.CommandExecutor;
import manager.CollectionManager;
import manager.Reader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.sql.SQLException;
import java.util.Scanner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Server {
    private static final int DEFAULT_PORT = 9876;
    private static final int IDLE_DELAY_MS = 50;
    private static final Logger logger = LogManager.getLogger(Server.class);

    public static void main(String[] args) throws IOException {
        int port = parsePort(args);
        DatabaseManager databaseManager = DatabaseManager.fromEnvironment();
        try {
            new DatabaseInitializer(databaseManager).initialize();
            logger.info("Database connection established");
        } catch (IllegalStateException | SQLException e) {
            logger.error("Database connection error", e);
            return;
        }

        Scanner scanner = new Scanner(new ByteArrayInputStream(new byte[0]));
        HumanBeingRepository humanBeingRepository = new HumanBeingRepository(databaseManager);
        CollectionManager collectionManager = new CollectionManager(scanner, humanBeingRepository);
        try {
            collectionManager.loadFromDatabase();
            logger.info("Collection loaded from database");
        } catch (SQLException e) {
            logger.error("Collection load error", e);
            return;
        }

        AuthService authService = new AuthService(new UserRepository(databaseManager), new PasswordHasher());
        CommandExecutor commandExecutor = new CommandExecutor(collectionManager, authService);
        Reader reader = new Reader(collectionManager, commandExecutor);
        commandExecutor.setReader(reader);
        commandExecutor.initCommands();
        ClientRequestHandler clientRequestHandler = new ClientRequestHandler(reader);
        ServerConsoleReader serverConsoleReader = new ServerConsoleReader();
        try (ConnectionAcceptor connectionAcceptor = new ConnectionAcceptor(port)) {
            logger.info("Server started on port {}", port);
            while (true) {
                SocketChannel clientChannel = connectionAcceptor.accept();
                if (clientChannel == null) {
                    serverConsoleReader.executeAvailableCommand();
                    waitForConnection();
                } else {
                    clientRequestHandler.handle(clientChannel);
                }
            }
        } catch (IOException e) {
            logger.error("Server error", e);
        }
    }

    private static int parsePort(String[] args) {
        if (args.length == 0) {
            return DEFAULT_PORT;
        }
        try {
            return Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            logger.warn("Invalid port '{}', using default {}", args[0], DEFAULT_PORT);
            return DEFAULT_PORT;
        }
    }

    private static void waitForConnection() {
        try {
            Thread.sleep(IDLE_DELAY_MS);
        } catch (InterruptedException e) {
            logger.warn("Server wait interrupted", e);
            Thread.currentThread().interrupt();
        }
    }
}
