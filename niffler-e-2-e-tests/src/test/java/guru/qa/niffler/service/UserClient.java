package guru.qa.niffler.service;

import guru.qa.niffler.model.UserJson;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserClient {
    @Nonnull
    static UserClient getInstance() {
        return "api".equals(System.getProperty("client.impl"))
                ? new UsersApiClient()
                : new UsersDbClient();
    }

    UserJson createUser(String username, String password);

    Optional<UserJson> findById(UUID id);

    Optional<UserJson> findByUsername(String username);

    UserJson update(UserJson user);

    List<UserJson> createIncomeInvitation(UserJson targetUser, int count);

    List<UserJson> createOutcomeInvitation(UserJson targetUser, int count);

    List<UserJson> createFriends(UserJson targetUser, int count);

    void delete(UserJson user);

    List<UserJson> findAll();
}
