package guru.qa.niffler.test.web;

import com.codeborne.selenide.Selenide;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.annotation.Category;
import guru.qa.niffler.jupiter.annotation.Spending;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.jupiter.annotation.meta.WebTest;
import guru.qa.niffler.jupiter.extension.TestMethodContextExtension;
import guru.qa.niffler.model.SpendJson;
import guru.qa.niffler.page.LoginPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

@WebTest
public class SpendingTest {

    private static final Config CFG = Config.getInstance();

    @User(
            username = "duck",
            spendings = @Spending(
                    description = "Новое обучение",
                    category = "Самая новая категория",
                    amount = 90000)
    )
    @Test
    void spendingDescriptionShouldBeEditedByTableAction(SpendJson spendJson) {
        ExtensionContext ctx = TestMethodContextExtension.context();
        final String newDescription = "Niffler - 12 поток!!!";

        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login("duck", "12345")
                .editSpending(spendJson.description())
                .setNewSpendingDescription(newDescription)
                .save()
                .checkThatTableContains(newDescription);
    }

    @User(
            username = "duck",
            categories = @Category(
                    archived = false
            ),
            spendings = @Spending(
                    description = "Новое обучение",
                    category = "",
                    amount = 90000
            )
    )
    @Test
    void categoryTest(SpendJson spendJson) {
        final String newDescription = "Niffler - 12 поток!!!";

        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login("duck", "12345")
                .editSpending(spendJson.description())
                .setNewSpendingDescription(newDescription)
                .save()
                .checkThatTableContains(newDescription);
    }

}
