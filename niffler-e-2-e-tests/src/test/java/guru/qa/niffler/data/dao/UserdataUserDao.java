package guru.qa.niffler.data.dao;

import guru.qa.niffler.data.entity.UserdataUserEntity;


import java.util.Optional;
import java.util.UUID;

public interface UserdataUserDao {
    UserdataUserEntity createUser(UserdataUserEntity user);

    void delete(UUID id);


    Optional<UserdataUserEntity> findById(UUID id);

    Optional<UserdataUserEntity> findByUsername(String username);
}
