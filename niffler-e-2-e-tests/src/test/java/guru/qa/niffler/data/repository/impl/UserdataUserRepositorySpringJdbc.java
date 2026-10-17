package guru.qa.niffler.data.repository.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.userdata.FriendshipStatus;
import guru.qa.niffler.data.entity.userdata.UserEntity;
import guru.qa.niffler.data.mapper.UserdataUserEntityRowMapper;
import guru.qa.niffler.data.repository.UserdataUserRepository;
import guru.qa.niffler.data.tpl.DataSources;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UserdataUserRepositorySpringJdbc implements UserdataUserRepository, ResultSetExtractor<List<UserEntity>> {
    private static final Config CFG = Config.getInstance();
    private static final String URL = CFG.userdataJdbcUrl();


    @Override
    public UserEntity create(UserEntity user) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(URL));
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
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
    public void delete(UUID id) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(URL));
        jdbcTemplate.update("DELETE FROM \"friendship\" WHERE requester_id = ? OR addressee_id = ?", id);
        jdbcTemplate.update("DELETE FROM \"user\" WHERE id = ?", id);
    }

    @Override
    public Optional<UserEntity> findById(UUID id) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(URL));
        List<UserEntity> users = jdbcTemplate.query(
                        "SELECT * FROM \"user\" WHERE id = ?",
                        this,
                        id
                );
        return users != null && !users.isEmpty() ? Optional.of(users.get(0)) : Optional.empty();
    }

    @Override
    public void addIncomeInvitation(UserEntity requester, UserEntity addressee) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(URL));
        jdbcTemplate.update(
                "INSERT INTO \"friendship\" (addressee_id, requester_id, created_date, status)" +
                        "VALUES (?, ?, ?, ?)",
                addressee.getId(),
                requester.getId(),
                LocalDate.now(),
                FriendshipStatus.PENDING.name()
        );

    }

    @Override
    public void addOutcomeInvitation(UserEntity requester, UserEntity addressee) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(URL));
        jdbcTemplate.update(
                "INSERT INTO \"friendship\" (addressee_id, requester_id, created_date, status)" +
                        "VALUES (?, ?, ?, ?)",
                addressee.getId(),
                requester.getId(),
                LocalDate.now(),
                FriendshipStatus.PENDING.name()
        );

    }

    @Override
    public void addFriend(UserEntity requester, UserEntity addressee) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(URL));
        jdbcTemplate.update("UPDATE \"friendship\" SET status = ? WHERE requester_id = ? AND addressee_id = ?",
                FriendshipStatus.ACCEPTED.name(),
                requester.getId(),
                addressee.getId());


        jdbcTemplate.update(
                "INSERT INTO \"friendship\" (addressee_id, requester_id, created_date, status)" +
                        "VALUES (?, ?, ?, ?)",
                requester.getId(),
                addressee.getId(),
                LocalDate.now(),
                FriendshipStatus.ACCEPTED.name()
        );
    }

    @Override
    public List<UserEntity> findAllByUsername(String username) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(URL));
        return jdbcTemplate.query(
                "SELECT * FROM \"user\" WHERE username = ?",
               this,
                username
        );
    }

    @Override
    public List<UserEntity> findAll() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(URL));
        return jdbcTemplate.query(
                "SELECT * FROM \"user\"",
               this
        );
    }

    @Override
    public List<UserEntity> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<UUID, UserEntity> userMap = new ConcurrentHashMap<>();
        while (rs.next()) {
            UUID userId = rs.getObject("id", UUID.class);
            userMap.computeIfAbsent(userId, id -> {
                try {
                    UserEntity ue = UserdataUserEntityRowMapper.instance.mapRow(rs, rs.getRow());
                    if (ue != null) {
                        ue.setFriendshipRequests(new ArrayList<>());
                        ue.setFriendshipAddressees(new ArrayList<>());
                    }
                    return ue;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return new ArrayList<>(userMap.values());
    }
}
