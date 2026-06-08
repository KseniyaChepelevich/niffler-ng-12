package guru.qa.niffler.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import guru.qa.niffler.data.entity.*;

import java.util.UUID;

public record AuthAuthorityJson(
        @JsonProperty("id")
        UUID id,
        @JsonProperty("authority")
        Authority authority,
        @JsonProperty("user_id")
        UUID user
) {
    public static AuthAuthorityJson fromEntity(AuthAuthorityEntity entity) {
        final AuthUserEntity user = entity.getUser();
        final UUID userId = entity.getUser().getId();

        return new AuthAuthorityJson(
                entity.getId(),
                entity.getAuthority(),
                entity.getUser().getId()
        );
    }
}
