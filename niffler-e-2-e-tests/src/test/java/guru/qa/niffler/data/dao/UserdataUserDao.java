package guru.qa.niffler.data.dao;

import guru.qa.niffler.data.entity.UserEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserdataUserDao {
    UserEntity createUser(UserEntity user);

    void delete(UUID id);


    Optional<UserEntity> findById(UUID id);

    Optional<UserEntity> findAllByUsername(String username);

    List<UserEntity> findAll();
}
