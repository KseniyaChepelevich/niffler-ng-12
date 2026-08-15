package guru.qa.niffler.data.dao.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.AuthAuthorityDao;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.data.mapper.AuthAuthorityEntityRowMapper;
import guru.qa.niffler.data.tpl.DataSources;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class AuthAuthorityDaoSpringJdbc implements AuthAuthorityDao {

    private static final Config CFG = Config.getInstance();

    private JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(DataSources.dataSource(CFG.authJdbcUrl()));
    }

    @Override
    public void create(AuthorityEntity... authorities) {
        jdbcTemplate().batchUpdate(
                "INSERT INTO \"authority\" (user_id, authority) VALUES (?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setObject(1, authorities[i].getUser().getId());
                        ps.setString(2, authorities[i].getAuthority().name());
                    }

                    @Override
                    public int getBatchSize() {
                        return authorities.length;
                    }
                }
        );

    }

    @Override
    public List<AuthorityEntity> findAll() {
        return jdbcTemplate().query(
                "SELECT * FROM \"authority\"",
                AuthAuthorityEntityRowMapper.instance
        );
    }

    @Override
    public List<AuthorityEntity> findAuthorityByUserId(UUID userId) {
        return jdbcTemplate().query(
                "SELECT * FROM \"authority\" WHERE user_id = ?",
                AuthAuthorityEntityRowMapper.instance,
                userId
        );
    }
}
