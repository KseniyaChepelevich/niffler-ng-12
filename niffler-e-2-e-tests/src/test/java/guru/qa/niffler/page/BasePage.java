package guru.qa.niffler.page;

import com.codeborne.selenide.*;
import guru.qa.niffler.config.Config;
import io.qameta.allure.Step;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.codeborne.selenide.Condition.text;


@ParametersAreNonnullByDefault
public class BasePage<T extends BasePage<?>> {
    protected static final Config CFG = Config.getInstance();

    @Nullable
    protected final SelenideDriver driver;



    private final SelenideElement alert;
    private final ElementsCollection formErrors;

    protected BasePage(SelenideDriver driver) {
        this.driver = driver;
        this.alert = driver.$(".MuiSnackbar-root");
        this.formErrors = driver.$$("p.Mui-error, .input__helper-text");
    }

    public BasePage() {
        this.driver = null;
        this.alert = Selenide.$(".MuiSnackbar-root");
        this.formErrors = Selenide.$$("p.Mui-error, .input__helper-text");
    }

    @Nonnull
    protected SelenideDriver driver() {
        if (driver == null) {
            throw new IllegalStateException(
                    "Страница создана без драйвера. Используйте конструктор с SelenideDriver."
            );
        }

        return driver;
    }



    @SuppressWarnings("unchecked")
    @Nonnull
    @Step("Check that alert message appears: {expectedText}")
    public T checkAlert(String text) {
        alert.shouldHave(text(text));
        return (T) this;
    }

    @Step("Check that form error message appears: {expectedText}")
    @SuppressWarnings("unchecked")
    @Nonnull
    public T checkFormErrorMessage(String... expectedText) {
        formErrors.should(CollectionCondition.textsInAnyOrder(expectedText));
        return (T) this;
    }
}
