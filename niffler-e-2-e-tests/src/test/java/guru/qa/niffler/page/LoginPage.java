package guru.qa.niffler.page;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideDriver;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static java.sql.DriverManager.getDriver;

@ParametersAreNonnullByDefault
public class LoginPage extends BasePage<LoginPage> {

    public static final String URL = CFG.authUrl() + "/login";

    private final SelenideElement usernameInput;
    private final SelenideElement passwordInput;
    private final SelenideElement submitBtn;
    private final SelenideElement registerButton;
    private final SelenideElement errorContainer;

    public LoginPage(SelenideDriver driver) {
        super(driver);
        this.usernameInput = driver.$("#username");
        this.passwordInput = driver.$("#password");
        this.submitBtn = driver.$("#login-button");
        this.registerButton = driver.$("a[href='/register']");
        this.errorContainer = driver.$(".form__error");
    }

    public LoginPage(){
        this.usernameInput = Selenide.$("#username");
        this.passwordInput = Selenide.$("#password");
        this.submitBtn = Selenide.$("#login-button");
        this.registerButton = Selenide.$("a[href='/register']");
        this.errorContainer = Selenide.$(".form__error");
    }

    public MainPage login(String username, String password) {
        usernameInput.setValue(username);
        passwordInput.setValue(password);
        submitBtn.click();
        return new MainPage(driver());
    }

    @Nonnull
    public RegisterPage doRegister() {
        registerButton.click();
        return new RegisterPage();
    }

    @Step("Fill login page with credentials: username: '{0}', password: {1}")
    @Nonnull
    public LoginPage fillLoginPage(String login, String password) {
        setUsername(login);
        setPassword(password);
        return this;
    }

    @Step("Set username: '{0}'")
    @Nonnull
    public LoginPage setUsername(String username) {
        usernameInput.setValue(username);
        return this;
    }

    @Step("Set password: '{0}'")
    @Nonnull
    public LoginPage setPassword(String password) {
        passwordInput.setValue(password);
        return this;
    }

    @Step("Submit login")
    @Nonnull
    public <T extends BasePage<?>> T submit(T expectedPage) {
        submitBtn.click();
        return expectedPage;
    }

    @Step("Check error on page: {error}")
    @Nonnull
    public LoginPage checkError(String error) {
        errorContainer.shouldHave(text(error));
        return this;
    }

    @Step("Check that the page is loaded")
    @Nonnull
    public LoginPage checkThatPageLoaded() {
        usernameInput.should(visible);
        passwordInput.should(visible);
        return this;
    }
}
