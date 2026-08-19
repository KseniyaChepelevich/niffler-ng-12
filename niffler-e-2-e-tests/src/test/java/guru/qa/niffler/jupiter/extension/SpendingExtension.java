package guru.qa.niffler.jupiter.extension;

import guru.qa.niffler.jupiter.annotation.Category;
import guru.qa.niffler.jupiter.annotation.Spending;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.SpendJson;
import guru.qa.niffler.service.SpendDbClient;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

import org.junit.platform.commons.support.AnnotationSupport;

import java.util.Date;

public class SpendingExtension implements BeforeEachCallback, ParameterResolver, AfterEachCallback {

    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(SpendingExtension.class);

    private final SpendDbClient spendClient = new SpendDbClient();

    @Override
    public void beforeEach(ExtensionContext context) {

        AnnotationSupport.findAnnotation(
                context.getRequiredTestMethod(),
                User.class
        ).ifPresent(
                userAnno -> {
                    if (ArrayUtils.isNotEmpty(userAnno.spendings())) {
                        Spending spendAnno = userAnno.spendings()[0];
                        CategoryJson targetCategory = context.getStore(CategoryExtension.NAMESPACE)
                                .get(context.getUniqueId(), CategoryJson.class);
                        CategoryJson categoryToUse;

                        if (targetCategory != null) {
                            categoryToUse = targetCategory;
                        } else {
                            categoryToUse = new CategoryJson(
                                    null,
                                    spendAnno.category(),
                                    userAnno.username(),
                                    false
                            );
                        }
                        final SpendJson created = spendClient.createSpending(
                                new SpendJson(
                                        null,
                                        new Date(),
                                        categoryToUse,
                                        spendAnno.currency(),
                                        spendAnno.amount(),
                                        spendAnno.description(),
                                        userAnno.username()
                                )
                        );
                        context.getStore(NAMESPACE).put(
                                context.getUniqueId(),
                                created
                        );
                    }
                }
        );
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().isAssignableFrom(SpendJson.class);
    }

    @Override
    public SpendJson resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return extensionContext.getStore(NAMESPACE).get(extensionContext.getUniqueId(), SpendJson.class);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        SpendJson createdSpending = context.getStore(NAMESPACE).get(
                context.getUniqueId(),
                SpendJson.class
        );
        if (createdSpending != null) {
            spendClient.deleteSpending(createdSpending);
        }
    }
}
