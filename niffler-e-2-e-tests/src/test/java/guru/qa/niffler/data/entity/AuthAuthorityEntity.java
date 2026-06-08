package guru.qa.niffler.data.entity;

import guru.qa.niffler.model.AuthAuthorityJson;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
public class AuthAuthorityEntity implements Serializable {
  private UUID id;
  private Authority authority;
  private AuthUserEntity user;

    public static AuthAuthorityEntity fromJson(AuthAuthorityJson json) {
      AuthAuthorityEntity authorityEntity = new AuthAuthorityEntity();
      authorityEntity.setId(json.id());
      authorityEntity.setAuthority(json.authority());
      AuthUserEntity authUserEntity = new AuthUserEntity();
      authUserEntity.setId(json.user());
      authorityEntity.setUser(authUserEntity);
      return authorityEntity;
    }
}
