package guru.qa.niffler.data.dao.impl;

import guru.qa.niffler.data.dao.AuthAuthorityDao;
import guru.qa.niffler.data.entity.AuthAuthorityEntity;
import guru.qa.niffler.data.entity.Authority;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AuthAuthorityDaoJdbc implements AuthAuthorityDao {

    private final Connection connection;

    public AuthAuthorityDaoJdbc(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void create(AuthAuthorityEntity... authorities) {
        if (authorities == null || authorities.length == 0) {
            return;
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO \"authority\" (\"authority\", user_id)" +
                        "VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS
        )) {
            for (AuthAuthorityEntity authority : authorities) {
                ps.setString(1, authority.getAuthority().name());
                ps.setObject(2, authority.getUserId());

                ps.executeUpdate();

                final UUID generatedKey;
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedKey = rs.getObject("id", UUID.class);
                    } else {
                        throw new SQLException("Can't find id in ResultSet");
                    }
                }
                authority.setId(generatedKey);

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<AuthAuthorityEntity> findAll() {
        List<AuthAuthorityEntity> listAuthorities = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM \"authority\""
        )) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AuthAuthorityEntity authority = new AuthAuthorityEntity();
                    authority.setId(rs.getObject("id", UUID.class));
                    authority.setAuthority(Authority.valueOf(rs.getString("authority")));
                    authority.setUserId(rs.getObject("user_id", UUID.class));

                    listAuthorities.add(authority);
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            return listAuthorities;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
