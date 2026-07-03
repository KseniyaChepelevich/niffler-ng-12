package guru.qa.niffler.data.dao.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.Databases;
import guru.qa.niffler.data.dao.CategoryDao;
import guru.qa.niffler.data.entity.CategoryEntity;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CategoryDaoJdbc implements CategoryDao {

    private static final Config CFG = Config.getInstance();


    @Override
    public CategoryEntity create(CategoryEntity category) throws SQLException{
        try (Connection connection = Databases.connection(CFG.spendJdbcUrl())) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO category (username, name, archived)" +
                            "VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            )) {
                ps.setString(1, category.getUsername());
                ps.setString(2, category.getName());
                ps.setBoolean(3, category.isArchived());

                ps.executeUpdate();

                final UUID generatedKey;
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedKey = rs.getObject("id", UUID.class);
                    } else {
                        throw new SQLException("Can't find id in ResultSet");
                    }
                }
                category.setId(generatedKey);
                return category;
            }
        }
    }

    @Override
    public Optional<CategoryEntity> findCategoryById(UUID id) throws SQLException{
        try (Connection connection = Databases.connection(CFG.spendJdbcUrl())) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM category WHERE id = ?"
            )) {
                ps.setObject(1, id);
                ps.execute();
                try (ResultSet rs = ps.getResultSet()) {
                    if (rs.next()) {
                        return Optional.of(mapRow(rs));
                    } else {
                        return Optional.empty();
                    }
                }
            }
        }
    }

    @Override
    public void deleteCategory(CategoryEntity category) throws SQLException{
        try (Connection connection = Databases.connection(CFG.spendJdbcUrl())) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM category WHERE id = ?"
            )) {
                ps.setObject(1, category.getId());
                ps.executeUpdate();
            }
        }
    }

    @Override
    public CategoryEntity update(CategoryEntity category) throws SQLException{
        try (Connection connection = Databases.connection(CFG.spendJdbcUrl())) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "UPDATE category SET username = ?, name = ?, archived = ?" +
                            "WHERE id = ?",
                    Statement.RETURN_GENERATED_KEYS
            )) {
                ps.setString(1, category.getUsername());
                ps.setString(2, category.getName());
                ps.setBoolean(3, category.isArchived());

                ps.executeUpdate();

                int updatedRows = ps.executeUpdate();
                if (updatedRows == 0) {
                    throw new SQLException("Updating spend failed, no rows affected.");
                }
                return category;
            }
        }
    }

    @Override
    public Optional<CategoryEntity> findAllByUsernameAndCategoryName(String username,String categoryName) throws SQLException{
        try (Connection connection = Databases.connection(CFG.spendJdbcUrl())) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM category WHERE username = ? AND name = ?")) {
                ps.setString(1, username);
                ps.setString(2, categoryName);
                ps.execute();

                try (ResultSet rs = ps.getResultSet()) {
                    if (rs.next()) {
                        return Optional.of(mapRow(rs));
                    } else {
                        return Optional.empty();
                    }
                }
            }
        }
    }

    @Override
    public List<CategoryEntity> findAllByUsername(String username) throws SQLException{
        List<CategoryEntity> listCategory = new ArrayList<>();
        try (Connection connection = Databases.connection(CFG.spendJdbcUrl())) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM category WHERE username = ?")) {
                ps.setString(1, username);
                ps.execute();

                try (ResultSet rs = ps.getResultSet()) {
                    while (rs.next()) {
                        listCategory.add(mapRow(rs));
                    }
                }
            }return listCategory;
        }
    }

    private CategoryEntity mapRow(ResultSet rs) throws SQLException{
        CategoryEntity ce = new CategoryEntity();
        ce.setId(rs.getObject("id", UUID.class));
        ce.setUsername(rs.getString("username"));
        ce.setName(rs.getString("name"));
        ce.setArchived(rs.getBoolean("archived"));

        return ce;
    }
}