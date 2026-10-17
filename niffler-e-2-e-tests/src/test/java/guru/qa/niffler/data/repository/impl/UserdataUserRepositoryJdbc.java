package guru.qa.niffler.data.repository.impl;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.UserdataUserDao;
import guru.qa.niffler.data.dao.impl.UserdataUserDaoJdbc;
import guru.qa.niffler.data.entity.userdata.UserEntity;
import guru.qa.niffler.data.repository.UserdataUserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserdataUserRepositoryJdbc implements UserdataUserRepository {

    private static final Config CFG = Config.getInstance();
    private static final String URL = CFG.userdataJdbcUrl();

    private final UserdataUserDao userdataUserDao = new UserdataUserDaoJdbc();

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
        return userdataUserDao.findAll();
    }

    @Override
    public void remove(UserEntity user) {
        userdataUserDao.delete(user);
    }
}
