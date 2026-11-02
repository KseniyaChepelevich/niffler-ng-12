package guru.qa.niffler.jupiter.extension;

import com.codeborne.selenide.SelenideDriver;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.Allure;
import io.qameta.allure.selenide.AllureSelenide;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.*;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class BrowserExtension implements
        BeforeEachCallback,
        AfterEachCallback,
        TestExecutionExceptionHandler,
        LifecycleMethodExecutionExceptionHandler {

    private static final ConcurrentHashMap<String, List<SelenideDriver>> driversByTest = new ConcurrentHashMap<>();
    private ThreadLocal<String> currentTestKey = new ThreadLocal<>();
    private final ThreadLocal<Boolean> screenshotTaken = ThreadLocal.withInitial(() -> false);


    public List<SelenideDriver> drivers() {
        String keyTest = currentTestKey.get();
        if (keyTest == null) {
            throw new IllegalStateException("drivers() можно использовать только внутри теста");
        }
        return driversByTest.computeIfAbsent(keyTest, k -> new ArrayList<>());
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        String testKey = context.getUniqueId();
        List<SelenideDriver> testDrivers;
        try {
            testDrivers = driversByTest.remove(testKey);
            if (testDrivers != null) {
                for (SelenideDriver testDriver : testDrivers) {
                    try {
                        if (testDriver.hasWebDriverStarted()) {
                            testDriver.close();
                        }
                    } catch (Exception e) {
                        log.debug("Ошибка при закрытии драйвера: {}", e.getMessage());
                    }
                }
            }
        } finally {
            currentTestKey.remove();
            screenshotTaken.remove();
        }
    }


    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        String testKey = context.getUniqueId();
        currentTestKey.set(testKey);

        SelenideLogger.addListener("Allure-selenide", new AllureSelenide()
                .savePageSource(false)
                .screenshots(false)
        );
    }

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        doScreenshot();
        throw throwable;
    }

    @Override
    public void handleBeforeEachMethodExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        doScreenshot();
        throw throwable;
    }

    @Override
    public void handleAfterEachMethodExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        doScreenshot();
        throw throwable;
    }

    private void doScreenshot() {
        if (screenshotTaken.get()) {
            return;
        }
        String keyTest = currentTestKey.get();
        if (keyTest != null) {
            List<SelenideDriver> listDrivers = driversByTest.get(keyTest);
            if (listDrivers != null) {

                for (SelenideDriver listDriver : listDrivers) {
                    if (listDriver.hasWebDriverStarted()) {
                        try {
                            Allure.addAttachment(
                                    "Screen on fail for browser " + listDriver.getSessionId(),
                                    new ByteArrayInputStream(
                                            ((TakesScreenshot) listDriver.getWebDriver()).getScreenshotAs(OutputType.BYTES)
                                    )
                            );
                        } catch (Exception e) {
                            log.debug("Ошибка при попытке сделать скриншот: {}", e.getMessage());
                        }

                    }
                }
            }

        }

    }
}
