package guru.qa.niffler.data.dao;

import guru.qa.niffler.data.entity.auth.AuthUserEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuthUserDao {
    AuthUserEntity create(AuthUserEntity user);

    Optional<AuthUserEntity> findUserById(UUID id);

    void delete(AuthUserEntity user);

    AuthUserEntity update(AuthUserEntity user);

    Optional<AuthUserEntity> findAllByUsername(String username);

    List<AuthUserEntity> findAll();
}
