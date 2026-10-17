package guru.qa.niffler.data.mapper;

import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.Authority;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class AuthAuthorityEntityRowMapper implements RowMapper<AuthorityEntity> {



    public static final RowMapper<AuthorityEntity> instance = new AuthAuthorityEntityRowMapper();

    public AuthAuthorityEntityRowMapper() {
    }

    @Override
    public AuthorityEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        AuthorityEntity result = new AuthorityEntity();
        result.setId(rs.getObject("id", UUID.class));
        UUID userId = rs.getObject("user_id", UUID.class);
        AuthUserEntity userStub = new AuthUserEntity();
        userStub.setId(userId);
        result.setUser(userStub);
        result.setAuthority(Authority.valueOf(rs.getString("authority")));
        return result;
    }
}
