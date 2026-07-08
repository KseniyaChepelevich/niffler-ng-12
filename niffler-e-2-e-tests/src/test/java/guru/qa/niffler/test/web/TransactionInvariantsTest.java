package guru.qa.niffler.test.web;


import guru.qa.niffler.data.dao.AuthAuthorityDao;
import guru.qa.niffler.data.dao.AuthUserDao;
import guru.qa.niffler.data.dao.UserdataUserDao;
import guru.qa.niffler.data.dao.impl.*;
import guru.qa.niffler.data.entity.AuthAuthorityEntity;
import guru.qa.niffler.data.entity.AuthUserEntity;
import guru.qa.niffler.data.entity.Authority;
import guru.qa.niffler.data.entity.UserEntity;
import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.model.UserdataUserJson;
import guru.qa.niffler.service.UsersDbClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static guru.qa.niffler.utils.RandomDataUtils.randomUsername;

public class TransactionInvariantsTest {

    private final AuthUserDao authUserDaoSpring = new AuthUserDaoSpringJdbc();
    private final AuthAuthorityDao authAuthorityDaoSpring = new AuthAuthorityDaoSpringJdbc();
    private final UserdataUserDao userdataUserDaoSpring = new UserdataUserDaoSpringJdbc();

    private final AuthUserDao authUserDao = new AuthUserDaoJdbc();
    private final AuthAuthorityDao authAuthorityDao = new AuthAuthorityDaoJdbc();
    private final UserdataUserDao userdataUserDao = new UserdataUserDaoJdbc();


    @Test
    void jdbcWithoutTransactionsTest() {
        String username = randomUsername();

        AuthUserEntity authUserEntity = new AuthUserEntity();
        authUserEntity.setId(null);
        authUserEntity.setUsername(username);
        authUserEntity.setAccountNonExpired(true);
        authUserEntity.setAccountNonLocked(true);
        authUserEntity.setCredentialsNonExpired(true);
        authUserEntity.setCredentialsNonExpired(true);
        authUserEntity.setEnabled(true);
        authUserEntity.setPassword("12345");
        UUID authUserId = authUserDao.create(authUserEntity).getId();

        AuthAuthorityEntity authorityEntityRead = new AuthAuthorityEntity();
        authorityEntityRead.setId(null);
        authorityEntityRead.setAuthority(Authority.read);
        authorityEntityRead.setUserId(authUserId);

        AuthAuthorityEntity authorityEntityWrite = new AuthAuthorityEntity();
        authorityEntityWrite.setId(null);
        authorityEntityWrite.setAuthority(Authority.write);
        authorityEntityWrite.setUserId(authUserId);

        authAuthorityDao.create(authorityEntityRead);
        authAuthorityDao.create(authorityEntityWrite);

        try {
            throw new RuntimeException("Сбой бизнес-логики без транзакции!");
        } catch (RuntimeException ignored) {}

        Assertions.assertFalse(authUserDao.findAllByUsername(username).isEmpty());
        Assertions.assertTrue(userdataUserDao.findAllByUsername(username).isEmpty());
    }

    @Test
    void springJdbcWithoutTransactionsTest() {
        String username = randomUsername();

        AuthUserEntity authUserEntity = new AuthUserEntity();
        authUserEntity.setId(null);
        authUserEntity.setUsername(username);
        authUserEntity.setAccountNonExpired(true);
        authUserEntity.setAccountNonLocked(true);
        authUserEntity.setCredentialsNonExpired(true);
        authUserEntity.setCredentialsNonExpired(true);
        authUserEntity.setPassword("12345");
        authUserEntity.setEnabled(true);
        UUID authUserId = authUserDaoSpring.create(authUserEntity).getId();

        AuthAuthorityEntity authorityEntityRead = new AuthAuthorityEntity();
        authorityEntityRead.setId(null);
        authorityEntityRead.setAuthority(Authority.read);
        authorityEntityRead.setUserId(authUserId);


        AuthAuthorityEntity authorityEntityWrite = new AuthAuthorityEntity();
        authorityEntityWrite.setId(null);
        authorityEntityWrite.setAuthority(Authority.write);
        authorityEntityWrite.setUserId(authUserId);

        authAuthorityDaoSpring.create(authorityEntityRead);
        authAuthorityDao.create(authorityEntityWrite);

        try {
            throw new RuntimeException("Сбой Spring JDBC без транзакции!");
        } catch (RuntimeException ignored) {}

        Assertions.assertFalse(authUserDao.findAllByUsername(username).isEmpty());
        Assertions.assertFalse(userdataUserDao.findAllByUsername(username).isEmpty());
    }

    @Test
    void springJdbcWithTransactionsTest() {
        String username = randomUsername();
        UsersDbClient usersDbClient = new UsersDbClient();
        UserdataUserJson user = usersDbClient.createUserSpringJdbc(
                new UserdataUserJson(
                        null,
                        username,
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
        Assertions.assertFalse(authUserDaoSpring.findAllByUsername(username).isEmpty());
        Assertions.assertFalse(userdataUserDaoSpring.findAllByUsername(username).isEmpty());
        Assertions.assertFalse(authAuthorityDaoSpring.findAllByUserId(authUserDaoSpring.findAllByUsername(username).get(0).getId()).isEmpty());
    }

    @Test
    void jdbcWithTransactionsTest() {
        String username = randomUsername();
        UsersDbClient usersDbClient = new UsersDbClient();
        UserdataUserJson user = usersDbClient.createUserJdbc(
                new UserdataUserJson(
                        null,
                        username,
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

        System.out.println(user);
        Assertions.assertFalse(authUserDao.findAllByUsername(username).isEmpty());
        Assertions.assertFalse(userdataUserDao.findAllByUsername(username).isEmpty());
        Assertions.assertFalse(authAuthorityDao.findAllByUserId(authUserDao.findAllByUsername(username).get(0).getId()).isEmpty());


    }
}
