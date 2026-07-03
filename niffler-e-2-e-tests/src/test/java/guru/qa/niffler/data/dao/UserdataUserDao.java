package guru.qa.niffler.data.dao;

import guru.qa.niffler.data.entity.UserEntity;


import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public interface UserdataUserDao {
    UserEntity createUser(UserEntity user) throws SQLException;

    void delete(UUID id) throws SQLException;


    Optional<UserEntity> findById(UUID id) throws SQLException;

    Optional<UserEntity> findByUsername(String username) throws SQLException;
}
