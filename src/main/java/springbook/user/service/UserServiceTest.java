package springbook.user.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import springbook.user.dao.UserDao;
import springbook.user.domain.Level;
import springbook.user.domain.User;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.*;
import static springbook.user.service.UserService.MIN_LOGCOUNT_FOR_SILVER;
import static springbook.user.service.UserService.MIN_RECCOMEND_FOR_GOLD;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/test-applicationContext.xml")
public class UserServiceTest {

    @Autowired
    UserDao userDao;
    @Autowired UserService userService;
    @Autowired
    DataSource dataSource;
    @Autowired
    PlatformTransactionManager transactionManager;
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
     public void upgradeLevels() throws Exception{
         userDao.deleteAll();
         for(User user :users){
             userDao.add(user);
         }

         userService.upgradeLevels();

         checkLevelUpgrade(users.get(0),false);
         checkLevelUpgrade(users.get(1),true);
         checkLevelUpgrade(users.get(2),false);
         checkLevelUpgrade(users.get(3),true);
         checkLevelUpgrade(users.get(4),false);
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
         UserService testUserService=new UserService.TestUserService(users.get(3).getId());
         testUserService.setUserDao(this.userDao);
         testUserService.setTransactionManager(transactionManager);
         userDao.deleteAll();
        for (User user : users) {
            userDao.add(user);
        }

        try{
            testUserService.upgradeLevels();
            fail("TestUserServiceException expected");
        }catch (UserService.TestUserServiceException e){
        }

        checkLevelUpgrade(users.get(1),false);
    }


}
