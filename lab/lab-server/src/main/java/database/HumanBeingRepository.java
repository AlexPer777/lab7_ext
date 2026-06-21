package database;

import model.Car;
import model.Coordinates;
import model.HumanBeing;
import model.Mood;
import model.WeaponType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HumanBeingRepository {
    private final DatabaseManager databaseManager;

    public HumanBeingRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public List<HumanBeing> loadAll() throws SQLException {
        String sql = """
                SELECT h.*, u.login AS owner_login
                FROM human_beings h
                JOIN users u ON u.id = h.owner_id
                ORDER BY h.id
                """;
        List<HumanBeing> result = new ArrayList<>();
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                result.add(readHumanBeing(resultSet));
            }
        }
        return result;
    }

    public HumanBeing insert(HumanBeing humanBeing, long ownerId, String ownerLogin) throws SQLException {
        String sql = """
                INSERT INTO human_beings (
                    owner_id, name, coordinates_x, coordinates_y, real_hero, has_toothpick,
                    impact_speed, weapon_type, mood, car_name, car_cool
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id, creation_date
                """;
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillHumanBeing(statement, humanBeing, ownerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return new HumanBeing(
                        resultSet.getLong("id"),
                        humanBeing.getName(),
                        humanBeing.getCoordinates(),
                        resultSet.getTimestamp("creation_date").toLocalDateTime(),
                        humanBeing.isRealHero(),
                        humanBeing.getHasToothpick(),
                        humanBeing.getImpactSpeed(),
                        humanBeing.getWeaponType(),
                        humanBeing.getMood(),
                        humanBeing.getCar(),
                        ownerId,
                        ownerLogin
                );
            }
        }
    }

    public boolean update(long id, HumanBeing humanBeing, long ownerId) throws SQLException {
        String sql = """
                UPDATE human_beings
                SET name = ?,
                    coordinates_x = ?,
                    coordinates_y = ?,
                    real_hero = ?,
                    has_toothpick = ?,
                    impact_speed = ?,
                    weapon_type = ?,
                    mood = ?,
                    car_name = ?,
                    car_cool = ?
                WHERE id = ? AND owner_id = ?
                """;
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, humanBeing.getName());
            statement.setInt(2, humanBeing.getCoordinates().getX());
            statement.setLong(3, humanBeing.getCoordinates().getY());
            statement.setBoolean(4, humanBeing.isRealHero());
            statement.setBoolean(5, humanBeing.getHasToothpick());
            setNullableInteger(statement, 6, humanBeing.getImpactSpeed());
            statement.setString(7, humanBeing.getWeaponType().name());
            setNullableString(statement, 8, humanBeing.getMood() == null ? null : humanBeing.getMood().name());
            setCar(statement, 9, 10, humanBeing.getCar());
            statement.setLong(11, id);
            statement.setLong(12, ownerId);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean deleteById(long id, long ownerId) throws SQLException {
        String sql = "DELETE FROM human_beings WHERE id = ? AND owner_id = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.setLong(2, ownerId);
            return statement.executeUpdate() == 1;
        }
    }

    public List<Long> deleteOwnedByIds(Collection<Long> ids, long ownerId) throws SQLException {
        List<Long> deletedIds = new ArrayList<>();
        if (ids.isEmpty()) {
            return deletedIds;
        }
        String placeholders = String.join(",", ids.stream().map(id -> "?").toList());
        String sql = "DELETE FROM human_beings WHERE owner_id = ? AND id IN (" + placeholders + ") RETURNING id";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, ownerId);
            int index = 2;
            for (Long id : ids) {
                statement.setLong(index++, id);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    deletedIds.add(resultSet.getLong("id"));
                }
            }
        }
        return deletedIds;
    }

    private HumanBeing readHumanBeing(ResultSet resultSet) throws SQLException {
        Car car = null;
        String carName = resultSet.getString("car_name");
        if (!resultSet.wasNull()) {
            car = new Car(carName, resultSet.getBoolean("car_cool"));
        }
        String mood = resultSet.getString("mood");
        return new HumanBeing(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                new Coordinates(resultSet.getInt("coordinates_x"), resultSet.getLong("coordinates_y")),
                resultSet.getTimestamp("creation_date").toLocalDateTime(),
                resultSet.getBoolean("real_hero"),
                resultSet.getBoolean("has_toothpick"),
                getNullableInteger(resultSet, "impact_speed"),
                WeaponType.valueOf(resultSet.getString("weapon_type")),
                mood == null ? null : Mood.valueOf(mood),
                car,
                resultSet.getLong("owner_id"),
                resultSet.getString("owner_login")
        );
    }

    private void fillHumanBeing(PreparedStatement statement, HumanBeing humanBeing, long ownerId) throws SQLException {
        statement.setLong(1, ownerId);
        statement.setString(2, humanBeing.getName());
        statement.setInt(3, humanBeing.getCoordinates().getX());
        statement.setLong(4, humanBeing.getCoordinates().getY());
        statement.setBoolean(5, humanBeing.isRealHero());
        statement.setBoolean(6, humanBeing.getHasToothpick());
        setNullableInteger(statement, 7, humanBeing.getImpactSpeed());
        statement.setString(8, humanBeing.getWeaponType().name());
        setNullableString(statement, 9, humanBeing.getMood() == null ? null : humanBeing.getMood().name());
        setCar(statement, 10, 11, humanBeing.getCar());
    }

    private Integer getNullableInteger(ResultSet resultSet, String column) throws SQLException {
        int value = resultSet.getInt(column);
        return resultSet.wasNull() ? null : value;
    }

    private void setNullableInteger(PreparedStatement statement, int index, Integer value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.INTEGER);
        } else {
            statement.setInt(index, value);
        }
    }

    private void setNullableString(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            statement.setString(index, value);
        }
    }

    private void setCar(PreparedStatement statement, int nameIndex, int coolIndex, Car car) throws SQLException {
        if (car == null) {
            statement.setNull(nameIndex, Types.VARCHAR);
            statement.setNull(coolIndex, Types.BOOLEAN);
        } else {
            statement.setString(nameIndex, car.getName());
            statement.setBoolean(coolIndex, car.getCool());
        }
    }
}
