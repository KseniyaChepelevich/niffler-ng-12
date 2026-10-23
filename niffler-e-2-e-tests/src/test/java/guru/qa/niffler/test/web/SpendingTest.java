package guru.qa.niffler.test.web;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.github.jknack.handlebars.helper.StringHelpers;
import guru.qa.niffler.condition.Bubble;
import guru.qa.niffler.condition.Color;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.annotation.Category;
import guru.qa.niffler.jupiter.annotation.ScreenShotTest;
import guru.qa.niffler.jupiter.annotation.Spending;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.jupiter.annotation.meta.WebTest;
import guru.qa.niffler.jupiter.extension.UserExtension;
import guru.qa.niffler.jupiter.extension.UsersQueueExtension;
import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.model.SpendJson;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.page.LoginPage;
import guru.qa.niffler.page.MainPage;
import guru.qa.niffler.page.component.SpendingTable;
import guru.qa.niffler.page.component.StatComponent;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import guru.qa.niffler.jupiter.extension.UsersQueueExtension.UserType;


import java.awt.image.BufferedImage;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


@WebTest
@ExtendWith({UserExtension.class, UsersQueueExtension.class})
public class SpendingTest {


    private static final Config CFG = Config.getInstance();

    @BeforeAll
    static void setupSelenide() {
        Configuration.browserSize = "1920x1080";
        Configuration.pageLoadStrategy = "eager";
        Configuration.timeout = 10000;
    }

    @User(
            username = "duck",
            spendings = @Spending(
                    description = "Новое обучение",
                    category = "Самая новая категория",
                    amount = 90000
            )
    )
    @Test
    void spendingDescriptionShouldBeEditedByTableAction(SpendJson spendJson) {
        final String newDescription = "Niffler - 12 поток!!!";

        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login("duck", "12345")
                .getSpendingTable().editSpending(spendJson.description())
                .setNewSpendingDescription(newDescription)
                .saveSpending()
                .getSpendingTable().searchSpendingByDescription(newDescription);

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
        final String newCategory = "Niffler - 12 поток!!!";

        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login("duck", "12345")
                .getSpendingTable().editSpending(spendJson.description())
                .setNewSpendingCategory(newCategory)
                .saveSpending()
                .getSpendingTable().searchSpendingByDescription(spendJson.description());
    }


    @User(
            username = "ivan7",
//            categories = @Category(
//                    archived = false
//            ),
            spendings = {
                    @Spending(
                            description = "Новое обучение",
                            category = "Обучение Advanced 2.0",
                            amount = 170000
                    ),
                    @Spending(
                            description = "Рыбалка",
                            category = "Рыбалка на Неве",
                            amount = 1000
                    )
            }
    )
    @ScreenShotTest(value = "img/expected_stat.png")
    void checkStatComponentTest(UserJson user, BufferedImage expected) throws IOException, InterruptedException {

        StatComponent statComponent = Selenide.open(LoginPage.URL, LoginPage.class)
                .fillLoginPage(user.username(), user.testData().password())
                .submit(new MainPage())
                .getStatComponent();

        Thread.sleep(3000);

//        assertFalse(new ScreenDiffResult(
//                expected,
//                statComponent.chartScreenshot()
//
//        ), "Screen comparison failure");

        statComponent.checkBubbles(Color.yellow, Color.green);

    }


    @User(
            username = "ivan8",
            spendings = {
                    @Spending(
                            description = "Новое обучение",
                            category = "Обучение Advanced 2.0",
                            amount = 170000
                    ),
                    @Spending(
                            description = "Рыбалка",
                            category = "Рыбалка на Неве",
                            amount = 1000
                    )
            }
    )
    @ScreenShotTest(value = "img/expected_stat.png")
    void checkStatComponentWithTextTest(UserJson user, BufferedImage expected) throws IOException, InterruptedException {

        StatComponent statComponent = Selenide.open(LoginPage.URL, LoginPage.class)
                .fillLoginPage(user.username(), user.testData().password())
                .submit(new MainPage())
                .getStatComponent();

        Thread.sleep(3000);

        Bubble bubble1 = new Bubble(Color.yellow, "Обучение Advanced 2.0 170000 ₽");
        Bubble bubble2 = new Bubble(Color.green, "Рыбалка на Неве 1000 ₽");

        statComponent.checkBubblesWithText(bubble1, bubble2);

    }

    @User(
            username = "ivan8",
            spendings = {
                    @Spending(
                            description = "Новое обучение",
                            category = "Обучение Advanced 2.0",
                            amount = 170000
                    ),
                    @Spending(
                            description = "Рыбалка",
                            category = "Рыбалка на Неве",
                            amount = 1000
                    )
            }
    )
    @ScreenShotTest(value = "img/expected_stat.png")
    void checkStatComponentBubblesInAnyOrderTest(UserJson user, BufferedImage expected) throws IOException, InterruptedException {

        StatComponent statComponent = Selenide.open(LoginPage.URL, LoginPage.class)
                .fillLoginPage(user.username(), user.testData().password())
                .submit(new MainPage())
                .getStatComponent();

        Thread.sleep(3000);

        Bubble bubble2 = new Bubble(Color.yellow, "Обучение Advanced 2.0 170000 ₽");
        Bubble bubble1 = new Bubble(Color.green, "Рыбалка на Неве 1000 ₽");

        statComponent.checkBubblesInAnyOrder(bubble1, bubble2);

    }

    @User(
            username = "ivan8",
            spendings = {
                    @Spending(
                            description = "Новое обучение",
                            category = "Обучение Advanced 2.0",
                            amount = 170000
                    ),
                    @Spending(
                            description = "Рыбалка",
                            category = "Рыбалка на Неве",
                            amount = 100000
                    ),
                    @Spending(
                            description = "Продукты",
                            category = "Закупка к Новому Году",
                            amount = 21000
                    )
            }
    )
    @ScreenShotTest(value = "img/expected_stat.png")
    void checkStatComponentBubblesContainsTest(UserJson user, BufferedImage expected) throws IOException, InterruptedException {

        StatComponent statComponent = Selenide.open(LoginPage.URL, LoginPage.class)
                .fillLoginPage(user.username(), user.testData().password())
                .submit(new MainPage())
                .getStatComponent();

        Thread.sleep(3000);

        Bubble bubble1 = new Bubble(Color.yellow, "Обучение Advanced 2.0 170000 ₽");
        Bubble bubble2 = new Bubble(Color.green, "Рыбалка на Неве 100000 ₽");

        statComponent.checkBubblesContains(bubble1, bubble2);

    }


    @Test
    void checkSpendsTableTest(@UserType UsersQueueExtension.StaticUser user) throws IOException, InterruptedException, ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH);
        SpendJson spend1 = new SpendJson(
                null,
                simpleDateFormat.parse("Oct 23, 2026"),
                new CategoryJson(
                        null,
                        "Питомцы",
                        user.username(),
                        false
                ),
                CurrencyValues.RUB,
                10000.0,
                "Ворона",
                user.username()
        );

        SpendJson spend2 = new SpendJson(
                null,
                simpleDateFormat.parse("Oct 13, 2026"),
                new CategoryJson(
                        null,
                        "Недвижимость",
                        user.username(),
                        false
                ),
                CurrencyValues.RUB,
                99999999.0,
                "Изумрудный город",
                user.username()
        );

        SpendJson spend3 = new SpendJson(
                null,
                simpleDateFormat.parse("Apr 22, 2026"),
                new CategoryJson(
                        null,
                        "Мозги",
                        user.username(),
                        false
                ),
                CurrencyValues.RUB,
                1000000.0,
                null,
                user.username()
        );

        SpendingTable spendingTable = Selenide.open(LoginPage.URL, LoginPage.class)
                .fillLoginPage(user.username(), user.password())
                .submit(new MainPage())
                .getSpendingTable();

        Thread.sleep(3000);


        spendingTable.checkSpending(spend1, spend2, spend3);

    }

}
