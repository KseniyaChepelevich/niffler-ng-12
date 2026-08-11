package guru.qa.niffler.test.web;

import com.codeborne.selenide.Selenide;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.annotation.Category;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.jupiter.annotation.meta.WebTest;
import guru.qa.niffler.jupiter.extension.UsersQueueExtension;
import guru.qa.niffler.jupiter.extension.UsersQueueExtension.UserType;
import guru.qa.niffler.jupiter.extension.UsersQueueExtension.StaticUser;
import guru.qa.niffler.page.LoginPage;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@WebTest
public class ProfileTest {
    private static final Config CFG = Config.getInstance();


    @SneakyThrows
    @Test
    @ExtendWith(UsersQueueExtension.class)
    void test(@UserType(UsersQueueExtension.Type.EMPTY) StaticUser user0, @UserType(UsersQueueExtension.Type.WITH_FRIEND) StaticUser user1) {
        Thread.sleep(1000);
        System.out.println(user0);
    }


}
