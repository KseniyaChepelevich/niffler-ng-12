package guru.qa.niffler.service;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.entity.spend.SpendEntity;
import guru.qa.niffler.data.repository.SpendRepository;
import guru.qa.niffler.data.repository.impl.SpendRepositoryHibernate;
import guru.qa.niffler.data.repository.impl.SpendRepositoryJdbc;
import guru.qa.niffler.data.repository.impl.SpendRepositorySpringJdbc;
import guru.qa.niffler.data.tpl.XaTransactionTemplate;
import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.SpendJson;

import java.util.Optional;
import java.util.UUID;

public class SpendDbClient implements SpendClient {

    private static final Config CFG = Config.getInstance();

    private final SpendRepository spendRepositoryJdbc = new SpendRepositoryJdbc();
    private final SpendRepository spendRepositorySpringJdbc = new SpendRepositorySpringJdbc();
    private final SpendRepository spendRepositoryHibernate = new SpendRepositoryHibernate();

    private final XaTransactionTemplate xaTransactionTemplate = new XaTransactionTemplate(
            CFG.spendJdbcUrl()
    );


    @Override
    public SpendJson createSpending(SpendJson spending) {
        return SpendJson.fromEntity(xaTransactionTemplate.execute(() -> {
            SpendEntity spend = SpendEntity.fromJson(spending);
            return spendRepositoryJdbc.create(spend);
        }));
    }

    public SpendJson createSpendingSpringJdbc(SpendJson spending) {
        return SpendJson.fromEntity(xaTransactionTemplate.execute(() -> {
            SpendEntity spend = SpendEntity.fromJson(spending);
            return spendRepositorySpringJdbc.create(spend);
        }));
    }

    public SpendJson createSpendingHibernate(SpendJson spending) {
        return SpendJson.fromEntity(xaTransactionTemplate.execute(() -> {
            SpendEntity spend = SpendEntity.fromJson(spending);
            return spendRepositoryHibernate.create(spend);
        }));
    }

    @Override
    public CategoryJson createCategory(CategoryJson category) {
        return CategoryJson.fromEntity(xaTransactionTemplate.execute(() -> {
            CategoryEntity categoryE = CategoryEntity.fromJson(category);
            return spendRepositoryJdbc.createCategory(categoryE);
        }));

    }

    public CategoryJson createCategorySpringJdbc(CategoryJson category) {
        return CategoryJson.fromEntity(xaTransactionTemplate.execute(() -> {
            CategoryEntity categoryE = CategoryEntity.fromJson(category);
            return spendRepositorySpringJdbc.createCategory(categoryE);
        }));
    }

    public CategoryJson createCategoryHibernate(CategoryJson category) {
        return CategoryJson.fromEntity(xaTransactionTemplate.execute(() -> {
            CategoryEntity categoryE = CategoryEntity.fromJson(category);
            return spendRepositoryHibernate.createCategory(categoryE);
        }));
    }

    @Override
    public Optional<CategoryJson> findCategoryByUsernameAndCategoryName(String username, String category) {
        return spendRepositoryJdbc.findCategoryByUsernameAndCategoryName(username, category)
                .map(CategoryJson::fromEntity);
    }

    public Optional<CategoryJson> findByUsernameAndNameSpringJdbc(String username, String category) {
        return spendRepositorySpringJdbc.findCategoryByUsernameAndCategoryName(username, category)
                .map(CategoryJson::fromEntity);
    }

    public Optional<CategoryJson> findByUsernameAndNameHibernate(String username, String category) {
        return spendRepositoryHibernate.findCategoryByUsernameAndCategoryName(username, category)
                .map(CategoryJson::fromEntity);
    }

    public SpendJson update(SpendJson spend) {
        SpendEntity se = SpendEntity.fromJson(spend);
        return SpendJson.fromEntity(spendRepositoryJdbc.update(se));
    }

    @Override
    public Optional<CategoryJson> findCategoryById(UUID id) {
        return spendRepositoryJdbc.findCategoryById(id)
                .map(CategoryJson::fromEntity);
    }

    @Override
    public Optional<SpendJson> findById(UUID id) {
        return spendRepositoryJdbc.findById(id)
                .map(SpendJson::fromEntity);
    }

    @Override
    public Optional<SpendJson> findByUsernameAndSpendDescription(String username, String description) {
        return spendRepositorySpringJdbc.findByUsernameAndSpendDescription(username, description)
                .map(SpendJson::fromEntity);
    }



    public SpendJson updateSpringJdbc(SpendJson spend) {
        SpendEntity se = SpendEntity.fromJson(spend);
        return SpendJson.fromEntity(spendRepositorySpringJdbc.update(se));
    }

    public SpendJson updateHibernate(SpendJson spend) {
        SpendEntity se = SpendEntity.fromJson(spend);
        return SpendJson.fromEntity(spendRepositoryHibernate.update(se));
    }
    @Override
    public void deleteSpending(SpendJson spending) {
        SpendEntity se = SpendEntity.fromJson(spending);
        spendRepositoryJdbc.remove(se);
    }

    public void deleteSpendingSpringJdbc(SpendJson spending) {
        SpendEntity se = SpendEntity.fromJson(spending);
        spendRepositorySpringJdbc.remove(se);
    }

    public void deleteSpendingHibernate(SpendJson spending) {
        SpendEntity se = SpendEntity.fromJson(spending);
        spendRepositoryHibernate.remove(se);
    }
    @Override
    public void deleteCategory(CategoryJson category) {
        CategoryEntity ce = CategoryEntity.fromJson(category);
        spendRepositoryJdbc.removeCategory(ce);
    }

    public void deleteCategorySpringJdbc(CategoryJson category) {
        CategoryEntity ce = CategoryEntity.fromJson(category);
        spendRepositorySpringJdbc.removeCategory(ce);
    }

    public void deleteCategoryHibernate(CategoryJson category) {
        CategoryEntity ce = CategoryEntity.fromJson(category);
        spendRepositoryHibernate.removeCategory(ce);
    }



}
