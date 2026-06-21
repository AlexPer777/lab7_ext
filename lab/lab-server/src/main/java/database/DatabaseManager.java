package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String DEFAULT_URL = "jdbc:postgresql://pg:5432/studs";
    private static final String DEFAULT_USER = "s501851";

    private final String url;
    private final String user;
    private final String password;

    public DatabaseManager(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public static DatabaseManager fromEnvironment() {
        String url = readConfig("db.url", "DB_URL", DEFAULT_URL);
        String user = readConfig("db.user", "DB_USER", DEFAULT_USER);
        String password = readConfig("db.password", "DB_PASSWORD", null);
        return new DatabaseManager(url, user, password);
    }

    public Connection getConnection() throws SQLException {
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("Database password is required. Use -Ddb.password=... or DB_PASSWORD.");
        }
        return DriverManager.getConnection(url, user, password);
    }

    private static String readConfig(String propertyName, String environmentName, String defaultValue) {
        String propertyValue = System.getProperty(propertyName);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue;
        }
        String environmentValue = System.getenv(environmentName);
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue;
        }
        return defaultValue;
    }
}
