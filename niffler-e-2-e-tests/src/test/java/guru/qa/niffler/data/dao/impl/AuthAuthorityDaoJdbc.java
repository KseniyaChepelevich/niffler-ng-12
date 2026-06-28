package guru.qa.niffler.data.dao.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.AuthAuthorityDao;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.Authority;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;

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
    public void create(AuthorityEntity... authorities) {
        if (authorities == null || authorities.length == 0) {
            return;
        }

        try (PreparedStatement ps = holder(CFG.authJdbcUrl()).connection().prepareStatement(
                "INSERT INTO \"authority\" (\"authority\", user_id)" +
                        "VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS
        )) {
            for (AuthorityEntity authority : authorities) {
                ps.setString(1, authority.getAuthority().name());
                ps.setObject(2, authority.getUser().getId());

                ps.addBatch();
            }
            ps.executeBatch();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                int i = 0;
                while (rs.next() && i < authorities.length) {
                    UUID generatedKey = rs.getObject(1, UUID.class);
                    authorities[i].setId(generatedKey);
                    i++;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<AuthorityEntity> findAll() {
        List<AuthorityEntity> listAuthorities = new ArrayList<>();
        try (PreparedStatement ps = holder(CFG.authJdbcUrl()).connection().prepareStatement(
                "SELECT * FROM \"authority\""
        )) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AuthorityEntity authority = new AuthorityEntity();
                    authority.setId(rs.getObject("id", UUID.class));
                    authority.setAuthority(Authority.valueOf(rs.getString("authority")));
                    UUID userId = rs.getObject("user_id", UUID.class);
                    AuthUserEntity userStub = new AuthUserEntity();
                    userStub.setId(userId);
                    authority.setUser(userStub);

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
    public List<AuthorityEntity> findAuthorityByUserId(UUID userId) {
        List<AuthorityEntity> userAuthorities = new ArrayList<>();
        try (PreparedStatement ps = holder(CFG.authJdbcUrl()).connection().prepareStatement(
                "SELECT * FROM \"authority\" WHERE user_id = ?"
        )) {
            ps.setObject(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AuthorityEntity authority = new AuthorityEntity();
                    authority.setId(rs.getObject("id", UUID.class));
                    authority.setAuthority(Authority.valueOf(rs.getString("authority")));
                    AuthUserEntity user = new AuthUserEntity();
                    user.setId(userId);
                    authority.setUser(user);

                    userAuthorities.add(authority);
                }
            }
            return userAuthorities;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
