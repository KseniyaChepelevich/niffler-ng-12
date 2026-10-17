package guru.qa.niffler.data.repository.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.spend.CategoryEntity;
import guru.qa.niffler.data.entity.spend.SpendEntity;
import guru.qa.niffler.data.repository.SpendRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.data.jpa.EntityManagers.em;

public class SpendRepositoryHibernate implements SpendRepository {

    private static final Config CFG = Config.getInstance();

    private final EntityManager entityManager = em(CFG.spendJdbcUrl());

    @Override
    public SpendEntity create(SpendEntity spend) {
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }

        CategoryEntity managedCategory = entityManager.merge(spend.getCategory());
        spend.setCategory(managedCategory);

        entityManager.persist(spend);
        entityManager.flush();
        return spend;
    }

    @Override
    public SpendEntity update(SpendEntity spend) {
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }
        SpendEntity merged = entityManager.merge(spend);
        entityManager.flush();
        return merged;
    }

    @Override
    public CategoryEntity createCategory(CategoryEntity category) {
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }
        entityManager.persist(category);
        entityManager.flush();
        return category;
    }

    @Override
    public Optional<CategoryEntity> findCategoryById(UUID id) {
        return Optional.ofNullable(entityManager.find(CategoryEntity.class, id));
    }

    @Override
    public Optional<CategoryEntity> findCategoryByUsernameAndCategoryName(String username, String name) {
        try {
            return Optional.of(
                    entityManager.createQuery("select c from CategoryEntity c where c.username = :username and c.name = :name", CategoryEntity.class)
                            .setParameter("username", username)
                            .setParameter("name", name)
                            .getSingleResult()
            );
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<SpendEntity> findById(UUID id) {
        return Optional.ofNullable(entityManager.find(SpendEntity.class, id));
    }

    @Override
    public Optional<SpendEntity> findByUsernameAndSpendDescription(String username, String description) {
        try {
            return Optional.of(
                    entityManager.createQuery("select s from SpendEntity s where s.username = :username and s.description = :description", SpendEntity.class)
                            .setParameter("username", username)
                            .setParameter("description", description)
                            .getSingleResult()
            );
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public void remove(SpendEntity spend) {
        entityManager.joinTransaction();
        entityManager.remove(entityManager.contains(spend) ? spend : entityManager.merge(spend));
    }

    @Override
    public void removeCategory(CategoryEntity category) {
        entityManager.joinTransaction();
        entityManager.remove(entityManager.contains(category) ? category : entityManager.merge(category));
    }
}
