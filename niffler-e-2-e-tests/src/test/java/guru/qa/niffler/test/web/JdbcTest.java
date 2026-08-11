package guru.qa.niffler.test.web;

import guru.qa.niffler.model.*;
import guru.qa.niffler.service.SpendDbClient;
import guru.qa.niffler.service.UsersDbClient;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class JdbcTest {

    @SneakyThrows
    @Test
    void daoTest() {
        SpendDbClient spendDbClient = new SpendDbClient();

        SpendJson spend = spendDbClient.createSpend(
                new SpendJson(
                        null,
                        new Date(),
                        new CategoryJson(
                                null,
                                "test-cat-name-16",
                                "Toto",
                                false
                        ),
                        CurrencyValues.RUB,
                        100.0,
                        "test desc,",
                        "Toto"
                )
        );
        System.out.println(spend);
    }

    @SneakyThrows
    @Test
    void spendSpringJdbcTest() {
        SpendDbClient spendDbClient = new SpendDbClient();

        SpendJson spend = spendDbClient.createSpendSpringJdbc(
                new SpendJson(
                        null,
                        new Date(),
                        new CategoryJson(
                                null,
                                "test-cat-name-36",
                                "Toto",
                                false
                        ),
                        CurrencyValues.RUB,
                        100.0,
                        "test desc,",
                        "Toto"
                )
        );
        System.out.println(spend);
    }



//    @Test
//    void xaTxTest() {
//        UsersDbClient usersDbClient = new UsersDbClient();
//        UserdataUserJson user = usersDbClient.createUser(
//                new UserdataUserJson(
//                        null,
//                        "valentin-41",
//                                null,
//                        null,
//                        null,
//                        CurrencyValues.RUB,
//                        null,
//                        null,
//                        null
//                )
//        );
//        System.out.println(user);
//    }


    @Test
    void springJdbcTest() {
        UsersDbClient usersDbClient = new UsersDbClient();
        UserdataUserJson user = usersDbClient.createUserSpringJdbc(
                new UserdataUserJson(
                        null,
                        "valentin-53",
                        null,
                        null,
                        null,
                        CurrencyValues.RUB,
                        null,
                        null,
                        null
                )
        );
        System.out.println(user);
    }
}
