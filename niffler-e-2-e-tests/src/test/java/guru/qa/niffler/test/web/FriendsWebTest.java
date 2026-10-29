package guru.qa.niffler.test.web;

import com.codeborne.selenide.SelenideDriver;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.annotation.meta.WebTest;
import guru.qa.niffler.jupiter.extension.BrowserExtension;
import guru.qa.niffler.jupiter.extension.UsersQueueExtension;
import guru.qa.niffler.page.LoginPage;
import guru.qa.niffler.utils.SelenideUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@WebTest
public class FriendsWebTest {

    private final SelenideDriver driver = new SelenideDriver(SelenideUtils.chromeConfig);


    @Test
    @ExtendWith(UsersQueueExtension.class)
    void friendShouldBePresentInFriendsTable(@UsersQueueExtension.UserType(UsersQueueExtension.Type.WITH_FRIEND) UsersQueueExtension.StaticUser user) {
        driver.open

(LoginPage.URL

, LoginPage.class)
                .login(user.username(), user.password())
                .getHeader().toFriendsPage()
                .checkExistingFriends(user.friend());
    }

    @Test
    @ExtendWith(UsersQueueExtension.class)
    void friendsTableShouldBeEmptyForNewUser(@UsersQueueExtension.UserType(UsersQueueExtension.Type.EMPTY) UsersQueueExtension.StaticUser user) {
        driver.open

(LoginPage.URL

, LoginPage.class)
                .login(user.username(), user.password())
                .getHeader().toFriendsPage()
                .checkExistingFriendsCount(0);
    }

    @Test
    @ExtendWith(UsersQueueExtension.class)
    void incomeInvitationBePresentInFriendsTable(@UsersQueueExtension.UserType(UsersQueueExtension.Type.WITH_INCOME_REQUEST) UsersQueueExtension.StaticUser user) {
        driver.open

(LoginPage.URL

, LoginPage.class)
                .login(user.username(), user.password())
                .getHeader().toFriendsPage()
                .checkExistingInvitations(user.income());
    }

    @Test
    @ExtendWith(UsersQueueExtension.class)
    void outcomeInvitationBePresentInAllPeopleTable(@UsersQueueExtension.UserType(UsersQueueExtension.Type.WITH_OUTCOME_REQUEST) UsersQueueExtension.StaticUser user) {
        driver.open

(LoginPage.URL

, LoginPage.class)
                .login(user.username(), user.password())
                .getHeader().toFriendsPage()
                .checkAllPeopleTable(user.outcome());
    }


}
