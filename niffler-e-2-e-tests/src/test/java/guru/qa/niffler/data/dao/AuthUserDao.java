package guru.qa.niffler.data.dao;

import guru.qa.niffler.data.entity.AuthUserEntity;
import java.util.Optional;
import java.util.UUID;

public interface AuthUserDao {
    AuthUserEntity create(AuthUserEntity user);

    Optional<AuthUserEntity> findUserById(UUID id);

    void delete(UUID id);

    AuthUserEntity update(AuthUserEntity user);

    Optional<AuthUserEntity> findByUsername(String username);
}
