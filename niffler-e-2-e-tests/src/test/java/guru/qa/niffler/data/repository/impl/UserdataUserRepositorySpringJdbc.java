package guru.qa.niffler.data.repository.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.UserdataUserDao;
import guru.qa.niffler.data.dao.impl.UserdataUserDaoSpringJdbc;
import guru.qa.niffler.data.entity.userdata.UserEntity;
import guru.qa.niffler.data.mapper.UserdataUserEntityRowMapper;
import guru.qa.niffler.data.repository.UserdataUserRepository;
import guru.qa.niffler.data.tpl.DataSources;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class UserdataUserRepositorySpringJdbc implements UserdataUserRepository, ResultSetExtractor<List<UserEntity>> {
    private static final Config CFG = Config.getInstance();
    private static final String URL = CFG.userdataJdbcUrl();

    private final UserdataUserDao userdataUserDao = new UserdataUserDaoSpringJdbc();

    @Override
    public UserEntity create(UserEntity user) {
        return userdataUserDao.createUser(user);
    }

    @Override
    public UserEntity update(UserEntity user) {
        return userdataUserDao.updateUser(user);
    }

    @Override
    public Optional<UserEntity> findById(UUID id) {
        return userdataUserDao.findById(id);
    }

    @Override
    public void addIncomeInvitation(UserEntity requester, UserEntity addressee) {
        userdataUserDao.addIncomeInvitation(requester, addressee);
    }

    @Override
    public void addOutcomeInvitation(UserEntity requester, UserEntity addressee) {
        userdataUserDao.addOutcomeInvitation(requester, addressee);
    }

    @Override
    public void addFriend(UserEntity requester, UserEntity addressee) {
       userdataUserDao.addFriend(requester, addressee);
    }

    @Override
    public Optional<UserEntity> findByUsername(String username) {
        return userdataUserDao.findAllByUsername(username).stream().findFirst();
    }

    @Override
    public List<UserEntity> findAll() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSources.dataSource(URL));
        return jdbcTemplate.query(
                "SELECT * FROM \"user\"",
                this
        );
    }

    @Override
    public List<UserEntity> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<UUID, UserEntity> userMap = new ConcurrentHashMap<>();
        while (rs.next()) {
            UUID userId = rs.getObject("id", UUID.class);
            userMap.computeIfAbsent(userId, id -> {
                try {
                    UserEntity ue = UserdataUserEntityRowMapper.instance.mapRow(rs, 0);
                    if (ue != null) {
                        ue.setFriendshipRequests(new ArrayList<>());
                        ue.setFriendshipAddressees(new ArrayList<>());
                    }
                    return ue;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return new ArrayList<>(userMap.values());
    }

    @Override
    public void remove(UserEntity user) {
        userdataUserDao.delete(user);
    }
}
