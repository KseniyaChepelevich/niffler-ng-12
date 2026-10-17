package guru.qa.niffler.data.dao.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.UserdataUserDao;
import guru.qa.niffler.data.entity.userdata.FriendshipStatus;
import guru.qa.niffler.data.entity.userdata.UserEntity;
import guru.qa.niffler.data.mapper.UserdataUserEntityRowMapper;
import guru.qa.niffler.data.tpl.DataSources;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserdataUserDaoSpringJdbc implements UserdataUserDao {
    private static final Config CFG = Config.getInstance();

    private JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(DataSources.dataSource(CFG.userdataJdbcUrl()));
    }


    @Override
    public UserEntity createUser(UserEntity user) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate().update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO \"user\" (username, currency, firstname, full_name, photo, photo_small, surname) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getCurrency().name());
            ps.setString(3, user.getFirstname());
            ps.setString(4, user.getFullname());
            ps.setBytes(5, user.getPhoto());
            ps.setBytes(6, user.getPhotoSmall());
            ps.setString(7, user.getSurname());
            return ps;
        }, kh);
        final UUID generatedKey = (UUID) kh.getKeys().get("id");
        user.setId(generatedKey);
        return user;
    }

    @Override
    public void delete(UserEntity user) {
        jdbcTemplate().update(
                "DELETE FROM \"user\" WHERE id = ?",
                user.getId()
        );
    }

    @Override
    public Optional<UserEntity> findById(UUID id) {
        return jdbcTemplate().query(

                        "SELECT * FROM \"user\" WHERE id = ?",
                        UserdataUserEntityRowMapper.instance,
                        id
                ).stream().findFirst();
    }

    @Override
    public List<UserEntity> findAllByUsername(String username) {
        return jdbcTemplate().query(
                "SELECT * FROM \"user\" WHERE username = ?",
                UserdataUserEntityRowMapper.instance,
                username
        );
    }

    @Override
    public List<UserEntity> findAll() {
        return jdbcTemplate().query(
                "SELECT * FROM \"user\"",
                UserdataUserEntityRowMapper.instance
        );
    }

    @Override
    public UserEntity updateUser(UserEntity user) {
        int updatedRows = jdbcTemplate().update(
                "UPDATE \"user\" SET currency = ?, firstname = ?, full_name = ?, photo = ?, photo_small = ?, surname = ?, username = ? " +
                        "WHERE id = ?",
                user.getCurrency() != null ? user.getCurrency().name() : null,
                user.getFirstname(),
                user.getFullname(),
                user.getPhoto(),
                user.getPhotoSmall(),
                user.getSurname(),
                user.getUsername(),
                user.getId()
        );

        if (updatedRows == 0) {
            throw new RuntimeException("User not found with id: " + user.getId());
        }
        return user;
    }

    @Override
    public void addIncomeInvitation(UserEntity requester, UserEntity addressee) {
        jdbcTemplate().update(
                "INSERT INTO \"friendship\" (addressee_id, requester_id, created_date, status)" +
                        "VALUES (?, ?, ?, ?)",
           addressee.getId(),
            requester.getId(),
           Date.valueOf(LocalDate.now()),
            FriendshipStatus.PENDING.name()
         );
    }

    @Override
    public void addOutcomeInvitation(UserEntity requester, UserEntity addressee) {
        jdbcTemplate().update(
                "INSERT INTO \"friendship\" (addressee_id, requester_id, created_date, status)" +
                        "VALUES (?, ?, ?, ?)",
           addressee.getId(),
            requester.getId(),
            Date.valueOf(LocalDate.now()),
            FriendshipStatus.PENDING.name()
        );
    }

    @Override
    public void addFriend(UserEntity requester, UserEntity addressee) {
        jdbcTemplate().update(
                "UPDATE \"friendship\" SET status = ? WHERE addressee_id = ? AND requester_id = ?",
                FriendshipStatus.ACCEPTED.name(),
                addressee.getId(),
                requester.getId()
        );

        jdbcTemplate().update(
                "INSERT INTO \"friendship\" (addressee_id, requester_id, created_date, status) VALUES (?, ?, ?, ?)",
                requester.getId(),
                addressee.getId(),
                Date.valueOf(LocalDate.now()),
                FriendshipStatus.ACCEPTED.name()
        );

    }
}
