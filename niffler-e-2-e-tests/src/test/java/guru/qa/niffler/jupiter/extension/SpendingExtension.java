package guru.qa.niffler.jupiter.extension;

import guru.qa.niffler.jupiter.annotation.Category;
import guru.qa.niffler.jupiter.annotation.Spending;
import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.SpendJson;
import guru.qa.niffler.service.SpendDbClient;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.platform.commons.support.AnnotationSupport;

import java.sql.SQLException;
import java.util.UUID;
import java.util.Optional;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

public class SpendingExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(SpendingExtension.class);

    private static final String CREATED_SPENDS_KEY = "-created-spends";
    private static final String CREATED_CATEGORIES_KEY = "-created-categories";
    private static final String CATEGORY_PARAM_KEY = "-category";

    private final SpendDbClient spendClient = new SpendDbClient();

    @Override
    public void beforeEach(ExtensionContext context) {
        final Category categoryAnno = AnnotationSupport.findAnnotation(
                context.getRequiredTestMethod(),
                Category.class
        ).orElse(null);

        final Spending spendingAnno = AnnotationSupport.findAnnotation(
                context.getRequiredTestMethod(),
                Spending.class
        ).orElse(null);

        if (categoryAnno == null && spendingAnno == null) {
            return;
        }

        final ExtensionContext.Store store = context.getStore(NAMESPACE);

        final List<SpendJson> createdSpends = new ArrayList<>();
        final List<CategoryJson> createdCategories = new ArrayList<>();

        store.put(context.getUniqueId() + CREATED_SPENDS_KEY, createdSpends);
        store.put(context.getUniqueId() + CREATED_CATEGORIES_KEY, createdCategories);

        final String username = resolveUsername(categoryAnno, spendingAnno);
        final String categoryName = resolveCategoryName(categoryAnno, spendingAnno);
        final boolean archived = categoryAnno != null && categoryAnno.archived();

        validateAnnotations(categoryAnno, spendingAnno);

        final CategoryJson category = findOrCreateCategory(
                username,
                categoryName,
                archived,
                createdCategories
        );

        store.put(context.getUniqueId() + CATEGORY_PARAM_KEY, category);

        if (spendingAnno != null) {
            final SpendJson spend = spendClient.createSpend(
                    new SpendJson(
                            null,
                            new Date(),
                            category,
                            spendingAnno.currency(),
                            spendingAnno.amount(),
                            spendingAnno.description(),
                            username
                    )
            );

            createdSpends.add(spend);

            store.put(context.getUniqueId(), spend);
        }
    }


    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        final Class<?> type = parameterContext.getParameter().getType();

        return SpendJson.class.isAssignableFrom(type) ||
                CategoryJson.class.isAssignableFrom(type);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        final Class<?> type = parameterContext.getParameter().getType();
        final ExtensionContext.Store store = extensionContext.getStore(NAMESPACE);

        if (SpendJson.class.isAssignableFrom(type)) {
            final SpendJson spend = store.get(extensionContext.getUniqueId(), SpendJson.class);

            if (spend == null) {
                throw new ParameterResolutionException(
                        "No SpendJson was created for parameter: "
                                + parameterContext.getParameter().getName()
                );
            }

            return spend;
        }

        if (CategoryJson.class.isAssignableFrom(type)) {
            final CategoryJson category = store.get(
                    extensionContext.getUniqueId() + CATEGORY_PARAM_KEY,
                    CategoryJson.class
            );

            if (category == null) {
                throw new ParameterResolutionException(
                        "No CategoryJson was created for parameter: "
                                + parameterContext.getParameter().getName()
                );
            }

            return category;
        }

        throw new ParameterResolutionException(
                "Unsupported parameter type: " + type
        );
    }


    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        final ExtensionContext.Store store = context.getStore(NAMESPACE);

        final List<SpendJson> createdSpends =
                (List<SpendJson>) store.remove(context.getUniqueId() + CREATED_SPENDS_KEY);

        final List<CategoryJson> createdCategories =
                (List<CategoryJson>) store.remove(context.getUniqueId() + CREATED_CATEGORIES_KEY);

        // Сначала удаляем spends, потому что они ссылаются на category_id.
        if (createdSpends != null) {
            for (int i = createdSpends.size() - 1; i >= 0; i--) {
                spendClient.deleteSpend(createdSpends.get(i));
            }
        }

        if (createdCategories != null) {
            for (int i = createdCategories.size() - 1; i >= 0; i--) {
                spendClient.deleteCategory(createdCategories.get(i));
            }
        }

        store.remove(context.getUniqueId() + CATEGORY_PARAM_KEY);
        store.remove(context.getUniqueId());
    }

    private String categoryName(String name) {
        if (name == null || name.isBlank()) {
            return "category-" + UUID.randomUUID();
        }
        return name + "-" + UUID.randomUUID();
    }

    private String resolveCategoryName(String name) {
        if (name == null || name.isBlank()) {
            return "category-" + UUID.randomUUID();
        }

        return name;
    }

    private void validateSpendCategoryMatchesAnnotation(Spending spendingAnno,
                                                        CategoryJson category) {
        final String spendCategoryName = spendingAnno.category();

        if (spendCategoryName == null || spendCategoryName.isBlank()) {
            return;
        }

        if (!spendCategoryName.equals(category.name())) {
            throw new IllegalStateException(
                    String.format(
                            "@Spending.category='%s' does not match @Category.name='%s'",
                            spendCategoryName,
                            category.name()
                    )
            );
        }
    }

    private CategoryJson findOrCreateCategory(final String username,
                                              final String categoryName,
                                              final boolean archived,
                                              final List<CategoryJson> createdCategories) {

        final Optional<CategoryJson> existing =
                spendClient.findCategoryByNameAndUsername(username, categoryName);

        if (existing.isPresent()) {
            return existing.get();
        }

        try {
            final CategoryJson created = spendClient.createCategory(
                    new CategoryJson(
                            null,
                            categoryName,
                            username,
                            archived
                    )
            );

            createdCategories.add(created);

            return created;
        } catch (RuntimeException e) {
            if (isUniqueViolation(e)) {
                return spendClient.findCategoryByNameAndUsername(username, categoryName)
                        .orElseThrow(() -> new IllegalStateException(
                                "Category was not found after unique violation: "
                                        + username + " / " + categoryName,
                                e
                        ));
            }

            throw e;
        }
    }

    private String resolveUsername(final Category categoryAnno,
                                   final Spending spendingAnno) {

        if (categoryAnno != null && categoryAnno.username() != null
                && !categoryAnno.username().isBlank()) {
            return categoryAnno.username();
        }

        if (spendingAnno != null && spendingAnno.username() != null
                && !spendingAnno.username().isBlank()) {
            return spendingAnno.username();
        }

        throw new IllegalStateException(
                "Username must be specified in @Category or @Spending"
        );
    }

    private String resolveCategoryName(final Category categoryAnno,
                                       final Spending spendingAnno) {

        if (categoryAnno != null && categoryAnno.name() != null
                && !categoryAnno.name().isBlank()) {
            return categoryAnno.name();
        }

        if (spendingAnno != null && spendingAnno.category() != null
                && !spendingAnno.category().isBlank()) {
            return spendingAnno.category();
        }

        return "category-" + UUID.randomUUID();
    }

    private void validateAnnotations(final Category categoryAnno,
                                     final Spending spendingAnno) {

        if (categoryAnno == null || spendingAnno == null) {
            return;
        }

        final String categoryUsername = categoryAnno.username();
        final String spendingUsername = spendingAnno.username();

        if (categoryUsername != null && !categoryUsername.isBlank()
                && spendingUsername != null && !spendingUsername.isBlank()
                && !categoryUsername.equals(spendingUsername)) {

            throw new IllegalStateException(
                    String.format(
                            "@Category.username='%s' does not match @Spending.username='%s'",
                            categoryUsername,
                            spendingUsername
                    )
            );
        }

        final String categoryName = categoryAnno.name();
        final String spendingCategory = spendingAnno.category();

        if (categoryName != null && !categoryName.isBlank()
                && spendingCategory != null && !spendingCategory.isBlank()
                && !categoryName.equals(spendingCategory)) {

            throw new IllegalStateException(
                    String.format(
                            "@Category.name='%s' does not match @Spending.category='%s'",
                            categoryName,
                            spendingCategory
                    )
            );
        }
    }

    private boolean isUniqueViolation(Throwable throwable) {
        Throwable current = throwable;

        while (current != null) {
            if (current instanceof SQLException sqlException) {
                // 23505 = unique_violation в PostgreSQL
                if ("23505".equals(sqlException.getSQLState())) {
                    return true;
                }
            }

            current = current.getCause();
        }

        return false;
    }

}
