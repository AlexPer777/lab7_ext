package aleksei.lab.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ServerConsoleReader {
    private static final Logger logger = LogManager.getLogger(ServerConsoleReader.class);
    private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public void executeAvailableCommand() throws IOException {
        if (!reader.ready()) {
            return;
        }
        String command = reader.readLine();
        if (command == null || command.isBlank()) {
            return;
        }
        logger.warn("Unknown server command: {}", command);
    }
}
