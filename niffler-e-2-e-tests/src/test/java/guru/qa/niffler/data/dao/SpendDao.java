package guru.qa.niffler.data.dao;

import guru.qa.niffler.data.entity.SpendEntity;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpendDao {
    SpendEntity create(SpendEntity spend) throws SQLException;

    void deleteSpend(SpendEntity spend) throws SQLException;

    SpendEntity update(SpendEntity spend) throws SQLException;

    Optional<SpendEntity> findSpendById(UUID id) throws SQLException;

    List<SpendEntity> findAllByUsername(String username) throws SQLException;

}
