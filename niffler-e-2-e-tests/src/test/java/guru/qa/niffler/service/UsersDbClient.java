package guru.qa.niffler.service;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.Databases;
import guru.qa.niffler.data.dao.AuthAuthorityDao;
import guru.qa.niffler.data.dao.AuthUserDao;
import guru.qa.niffler.data.dao.UserdataUserDao;
import guru.qa.niffler.data.dao.impl.*;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.Authority;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.data.entity.userdata.UserEntity;
import guru.qa.niffler.data.repository.AuthUserRepository;
import guru.qa.niffler.data.repository.impl.AuthUserRepositoryJdbc;
import guru.qa.niffler.data.tpl.DataSources;
import guru.qa.niffler.data.tpl.XaTransactionTemplate;
import guru.qa.niffler.model.UserdataUserJson;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Arrays;


import static guru.qa.niffler.data.Databases.xaTransaction;



public class UsersDbClient {


    private static final Config CFG = Config.getInstance();
    private static final PasswordEncoder pe = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private final AuthUserRepository authUserRepository = new AuthUserRepositoryJdbc();
    private final UserdataUserDao userdataUserDao = new UserdataUserDaoSpringJdbc();

    private final AuthUserDao authUserDao = new AuthUserDaoSpringJdbc();
    private final AuthAuthorityDao authAuthorityDao = new AuthAuthorityDaoSpringJdbc();
    private final UserdataUserDao udUserDao = new UserdataUserDaoSpringJdbc();

    private final TransactionTemplate txTemplate = new TransactionTemplate(
            new JdbcTransactionManager(
                    DataSources.dataSource(CFG.authJdbcUrl())
            )
    );

    private final XaTransactionTemplate xaTransactionTemplate = new XaTransactionTemplate(
            CFG.authJdbcUrl(),
            CFG.userdataJdbcUrl()
    );

    public UserdataUserJson createUserSpringJdbc(UserdataUserJson user) {
                    AuthUserEntity authUserEntity = new AuthUserEntity();
                    authUserEntity.setUsername(user.username());
                    authUserEntity.setPassword(pe.encode("12345"));
                    authUserEntity.setEnabled(true);
                    authUserEntity.setAccountNonExpired(true);
                    authUserEntity.setAccountNonLocked(true);
                    authUserEntity.setCredentialsNonExpired(true);
        AuthUserEntity createdAuthUser = authUserDao.create(authUserEntity);

        AuthorityEntity[] authorityEntities = Arrays.stream(Authority.values()).map(
                e -> {
                    AuthorityEntity ae = new AuthorityEntity();
                    ae.setUser(createdAuthUser);
                    ae.setAuthority(e);
                    return ae;
                }
        ).toArray(AuthorityEntity[]::new);

        authAuthorityDao.create(authorityEntities);

        return UserdataUserJson.fromEntity(
                udUserDao.createUser(UserEntity.fromJson(user)),
                null
        );
    }


    public UserdataUserJson createUser(UserdataUserJson user) {
        return UserdataUserJson.fromEntity(
                xaTransaction(
                        new Databases.XaFunction<>(
                                con -> {
                                    AuthUserEntity authUserEntity = new AuthUserEntity();
                                    authUserEntity.setUsername(user.username());
                                    authUserEntity.setPassword(pe.encode("12345"));
                                    authUserEntity.setEnabled(true);
                                    authUserEntity.setAccountNonExpired(true);
                                    authUserEntity.setAccountNonLocked(true);
                                    authUserEntity.setCredentialsNonExpired(true);

                                    new AuthUserDaoJdbc(con).create(authUserEntity);
                                    new AuthAuthorityDaoJdbc(con).create(
                                            Arrays.stream(Authority.values())
                                                    .map(a -> {
                                                                AuthorityEntity authorityEntity = new AuthorityEntity();
                                                                authorityEntity.setUser(authUserEntity);
                                                                authorityEntity.setAuthority(a);
                                                                return authorityEntity;
                                                            }
                                                    ).toArray(AuthorityEntity[]::new));
                                    return null;
                                },
                                CFG.authJdbcUrl()
                        ),
                        new Databases.XaFunction<>(
                                con -> {
                                    UserEntity userdataUserEntity = new UserEntity();
                                    userdataUserEntity.setUsername(user.username());
                                    userdataUserEntity.setFullname(user.fullname());
                                    userdataUserEntity.setCurrency(user.currency());
                                    new UserdataUserDaoJdbc(con).createUser(userdataUserEntity);
                                    return userdataUserEntity;
                                },
                                CFG.userdataJdbcUrl()
                        )
                ),
                null);
    }

}
