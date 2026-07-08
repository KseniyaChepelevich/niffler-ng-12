package guru.qa.niffler.data.dao;

import guru.qa.niffler.data.entity.AuthAuthorityEntity;

import java.util.List;
import java.util.UUID;


public interface AuthAuthorityDao {
    void create(AuthAuthorityEntity... authorities);

    List<AuthAuthorityEntity> findAll();

    List<AuthAuthorityEntity> findAllByUserId(UUID id);

}
