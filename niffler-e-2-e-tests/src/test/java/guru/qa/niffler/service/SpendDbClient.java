package guru.qa.niffler.service;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.impl.CategoryDaoJdbc;
import guru.qa.niffler.data.dao.impl.CategoryDaoSpringJdbc;
import guru.qa.niffler.data.dao.impl.SpendDaoJdbc;
import guru.qa.niffler.data.dao.impl.SpendDaoSpringJdbc;
import guru.qa.niffler.data.entity.CategoryEntity;
import guru.qa.niffler.data.entity.SpendEntity;
import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.SpendJson;

import javax.sql.DataSource;
import java.sql.SQLException;

import static guru.qa.niffler.data.Databases.transaction;
import static guru.qa.niffler.data.Databases.dataSource;

public class SpendDbClient {

    private static final Config CFG = Config.getInstance();



    public SpendJson createSpend(SpendJson spend) throws SQLException {
        return transaction(connection -> {
                    SpendEntity spendEntity = SpendEntity.fromJson(spend);
                    if (spendEntity.getCategory().getId() == null) {
                        CategoryEntity categoryEntity = new CategoryDaoJdbc(connection)
                                .create(spendEntity.getCategory());
                        spendEntity.setCategory(categoryEntity);
                    }
                    return SpendJson.fromEntity(
                            new SpendDaoJdbc(connection).create(spendEntity));
                },
                CFG.spendJdbcUrl()
        );
    }

    public CategoryJson createCategory(CategoryJson category) throws SQLException {
        return transaction(connection -> {
                    CategoryEntity categoryEntity = CategoryEntity.fromJson(category);
                    return CategoryJson.fromEntity(
                            new CategoryDaoJdbc(connection).create(categoryEntity));
                },
                CFG.spendJdbcUrl()

        );
    }

    public CategoryJson updateCategory(CategoryJson category) {
        return transaction(connection -> {
                    CategoryEntity categoryEntity = CategoryEntity.fromJson(category);
                    return CategoryJson.fromEntity(
                            new CategoryDaoJdbc(connection).update(categoryEntity));
                },
                CFG.spendJdbcUrl()
        );
    }

    public void deleteSpending(SpendJson spending) {
        transaction(connection -> {
                    SpendEntity spendEntity = SpendEntity.fromJson(spending);
                    new SpendDaoJdbc(connection).deleteSpend(spendEntity);
                    return null;
                },
                CFG.spendJdbcUrl()
        );
    }

    public void deleteCategory(CategoryJson category) {
        transaction(connection -> {
                    CategoryEntity categoryEntity = CategoryEntity.fromJson(category);
                    new CategoryDaoJdbc(connection).deleteCategory(categoryEntity);
                    return null;
                },
                CFG.spendJdbcUrl()
        );
    }

    public SpendJson createSpendSpringJdbc(SpendJson spend) throws SQLException {
        return transaction(connection -> {
                    SpendEntity spendEntity = SpendEntity.fromJson(spend);
                    if (spendEntity.getCategory().getId() == null) {
                        CategoryEntity categoryEntity = new CategoryDaoSpringJdbc(dataSource(CFG.spendJdbcUrl()))
                                .create(spendEntity.getCategory());
                        spendEntity.setCategory(categoryEntity);
                    }
                    return SpendJson.fromEntity(
                            new SpendDaoSpringJdbc(dataSource(CFG.spendJdbcUrl())).create(spendEntity));
                },
                CFG.spendJdbcUrl()
        );
    }
    public CategoryJson createCategorySpringJdbc(CategoryJson category) throws SQLException {
        return transaction(connection -> {
                    CategoryEntity categoryEntity = CategoryEntity.fromJson(category);
                    return CategoryJson.fromEntity(
                            new CategoryDaoSpringJdbc(dataSource(CFG.spendJdbcUrl())).create(categoryEntity));
                },
                CFG.spendJdbcUrl()

        );
    }
}
