package guru.qa.niffler.data.repository.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.userdata.FriendshipStatus;
import guru.qa.niffler.data.entity.userdata.UserEntity;
import guru.qa.niffler.data.repository.UserdataUserRepository;
import guru.qa.niffler.model.CurrencyValues;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.data.tpl.Connections.holder;

public class UserdataUserRepositoryJdbc implements UserdataUserRepository {


    private static final Config CFG = Config.getInstance();
    private static final String URL = CFG.userdataJdbcUrl();


    @Override
    public UserEntity create(UserEntity user) {
        try (PreparedStatement ps = holder(URL).connection().prepareStatement(
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
        try {
            try (PreparedStatement psFriendship = holder(URL).connection().prepareStatement(
                    "DELETE FROM \"friendship\" WHERE requester_id = ? OR addressee_id = ?"
            )) {
                psFriendship.setObject(1, id);
                psFriendship.setObject(2, id);
                psFriendship.execute();
            }
            try (PreparedStatement ps = holder(URL).connection().prepareStatement(
                    "DELETE FROM \"user\" WHERE id = ?"
            )) {
                ps.setObject(1, id);
                ps.execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Optional<UserEntity> findById(UUID id) {
        try (PreparedStatement ps = holder(URL).connection().prepareStatement(
                "SELECT * FROM \"user\" WHERE id = ?"
        )) {
            ps.setObject(1, id);
            ps.execute();
            try (ResultSet rs = ps.getResultSet()) {
                if (rs.next()) {

                    UserEntity user = new UserEntity();
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
    public void addIncomeInvitation(UserEntity requester, UserEntity addressee) {
        try (PreparedStatement ps = holder(URL).connection().prepareStatement(
                "INSERT INTO \"friendship\" (addressee_id, requester_id, created_date, status)" +
                        "VALUES (?, ?, ?, ?)"
        )) {
            ps.setObject(1, addressee.getId());
            ps.setObject(2, requester.getId());
            ps.setObject(3, LocalDate.now());
            ps.setString(4, FriendshipStatus.PENDING.name());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addOutcomeInvitation(UserEntity requester, UserEntity addressee) {
        try (PreparedStatement ps = holder(URL).connection().prepareStatement(
                "INSERT INTO \"friendship\" (addressee_id, requester_id, created_date, status)" +
                        "VALUES (?, ?, ?, ?)"
        )) {
            ps.setObject(1, addressee.getId());
            ps.setObject(2, requester.getId());
            ps.setObject(3, LocalDate.now());
            ps.setString(4, FriendshipStatus.PENDING.name());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addFriend(UserEntity requester, UserEntity addressee) {
        try (PreparedStatement psUpdate = holder(URL).connection().prepareStatement(
                "UPDATE \"friendship\" SET status = ? WHERE addressee_id = ? AND requester_id = ?");
       PreparedStatement psInsert = holder(URL).connection().prepareStatement(
                "INSERT INTO \"friendship\" (addressee_id, requester_id, created_date, status)" +
                        "VALUES (?, ?, ?, ?)");
        ) {
            psUpdate.setString(1, FriendshipStatus.ACCEPTED.name());
            psUpdate.setObject(2, addressee.getId());
            psUpdate.setObject(3, requester.getId());

            psUpdate.executeUpdate();

            psInsert.setObject(1, requester.getId());
            psInsert.setObject(2, addressee.getId());
            psInsert.setDate(3, Date.valueOf(LocalDate.now()));
            psInsert.setString(4, FriendshipStatus.ACCEPTED.name());
            psInsert.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<UserEntity> findAllByUsername(String username) {
        List<UserEntity> users = new ArrayList<>();
        try (PreparedStatement ps = holder(URL).connection().prepareStatement(
                "SELECT * FROM \"user\" WHERE username = ?"
        )) {
            ps.setObject(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UserEntity user = new UserEntity();
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
    public List<UserEntity> findAll() {
        List<UserEntity> listUsers = new ArrayList<>();
        try (PreparedStatement ps = holder(URL).connection().prepareStatement(
                "SELECT * FROM \"user\""
        )) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UserEntity user = new UserEntity();
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
