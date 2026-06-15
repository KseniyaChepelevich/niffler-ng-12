package guru.qa.niffler.jupiter.extension;

import guru.qa.niffler.jupiter.annotation.Category;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.service.SpendDbClient;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.platform.commons.support.AnnotationSupport;

import static guru.qa.niffler.utils.RandomDataUtils.randomCategoryName;

public class CategoryExtension implements BeforeEachCallback, ParameterResolver, AfterEachCallback {
    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(CategoryExtension.class);
    private final SpendDbClient spendClient = new SpendDbClient();

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        AnnotationSupport.findAnnotation(context.getRequiredTestMethod(),
                User.class
        ).ifPresent(
                userAnno -> {
                    if (ArrayUtils.isNotEmpty(userAnno.categories())) {
                        Category categoryAnno = userAnno.categories()[0];

                        final CategoryJson created = spendClient.createCategory(
                                new CategoryJson(
                                        null,
                                        randomCategoryName(),
                                        userAnno.username(),
                                        categoryAnno.archived()
                                )
                        );
                        context.getStore(NAMESPACE).put(
                                context.getUniqueId(),
                                created
                        );
                        context.getStore(NAMESPACE).put(context.getUniqueId() + "_name", created.name());

                    }

                }
        );
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().isAssignableFrom(CategoryJson.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return extensionContext.getStore(NAMESPACE).get(extensionContext.getUniqueId(), CategoryJson.class);
    }


    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        System.out.println("Запустился afterEach");
        CategoryJson createdCategory = context.getStore(NAMESPACE).get(
                context.getUniqueId(),
                CategoryJson.class
        );
        if (createdCategory != null) {
            spendClient.deleteCategory(createdCategory);

        }

    }
}
