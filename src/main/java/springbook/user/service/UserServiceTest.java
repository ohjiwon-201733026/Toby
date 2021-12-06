package springbook.user.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import springbook.user.dao.UserDao;
import springbook.user.domain.Level;
import springbook.user.domain.User;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.*;
import static springbook.user.service.UserServiceImpl.MIN_LOGCOUNT_FOR_SILVER;
import static springbook.user.service.UserServiceImpl.MIN_RECCOMEND_FOR_GOLD;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/test-applicationContext.xml")
public class UserServiceTest {

    @Autowired UserDao userDao;
    @Autowired UserService userService;
    @Autowired UserServiceImpl userServiceImpl;
    @Autowired DataSource dataSource;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired MailSender mailSender;
    List<User> users;

     @Before
     public void setUp(){
         users= Arrays.asList(
                 new User("1","a","p1", Level.BASIC,MIN_LOGCOUNT_FOR_SILVER-1,0,"1@a.com"),
                 new User("2","b","p2", Level.BASIC,MIN_LOGCOUNT_FOR_SILVER,0,"2@b.com"),
                 new User("3","c","p3", Level.SILVER,60,MIN_RECCOMEND_FOR_GOLD-1,"3@c.com"),
                 new User("4","d","p4", Level.SILVER,60,MIN_RECCOMEND_FOR_GOLD,"4@d.com"),
                 new User("5","e","p5", Level.GOLD,100,Integer.MAX_VALUE,"5@e.com")
         );
     }

    @Test
    public void bean(){
        assertThat(this.userService,is(notNullValue()));
    }

     @Test
     @DirtiesContext // 컨텍스트와 DI 설정을 변경하는 테스트
     public void upgradeLevels() throws Exception{
         userDao.deleteAll();
         for(User user :users){
             userDao.add(user);
         }
         MockMailSender mockMailSender=new MockMailSender();
         userServiceImpl.setMailSender(mockMailSender);

         userService.upgradeLevels();

         checkLevelUpgrade(users.get(0),false);
         checkLevelUpgrade(users.get(1),true);
         checkLevelUpgrade(users.get(2),false);
         checkLevelUpgrade(users.get(3),true);
         checkLevelUpgrade(users.get(4),false);

         List<String> requests= mockMailSender.getRequest();
         assertThat(requests.size(),is(2));
         assertThat(requests.get(0),is(users.get(1).getEmail()));
         assertThat(requests.get(1),is(users.get(3).getEmail()));
     }
     public void checkLevelUpgrade(User user,boolean upgraded){
         User userUpdate=userDao.get(user.getId());
         if(upgraded){
             assertThat(userUpdate.getLevel(),is(user.getLevel().nextLevel()));
         }
         else{
             assertThat(userUpdate.getLevel(),is(user.getLevel()));
         }
     }

     public void checkLevel(User user, Level expectedLevel){
         User userUpdate=userDao.get(user.getId());
         assertThat(userUpdate.getLevel(),is(expectedLevel));
     }

     @Test
    public void add(){
         userDao.deleteAll();

         User userWithLevel=users.get(4); // GOLD 레벨이 이미 지정된 경우 레벨 초기화하지 않음
         User userWithoutLevel=users.get(0);
         userWithoutLevel.setLevel(null); // 레벨이 비어있는 사용자-> BASIC

         userService.add(userWithLevel);
         userService.add(userWithoutLevel);

         User userWithLevelRead=userDao.get(userWithLevel.getId());
         User userWithoutLevelRead=userDao.get(userWithoutLevel.getId());

         assertThat(userWithLevelRead.getLevel(),is(userWithLevel.getLevel()));
         assertThat(userWithoutLevelRead.getLevel(),is(Level.BASIC));
     }

    @Test
    public void upgradeAllOrNothing() throws Exception{
         UserServiceImpl.TestUserService testUserService=
                 new UserServiceImpl.TestUserService(users.get(3).getId());
         testUserService.setUserDao(this.userDao);
         testUserService.setMailSender(mailSender);
         
         // 트랜잭션 기능을 분리한 UserServiceTx는 예외 발생용으로
         // 수정할 필요가 없으니 그대로 사용
         UserServiceTx txUserService=new UserServiceTx();
         txUserService.setTransactionManager(transactionManager);
         txUserService.setUserService(testUserService);

         userDao.deleteAll();
        for (User user : users) {
            userDao.add(user);
        }

        try{
            // 트랜잭션 기능을 분리한 오브젝트를 통해 예외 발생용 TestUserService가 호출되게 함
            txUserService.upgradeLevels();
            fail("TestUserServiceException expected");
        }catch (UserServiceImpl.TestUserServiceException e){
        }

        checkLevelUpgrade(users.get(1),false);
    }

    static class MockMailSender implements MailSender{
         // UserService로부터 전송 요청을 받은 메일 주소를 저장해두고
         // 이를 읽을 수 있게 한다
         private List<String> request=new ArrayList<>();

         public List<String> getRequest(){
             return request;
         }

        @Override
        public void send(SimpleMailMessage mailMessage) throws MailException {
             // 전송 요청을 받은 이메일 주소를 저장해둔다.
             // 간단하게 첫번째 수신자 메일 주소만 저장
             request.add(mailMessage.getTo()[0]);
        }

        @Override
        public void send(SimpleMailMessage[] simpleMessages) throws MailException {

        }
    }

    static class MockUserDao implements UserDao{
         private List<User> users; // 레벨 업그레이드 후보 User 오브젝트 목록
         private List<User> updated=new ArrayList<>(); // 업그레이드 대상 오브젝트를 저장해 둘 목록

        private MockUserDao(List<User> users){
            this.users=users;
        }

        public List<User> getUpdated(){
            return this.updated;
        }

        @Override
        public void update(User user) {
            updated.add(user);
        }
        @Override
        public List<User> getAll() {
            return this.users;
        }
        // 테스트에 사용되지 않는 메소드
        @Override
        public void add(User user) {
            throw new UnsupportedOperationException();
        }

        @Override
        public User get(String id) {
            throw new UnsupportedOperationException();
        }


        @Override
        public void deleteAll() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getCount() {
            throw new UnsupportedOperationException();
        }


    }


}
