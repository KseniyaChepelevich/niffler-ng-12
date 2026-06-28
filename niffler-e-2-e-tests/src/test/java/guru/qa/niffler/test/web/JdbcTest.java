package guru.qa.niffler.test.web;

import guru.qa.niffler.data.entity.auth.Authority;
import guru.qa.niffler.model.*;
import guru.qa.niffler.service.SpendDbClient;
import guru.qa.niffler.service.UserdataDbClient;
import guru.qa.niffler.service.UsersDbClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Date;

public class JdbcTest {

    static UsersDbClient usersDbClient = new UsersDbClient();
    static SpendDbClient spendDbClient = new SpendDbClient();
    static UserdataDbClient userdataDbClient = new UserdataDbClient();

    @Test
    void daoTest() {
        SpendJson spend = spendDbClient.createSpending(
                new SpendJson(
                        null,
                        new Date(),
                        new CategoryJson(
                                null,
                                "test-cat-name-30",
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


    @ValueSource(strings = {
            "egor-07"
    })
    @ParameterizedTest
    void jdbcTest(String uname) {
        UserJson user = usersDbClient.createUserJdbc(
                uname,
                "12345"
        );
        usersDbClient.createIncomeInvitationJdbc(user, 1);
        usersDbClient.createOutcomeInvitationJdbc(user, 1);
        usersDbClient.createFriends(user, 1);
    }

    @ValueSource(strings = {
            "egor-08"
    })
    @ParameterizedTest
    void springJdbcTest(String uname) {
        UserJson user = usersDbClient.createUserSpringJdbc(
                uname,
                "12345"
        );
        usersDbClient.createIncomeInvitationSpringJdbc(user, 1);
        usersDbClient.createOutcomeInvitationSpringJdbc(user, 1);
        usersDbClient.createFriendsSpringJdbc(user, 1);
    }

    @ValueSource(strings = {
            "egor-09"
    })
    @ParameterizedTest
    void hibernateTest(String uname) {
        UserJson user = usersDbClient.createUser(
                uname,
                "12345"
        );
        usersDbClient.createIncomeInvitation(user, 1);
        usersDbClient.createOutcomeInvitation(user, 1);
        usersDbClient.createFriends(user, 1);
    }


    @Test
    void spendJdbcTest() {
        spendDbClient.createSpending(new SpendJson(
                null,
                new Date(),
                new CategoryJson(
                        null,
                        "тест spend jdbc4",
                        "duck",
                        false
                ),
                CurrencyValues.RUB,
                5000.5,
                "Тест 1",
                "duck"
        ));
    }

    @Test
    void spendSpringJdbcTest() {
        spendDbClient.createSpendingSpringJdbc(new SpendJson(
                null,
                new Date(),
                new CategoryJson(
                        null,
                        "тест spend spring jdbc4",
                        "duck",
                        false
                ),
                CurrencyValues.RUB,
                5000.1,
                "Тест 2",
                "duck"
        ));
    }

    @Test
    void spendHibernateTest() {
        spendDbClient.createSpendingHibernate(new SpendJson(
                null,
                new Date(),
                new CategoryJson(
                        null,
                        "тест spend hibernate4",
                        "duck",
                        false
                ),
                CurrencyValues.RUB,
                5000.0,
                "Тест 3",
                "duck"
        ));
    }

    @Test
    void createUserWithAuthoritiesJdbcTest() {
        AuthUserJson user = userdataDbClient.createUserWithAuthoritiesJdbc(
                new AuthUserJson(
                        null,
                        "ivan-14",
                        "12345",
                        true,
                        true,
                        true,
                        true
                ),
                Arrays.asList(
                        new AuthAuthorityJson(null, Authority.read, null),
                        new AuthAuthorityJson(null, Authority.write, null)
                )
        );

        System.out.println("Created user: " + user);
    }

    @Test
    void createUserWithAuthoritiesSpringJdbcTest() {
        AuthUserJson user = userdataDbClient.createUserWithAuthoritiesSpringJdbc(
                new AuthUserJson(
                        null,
                        "ivan-24",
                        "12345",
                        true,
                        true,
                        true,
                        true
                ),
                Arrays.asList(
                        new AuthAuthorityJson(null, Authority.read, null),
                        new AuthAuthorityJson(null, Authority.write, null)
                )
        );

        System.out.println("Created user: " + user);
    }

    @Test
    void createUserWithAuthoritiesHibernateTest() {
        AuthUserJson user = userdataDbClient.createUserWithAuthoritiesHibernate(
                new AuthUserJson(
                        null,
                        "ivan-37",
                        "12345",
                        true,
                        true,
                        true,
                        true
                ),
                Arrays.asList(
                        new AuthAuthorityJson(null, Authority.read, null),
                        new AuthAuthorityJson(null, Authority.write, null)
                )
        );

        System.out.println("Created user: " + user);
    }
}
