package springbook.user.service;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import springbook.user.dao.UserDao;
import springbook.user.domain.Level;
import springbook.user.domain.User;


import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.sql.DataSource;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.util.List;
import java.util.Properties;

public class UserServiceImpl implements UserService{

    UserDao userDao;
    private MailSender mailSender;

    public static final int MIN_LOGCOUNT_FOR_SILVER=50;
    public static final int MIN_RECCOMEND_FOR_GOLD=30;

    public void setUserDao(UserDao userDao){
        this.userDao=userDao;
    }

    public void setMailSender(MailSender mailSender){
        this.mailSender=mailSender;
    }


    public void upgradeLevels() {
        List<User> users=userDao.getAll();
        for (User user : users) {
            if (canUpgradeLevel(user)) {
                upgradeLevel(user);
            }
        }
    }


    private boolean canUpgradeLevel(User user){
        Level currentLevel=user.getLevel();
        switch (currentLevel){
            case BASIC: return (user.getLogin()>=MIN_LOGCOUNT_FOR_SILVER);
            case SILVER: return (user.getRecommend()>=MIN_RECCOMEND_FOR_GOLD);
            case GOLD: return false;
            default: throw new IllegalArgumentException("Unknown Level: "+currentLevel);
        }
    }

    protected void upgradeLevel(User user){
        user.upgradeLevel();
        userDao.update(user);
        sendUpgradeEmail(user);
    }

    private void sendUpgradeEmail(User user){
         //MailMessage ?????????????????? ?????? ????????? ??????????????? ????????? ?????? ?????? ??????
        SimpleMailMessage mailMessage=new SimpleMailMessage();
        mailMessage.setTo(user.getEmail());
        mailMessage.setFrom("useradmin@ksug.org");
        mailMessage.setSubject("Upgrade ??????");
        mailMessage.setText("??????????????? ????????? "+user.getLevel().name());

        this.mailSender.send(mailMessage);
    }

    public void add(User user){
        if(user.getLevel()==null) user.setLevel(Level.BASIC);
        userDao.add(user);
    }

    @Override
    @Transactional
    public User get(String id) {
        return userDao.get(id);
    }

    @Override
    public List<User> getAll() {
        return userDao.getAll();
    }

    @Override
    public void deleteAll() {
        userDao.deleteAll();
    }

    @Override
    public void update(User user) {
        userDao.update(user);
    }

    static class TestUserService extends UserServiceImpl {
        private String id;
        
        public TestUserService(String id){
            this.id=id;
        }
        
        protected void upgradeLevel(User user){
            if(user.getId().equals(this.id)) throw new TestUserServiceException();
            // ????????? id??? User ??????????????? ???????????? ????????? ????????? ????????? ?????? ??????
            super.upgradeLevel(user);
        }


    }

    static class TestUserServiceException extends RuntimeException{

    }
}
