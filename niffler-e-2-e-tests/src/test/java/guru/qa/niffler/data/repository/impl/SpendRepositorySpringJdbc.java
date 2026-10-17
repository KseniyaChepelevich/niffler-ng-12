package guru.qa.niffler.data.repository.impl;

import guru.qa.niffler.data.dao.CategoryDao;
import guru.qa.niffler.data.dao.SpendDao;
import guru.qa.niffler.data.dao.impl.CategoryDaoSpringJdbc;
import guru.qa.niffler.data.dao.impl.SpendDaoSpringJdbc;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.entity.spend.SpendEntity;
import guru.qa.niffler.data.repository.SpendRepository;

import java.util.Optional;
import java.util.UUID;


public class SpendRepositorySpringJdbc implements SpendRepository {


    private final CategoryDao categoryDao = new CategoryDaoSpringJdbc();
    private final SpendDao spendDao = new SpendDaoSpringJdbc();

    @Override
    public SpendEntity create(SpendEntity spend) {
        CategoryEntity category = spend.getCategory();

        if (category.getId() == null) {
            Optional<CategoryEntity> existingCategory = categoryDao.findAllByUsernameAndCategoryName(
                    category.getUsername(),
                    category.getName()
            );

            if (existingCategory.isPresent()) {
                spend.setCategory(existingCategory.get());
            } else {
                spend.setCategory(categoryDao.create(category));
            }
        }
        return spendDao.create(spend);
    }

    @Override
    public SpendEntity update(SpendEntity spend) {
        return spendDao.update(spend);
    }

    @Override
    public CategoryEntity createCategory(CategoryEntity category) {
        return categoryDao.create(category);
    }

    @Override
    public Optional<CategoryEntity> findCategoryById(UUID id) {
        return categoryDao.findCategoryById(id);
    }

    @Override
    public Optional<CategoryEntity> findCategoryByUsernameAndCategoryName(String username, String name) {
        return categoryDao.findAllByUsernameAndCategoryName(username, name);
    }

    @Override
    public Optional<SpendEntity> findById(UUID id) {
        return spendDao.findSpendById(id);
    }

    @Override
    public Optional<SpendEntity> findByUsernameAndSpendDescription(String username, String description) {
        return spendDao.findSpendByUsernameAndDescription(username, description);
    }

    @Override
    public void remove(SpendEntity spend) {
        spendDao.delete(spend);

    }

    @Override
    public void removeCategory(CategoryEntity category) {
        categoryDao.delete(category);

    }
}
