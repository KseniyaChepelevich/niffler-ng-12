package guru.qa.niffler.data.repository;


import guru.qa.niffler.data.entity.userdata.UserEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserdataUserRepository {
    UserEntity create(UserEntity user);

    void delete(UUID id);


    Optional<UserEntity> findById(UUID id);

    void addIncomeInvitation(UserEntity requester, UserEntity addressee);
    void addOutcomeInvitation(UserEntity requester, UserEntity addressee);
    void addFriend(UserEntity requester, UserEntity addressee);

    List<UserEntity> findAllByUsername(String username);

    List<UserEntity> findAll();
}
