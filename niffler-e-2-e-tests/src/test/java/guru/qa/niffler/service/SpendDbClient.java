package guru.qa.niffler.service;

import guru.qa.niffler.data.dao.CategoryDao;
import guru.qa.niffler.data.dao.SpendDao;
import guru.qa.niffler.data.dao.impl.CategoryDaoJdbc;
import guru.qa.niffler.data.dao.impl.SpendDaoJdbc;
import guru.qa.niffler.data.entity.CategoryEntity;
import guru.qa.niffler.data.entity.SpendEntity;
import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.SpendJson;

import java.sql.SQLException;
import java.util.Optional;

public class SpendDbClient {

    private final SpendDao spendDao = new SpendDaoJdbc();
    private final CategoryDao categoryDao = new CategoryDaoJdbc();

    public SpendJson createSpend(SpendJson spend) {
        SpendEntity spendEntity = SpendEntity.fromJson(spend);
        return SpendJson.fromEntity(
                spendDao.create(spendEntity));
    }

    public CategoryJson createCategory(CategoryJson category) {
        CategoryEntity categoryEntity = CategoryEntity.fromJson(category);
        return CategoryJson.fromEntity(categoryDao.create(categoryEntity));
    }

    public void deleteSpend(SpendJson spend) {
        SpendEntity spendEntity = SpendEntity.fromJson(spend);
        try {
            spendDao.deleteSpend(spendEntity);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteCategory(CategoryJson category) {
        CategoryEntity categoryEntity = CategoryEntity.fromJson(category);
        try {
            categoryDao.deleteCategory(categoryEntity);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<CategoryJson> findCategory(SpendJson spend) {
        return categoryDao.findCategoryById(spend.category().id()).map(CategoryJson::fromEntity);
    }


    public CategoryJson getOrCreateCategory(CategoryJson category) {
        final Optional<CategoryJson> existing =
                findCategoryByNameAndUsername(category.username(), category.name());

        if (existing.isPresent()) {
            return existing.get();
        }

        return createCategory(category);
    }

    public Optional<CategoryJson> findCategoryByNameAndUsername(String username, String name) {
        return categoryDao.findAllByUsernameAndCategoryName(username, name).map(CategoryJson::fromEntity);
    }


    public SpendJson updateSpend(SpendJson spend) {
        SpendEntity spendEntity = SpendEntity.fromJson(spend);
        return SpendJson.fromEntity(
                spendDao.update(spendEntity));
    }
}
