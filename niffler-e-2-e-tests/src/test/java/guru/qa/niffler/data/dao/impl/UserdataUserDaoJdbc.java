package guru.qa.niffler.data.dao.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.Databases;
import guru.qa.niffler.data.dao.UserdataUserDao;
import guru.qa.niffler.data.entity.UserdataUserEntity;
import guru.qa.niffler.model.CurrencyValues;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class UserdataUserDaoJdbc implements UserdataUserDao {

    private static final Config CFG = Config.getInstance();

    @Override
    public UserdataUserEntity createUser(UserdataUserEntity user) {
        try (Connection connection = Databases.connection(CFG.userdataJdbcUrl())) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO user (currency, firstname, full_name, photo, photo_small, surname, username)" +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            )) {
                ps.setObject(1, user.getCurrency());
                ps.setString(2, user.getFirstname());
                ps.setString(3, user.getFullname());
                ps.setBytes(4, user.getPhoto());
                ps.setBytes(5, user.getPhotoSmall());
                ps.setString(6, user.getSurname());
                ps.setString(7, user.getUsername());

                ps.executeUpdate();

                final UUID generatedKey;
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedKey = rs.getObject("id", UUID.class);
                    } else {
                        throw new SQLException("Can't find id in ResultSet");
                    }
                }
                user.setId(generatedKey);
                return user;

            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(UUID id) {
        try (Connection connection = Databases.connection(CFG.userdataJdbcUrl())) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM user WHERE id = ?"
            )) {
                ps.setObject(1, id);
                ps.execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Optional<UserdataUserEntity> findById(UUID id) {
        try (Connection connection = Databases.connection(CFG.userdataJdbcUrl())) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM user WHERE id = ?"
            )) {
                ps.setObject(1, id);
                ps.execute();
                try (ResultSet rs = ps.getResultSet()) {
                    if (rs.next()) {

                        UserdataUserEntity user = new UserdataUserEntity();
                        user.setId(rs.getObject("id", UUID.class));
                        user.setCurrency((CurrencyValues) rs.getObject("currency"));
                        user.setFirstname(rs.getString("firstname"));
                        user.setFullname(rs.getString("full_name"));
                        user.setPhoto(rs.getString("photo").getBytes());
                        user.setPhotoSmall(rs.getString("photo_small").getBytes());
                        user.setSurname(rs.getString("surname"));
                        user.setUsername(rs.getString("username"));

                        return Optional.of(user);
                    } else {
                        return Optional.empty();
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<UserdataUserEntity> findByUsername(String username) {
        try (Connection connection = Databases.connection(CFG.userdataJdbcUrl())) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM user WHERE username = ?"
            )) {
                ps.setObject(1, username);
                ps.execute();
                try (ResultSet rs = ps.getResultSet()) {
                    if (rs.next()) {

                        UserdataUserEntity user = new UserdataUserEntity();
                        user.setId(rs.getObject("id", UUID.class));
                        user.setCurrency((CurrencyValues) rs.getObject("currency"));
                        user.setFirstname(rs.getString("firstname"));
                        user.setFullname(rs.getString("full_name"));
                        user.setPhoto(rs.getString("photo").getBytes());
                        user.setPhotoSmall(rs.getString("photo_small").getBytes());
                        user.setSurname(rs.getString("surname"));
                        user.setUsername(rs.getString("username"));

                        return Optional.of(user);
                    } else {
                        return Optional.empty();
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
