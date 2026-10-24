package guru.qa.niffler.test.web;

import com.codeborne.selenide.Selenide;
import guru.qa.niffler.jupiter.annotation.Category;
import guru.qa.niffler.jupiter.annotation.Spending;
import guru.qa.niffler.jupiter.extension.SpendingExtension;
import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.model.SpendJson;
import guru.qa.niffler.page.LoginPage;
import guru.qa.niffler.service.SpendDbClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Date;
import java.util.UUID;

@ExtendWith(SpendingExtension.class)
public class JdbcTest {

    @Test
    void daotest() {
        SpendDbClient spendDbClient = new SpendDbClient();
        String username = "Toto";
        String categoryName = "test-cat-" + UUID.randomUUID();

        CategoryJson category = null;
        SpendJson spend = null;

        try {
            category = spendDbClient.createCategory(
                    new CategoryJson(
                            null,
                            categoryName,
                            username,
                            false
                    )
            );

            spend = spendDbClient.createSpend(
                    new SpendJson(
                            null,
                            new Date(),
                            category,
                            CurrencyValues.RUB,
                            100.0,
                            "test desc,",
                            username
                    )
            );
            System.out.println(spend);
        } finally {
            if (spend != null) {
                spendDbClient.deleteSpend(spend);
            }

            if (category != null) {
                spendDbClient.deleteCategory(category);
            }
        }
    }


    @Test
    @Category(
            name = "Обучение",
            username = "duck"
    )
    @Spending(
            username = "duck",
            category = "Обучение",
            description = "Niffler 12 поток!",
            amount = 119000
    )
    void spendingUpdateCheck(SpendJson spend, CategoryJson category) {
        SpendDbClient spendDbClient = new SpendDbClient();
        final String newDescription = "Niffler - финальный поток";

        SpendJson spendJson = spendDbClient.updateSpend(
                new SpendJson(
                spend.id(),
                spend.spendDate(),
                category,
                spend.currency(),
                        spend.amount(),
                newDescription,
                spend.username()
        ));

System.out.println(spendJson);
    }
}
