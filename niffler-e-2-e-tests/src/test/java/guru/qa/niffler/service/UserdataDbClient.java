package guru.qa.niffler.service;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.data.entity.userdata.UserEntity;
import guru.qa.niffler.data.repository.AuthUserRepository;
import guru.qa.niffler.data.repository.UserdataUserRepository;
import guru.qa.niffler.data.repository.impl.AuthUserRepositoryHibernate;
import guru.qa.niffler.data.repository.impl.UserdataUserRepositoryHibernate;
import guru.qa.niffler.data.repository.impl.UserdataUserRepositoryJdbc;
import guru.qa.niffler.data.repository.impl.AuthUserRepositoryJdbc;
import guru.qa.niffler.data.repository.impl.UserdataUserRepositorySpringJdbc;
import guru.qa.niffler.data.repository.impl.AuthUserRepositorySpringJdbc;
import guru.qa.niffler.data.tpl.XaTransactionTemplate;
import guru.qa.niffler.model.AuthAuthorityJson;
import guru.qa.niffler.model.AuthUserJson;
import guru.qa.niffler.model.UserJson;

import java.util.List;

public class UserdataDbClient {
    private static final Config CFG = Config.getInstance();

    private final UserdataUserRepository userdataUserRepositoryHibernate = new UserdataUserRepositoryHibernate();
    private final AuthUserRepository authUserRepositoryHibernate = new AuthUserRepositoryHibernate();

    private final UserdataUserRepository userdataUserRepositoryJdbc = new UserdataUserRepositoryJdbc();
    private final AuthUserRepository authUserRepositoryJdbc = new AuthUserRepositoryJdbc();

    private final UserdataUserRepository userdataUserRepositorySpringJdbc = new UserdataUserRepositorySpringJdbc();
    private final AuthUserRepository authUserRepositorySpringJdbc = new AuthUserRepositorySpringJdbc();

    private final XaTransactionTemplate xaTransactionTemplate = new XaTransactionTemplate(
            CFG.userdataJdbcUrl(),
            CFG.authJdbcUrl()
    );

    public UserJson createUserJdbc(UserJson user) {
        return xaTransactionTemplate.execute(() -> {
            UserEntity userEntity = UserEntity.fromJson(user);
            UserEntity createdUser = userdataUserRepositoryJdbc.create(userEntity);
            return UserJson.fromEntity(createdUser);
        });
    }

    public AuthUserJson createUserWithAuthoritiesJdbc(AuthUserJson user, List<AuthAuthorityJson> authorities) {
        return xaTransactionTemplate.execute(() -> {
            AuthUserEntity userEntity = AuthUserEntity.fromJson(user);

            for (AuthAuthorityJson authorityJson : authorities) {
                AuthorityEntity authorityEntity = AuthorityEntity.fromJson(authorityJson);
                authorityEntity.setUser(userEntity);
                userEntity.getAuthorities().add(authorityEntity);
            }

            AuthUserEntity createdUser = authUserRepositoryJdbc.create(userEntity);
            return AuthUserJson.fromEntity(createdUser);
        });
    }

    public UserJson createUserSpringJdbc(UserJson user) {
        return xaTransactionTemplate.execute(() -> {
            UserEntity userEntity = UserEntity.fromJson(user);
            UserEntity createdUser = userdataUserRepositorySpringJdbc.create(userEntity);
            return UserJson.fromEntity(createdUser);
        });
    }

    public AuthUserJson createUserWithAuthoritiesSpringJdbc(AuthUserJson user, List<AuthAuthorityJson> authorities) {
        return xaTransactionTemplate.execute(() -> {
            AuthUserEntity userEntity = AuthUserEntity.fromJson(user);

            for (AuthAuthorityJson authorityJson : authorities) {
                AuthorityEntity authorityEntity = AuthorityEntity.fromJson(authorityJson);
                authorityEntity.setUser(userEntity);
                userEntity.getAuthorities().add(authorityEntity);
            }

            AuthUserEntity createdUser = authUserRepositorySpringJdbc.create(userEntity);
            return AuthUserJson.fromEntity(createdUser);
        });
    }

    public UserJson createUserHibernate(UserJson user) {
        return xaTransactionTemplate.execute(() -> {
            UserEntity userEntity = UserEntity.fromJson(user);
            UserEntity createdUser = userdataUserRepositoryHibernate.create(userEntity);
            return UserJson.fromEntity(createdUser);
        });
    }

    public AuthUserJson createUserWithAuthoritiesHibernate(AuthUserJson user, List<AuthAuthorityJson> authorities) {
        return xaTransactionTemplate.execute(() -> {
            AuthUserEntity userEntity = AuthUserEntity.fromJson(user);

            for (AuthAuthorityJson authorityJson : authorities) {
                AuthorityEntity authorityEntity = AuthorityEntity.fromJson(authorityJson);
                authorityEntity.setUser(userEntity);
                userEntity.getAuthorities().add(authorityEntity);
            }

            AuthUserEntity createdUser = authUserRepositoryHibernate.create(userEntity);
            return AuthUserJson.fromEntity(createdUser);
        });
    }
}
