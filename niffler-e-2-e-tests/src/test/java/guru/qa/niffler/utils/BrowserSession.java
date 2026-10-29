package guru.qa.niffler.utils;

import com.codeborne.selenide.SelenideDriver;
import guru.qa.niffler.config.Browser;

public class BrowserSession {
    private final Browser browser;
    private final SelenideDriver driver;

    public BrowserSession(Browser browser, SelenideDriver driver) {
        this.browser = browser;
        this.driver = driver;
    }

    public Browser browser() {
        return browser;
    }

    public SelenideDriver driver() {
        return driver;
    }

    public void close() {
        driver.close();
    }

    @Override
    public String toString() {
        return browser.name();
    }
}
