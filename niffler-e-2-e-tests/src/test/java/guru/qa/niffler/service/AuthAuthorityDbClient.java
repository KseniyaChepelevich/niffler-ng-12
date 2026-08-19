package guru.qa.niffler.service;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.impl.AuthAuthorityDaoJdbc;
import guru.qa.niffler.data.entity.AuthAuthorityEntity;
import guru.qa.niffler.data.entity.AuthUserEntity;
import guru.qa.niffler.model.AuthAuthorityJson;

import static guru.qa.niffler.data.Databases.transaction;

public class AuthAuthorityDbClient {
    private static final Config CFG = Config.getInstance();

    public AuthAuthorityJson createAuthority(AuthAuthorityJson authority) {
        return transaction(connection -> {
                    AuthAuthorityEntity authorityEntity = AuthAuthorityEntity.fromJson(authority);
                    AuthUserEntity authUserEntity = new AuthUserEntity();
                    authUserEntity.setId(authority.user());
                    authorityEntity.setUser(authUserEntity);

                    new AuthAuthorityDaoJdbc(connection).create(authorityEntity);
                    return AuthAuthorityJson.fromEntity(authorityEntity);
                },
                CFG.authJdbcUrl()
        );
    }
}
