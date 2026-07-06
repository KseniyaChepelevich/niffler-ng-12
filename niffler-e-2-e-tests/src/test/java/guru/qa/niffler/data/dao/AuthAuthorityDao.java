package guru.qa.niffler.data.dao;

import guru.qa.niffler.data.entity.AuthAuthorityEntity;
import guru.qa.niffler.data.entity.AuthUserEntity;

import java.util.List;


public interface AuthAuthorityDao {
    void create(AuthAuthorityEntity... authorities);
    List<AuthAuthorityEntity> findAll();

}
