package guru.qa.niffler.data.repository.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.Authority;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.data.repository.AuthUserRepository;
import guru.qa.niffler.data.tpl.DataSources;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AuthUserRepositorySpringJdbc implements AuthUserRepository, ResultSetExtractor<List<AuthUserEntity>> {

    private static final Config CFG = Config.getInstance();
    private static final String URL = CFG.authJdbcUrl();


    @Override
    public AuthUserEntity create(AuthUserEntity user) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(URL));
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO \"user\" (username, password, enabled, account_non_expired, account_non_locked, credentials_non_expired) " +
                            "VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setBoolean(3, user.getEnabled());
            ps.setBoolean(4, user.getAccountNonExpired());
            ps.setBoolean(5, user.getAccountNonLocked());
            ps.setBoolean(6, user.getCredentialsNonExpired());
            return ps;
        }, kh);
        final UUID generatedKey = (UUID) kh.getKeys().get("id");
        user.setId(generatedKey);
        List<AuthorityEntity> authorities = user.getAuthorities();
        jdbcTemplate.batchUpdate(
                "INSERT INTO \"authority\" (\"authority\", user_id)" +
                        "VALUES (?, ?)",
                new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        AuthorityEntity authority = authorities.get(i);
                        ps.setString(1, authority.getAuthority().name());
                        ps.setObject(2, generatedKey);
                    }

                    @Override
                    public int getBatchSize() {
                        return authorities.size();
                    }
                }
        );

        return user;
    }

    @Override
    public Optional<AuthUserEntity> findUserById(UUID id) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(URL));
        List<AuthUserEntity> users = jdbcTemplate.query(
                "SELECT a.id as authority_id, authority, u.id, u.username, u.password, " +
                        "u.enabled, u.account_non_expired, u.account_non_locked, u.credentials_non_expired " +
                        "FROM \"user\" u JOIN authority a ON u.id = a.user_id WHERE u.id = ?",
                this,
                id
        );
        return users != null && !users.isEmpty()
                ? Optional.of(users.get(0))
                : Optional.empty();
    }


    @Override
    public void delete(UUID id) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(URL));
        jdbcTemplate.update("DELETE FROM \"authority\" WHERE user_id = ?", id);
        jdbcTemplate.update("DELETE FROM \"user\" WHERE id = ?", id);
    }

    @Override
    public AuthUserEntity update(AuthUserEntity user) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(URL));
        jdbcTemplate.update(
                "UPDATE \"user\" SET username = ?, account_non_expired = ?, account_non_locked = ?, credentials_non_expired = ?, enabled = ?, password = ?" +
                        "WHERE id = ?",
                user.getUsername(),
                user.getAccountNonExpired(),
                user.getAccountNonLocked(),
                user.getCredentialsNonExpired(),
                user.getEnabled(),
                user.getPassword(),
                user.getId()
        );
        return user;
    }

    @Override
    public Optional<AuthUserEntity> findAllByUsername(String username) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(URL));
        List<AuthUserEntity> users = jdbcTemplate.query(
                "SELECT a.id as authority_id, authority, u.id, u.username, u.password, " +
                        "u.enabled, u.account_non_expired, u.account_non_locked, u.credentials_non_expired " +
                        "FROM \"user\" u JOIN authority a ON u.id = a.user_id WHERE u.username = ?",
                this,
                username
        );
        return users != null && !users.isEmpty()
                ? Optional.of(users.get(0))
                : Optional.empty();
    }

    @Override
    public List<AuthUserEntity> findAll() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(URL));
        return jdbcTemplate.query(
                "SELECT a.id as authority_id, authority, u.id, u.username, u.password, " +
                        "u.enabled, u.account_non_expired, u.account_non_locked, u.credentials_non_expired " +
                        "FROM \"user\" u JOIN authority a ON u.id = a.user_id",
                this
        );
    }

    @Override
    public List<AuthUserEntity> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<UUID, AuthUserEntity> userMap = new ConcurrentHashMap<>();
        UUID userId = null;
        while (rs.next()) {
            userId = rs.getObject("id", UUID.class);
            AuthUserEntity user = userMap.computeIfAbsent(userId, id -> {
                AuthUserEntity ue = new AuthUserEntity();
                ue.setId(id);
                try {
                    ue.setUsername(rs.getString("username"));

                    ue.setPassword(rs.getString("password"));
                    ue.setEnabled(rs.getBoolean("enabled"));
                    ue.setAccountNonExpired(rs.getBoolean("account_non_expired"));
                    ue.setAccountNonLocked(rs.getBoolean("account_non_locked"));
                    ue.setCredentialsNonExpired(rs.getBoolean("credentials_non_expired"));
                    ue.setAuthorities(new ArrayList<>());
                    return ue;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

            AuthorityEntity authority = new AuthorityEntity();
            authority.setId(rs.getObject("authority_id", UUID.class));
            authority.setAuthority(Authority.valueOf(rs.getString("authority")));
            authority.setUser(user);
            user.getAuthorities().add(authority);
        }
        return new ArrayList<>(userMap.values());
    }
}
