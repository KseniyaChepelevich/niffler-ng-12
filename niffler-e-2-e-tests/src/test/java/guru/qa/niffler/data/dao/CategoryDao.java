package guru.qa.niffler.data.dao;

import guru.qa.niffler.data.entity.spend.CategoryEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryDao {
    CategoryEntity create(CategoryEntity category);

    Optional<CategoryEntity> findCategoryById(UUID id);

    void delete(CategoryEntity category);

    CategoryEntity update(CategoryEntity category);

    List<CategoryEntity> findAllByUsername(String username);

    List<CategoryEntity> findAll();
}
