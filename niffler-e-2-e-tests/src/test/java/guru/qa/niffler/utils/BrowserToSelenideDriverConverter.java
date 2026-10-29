package guru.qa.niffler.utils;

import com.codeborne.selenide.SelenideConfig;
import com.codeborne.selenide.SelenideDriver;
import guru.qa.niffler.config.Browser;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;

import java.time.Instant;

public class BrowserToSelenideDriverConverter implements ArgumentConverter {
    @Override
    public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
        if (!(source instanceof Browser)) {
            throw new ArgumentConversionException(
                    "Expected Browser enum but got: " + source
            );
        }

        Browser browser = (Browser) source;

        System.out.printf(
                "[SETUP] %s | browser=%s | thread=%s%n",
                Instant.now(),
                browser,
                Thread.currentThread().getName()
        );

        SelenideConfig config = new SelenideConfig()
                .browser(browser.browserName())
                .headless(false)
                .timeout(5000);

        return new SelenideDriver(config);
    }
}
