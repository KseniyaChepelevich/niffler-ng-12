package guru.qa.niffler.service;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.impl.CategoryDaoJdbc;
import guru.qa.niffler.data.dao.impl.SpendDaoJdbc;
import guru.qa.niffler.data.entity.CategoryEntity;
import guru.qa.niffler.data.entity.SpendEntity;
import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.SpendJson;

import java.util.Optional;

import static guru.qa.niffler.data.Databases.transaction;

public class SpendDbClient implements SpendClient {

    private static final Config CFG = Config.getInstance();


    @Override
    public SpendJson createSpending(SpendJson spending) {
        return transaction(connection -> {
                    SpendEntity spendEntity = SpendEntity.fromJson(spending);
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

    @Override
    public CategoryJson createCategory(CategoryJson category) {
        return transaction(connection -> {
                    CategoryEntity categoryEntity = CategoryEntity.fromJson(category);
                    return CategoryJson.fromEntity(
                            new CategoryDaoJdbc(connection).create(categoryEntity));
                },
                CFG.spendJdbcUrl()

        );
    }

    @Override
    public Optional<CategoryJson> findByUsernameAndName(String username, String category) {
        return Optional.empty();
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
                    new SpendDaoJdbc(connection).delete(spendEntity);
                    return null;
                },
                CFG.spendJdbcUrl()
        );
    }

    public void deleteCategory(CategoryJson category) {
        transaction(connection -> {
                    CategoryEntity categoryEntity = CategoryEntity.fromJson(category);
                    new CategoryDaoJdbc(connection).delete(categoryEntity);
                    return null;
                },
                CFG.spendJdbcUrl()
        );
    }


}
