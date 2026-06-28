package guru.qa.niffler.data.dao.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.UserdataUserDao;
import guru.qa.niffler.data.entity.userdata.FriendshipStatus;
import guru.qa.niffler.data.entity.userdata.UserEntity;
import guru.qa.niffler.model.CurrencyValues;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.data.tpl.Connections.holder;

public class UserdataUserDaoJdbc implements UserdataUserDao {


    private static final Config CFG = Config.getInstance();


    @Override
    public UserEntity createUser(UserEntity user) {
        try (PreparedStatement ps = holder(CFG.userdataJdbcUrl()).connection().prepareStatement(
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
                    generatedKey = rs.getObject(1, UUID.class);
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
    public void delete(UserEntity user) {
        try (PreparedStatement ps = holder(CFG.userdataJdbcUrl()).connection().prepareStatement(
                "DELETE FROM \"user\" WHERE id = ?"
        )) {
            ps.setObject(1, user.getId());
            ps.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Optional<UserEntity> findById(UUID id) {
        try (PreparedStatement ps = holder(CFG.userdataJdbcUrl()).connection().prepareStatement(
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
    public List<UserEntity> findAllByUsername(String username) {
        List<UserEntity> users = new ArrayList<>();
        try (PreparedStatement ps = holder(CFG.userdataJdbcUrl()).connection().prepareStatement(
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
        try (PreparedStatement ps = holder(CFG.userdataJdbcUrl()).connection().prepareStatement(
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

    @Override
    public UserEntity updateUser(UserEntity user) {
        try (PreparedStatement ps = holder(CFG.userdataJdbcUrl()).connection().prepareStatement(
                "UPDATE \"user\" SET currency = ?, firstname = ?, full_name = ?, photo = ?, photo_small = ?, surname = ?, username = ? " +
                        "WHERE id = ?"
        )) {
            ps.setObject(1, user.getCurrency().name());
            ps.setString(2, user.getFirstname());
            ps.setString(3, user.getFullname());
            ps.setBytes(4, user.getPhoto());
            ps.setBytes(5, user.getPhotoSmall());
            ps.setString(6, user.getSurname());
            ps.setString(7, user.getUsername());
            ps.setObject(8, user.getId());

            int updatedRows = ps.executeUpdate();

            if (updatedRows == 0) {
                throw new SQLException("Updating user failed, no rows affected.");
            }
            return user;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addIncomeInvitation(UserEntity requester, UserEntity addressee) {
        try (PreparedStatement ps = holder(CFG.userdataJdbcUrl()).connection().prepareStatement(
                "INSERT INTO \"friendship\" (addressee_id, requester_id, created_date, status)" +
                        "VALUES (?, ?, ?, ?)"
        )) {
            ps.setObject(1, addressee.getId());
            ps.setObject(2, requester.getId());
            ps.setDate(3, Date.valueOf(LocalDate.now()));
            ps.setString(4, FriendshipStatus.PENDING.name());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addOutcomeInvitation(UserEntity requester, UserEntity addressee) {
        try (PreparedStatement ps = holder(CFG.userdataJdbcUrl()).connection().prepareStatement(
                "INSERT INTO \"friendship\" (addressee_id, requester_id, created_date, status)" +
                        "VALUES (?, ?, ?, ?)"
        )) {
            ps.setObject(1, addressee.getId());
            ps.setObject(2, requester.getId());
            ps.setDate(3, Date.valueOf(LocalDate.now()));
            ps.setString(4, FriendshipStatus.PENDING.name());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addFriend(UserEntity requester, UserEntity addressee) {
        try (PreparedStatement psUpdate = holder(CFG.userdataJdbcUrl()).connection().prepareStatement(
                "UPDATE \"friendship\" SET status = ? WHERE addressee_id = ? AND requester_id = ?");
             PreparedStatement psInsert = holder(CFG.userdataJdbcUrl()).connection().prepareStatement(
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
}
