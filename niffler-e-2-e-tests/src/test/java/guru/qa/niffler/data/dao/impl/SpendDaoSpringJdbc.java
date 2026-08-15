package guru.qa.niffler.data.dao.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.SpendDao;
import guru.qa.niffler.data.entity.spend.SpendEntity;
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

    private JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(DataSources.dataSource(CFG.spendJdbcUrl()));
    }

    @Override
    public SpendEntity create(SpendEntity spend) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate().update(con -> {
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
        if (kh.getKeys() != null) {
            Object idObj = kh.getKeys().get("id");
            if (idObj == null) {
                idObj = kh.getKeys().get("ID");
            }
            if (idObj != null) {
                spend.setId(UUID.fromString(idObj.toString()));
            }
        }
        return spend;
    }

    @Override
    public void delete(SpendEntity spend) {
        jdbcTemplate().update(
                "DELETE FROM \"spend\" WHERE id = ?",
                spend.getId()
        );
    }

    @Override
    public SpendEntity update(SpendEntity spend) {
        jdbcTemplate().update(
                    "UPDATE \"spend\" SET amount = ?, category_id = ?, currency = ?, description = ?, spend_date = ?, username = ? " +
                            "WHERE id = ?",

           spend.getAmount(),
            spend.getCategory().getId(),
           spend.getCurrency().name(),
           spend.getDescription(),
        new java.sql.Date(spend.getSpendDate().getTime()),
           spend.getUsername(),
            spend.getId()
        );
        return spend;
    }

    @Override
    public Optional<SpendEntity> findSpendById(UUID id) {
        return Optional.ofNullable(
                jdbcTemplate().queryForObject(
                        "SELECT * FROM \"spend\" WHERE id = ?",
                        SpendEntityRowMapper.instance,
                        id
                )
        );
    }

    @Override
    public Optional<SpendEntity> findSpendByUsernameAndDescription(String username, String description) {
        return jdbcTemplate().query(
                "SELECT * FROM \"spend\" WHERE username = ? AND description = ?",
                SpendEntityRowMapper.instance,
                username,
                description
        ).stream().findFirst();
    }

    @Override
    public List<SpendEntity> findAllByUsername(String username) {
        return jdbcTemplate().query(
                "SELECT * FROM \"spend\" WHERE username = ?",
                SpendEntityRowMapper.instance,
                username
        );
    }

    @Override
    public List<SpendEntity> findAll() {
        return jdbcTemplate().query(
                "SELECT * FROM \"spend\"",
                SpendEntityRowMapper.instance
        );
    }
}
