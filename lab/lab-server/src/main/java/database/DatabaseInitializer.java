package database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    private final DatabaseManager databaseManager;

    public DatabaseInitializer(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void initialize() throws SQLException {
        try (Connection connection = databaseManager.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE SEQUENCE IF NOT EXISTS human_being_id_seq
                        START WITH 1
                        INCREMENT BY 1
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS users (
                        id BIGSERIAL PRIMARY KEY,
                        login VARCHAR(100) NOT NULL UNIQUE,
                        password_hash VARCHAR(64) NOT NULL,
                        CONSTRAINT users_login_not_blank CHECK (trim(login) <> ''),
                        CONSTRAINT users_password_hash_sha256_length CHECK (length(password_hash) = 64)
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS human_beings (
                        id BIGINT PRIMARY KEY DEFAULT nextval('human_being_id_seq'),
                        owner_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                        name TEXT NOT NULL,
                        coordinates_x INTEGER NOT NULL,
                        coordinates_y BIGINT NOT NULL,
                        creation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        real_hero BOOLEAN NOT NULL,
                        has_toothpick BOOLEAN NOT NULL,
                        impact_speed INTEGER,
                        weapon_type VARCHAR(20) NOT NULL,
                        mood VARCHAR(20),
                        car_name TEXT,
                        car_cool BOOLEAN,
                        CONSTRAINT human_beings_name_not_blank CHECK (trim(name) <> ''),
                        CONSTRAINT human_beings_coordinates_y_check CHECK (coordinates_y > -383),
                        CONSTRAINT human_beings_impact_speed_check CHECK (
                            impact_speed IS NULL OR impact_speed > -117
                        ),
                        CONSTRAINT human_beings_weapon_type_check CHECK (
                            weapon_type IN ('HAMMER', 'AXE', 'PISTOL', 'RIFLE', 'KNIFE')
                        ),
                        CONSTRAINT human_beings_mood_check CHECK (
                            mood IS NULL OR mood IN ('SORROW', 'CALM', 'RAGE')
                        ),
                        CONSTRAINT human_beings_car_check CHECK (
                            (car_name IS NULL AND car_cool IS NULL)
                            OR
                            (car_name IS NOT NULL AND car_cool IS NOT NULL)
                        )
                    )
                    """);
            statement.executeUpdate("ALTER TABLE users DROP CONSTRAINT IF EXISTS users_password_hash_sha256_length");
            statement.executeUpdate("ALTER TABLE users ALTER COLUMN password_hash TYPE VARCHAR(64)");
            statement.executeUpdate("""
                    ALTER TABLE users
                    ADD CONSTRAINT users_password_hash_sha256_length CHECK (length(password_hash) = 64)
                    """);
            statement.executeUpdate("ALTER SEQUENCE human_being_id_seq OWNED BY human_beings.id");
            statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_human_beings_owner_id ON human_beings(owner_id)");
            statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_human_beings_name ON human_beings(name)");
        }
    }
}
