package guru.qa.niffler.data.repository.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.Authority;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.data.mapper.AuthUserEntityRowMapper;
import guru.qa.niffler.data.repository.AuthUserRepository;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import static guru.qa.niffler.data.tpl.Connections.holder;

public class AuthUserRepositoryJdbc implements AuthUserRepository {

    private static final PasswordEncoder pe = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private static final Config CFG = Config.getInstance();
    private static final String URL = CFG.authJdbcUrl();

    @Override
    public AuthUserEntity create(AuthUserEntity user) {
        try (PreparedStatement userPs = holder(URL).connection().prepareStatement(
                "INSERT INTO \"user\" (username, account_non_expired, account_non_locked, credentials_non_expired, enabled, password)" +
                        "VALUES (?, ?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
             PreparedStatement authorityPs = holder(URL).connection().prepareStatement(
                     "INSERT INTO \"authority\" (\"authority\", user_id)" +
                             "VALUES (?, ?)")
        ) {
            userPs.setString(1, user.getUsername());
            userPs.setBoolean(2, user.getAccountNonExpired());
            userPs.setBoolean(3, user.getAccountNonLocked());
            userPs.setBoolean(4, user.getCredentialsNonExpired());
            userPs.setBoolean(5, user.getEnabled());
            userPs.setString(6, pe.encode(user.getPassword()));

            userPs.executeUpdate();

            final UUID generatedKey;
            try (ResultSet rs = userPs.getGeneratedKeys()) {
                if (rs.next()) {
                    generatedKey = rs.getObject(1, UUID.class);
                } else {
                    throw new SQLException("Can't find id in ResultSet");
                }
            }
            user.setId(generatedKey);

            for (AuthorityEntity authority : user.getAuthorities()) {
                authorityPs.setString(1, authority.getAuthority().name());
                authorityPs.setObject(2, generatedKey);
                authorityPs.addBatch();
            }
            authorityPs.executeBatch();
            return user;
        } catch (
                SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<AuthUserEntity> findById(UUID id) {
        try (PreparedStatement ps = holder(URL).connection().prepareStatement(
                "SELECT a.id as authority_id, authority, u.id, u.username, u.password, " +
                        "u.enabled, u.account_non_expired, u.account_non_locked, u.credentials_non_expired " +
                        "FROM \"user\" u JOIN authority a ON u.id = a.user_id WHERE u.id = ?"
        )) {
            ps.setObject(1, id);
            ps.execute();

            try (ResultSet rs = ps.getResultSet()) {
                AuthUserEntity user = null;
                List<AuthorityEntity> authorityEntities = new ArrayList<>();
                while (rs.next()) {
                    if (user == null) {
                        user = AuthUserEntityRowMapper.instance.mapRow(rs, 1);
                    }

                    AuthorityEntity ae = new AuthorityEntity();
                    ae.setUser(user);
                    ae.setAuthority(Authority.valueOf(rs.getString("authority")));
                    ae.setId(rs.getObject("authority_id", UUID.class));
                    authorityEntities.add(ae);
                }
                if (user == null) {
                    return Optional.empty();
                } else {
                    user.setAuthorities(authorityEntities);
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(AuthUserEntity user) {
        try {
            try (PreparedStatement psAuth = holder(URL).connection().prepareStatement(
                    "DELETE FROM \"authority\" WHERE user_id = ?"
            )) {
                psAuth.setObject(1, user.getId());
                psAuth.execute();

            }
            try (PreparedStatement ps = holder(URL).connection().prepareStatement(
                    "DELETE FROM \"user\" WHERE id = ?"
            )) {
                ps.setObject(1, user.getId());
                ps.execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AuthUserEntity update(AuthUserEntity user) {
        try (PreparedStatement ps = holder(URL).connection().prepareStatement(
                "UPDATE \"user\" SET username = ?, account_non_expired = ?, account_non_locked = ?, credentials_non_expired = ?, enabled = ?, password = ?" +
                        "WHERE id = ?",
                Statement.RETURN_GENERATED_KEYS
        )) {
            ps.setString(1, user.getUsername());
            ps.setBoolean(2, user.getAccountNonExpired());
            ps.setBoolean(3, user.getAccountNonLocked());
            ps.setBoolean(4, user.getCredentialsNonExpired());
            ps.setBoolean(5, user.getEnabled());
            ps.setString(6, user.getPassword());
            ps.setObject(7, user.getId());

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
    public Optional<AuthUserEntity> findByUsername(String username) {
        try (PreparedStatement ps = holder(URL).connection().prepareStatement(
                "SELECT a.id as authority_id, authority, u.id, u.username, u.password, " +
                        "u.enabled, u.account_non_expired, u.account_non_locked, u.credentials_non_expired " +
                        "FROM \"user\" u JOIN authority a ON u.id = a.user_id WHERE u.username = ?"
        )) {
            ps.setString(1, username);
            ps.execute();
            try (ResultSet rs = ps.getResultSet()) {
                AuthUserEntity user = null;
                List<AuthorityEntity> authorityEntities = new ArrayList<>();
                while (rs.next()) {
                    if (user == null) {
                        user = AuthUserEntityRowMapper.instance.mapRow(rs, 1);
                    }
                    AuthorityEntity authorityEntity = new AuthorityEntity();
                    authorityEntity.setUser(user);
                    authorityEntity.setId(rs.getObject("authority_id", UUID.class));
                    authorityEntity.setAuthority(Authority.valueOf(rs.getString("authority")));
                    authorityEntities.add(authorityEntity);
                }
                if (user == null) {
                    return Optional.empty();
                } else {
                    user.setAuthorities(authorityEntities);
                    return Optional.of(user);
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<AuthUserEntity> findAll() {
        Map<UUID, AuthUserEntity> userMap = new LinkedHashMap<>();
        try (PreparedStatement ps = holder(URL).connection().prepareStatement(
                "SELECT a.id as authority_id, authority, u.id, u.username, u.password, " +
                        "u.enabled, u.account_non_expired, u.account_non_locked, u.credentials_non_expired " +
                        "FROM \"user\" u JOIN authority a ON u.id = a.user_id"
        )) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID userId = rs.getObject("id", UUID.class);
                    AuthUserEntity user = userMap.computeIfAbsent(userId, id -> {
                        try {
                            AuthUserEntity ue = AuthUserEntityRowMapper.instance.mapRow(rs, 1);
                            if (ue != null) {
                                ue.setAuthorities(new ArrayList<>());
                            }
                            return ue;
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    AuthorityEntity ae = new AuthorityEntity();
                    ae.setUser(user);
                    ae.setId(rs.getObject("authority_id", UUID.class));
                    ae.setAuthority(Authority.valueOf(rs.getString("authority")));
                    user.getAuthorities().add(ae);
                }
            }
            return new ArrayList<>(userMap.values());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
