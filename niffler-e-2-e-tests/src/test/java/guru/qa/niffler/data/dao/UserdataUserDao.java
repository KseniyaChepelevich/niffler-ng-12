package guru.qa.niffler.data.dao;


import guru.qa.niffler.data.entity.userdata.UserEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserdataUserDao {
    UserEntity createUser(UserEntity user);

    void delete(UserEntity user);


    Optional<UserEntity> findById(UUID id);

    List<UserEntity> findAllByUsername(String username);

    List<UserEntity> findAll();

    UserEntity updateUser(UserEntity user);

    void addIncomeInvitation(UserEntity requester, UserEntity addressee);
    void addOutcomeInvitation(UserEntity requester, UserEntity addressee);
    void addFriend(UserEntity requester, UserEntity addressee);
}
