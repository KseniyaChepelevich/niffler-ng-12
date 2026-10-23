package guru.qa.niffler.service;

import guru.qa.niffler.model.UserJson;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UsersApiClient implements UserClient{

    @Override
    public UserJson createUser(String username, String password) {
        return null;
    }

    @Override
    public Optional<UserJson> findById(UUID id) {
        return Optional.empty();
    }

    @Override
    public Optional<UserJson> findByUsername(String username) {
        return Optional.empty();
    }

    @Override
    public UserJson update(UserJson user) {
        return null;
    }

    @Override
    public List<UserJson> createIncomeInvitation(UserJson targetUser, int count) {
        return List.of();
    }

    @Override
    public List<UserJson> createOutcomeInvitation(UserJson targetUser, int count) {
        return List.of();
    }

    @Override
    public List<UserJson> createFriends(UserJson targetUser, int count) {
        return List.of();
    }

    @Override
    public void delete(UserJson user) {

    }

    @Override
    public List<UserJson> findAll() {
        return List.of();
    }
}
