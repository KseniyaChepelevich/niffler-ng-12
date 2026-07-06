package guru.qa.niffler.jupiter.extension;

import guru.qa.niffler.jupiter.annotation.Spending;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.SpendJson;
import guru.qa.niffler.service.SpendDbClient;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.platform.commons.support.AnnotationSupport;

import java.sql.SQLException;
import java.util.Date;

import static guru.qa.niffler.jupiter.extension.TestMethodContextExtension.context;
import static guru.qa.niffler.utils.RandomDataUtils.randomSentence;

public class SpendingExtension implements BeforeEachCallback, ParameterResolver {

    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(SpendingExtension.class);

    private final SpendDbClient spendClient = new SpendDbClient();

    @Override
    public void beforeEach(ExtensionContext context) {
        AnnotationSupport.findAnnotation(
                context.getRequiredTestMethod(),
                User.class
        ).ifPresent(
                anno -> {
                    if (anno.spendings().length > 0) {
                        Spending spending = anno.spendings()[0];
                        String description = spending.description().isBlank() ? randomSentence(15) : spending.description();
                        CategoryJson categoryJson = context().getStore(CategoryExtension.NAMESPACE)
                                .get(context().getUniqueId(), CategoryJson.class);

                        SpendJson spendJson = new SpendJson(
                                null,
                                new Date(),
                                categoryJson,
                                spending.currency(),
                                spending.amount(),
                                description,
                                anno.username()
                        );

                        try {
                            context.getStore(NAMESPACE).put(
                                    context.getUniqueId(),
                                    spendClient.createSpend(spendJson));
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
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
}
