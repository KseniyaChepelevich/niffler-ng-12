package guru.qa.niffler.test.web;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideDriver;

import guru.qa.niffler.config.Browser;
import guru.qa.niffler.jupiter.annotation.meta.WebTest;
import guru.qa.niffler.jupiter.extension.BrowserExtension;
import guru.qa.niffler.page.LoginPage;
import guru.qa.niffler.utils.BrowserToSelenideDriverConverter;
import guru.qa.niffler.utils.SelenideUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static guru.qa.niffler.utils.RandomDataUtils.randomUsername;

@WebTest
public class LoginTest {

@RegisterExtension
private final BrowserExtension browserExtension = new BrowserExtension();
  private final SelenideDriver chrome = new SelenideDriver(SelenideUtils.chromeConfig);


//  @Test
//  void mainPageShouldBeDisplayedAfterSuccessLogin(@ConvertWith(BrowserToSelenideDriverConverter.class) SelenideDriver driver) {
//    String browser = driver.config().browser();
//    browserExtension.drivers().add(driver);
//  driver.open(LoginPage.URL);
//  new LoginPage(chrome).login("duck", "12345")
//          .checkThatPageLoaded();
//
//  }

  @Test
  void userShouldStayOnLoginPageAfterLoginWithBadCredentials() {
    SelenideDriver firefox = new SelenideDriver(SelenideUtils.firefoxConfig);

    browserExtension.drivers().addAll(List.of(chrome, firefox));

    chrome.open(LoginPage.URL);
    firefox.open(LoginPage.URL);
    new LoginPage(chrome).fillLoginPage(randomUsername(), "badPass")
            .submit(new LoginPage(chrome))
            .checkError("Неверные учетные данные пользователя");


    firefox.$(".logo-section__text").should(Condition.text("Niffler"));
  }

  @ParameterizedTest(name = "browser={0}")
  @EnumSource(Browser.class)
  void userShouldStayOnLoginPageAfterLoginWithBadCredentialsWithBrowserConverterTwoBrowserTest(@ConvertWith(BrowserToSelenideDriverConverter.class) SelenideDriver driver) {
    browserExtension.drivers().add(driver);
      driver.open(LoginPage.URL);
      new LoginPage(driver).fillLoginPage(randomUsername(), "badPass")
              .submit(new LoginPage(driver))
              .checkError("Неверные учетные данные пользователя");

  }

  @ParameterizedTest(name = "browser={0}")
  @EnumSource(Browser.class)
  void checkThatPageLoadedTwoBrowserTest(@ConvertWith(BrowserToSelenideDriverConverter.class) SelenideDriver driver) {
    browserExtension.drivers().add(driver);
    driver.open(LoginPage.URL);
    new LoginPage(driver).checkThatPageLoaded();

  }
}
