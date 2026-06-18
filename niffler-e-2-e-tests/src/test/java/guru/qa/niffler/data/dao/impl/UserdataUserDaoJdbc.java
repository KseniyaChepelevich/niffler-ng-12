package guru.qa.niffler.data.dao.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.UserdataUserDao;
import guru.qa.niffler.data.entity.SpendEntity;
import guru.qa.niffler.data.entity.UserdataUserEntity;
import guru.qa.niffler.model.CurrencyValues;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserdataUserDaoJdbc implements UserdataUserDao {

    private final Connection connection;

    private static final Config CFG = Config.getInstance();

    public UserdataUserDaoJdbc(Connection connection) {
        this.connection = connection;
    }

    @Override
    public UserdataUserEntity createUser(UserdataUserEntity user) {

        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO \"user\" (currency, firstname, full_name, photo, photo_small, surname, username)" +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
        )) {
            ps.setObject(1, user.getCurrency().name());
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

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(UUID id) {

        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM user WHERE id = ?"
        )) {
            ps.setObject(1, id);
            ps.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Optional<UserdataUserEntity> findById(UUID id) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM user WHERE id = ?"
        )) {
            ps.setObject(1, id);
            ps.execute();
            try (ResultSet rs = ps.getResultSet()) {
                if (rs.next()) {

                    UserdataUserEntity user = new UserdataUserEntity();
                    user.setId(rs.getObject("id", UUID.class));
                    String currencyStr = rs.getString("currency");
                    if (currencyStr != null) {
                        user.setCurrency(CurrencyValues.valueOf(currencyStr));
                    }
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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<UserdataUserEntity> findAllByUsername(String username) {
        List<UserdataUserEntity> users = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM \"user\" WHERE username = ?"
        )) {
            ps.setObject(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UserdataUserEntity user = new UserdataUserEntity();
                    user.setId(rs.getObject("id", UUID.class));
                    String currencyStr = rs.getString("currency");
                    if (currencyStr != null) {
                        user.setCurrency(CurrencyValues.valueOf(currencyStr));
                    }
                    user.setFirstname(rs.getString("firstname"));
                    user.setFullname(rs.getString("full_name"));
                    user.setPhoto(rs.getBytes("photo"));
                    user.setPhotoSmall(rs.getBytes("photo_small"));
                    user.setSurname(rs.getString("surname"));
                    user.setUsername(rs.getString("username"));
                    users.add(user);
                }
                return users;
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<UserdataUserEntity> findAll() {
        List<UserdataUserEntity> listUsers = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM \"user\""
        )) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UserdataUserEntity user = new UserdataUserEntity();
                    user.setId(rs.getObject("id", UUID.class));
                    String currencyStr = rs.getString("currency");
                    if (currencyStr != null) {
                        user.setCurrency(CurrencyValues.valueOf(currencyStr));
                    }
                    user.setFirstname(rs.getString("firstname"));
                    user.setFullname(rs.getString("full_name"));
                    user.setPhoto(rs.getBytes("photo"));
                    user.setPhotoSmall(rs.getBytes("photo_small"));
                    user.setSurname(rs.getString("surname"));
                    user.setUsername(rs.getString("username"));
                    listUsers.add(user);
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            return listUsers;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
