package guru.qa.niffler.service;

import guru.qa.niffler.model.UserJson;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserClient {
    UserJson createUser(String username, String password);
    Optional<UserJson> findById(UUID id);
    Optional<UserJson> findByUsername(String username);
    UserJson update(UserJson user);
    void createIncomeInvitation(UserJson targetUser, int count);
    void createOutcomeInvitation(UserJson targetUser, int count);
    void createFriends(UserJson targetUser, int count);

    void delete(UserJson user);

    List<UserJson> findAll();
}
