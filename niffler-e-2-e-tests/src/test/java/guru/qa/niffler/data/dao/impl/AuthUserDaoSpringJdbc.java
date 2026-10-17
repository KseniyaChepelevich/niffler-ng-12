package guru.qa.niffler.data.dao.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.AuthUserDao;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.data.mapper.AuthAuthorityEntityRowMapper;
import guru.qa.niffler.data.mapper.AuthUserEntityRowMapper;
import guru.qa.niffler.data.tpl.DataSources;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;

import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AuthUserDaoSpringJdbc implements AuthUserDao {

    private static final Config CFG = Config.getInstance();

    private JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(DataSources.dataSource(CFG.authJdbcUrl()));
    }

    @Override
    public AuthUserEntity create(AuthUserEntity user) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate().update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO \"user\" (username, password, enabled, account_non_expired, account_non_locked, credentials_non_expired) " +
                            "VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
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

        return user;
    }

    @Override
    public Optional<AuthUserEntity> findUserById(UUID id) {
        Optional<AuthUserEntity> userOpt = jdbcTemplate().query(
                "SELECT * FROM \"user\" WHERE id = ?",
                AuthUserEntityRowMapper.instance,
                id
        ).stream().findFirst();

        userOpt.ifPresent(this::enrichAuthorities);
        return userOpt;
    }

    @Override
    public void delete(AuthUserEntity user) {
        jdbcTemplate().update("DELETE FROM \"authority\" WHERE user_id = ?", user.getId());
        jdbcTemplate().update("DELETE FROM \"user\" WHERE id = ?", user.getId());
    }

    @Override
    public AuthUserEntity update(AuthUserEntity user) {
        int updatedRows = jdbcTemplate().update(
                "UPDATE \"user\" SET username = ?, account_non_expired = ?, account_non_locked = ?, credentials_non_expired = ?, " +
                        "enabled = ?, password = ? WHERE id = ?",
                user.getUsername(),
                user.getAccountNonExpired(),
                user.getAccountNonLocked(),
                user.getCredentialsNonExpired(),
                user.getEnabled(),
                user.getPassword(),
                user.getId()
        );
        if (updatedRows == 0) {
            throw new RuntimeException("User not found with id: " + user.getId());
        }
        return user;
    }

    @Override
    public Optional<AuthUserEntity> findAllByUsername(String username) {
        List<AuthUserEntity> users = jdbcTemplate().query(
                "SELECT * FROM \"user\" WHERE username = ?",
                AuthUserEntityRowMapper.instance,
                username
        );
        users.forEach(this::enrichAuthorities);
        return users.stream().findFirst();
    }

    @Override
    public List<AuthUserEntity> findAll() {
        return jdbcTemplate().query(
                "SELECT * FROM \"user\"",
                AuthUserEntityRowMapper.instance
        );
    }

    private void enrichAuthorities(AuthUserEntity user) {
        List<AuthorityEntity> authorities = jdbcTemplate().query(
                "SELECT * FROM \"authority\" WHERE user_id = ?",
                AuthAuthorityEntityRowMapper.instance,
                user.getId()
        );
        // Связываем роли с пользователем с двух сторон (для консистентности)
        for (AuthorityEntity auth : authorities) {
            auth.setUser(user);
        }
        user.setAuthorities(authorities);
    }
}
