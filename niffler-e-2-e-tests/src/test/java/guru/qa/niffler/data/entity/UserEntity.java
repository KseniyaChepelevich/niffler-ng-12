package guru.qa.niffler.data.entity;

import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.model.UserdataUserJson;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Getter
@Setter
public class UserEntity implements Serializable {
  private UUID id;
  private String username;
  private CurrencyValues currency;
  private String firstname;
  private String surname;
  private String fullname;
  private byte[] photo;
  private byte[] photoSmall;

  public static UserdataUserEntity fromJson(UserdataUserJson json) {
    UserdataUserEntity ue = new UserdataUserEntity();
    ue.setId(json.id());
    ue.setUsername(json.username());
    ue.setCurrency(json.currency());
    ue.setFirstname(json.firstname());
    ue.setSurname(json.surname());
    ue.setFullname(json.fullname());
    ue.setPhoto(json.photo() != null ? json.photo().getBytes(StandardCharsets.UTF_8) : null);
    ue.setPhotoSmall(json.photoSmall() != null ? json.photoSmall().getBytes(StandardCharsets.UTF_8) : null);
    return ue;
  }

}