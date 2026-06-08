package guru.qa.niffler.test.web;

import guru.qa.niffler.jupiter.extension.UsersQueueExtension;
import guru.qa.niffler.jupiter.extension.UsersQueueExtension.UserType;
import guru.qa.niffler.jupiter.extension.UsersQueueExtension.StaticUser;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(UsersQueueExtension.class)
public class ProfileTest {

//    @Test
//    void testWithEmptyUser0(@UserType(empty = true) StaticUser user) throws InterruptedException {
//        Thread.sleep(1000);
//        System.out.println(user);
//    }

    @SneakyThrows
    @Test
    @ExtendWith(UsersQueueExtension.class)
    void test(@UserType(UsersQueueExtension.Type.EMPTY) StaticUser user0, @UserType(UsersQueueExtension.Type.WITH_FRIEND) StaticUser user1){
        Thread.sleep(1000);
        System.out.println(user0);
    }
}
