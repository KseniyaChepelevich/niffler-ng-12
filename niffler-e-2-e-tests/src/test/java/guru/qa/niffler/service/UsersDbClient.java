package guru.qa.niffler.service;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.Databases;
import guru.qa.niffler.data.dao.impl.AuthUserDaoSpringJdbc;
import guru.qa.niffler.data.dao.impl.AuthAuthorityDaoSpringJdbc;
import guru.qa.niffler.data.dao.impl.UserdataUserDaoSpringJdbc;
import guru.qa.niffler.data.dao.impl.AuthUserDaoJdbc;
import guru.qa.niffler.data.dao.impl.AuthAuthorityDaoJdbc;
import guru.qa.niffler.data.dao.impl.UserdataUserDaoJdbc;
import guru.qa.niffler.data.entity.AuthAuthorityEntity;
import guru.qa.niffler.data.entity.AuthUserEntity;
import guru.qa.niffler.data.entity.Authority;
import guru.qa.niffler.data.entity.UserEntity;
import guru.qa.niffler.model.UserdataUserJson;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.SQLException;
import java.util.Arrays;


import static guru.qa.niffler.data.Databases.dataSource;
import static guru.qa.niffler.data.Databases.xaTransaction;



public class UsersDbClient {


    private static final Config CFG = Config.getInstance();
    private static final PasswordEncoder pe = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    public UserdataUserJson createUserSpringJdbc(UserdataUserJson user) {
        AuthUserEntity authUserEntity = new AuthUserEntity();
        authUserEntity.setUsername(user.username());
        authUserEntity.setPassword(pe.encode("12345"));
        authUserEntity.setEnabled(true);
        authUserEntity.setAccountNonExpired(true);
        authUserEntity.setAccountNonLocked(true);
        authUserEntity.setCredentialsNonExpired(true);

AuthUserEntity createdAuthUser = new AuthUserDaoSpringJdbc(dataSource(CFG.authJdbcUrl()))
        .create(authUserEntity);
AuthAuthorityEntity[] authAuthorityEntities = Arrays.stream(Authority.values()).map(
        e -> {
            AuthAuthorityEntity ae = new AuthAuthorityEntity();
            ae.setUserId(createdAuthUser.getId());
            ae.setAuthority(e);
            return  ae;
        }
).toArray(AuthAuthorityEntity[]::new);

new AuthAuthorityDaoSpringJdbc(dataSource(CFG.authJdbcUrl()))
        .create(authAuthorityEntities);
return UserdataUserJson.fromEntity(
        new UserdataUserDaoSpringJdbc(dataSource(CFG.userdataJdbcUrl()))
        .createUser(
                UserEntity.fromJson(user)
        ),
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
                                                                AuthAuthorityEntity authorityEntity = new AuthAuthorityEntity();
                                                                authorityEntity.setUserId(authUserEntity.getId());
                                                                authorityEntity.setAuthority(a);
                                                                return authorityEntity;
                                                            }
                                                    ).toArray(AuthAuthorityEntity[]::new));
                                    return null;
                                },
                                CFG.authJdbcUrl()
                        ),
                        new Databases.XaFunction<>(
                                con -> {
                                    UserEntity userEntity = new UserEntity();
                                    userEntity.setUsername(user.username());
                                    userEntity.setFullname(user.fullname());
                                    userEntity.setCurrency(user.currency());
                                    try {
                                        new UserdataUserDaoJdbc(con).createUser(userEntity);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                    return userEntity;
                                },
                                CFG.userdataJdbcUrl()
                        )
                ),
                null);
    }

}
