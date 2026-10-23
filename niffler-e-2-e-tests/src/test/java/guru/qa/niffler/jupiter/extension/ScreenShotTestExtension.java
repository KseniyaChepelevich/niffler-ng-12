package guru.qa.niffler.jupiter.extension;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.qa.niffler.jupiter.annotation.ScreenShotTest;
import guru.qa.niffler.model.allure.ScreenDif;
import io.qameta.allure.Allure;
import lombok.SneakyThrows;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.platform.commons.support.AnnotationSupport;
import org.springframework.core.io.ClassPathResource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class ScreenShotTestExtension implements ParameterResolver, TestExecutionExceptionHandler {

    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(ScreenShotTestExtension.class);
    public static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String REWRITE_PROPERTY = "rewriteExpected";

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return AnnotationSupport.isAnnotated(extensionContext.getRequiredTestMethod(), ScreenShotTest.class) &&
                parameterContext.getParameter().getType().isAssignableFrom(BufferedImage.class);
    }

    @SneakyThrows
    @Override
    public BufferedImage resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {

        ScreenShotTest annotation = extensionContext.getRequiredTestMethod().getAnnotation(ScreenShotTest.class);
        BufferedImage expectedImage = ImageIO.read(
                new ClassPathResource(annotation.value()).getInputStream()
        );

        setExpected(expectedImage);
        return expectedImage;
    }

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        BufferedImage expected = getExpected();
        BufferedImage actual = getActual();
        BufferedImage diff = getDiff();

        if (throwable.getMessage().contains("Screen comparison failure")) {
            ScreenDif screenDif = new ScreenDif(
                    "data:image/png;base64," + Base64.getEncoder().encodeToString(imageToBytes(expected)),
                    "data:image/png;base64," + Base64.getEncoder().encodeToString(imageToBytes(actual)),
                    "data:image/png;base64," + Base64.getEncoder().encodeToString(imageToBytes(diff))
            );

            Allure.addAttachment(
                    "Screenshot diff",
                    "application/vnd.allure.image.diff",
                    objectMapper.writeValueAsString(screenDif)
            );
        }

        if (shouldRewriteExpected(context) && actual != null) {
            try {
                ScreenShotTest annotation = context.getRequiredTestMethod().getAnnotation(ScreenShotTest.class);
                String fileName = annotation.value();

                java.io.File targetFile = new java.io.File("src/test/resources/" + fileName);

                if (targetFile.getParentFile() != null && !targetFile.getParentFile().exists()) {
                    boolean dirsCreated = targetFile.getParentFile().mkdirs();
                    System.out.println("Созданы директории: " + dirsCreated);
                }

                javax.imageio.ImageIO.write(actual, "png", targetFile);

                System.out.println("[ScreenShotTestExtension] Ожидаемый скриншот УСПЕШНО перезаписан: " + targetFile.getAbsolutePath());
                return;

            } catch (Exception e) {
                System.err.println(" [ScreenShotTestExtension] КРИТИЧЕСКАЯ ОШИБКА при перезаписи скриншота:");
                e.printStackTrace();
                throw throwable;
            }
        }

        // 2. СТАНДАРТНАЯ ЛОГИКА (если перезапись не включена)
        if (expected == null || actual == null || diff == null) {
            System.err.println("[ScreenShotTestExtension] Не удалось создать Allure-дифф: одна из картинок равна null.");
            throw throwable;
        }


        throw throwable;
    }


    public static void setExpected(BufferedImage expected) {
        TestMethodContextExtension.context().getStore(NAMESPACE).put("expected", expected);
    }

    public static BufferedImage getExpected() {
        return TestMethodContextExtension.context().getStore(NAMESPACE).get("expected", BufferedImage.class);
    }

    public static void setActual(BufferedImage actual) {
        TestMethodContextExtension.context().getStore(NAMESPACE).put("actual", actual);
    }

    public static BufferedImage getActual() {
        return TestMethodContextExtension.context().getStore(NAMESPACE).get("actual", BufferedImage.class);
    }

    public static void setDiff(BufferedImage diff) {
        TestMethodContextExtension.context().getStore(NAMESPACE).put("diff", diff);
    }

    public static BufferedImage getDiff() {
        return TestMethodContextExtension.context().getStore(NAMESPACE).get("diff", BufferedImage.class);
    }

    private static byte[] imageToBytes(BufferedImage image) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean shouldRewriteExpected(ExtensionContext context) {
        String sysProp = System.getProperty(REWRITE_PROPERTY);
        if (sysProp != null) {
            return Boolean.parseBoolean(sysProp);
        }

        String envVar = System.getenv("REWRITE_EXPECTED");
        if (envVar != null) {
            return Boolean.parseBoolean(envVar);
        }

        return AnnotationSupport.findAnnotation(context.getRequiredTestMethod(), ScreenShotTest.class)
                .map(ScreenShotTest::rewriteExpected)
                .orElse(false);
    }
}
