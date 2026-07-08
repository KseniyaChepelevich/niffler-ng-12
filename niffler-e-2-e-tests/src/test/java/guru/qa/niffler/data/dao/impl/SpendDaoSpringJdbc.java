package guru.qa.niffler.data.dao.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.SpendDao;
import guru.qa.niffler.data.entity.SpendEntity;
import guru.qa.niffler.data.mapper.SpendEntityRowMapper;
import guru.qa.niffler.data.tpl.DataSources;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SpendDaoSpringJdbc implements SpendDao {

    private static final Config CFG = Config.getInstance();


    @Override
    public SpendEntity create(SpendEntity spend) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.spendJdbcUrl()));
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO \"spend\" (amount, category_id, currency, description, spend_date, username)" +
                            "VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setDouble(1, spend.getAmount());
            ps.setObject(2, spend.getCategory().getId());
            ps.setString(3, spend.getCurrency().name());
            ps.setString(4, spend.getDescription());
            ps.setDate(5, new java.sql.Date(spend.getSpendDate().getTime()));
            ps.setString(6, spend.getUsername());
            return ps;


        }, kh);
        final UUID generateKey = (UUID) kh.getKeys().get("id");
        spend.setId(generateKey);
        return spend;
    }

    @Override
    public void deleteSpend(SpendEntity spend) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.spendJdbcUrl()));
        jdbcTemplate.update(
                "DELETE FROM \"spend\" WHERE id = ?",
                spend.getId()
        );
    }

    @Override
    public SpendEntity update(SpendEntity spend) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.spendJdbcUrl()));
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE \"spend\" SET amount = ?, category_id = ?, currency = ?, description = ?, spend_date = ?, username = ? " +
                            "WHERE id = ?"
            );
            ps.setDouble(1, spend.getAmount());
            ps.setObject(2, spend.getCategory().getId());
            ps.setString(3, spend.getCurrency().name());
            ps.setString(4, spend.getDescription());
            ps.setDate(5, new java.sql.Date(spend.getSpendDate().getTime()));
            ps.setString(6, spend.getUsername());
            ps.setObject(7, spend.getId());
            return ps;
        });
        return spend;
    }

    @Override
    public Optional<SpendEntity> findSpendById(UUID id) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.spendJdbcUrl()));
        return Optional.ofNullable(
                jdbcTemplate.queryForObject(
                        "SELECT * FROM \"spend\" WHERE id = ?",
                        SpendEntityRowMapper.instance,
                        id
                )
        );
    }

    @Override
    public List<SpendEntity> findAllByUsername(String username) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.spendJdbcUrl()));
        return jdbcTemplate.query(
                "SELECT * FROM \"spend\" WHERE username = ?",
                SpendEntityRowMapper.instance,
                username
        );
    }

    @Override
    public List<SpendEntity> findAll() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(CFG.spendJdbcUrl()));
        return jdbcTemplate.query(
                "SELECT * FROM \"spend\" WHERE",
                SpendEntityRowMapper.instance
        );
    }
}
