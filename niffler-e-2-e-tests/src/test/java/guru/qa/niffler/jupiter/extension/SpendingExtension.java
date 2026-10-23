package guru.qa.niffler.jupiter.extension;

import guru.qa.niffler.jupiter.annotation.Spending;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.SpendJson;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.service.SpendDbClient;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.platform.commons.support.AnnotationSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class SpendingExtension implements BeforeEachCallback, ParameterResolver, AfterEachCallback {

    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(SpendingExtension.class);

    private static final String CREATED_CATEGORIES_KEY = "created_categories_for_cleanup";

    private final SpendDbClient spendClient = new SpendDbClient();

    @Override
    public void beforeEach(ExtensionContext context) {
        AnnotationSupport.findAnnotation(context.getRequiredTestMethod(), User.class)
                .ifPresent(userAnno -> {
                    if (ArrayUtils.isNotEmpty(userAnno.spendings())) {
                        List<SpendJson> createdSpendings = new ArrayList<>();
                        List<CategoryJson> categoriesToClean = new ArrayList<>();

                        for (Spending spendAnno : userAnno.spendings()) {
                            UserJson actualUser = UserExtension.createdUser().orElseThrow();

                            CategoryJson targetCategory = context.getStore(CategoryExtension.NAMESPACE)
                                    .get(context.getUniqueId(), CategoryJson.class);

                            CategoryJson categoryToUse;
                            if (targetCategory != null && targetCategory.name().equals(spendAnno.category())) {
                                categoryToUse = targetCategory;
                            } else {
                                categoryToUse = spendClient.createCategory(new CategoryJson(
                                        null,
                                        spendAnno.category(),
                                        actualUser.username(),
                                        false
                                ));
                                categoriesToClean.add(categoryToUse);
                            }

                            final SpendJson created = spendClient.createSpending(
                                    new SpendJson(
                                            null,
                                            new Date(),
                                            categoryToUse,
                                            spendAnno.currency(),
                                            spendAnno.amount(),
                                            spendAnno.description(),
                                            actualUser.username()
                                    )
                            );
                            createdSpendings.add(created);
                        }
                        context.getStore(NAMESPACE).put(context.getUniqueId(), createdSpendings);
                        context.getStore(NAMESPACE).put(CREATED_CATEGORIES_KEY, categoriesToClean);
                    }
                });
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().isAssignableFrom(SpendJson.class) ||
                parameterContext.getParameter().getType().isAssignableFrom(List.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext context) throws ParameterResolutionException {
        List<SpendJson> spendings = getSpendings(context);
        if (spendings == null) {
            spendings = Collections.emptyList();
        }

        if (parameterContext.getParameter().getType().isAssignableFrom(SpendJson.class)) {
            return spendings.isEmpty() ? null : spendings.get(0);
        }
        return spendings;
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        List<SpendJson> createdSpendings = getSpendings(context);
        if (createdSpendings != null) {
            for (int i = createdSpendings.size() - 1; i >= 0; i--) {
                spendClient.deleteSpending(createdSpendings.get(i));
            }
        }

        @SuppressWarnings("unchecked")
        List<CategoryJson> categoriesToClean = (List<CategoryJson>) context.getStore(NAMESPACE).get(CREATED_CATEGORIES_KEY);
        if (categoriesToClean != null) {
            for (CategoryJson category : categoriesToClean) {
                spendClient.deleteCategory(category);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<SpendJson> getSpendings(ExtensionContext context) {
        Object stored = context.getStore(NAMESPACE).get(context.getUniqueId());
        if (stored instanceof List) {
            return (List<SpendJson>) stored;
        }
        return null;
    }
}