package springbook.user.service;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
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

public class UserService {

    UserDao userDao;
//    private DataSource dataSource;
    private MailSender mailSender;
    private PlatformTransactionManager transactionManager;

    public static final int MIN_LOGCOUNT_FOR_SILVER=50;
    public static final int MIN_RECCOMEND_FOR_GOLD=30;

    public void setUserDao(UserDao userDao){
        this.userDao=userDao;
    }
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }
    public void setMailSender(MailSender mailSender){
        this.mailSender=mailSender;
    }


    public void upgradeLevels() throws Exception{

        TransactionStatus status=this.transactionManager.getTransaction(new DefaultTransactionDefinition());

        try{
            List<User> users=userDao.getAll();
            for (User user : users) {
                if(canUpgradeLevel(user)){
                    upgradeLevel(user);
                }
            }
            this.transactionManager.commit(status);
        }catch (Exception e){
            this.transactionManager.rollback(status);
            throw e;
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
        /*
        Properties props=new Properties();
        props.put("mail.smtp.host","mail.ksug.org");
        Session s= Session.getInstance(props,null);

        MimeMessage message =new MimeMessage(s);
        try{
            message.setFrom(new InternetAddress("useradmin@ksug.org"));
            message.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(user.getEmail()));
            message.setSubject("Upgrade 안내");
            message.setText("사용자님의 등급이 "+user.getLevel().name()+"로 업그레이드 되었습니다.");

            Transport.send(message);
        } catch (AddressException e) {
            throw new RuntimeException(e);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        */


         //MailMessage 인터페이스의 구현 클래스 오브젝트를 만들어 메일 내용 작성
        SimpleMailMessage mailMessage=new SimpleMailMessage();
        mailMessage.setTo(user.getEmail());
        mailMessage.setFrom("useradmin@ksug.org");
        mailMessage.setSubject("Upgrade 안내");
        mailMessage.setText("사용자님의 등급이 "+user.getLevel().name());

        this.mailSender.send(mailMessage);
    }

    public void add(User user){
        if(user.getLevel()==null) user.setLevel(Level.BASIC);
        userDao.add(user);
    }
    
    static class TestUserService extends UserService{
        private String id;
        
        public TestUserService(String id){
            this.id=id;
        }
        
        protected void upgradeLevel(User user){
            if(user.getId().equals(this.id)) throw new TestUserServiceException();
            // 지정된 id의 User 오브젝트가 발견되면 예외를 던져서 작업을 강제 중단
            super.upgradeLevel(user);
        }
    }

    static class TestUserServiceException extends RuntimeException{

    }
}
