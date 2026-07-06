package guru.qa.niffler.jupiter.extension;

import guru.qa.niffler.jupiter.annotation.Category;

import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.service.SpendDbClient;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.platform.commons.support.AnnotationSupport;

import java.sql.SQLException;

import static guru.qa.niffler.utils.RandomDataUtils.randomCategoryName;

public class CategoryExtension implements BeforeEachCallback, ParameterResolver {

    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(CategoryExtension.class);

    private final SpendDbClient spendClient = new SpendDbClient();

    @Override
    public void beforeEach(ExtensionContext context) {
        AnnotationSupport.findAnnotation(
                context.getRequiredTestMethod(),
                User.class
        ).ifPresent(
                anno -> {
                    if (anno.categories().length > 0) {
                        Category category = anno.categories()[0];
                        String categoryName = category.name().isBlank() ? randomCategoryName() : category.name();
                        CategoryJson categoryJson =
                                new CategoryJson(
                                        null,
                                        categoryName,
                                        anno.username(),
                                        category.archived()
                                );

                        CategoryJson createdCategory = null;
                        try {
                            createdCategory = spendClient.createCategory(categoryJson);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                        if (createdCategory.archived()) {
                                CategoryJson archivedCategory = new CategoryJson(
                                        createdCategory.id(),
                                        createdCategory.name(),
                                        createdCategory.username(),
                                        true
                                );
                            try {
                                createdCategory = spendClient.createCategory(archivedCategory);
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }

                            context.getStore(NAMESPACE).put(
                                    context.getUniqueId(),
                                    createdCategory
                            );
                        }
                    }
        );
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().isAssignableFrom(CategoryJson.class);
    }

    @Override
    public CategoryJson resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return extensionContext.getStore(NAMESPACE).get(extensionContext.getUniqueId(), CategoryJson.class);
    }
}
