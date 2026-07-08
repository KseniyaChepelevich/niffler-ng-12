package guru.qa.niffler.data.dao.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.AuthAuthorityDao;
import guru.qa.niffler.data.entity.AuthAuthorityEntity;
import guru.qa.niffler.data.entity.Authority;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static guru.qa.niffler.data.tpl.Connections.holder;

public class AuthAuthorityDaoJdbc implements AuthAuthorityDao {

    private static final Config CFG = Config.getInstance();

    @Override
    public void create(AuthAuthorityEntity... authorities) {
        if (authorities == null || authorities.length == 0) {
            return;
        }

        try (PreparedStatement ps = holder(CFG.authJdbcUrl()).connection().prepareStatement(
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
        try (PreparedStatement ps = holder(CFG.authJdbcUrl()).connection().prepareStatement(
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

    @Override
    public List<AuthAuthorityEntity> findAllByUserId(UUID id) {
        List<AuthAuthorityEntity> listAuthorities = new ArrayList<>();
        try (PreparedStatement ps = holder(CFG.authJdbcUrl()).connection().prepareStatement(
                "SELECT * FROM \"authority\" WHERE user_id = ?"
        )) {
            ps.setObject(1, id);
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
