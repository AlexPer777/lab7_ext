package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UserRepository {
    private final DatabaseManager databaseManager;

    public UserRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public boolean createNewUser(String login, String passwordHash) throws SQLException {
        String sql = "INSERT INTO users (login, password_hash) VALUES (?, ?) ON CONFLICT (login) DO NOTHING";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, login);
            statement.setString(2, passwordHash);
            return statement.executeUpdate() == 1;
        }
    }

    public Optional<User> findByLoginAndPasswordHash(String login, String passwordHash) throws SQLException {
        String sql = "SELECT id, login FROM users WHERE login = ? AND password_hash = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, login);
            statement.setString(2, passwordHash);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(new User(resultSet.getLong("id"), resultSet.getString("login")));
                }
            }
        }
        return Optional.empty();
    }
}
