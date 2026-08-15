package guru.qa.niffler.service;

import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.SpendJson;

import java.util.Optional;
import java.util.UUID;

public interface SpendClient {
  SpendJson createSpending(SpendJson spending);

  CategoryJson createCategory(CategoryJson category);

  Optional<CategoryJson> findCategoryByUsernameAndCategoryName(String username, String category);

  SpendJson update(SpendJson spend);

  Optional<CategoryJson> findCategoryById(UUID id);

  Optional<SpendJson> findById(UUID id);
  Optional<SpendJson> findByUsernameAndSpendDescription(String username, String description);

  void deleteSpending(SpendJson spend);

  void deleteCategory(CategoryJson category);
}
