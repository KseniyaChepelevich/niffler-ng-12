package guru.qa.niffler.test.web;

import guru.qa.niffler.data.entity.AuthUserEntity;
import guru.qa.niffler.data.entity.Authority;
import guru.qa.niffler.model.*;
import guru.qa.niffler.service.AuthAuthorityDbClient;
import guru.qa.niffler.service.AuthUserDbClient;
import guru.qa.niffler.service.SpendDbClient;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class JdbcTest {

    @Test
    void daoTest() {
        SpendDbClient spendDbClient = new SpendDbClient();

        SpendJson spend = spendDbClient.createSpend(
                new SpendJson(
                        null,
                        new Date(),
                        new CategoryJson(
                                null,
                                "test-cat-name-3",
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
}
