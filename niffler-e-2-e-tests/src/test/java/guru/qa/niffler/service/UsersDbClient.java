package guru.qa.niffler.service;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.AuthAuthorityDao;
import guru.qa.niffler.data.dao.AuthUserDao;
import guru.qa.niffler.data.dao.UserdataUserDao;
import guru.qa.niffler.data.dao.impl.*;
import guru.qa.niffler.data.entity.AuthAuthorityEntity;
import guru.qa.niffler.data.entity.AuthUserEntity;
import guru.qa.niffler.data.entity.Authority;
import guru.qa.niffler.data.entity.UserEntity;
import guru.qa.niffler.data.tpl.DataSources;
import guru.qa.niffler.data.tpl.XaTransactionTemplate;
import guru.qa.niffler.model.UserdataUserJson;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Arrays;

public class UsersDbClient {


    private static final Config CFG = Config.getInstance();
    private static final PasswordEncoder pe = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private final AuthUserDao authUserDao = new AuthUserDaoSpringJdbc();
    private final AuthAuthorityDao authAuthorityDao = new AuthAuthorityDaoSpringJdbc();
    private final UserdataUserDao userdataUserDao = new UserdataUserDaoSpringJdbc();

    private final AuthUserDao authUserDaoJdbc = new AuthUserDaoJdbc();
    private final AuthAuthorityDao authAuthorityDaoJdbc = new AuthAuthorityDaoJdbc();
    private final UserdataUserDao userdataUserDaoJdbc = new UserdataUserDaoJdbc();

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
        return xaTransactionTemplate.execute(() -> {
            AuthUserEntity authUserEntity = new AuthUserEntity();
            authUserEntity.setUsername(user.username());
            authUserEntity.setPassword(pe.encode("12345"));
            authUserEntity.setEnabled(true);
            authUserEntity.setAccountNonExpired(true);
            authUserEntity.setAccountNonLocked(true);
            authUserEntity.setCredentialsNonExpired(true);

            AuthUserEntity createdAuthUser = authUserDao.create(authUserEntity);
            AuthAuthorityEntity[] authAuthorityEntities = Arrays.stream(Authority.values()).map(
                    e -> {
                        AuthAuthorityEntity ae = new AuthAuthorityEntity();
                        ae.setUserId(createdAuthUser.getId());
                        ae.setAuthority(e);
                        return ae;
                    }
            ).toArray(AuthAuthorityEntity[]::new);

            authAuthorityDao.create(authAuthorityEntities);
            return UserdataUserJson.fromEntity(
                    userdataUserDao.createUser(UserEntity.fromJson(user)),
                    null
            );
        });


    }

    public UserdataUserJson createUserJdbc(UserdataUserJson user) {
        return xaTransactionTemplate.execute(() -> {
            AuthUserEntity authUserEntity = new AuthUserEntity();
            authUserEntity.setUsername(user.username());
            authUserEntity.setPassword(pe.encode("12345"));
            authUserEntity.setEnabled(true);
            authUserEntity.setAccountNonExpired(true);
            authUserEntity.setAccountNonLocked(true);
            authUserEntity.setCredentialsNonExpired(true);

            AuthUserEntity createdAuthUser = authUserDaoJdbc.create(authUserEntity);
            AuthAuthorityEntity[] authAuthorityEntities = Arrays.stream(Authority.values()).map(
                    e -> {
                        AuthAuthorityEntity ae = new AuthAuthorityEntity();
                        ae.setUserId(createdAuthUser.getId());
                        ae.setAuthority(e);
                        return ae;
                    }
            ).toArray(AuthAuthorityEntity[]::new);

            authAuthorityDaoJdbc.create(authAuthorityEntities);
            return UserdataUserJson.fromEntity(
                    userdataUserDaoJdbc.createUser(UserEntity.fromJson(user)),
                    null
            );
        });
    }


}
