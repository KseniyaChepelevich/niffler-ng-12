package guru.qa.niffler.service;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.impl.AuthUserDaoJdbc;
import guru.qa.niffler.data.entity.AuthUserEntity;
import java.sql.Connection;
import static guru.qa.niffler.data.Databases.transaction;

public class AuthUserDbClient {
    private static final Config CFG = Config.getInstance();

    public AuthUserEntity createUser(AuthUserEntity user) {
        return transaction((Connection connection) -> {
                    return new AuthUserDaoJdbc(connection).create(user);
                },
                CFG.authJdbcUrl()
        );
    }

}
