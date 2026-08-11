package guru.qa.niffler.test.web;

import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.Authority;
import guru.qa.niffler.model.*;
import guru.qa.niffler.service.AuthAuthorityDbClient;
import guru.qa.niffler.service.AuthUserDbClient;
import guru.qa.niffler.service.SpendDbClient;
import guru.qa.niffler.service.UsersDbClient;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class JdbcTest {

    @Test
    void daoTest() {
        SpendDbClient spendDbClient = new SpendDbClient();

        SpendJson spend = spendDbClient.createSpending(
                new SpendJson(
                        null,
                        new Date(),
                        new CategoryJson(
                                null,
                                "test-cat-name-6",
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

    @Test
    void daoAuthTest() {
        AuthUserDbClient authUserDbClient = new AuthUserDbClient();
        AuthAuthorityDbClient authAuthorityDbClient = new AuthAuthorityDbClient();

        AuthUserEntity user = new AuthUserEntity();
        user.setUsername("dogdog2" + System.currentTimeMillis());
        user.setPassword("555");
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setEnabled(true);

        AuthUserEntity savedUser = authUserDbClient.createUser(user);

        AuthAuthorityJson authorityJson = authAuthorityDbClient.createAuthority(
                new AuthAuthorityJson(
                        null,
                        Authority.read,
                        savedUser.getId()
                )
        );

        System.out.println(authorityJson);


    }

    @Test
    void xaTxTest() {
        UsersDbClient usersDbClient = new UsersDbClient();
        UserdataUserJson user = usersDbClient.createUser(
                new UserdataUserJson(
                        null,
                        "valentin-10",
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


    @Test
    void springJdbcTest() {
        UsersDbClient usersDbClient = new UsersDbClient();
        UserdataUserJson user = usersDbClient.createUserSpringJdbc(
                new UserdataUserJson(
                        null,
                        "valentin-15",
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
