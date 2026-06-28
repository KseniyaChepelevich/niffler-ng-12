package guru.qa.niffler.service;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.Authority;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.data.entity.userdata.UserEntity;
import guru.qa.niffler.data.repository.AuthUserRepository;
import guru.qa.niffler.data.repository.UserdataUserRepository;
import guru.qa.niffler.data.repository.impl.*;
import guru.qa.niffler.data.tpl.XaTransactionTemplate;
import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.model.UserJson;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static guru.qa.niffler.utils.RandomDataUtils.randomUsername;


public class UsersDbClient implements UserClient {


    private static final Config CFG = Config.getInstance();
    private static final PasswordEncoder pe = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private final AuthUserRepository authUserRepositoryH = new AuthUserRepositoryHibernate();
    private final UserdataUserRepository userdataUserRepositoryH = new UserdataUserRepositoryHibernate();

    private final AuthUserRepository authUserRepositorySpringJdbc = new AuthUserRepositorySpringJdbc();
    private final UserdataUserRepository userdataUserRepositorySpringJdbc = new UserdataUserRepositorySpringJdbc();

    private final AuthUserRepository authUserRepositoryJdbc = new AuthUserRepositoryJdbc();
    private final UserdataUserRepository userdataUserRepositoryJdbc = new UserdataUserRepositoryJdbc();

    private final XaTransactionTemplate xaTransactionTemplate = new XaTransactionTemplate(
            CFG.authJdbcUrl(),
            CFG.userdataJdbcUrl()
    );

    @Override
    public UserJson createUser(String username, String password) {
        return xaTransactionTemplate.execute(() -> {
                    AuthUserEntity authUserEntity = authUserEntity(username, password);
                    authUserRepositoryH.create(authUserEntity);
                    return UserJson.fromEntity(
                            userdataUserRepositoryH.create(userEntity(username)),
                            null
                    );
                }
        );
    }

    public UserJson createUserJdbc(String username, String password) {
        return xaTransactionTemplate.execute(() -> {
                    AuthUserEntity authUserEntity = authUserEntity(username, password);
                    authUserRepositoryJdbc.create(authUserEntity);
                    return UserJson.fromEntity(
                            userdataUserRepositoryJdbc.create(userEntity(username)),
                            null
                    );
                }
        );
    }

    public UserJson createUserSpringJdbc(String username, String password) {
        return xaTransactionTemplate.execute(() -> {
                    AuthUserEntity authUserEntity = authUserEntity(username, password);
                    authUserRepositorySpringJdbc.create(authUserEntity);
                    return UserJson.fromEntity(
                            userdataUserRepositorySpringJdbc.create(userEntity(username)),
                            null
                    );
                }
        );
    }

    private UserEntity userEntity(String username) {
        UserEntity ue = new UserEntity();
        ue.setUsername(username);
        ue.setCurrency(CurrencyValues.RUB);
        return ue;
    }

    private AuthUserEntity authUserEntity(String username, String password) {
        AuthUserEntity authUserEntity = new AuthUserEntity();
        authUserEntity.setUsername(username);
        authUserEntity.setPassword(pe.encode(password));
        authUserEntity.setEnabled(true);
        authUserEntity.setAccountNonExpired(true);
        authUserEntity.setAccountNonLocked(true);
        authUserEntity.setCredentialsNonExpired(true);
        authUserEntity.setAuthorities(
                Arrays.stream(Authority.values()).map(
                        e -> {
                            AuthorityEntity ae = new AuthorityEntity();

                            ae.setUser(authUserEntity);
                            ae.setAuthority(e);
                            return ae;
                        }
                ).toList()
        );
        return authUserEntity;
    }

    @Override
    public void createIncomeInvitation(UserJson targetUser, int count) {
        if (count > 0) {
            UserEntity targetEntity = userdataUserRepositoryH.findById(
                    targetUser.id()
            ).orElseThrow();

            for (int i = 0; i < count; i++) {
                xaTransactionTemplate.execute(() -> {
                            String username = randomUsername();
                            AuthUserEntity authUser = authUserEntity(username, "12345");
                            authUserRepositoryH.create(authUser);
                            UserEntity adressee = userdataUserRepositoryH.create(userEntity(username));
                            userdataUserRepositoryH.addIncomeInvitation(targetEntity, adressee);
                            return null;
                        }
                );
            }
        }
    }

    public void createIncomeInvitationJdbc(UserJson targetUser, int count) {
        if (count > 0) {
            UserEntity targetEntity = userdataUserRepositoryJdbc.findById(
                    targetUser.id()
            ).orElseThrow();

            for (int i = 0; i < count; i++) {
                xaTransactionTemplate.execute(() -> {
                            String username = randomUsername();
                            AuthUserEntity authUser = authUserEntity(username, "12345");
                            authUserRepositoryJdbc.create(authUser);
                            UserEntity adressee = userdataUserRepositoryJdbc.create(userEntity(username));
                            userdataUserRepositoryJdbc.addIncomeInvitation(targetEntity, adressee);
                            return null;
                        }
                );
            }
        }
    }

    public void createIncomeInvitationSpringJdbc(UserJson targetUser, int count) {
        if (count > 0) {
            UserEntity targetEntity = userdataUserRepositorySpringJdbc.findById(
                    targetUser.id()
            ).orElseThrow();

            for (int i = 0; i < count; i++) {
                xaTransactionTemplate.execute(() -> {
                            String username = randomUsername();
                            AuthUserEntity authUser = authUserEntity(username, "12345");
                            authUserRepositorySpringJdbc.create(authUser);
                            UserEntity adressee = userdataUserRepositorySpringJdbc.create(userEntity(username));
                            userdataUserRepositorySpringJdbc.addIncomeInvitation(targetEntity, adressee);
                            return null;
                        }
                );
            }
        }
    }

    @Override
    public void createOutcomeInvitation(UserJson targetUser, int count) {
        if (count > 0) {
            UserEntity targetEntity = userdataUserRepositoryH.findById(
                    targetUser.id()
            ).orElseThrow();

            for (int i = 0; i < count; i++) {
                xaTransactionTemplate.execute(() -> {
                            String username = randomUsername();
                            AuthUserEntity authUser = authUserEntity(username, "12345");
                            authUserRepositoryH.create(authUser);
                            UserEntity adressee = userdataUserRepositoryH.create(userEntity(username));
                            userdataUserRepositoryH.addOutcomeInvitation(targetEntity, adressee);
                            return null;
                        }
                );
            }
        }
    }

    public void createOutcomeInvitationJdbc(UserJson targetUser, int count) {
        if (count > 0) {
            UserEntity targetEntity = userdataUserRepositoryJdbc.findById(
                    targetUser.id()
            ).orElseThrow();

            for (int i = 0; i < count; i++) {
                xaTransactionTemplate.execute(() -> {
                            String username = randomUsername();
                            AuthUserEntity authUser = authUserEntity(username, "12345");
                            authUserRepositoryJdbc.create(authUser);
                            UserEntity adressee = userdataUserRepositoryJdbc.create(userEntity(username));
                            userdataUserRepositoryJdbc.addOutcomeInvitation(targetEntity, adressee);
                            return null;
                        }
                );
            }
        }
    }

    public void createOutcomeInvitationSpringJdbc(UserJson targetUser, int count) {
        if (count > 0) {
            UserEntity targetEntity = userdataUserRepositorySpringJdbc.findById(
                    targetUser.id()
            ).orElseThrow();

            for (int i = 0; i < count; i++) {
                xaTransactionTemplate.execute(() -> {
                            String username = randomUsername();
                            AuthUserEntity authUser = authUserEntity(username, "12345");
                            authUserRepositorySpringJdbc.create(authUser);
                            UserEntity adressee = userdataUserRepositorySpringJdbc.create(userEntity(username));
                            userdataUserRepositorySpringJdbc.addOutcomeInvitation(targetEntity, adressee);
                            return null;
                        }
                );
            }
        }
    }

    @Override
    public void createFriends(UserJson targetUser, int count) {
        if (count > 0) {
            UserEntity targetEntity = userdataUserRepositoryH.findById(
                    targetUser.id()
            ).orElseThrow();

            for (int i = 0; i < count; i++) {
                xaTransactionTemplate.execute(() -> {
                            String username = randomUsername();
                            AuthUserEntity authUser = authUserEntity(username, "12345");
                            authUserRepositoryH.create(authUser);
                            UserEntity adressee = userdataUserRepositoryH.create(userEntity(username));
                            userdataUserRepositoryH.addFriend(targetEntity, adressee);
                            return null;
                        }
                );
            }
        }
    }

    public void createFriendsJdbc(UserJson targetUser, int count) {
        if (count > 0) {
            UserEntity targetEntity = userdataUserRepositoryJdbc.findById(
                    targetUser.id()
            ).orElseThrow();

            for (int i = 0; i < count; i++) {
                xaTransactionTemplate.execute(() -> {
                            String username = randomUsername();
                            AuthUserEntity authUser = authUserEntity(username, "12345");
                            authUserRepositoryJdbc.create(authUser);
                            UserEntity adressee = userdataUserRepositoryJdbc.create(userEntity(username));
                            userdataUserRepositoryJdbc.addFriend(targetEntity, adressee);
                            return null;
                        }
                );
            }
        }
    }

    public void createFriendsSpringJdbc(UserJson targetUser, int count) {
        if (count > 0) {
            UserEntity targetEntity = userdataUserRepositorySpringJdbc.findById(
                    targetUser.id()
            ).orElseThrow();

            for (int i = 0; i < count; i++) {
                xaTransactionTemplate.execute(() -> {
                            String username = randomUsername();
                            AuthUserEntity authUser = authUserEntity(username, "12345");
                            authUserRepositorySpringJdbc.create(authUser);
                            UserEntity adressee = userdataUserRepositorySpringJdbc.create(userEntity(username));
                            userdataUserRepositorySpringJdbc.addFriend(targetEntity, adressee);
                            return null;
                        }
                );
            }
        }
    }

    @Override
    public Optional<UserJson> findById(UUID id) {
        return userdataUserRepositoryH.findById(id)
                .map(UserJson::fromEntity);
    }

    @Override
    public Optional<UserJson> findByUsername(String username) {
        return userdataUserRepositoryH.findByUsername(username)
                .map(UserJson::fromEntity);
    }

    @Override
    public UserJson update(UserJson user) {
        UserEntity ue = UserEntity.fromJson(user);
        return UserJson.fromEntity(userdataUserRepositoryH.update(ue));
    }

    @Override
    public void delete(UserJson user) {
        UserEntity ue = UserEntity.fromJson(user);
        userdataUserRepositoryH.remove(ue);
    }

    @Override
    public List<UserJson> findAll() {
        return Collections.singletonList(UserJson.fromEntity((UserEntity) userdataUserRepositoryH.findAll()));

    }
}
