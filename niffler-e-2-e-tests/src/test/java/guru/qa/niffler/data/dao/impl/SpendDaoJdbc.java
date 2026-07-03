package guru.qa.niffler.data.dao.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.Databases;
import guru.qa.niffler.data.dao.SpendDao;
import guru.qa.niffler.data.entity.CategoryEntity;
import guru.qa.niffler.data.entity.SpendEntity;
import guru.qa.niffler.model.CurrencyValues;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SpendDaoJdbc implements SpendDao {

    private static final Config CFG = Config.getInstance();

    @Override
    public SpendEntity create(SpendEntity spend) throws SQLException{
        try (Connection connection = Databases.connection(CFG.spendJdbcUrl())) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO spend (username, spend_date, currency, amount, description, category_id)" +
                            "VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            )) {
                ps.setString(1, spend.getUsername());
                ps.setDate(2, spend.getSpendDate());
                ps.setString(3, spend.getCurrency().name());
                ps.setDouble(4, spend.getAmount());
                ps.setString(5, spend.getDescription());
                ps.setObject(6, spend.getCategory().getId());

                ps.executeUpdate();

                final UUID generatedKey;
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedKey = rs.getObject("id", UUID.class);
                    } else {
                        throw new SQLException("Can't find id in ResultSet");
                    }
                }
                spend.setId(generatedKey);
                return spend;
            }
        }
    }

    @Override
    public void deleteSpend(SpendEntity spend) throws SQLException{
        try (Connection connection = Databases.connection(CFG.spendJdbcUrl())) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM spend WHERE id = ?")) {
                ps.setObject(1, spend.getId());
                ps.executeUpdate();
            }
        }
    }


    @Override
    public SpendEntity update(SpendEntity spend) throws SQLException{
        try (Connection connection = Databases.connection(CFG.spendJdbcUrl())) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "UPDATE spend SET username = ?, spend_date = ?, currency = ?, amount = ?, description = ?, category_id = ?)" +
                            "WHERE id = ?",
                    Statement.RETURN_GENERATED_KEYS
            )) {
                ps.setString(1, spend.getUsername());
                ps.setDate(2, spend.getSpendDate());
                ps.setString(3, spend.getCurrency().name());
                ps.setDouble(4, spend.getAmount());
                ps.setString(5, spend.getDescription());
                ps.setObject(6, spend.getCategory().getId());

                int updatedRows = ps.executeUpdate();
                if (updatedRows == 0) {
                    throw new SQLException("Updating spend failed, no rows affected.");
                }
                return spend;
            }
        }
    }

    @Override
    public Optional<SpendEntity> findSpendById(UUID id) throws SQLException {
        try (Connection connection = Databases.connection(CFG.spendJdbcUrl())) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM spend WHERE id = ?")) {
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
    public List<SpendEntity> findAllByUsername(String username) throws SQLException{
        List<SpendEntity> listSpends = new ArrayList<>();
        try (Connection connection = Databases.connection(CFG.spendJdbcUrl())) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM spend WHERE username = ?")) {
                ps.setString(1, username);
                ps.execute();
                try (ResultSet rs = ps.getResultSet()) {
                    while (rs.next()) {
                        listSpends.add(mapRow(rs));
                    }
                }
                return listSpends;
            }
        }
    }

    private SpendEntity mapRow(ResultSet rs) throws SQLException {
        SpendEntity spend = new SpendEntity();
        spend.setId(rs.getObject("id", UUID.class));
        spend.setUsername(rs.getString("username"));
        spend.setSpendDate(rs.getDate("spend_date"));
        spend.setCurrency(CurrencyValues.valueOf(rs.getString("currency")));
        spend.setAmount(rs.getDouble("amount"));
        spend.setDescription(rs.getString("description"));

        CategoryEntity category = new CategoryEntity();
        category.setId(rs.getObject("category_id", UUID.class));
        spend.setCategory(category);

        return spend;
    }
}
