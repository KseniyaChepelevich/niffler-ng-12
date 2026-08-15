package guru.qa.niffler.data.repository.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.AuthAuthorityDao;
import guru.qa.niffler.data.dao.AuthUserDao;
import guru.qa.niffler.data.dao.impl.AuthAuthorityDaoSpringJdbc;
import guru.qa.niffler.data.dao.impl.AuthUserDaoSpringJdbc;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.Authority;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.data.repository.AuthUserRepository;
import guru.qa.niffler.data.tpl.DataSources;
import org.springframework.dao.DataAccessException;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;
import java.util.ArrayList;

import java.util.concurrent.ConcurrentHashMap;

public class AuthUserRepositorySpringJdbc implements AuthUserRepository, ResultSetExtractor<List<AuthUserEntity>> {

    private static final Config CFG = Config.getInstance();
    private static final String URL = CFG.authJdbcUrl();

    private final AuthUserDao authUserDao = new AuthUserDaoSpringJdbc();
    private final AuthAuthorityDao authAuthorityDao = new AuthAuthorityDaoSpringJdbc();

    @Override
    public AuthUserEntity create(AuthUserEntity user) {
        authUserDao.create(user);
        for (AuthorityEntity authority : user.getAuthorities()) {
            authority.setUser(user);
        }
        authAuthorityDao.create(user.getAuthorities().toArray(new AuthorityEntity[0]));
                return user;
    }

    @Override
    public Optional<AuthUserEntity> findById(UUID id) {
       return authUserDao.findUserById(id);
    }

    @Override
    public void remove(AuthUserEntity user) {
        authUserDao.delete(user);
    }

    @Override
    public AuthUserEntity update(AuthUserEntity user) {
        authUserDao.update(user);
        return user;
    }

    @Override
    public Optional<AuthUserEntity> findByUsername(String username) {
        return authUserDao.findAllByUsername(username).stream().findFirst();
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
