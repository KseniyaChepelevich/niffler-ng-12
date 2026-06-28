package guru.qa.niffler.data.dao.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.CategoryDao;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.mapper.CategoryEntityRowMapper;
import guru.qa.niffler.data.tpl.DataSources;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CategoryDaoSpringJdbc implements CategoryDao {

    private static final Config CFG = Config.getInstance();

    private JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(DataSources.dataSource(CFG.spendJdbcUrl()));
    }


    @Override
    public CategoryEntity create(CategoryEntity category) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate().update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO \"category\" (archived, name, username)" +
                            "VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setBoolean(1, category.isArchived());
            ps.setString(2, category.getName());
            ps.setString(3, category.getUsername());
            return ps;
        }, kh);
        final UUID generateKey = (UUID) kh.getKeys().get("id");
        category.setId(generateKey);
        return category;
    }

    @Override
    public Optional<CategoryEntity> findCategoryById(UUID id) {
        return Optional.ofNullable(
                jdbcTemplate().queryForObject(
                        "SELECT * FROM \"category\" WHERE id = ?",
                        CategoryEntityRowMapper.instance,
                        id
                )
        );
    }

    @Override
    public void delete(CategoryEntity category) {
        jdbcTemplate().update(
                "DELETE FROM \"category\" WHERE id = ?",
                category.getId()
        );
    }

    @Override
    public CategoryEntity update(CategoryEntity category) {
        jdbcTemplate().update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE \"category\" SET archived = ?, name = ?, username = ?  " +
                            "WHERE id = ?"
            );
            ps.setBoolean(1, category.isArchived());
            ps.setString(2, category.getName());
            ps.setString(3, category.getUsername());
            ps.setObject(4, category.getId());

            return ps;
        });
        return category;
    }

    @Override
    public List<CategoryEntity> findAllByUsername(String username) {
        return jdbcTemplate().query(
                "SELECT * FROM \"category\" WHERE username = ?",
                CategoryEntityRowMapper.instance,
                username
        );
    }

    @Override
    public Optional<CategoryEntity> findAllByUsernameAndCategoryName(String username, String name) {
        return jdbcTemplate().query(
                "SELECT * FROM \"category\" WHERE username = ? AND name = ?",
                (rs, rowNum) -> {
                    CategoryEntity ce = new CategoryEntity();
                    ce.setId(rs.getObject("id", UUID.class));
                    ce.setName(rs.getString("name"));
                    ce.setUsername(rs.getString("username"));
                    return ce;
                },
                username,
                name
        ).stream().findFirst();
    }

    @Override
    public List<CategoryEntity> findAll() {
        return jdbcTemplate().query(
                "SELECT * FROM \"category\"",
                CategoryEntityRowMapper.instance
        );
    }
}
