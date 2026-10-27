package guru.qa.niffler.test.web;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideConfig;
import com.codeborne.selenide.SelenideDriver;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.extension.BrowserExtension;
import guru.qa.niffler.page.LoginPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(BrowserExtension.class)
public class LoginTest {

private static final SelenideConfig defaultConfig = new SelenideConfig()
        .browser("chrome")
        .pageLoadStrategy("eager")
        .timeout(5000L);

  @Test
  void mainPageShouldBeDisplayedAfterSuccessLogin() {
    SelenideDriver driver = new SelenideDriver(defaultConfig);



    driver.open(LoginPage.URL, LoginPage.class)
        .login("duck", "12345")
        .checkThatPageLoaded();
  }
}
