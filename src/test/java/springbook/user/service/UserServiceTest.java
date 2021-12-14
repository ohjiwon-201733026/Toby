package springbook.user.service;

import javassist.util.proxy.ProxyFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import springbook.learningtest.spring.pointcut.Bean;
import springbook.learningtest.spring.pointcut.Target;
import springbook.user.dao.UserDao;
import springbook.user.domain.Level;
import springbook.user.domain.User;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static springbook.user.service.UserServiceImpl.MIN_LOGCOUNT_FOR_SILVER;
import static springbook.user.service.UserServiceImpl.MIN_RECCOMEND_FOR_GOLD;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/test-applicationContext.xml")
@Transactional
public class UserServiceTest {

    @Autowired UserDao userDao;
    @Autowired UserService userService;
    @Autowired UserService testUserService;
    @Autowired PlatformTransactionManager transactionManager;
//    @Autowired UserServiceImpl userServiceImpl;
//    @Autowired DataSource dataSource;
//    @Autowired PlatformTransactionManager transactionManager;
    @Autowired MailSender mailSender;
    @Autowired ApplicationContext context; // 팩토리 빈을 가져오려면 애플리케이션 컨텍스트 필요
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
//     @DirtiesContext // 컨텍스트와 DI 설정을 변경하는 테스트
    public void upgradeLevels() throws Exception{
         /*
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
        */
        // 고립된 테스트에서는 테스트 대상 오브젝트를 직접 생성
        UserServiceImpl userServiceImpl=new UserServiceImpl();

        // 목 오브젝트로 만든 UserDao를 직접 DI
        MockUserDao mockUserDao=new MockUserDao(this.users);
        userServiceImpl.setUserDao(mockUserDao);

        MockMailSender mockMailSender=new MockMailSender();
        userServiceImpl.setMailSender(mockMailSender);

        userServiceImpl.upgradeLevels();

        List<User> updated=mockUserDao.getUpdated(); // MockUserDao로부터 업데이트 결과 가져옴
        assertThat(updated.size(),is(2));
        checkUserAndLevel(updated.get(0),"2",Level.SILVER);
        checkUserAndLevel(updated.get(1),"4",Level.GOLD);

        List<String> request=mockMailSender.getRequest();
        assertThat(request.size(),is(2));
        assertThat(request.get(0),is(users.get(1).getEmail()));
        assertThat(request.get(1),is(users.get(3).getEmail()));

    }

    private void checkUserAndLevel(User updated, String expectedId, Level expectedLevel) {
        assertThat(updated.getId(),is(expectedId));
        assertThat(updated.getLevel(),is(expectedLevel));
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
    public void mockUpgradeLevels() throws Exception{
        UserServiceImpl userServiceImpl=new UserServiceImpl();
        
        // 다이내믹한 목 오브젝트 생성과 메소드의 리턴 값 설정, DI까지
        UserDao mockUserDao=mock(UserDao.class);
        when(mockUserDao.getAll()).thenReturn(this.users);
        userServiceImpl.setUserDao(mockUserDao);

        // 리턴 값이 없는 메소드를 가진 목 오브젝트는 더욱 간단히 만들 수 있다
        MailSender mockMailSender=mock(MailSender.class);
        userServiceImpl.setMailSender(mockMailSender);

        userServiceImpl.upgradeLevels();

        // 목 오브젝트가 제공하는 검증 기능을 통해서
        // 어떤 메소드가 몇 번 호출됐는지, 파라미터는 무엇인지 확인할 수 있다
        verify(mockUserDao,times(2)).update(any(User.class));
        verify(mockUserDao,times(2)).update(any(User.class));
        verify(mockUserDao).update(users.get(1));
        assertThat(users.get(1).getLevel(),is(Level.SILVER));
        verify(mockUserDao).update(users.get(3));
        assertThat(users.get(3).getLevel(),is(Level.GOLD));

        ArgumentCaptor<SimpleMailMessage> mailMessageArg=
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mockMailSender,times(2)).send(mailMessageArg.capture());
        List<SimpleMailMessage> mailMessages=mailMessageArg.getAllValues();
        assertThat(mailMessages.get(0).getTo()[0],is(users.get(1).getEmail()));
        assertThat(mailMessages.get(1).getTo()[0],is(users.get(3).getEmail()));

        
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

        userDao.deleteAll();
        for (User user : users) {
            userDao.add(user);
        }

        try{
            // 트랜잭션 기능을 분리한 오브젝트를 통해 예외 발생용 TestUserService가 호출되게 함
            this.testUserService.upgradeLevels();
            fail("TestUserServiceException expected");
        }catch (UserServiceImpl.TestUserServiceException e){
        }

        checkLevelUpgrade(users.get(1),false);
    }

//    @Test
//    public void advisorAutoProxyCreator(){
//        assertThat(testUserService,is(java.lang.reflect.Proxy.class));
//    }

    @Test
    public void methodSignaturePointcut() throws SecurityException,NoSuchMethodException{
        AspectJExpressionPointcut pointcut=new AspectJExpressionPointcut();
        pointcut.setExpression("execution(public int "+
                "springbook.learningtest.spring.pointcut.Target.minus(int,int) "+
                "throws java.lang.RuntimeException)");

        // Target.minus()
        assertThat(pointcut.getClassFilter().matches(Target.class) &&
                pointcut.getMethodMatcher().matches(
                        Target.class.getMethod("minus",int.class,int.class),null)
                ,is(true));

        // Target.plus()
        assertThat(pointcut.getClassFilter().matches(Target.class) &&
                        pointcut.getMethodMatcher().matches(
                                Target.class.getMethod("plus",int.class,int.class),null)
                ,is(false));

        // Bean.method()
        assertThat(pointcut.getClassFilter().matches(Bean.class) &&
                pointcut.getMethodMatcher().matches(
                        Target.class.getMethod("method"),null)
                ,is(false));
    }

    @Test
    public void pointcut() throws Exception{
        targetClassPointcutMatches("execution(* *(..))",true,true,true,true,true,true);
    }

    @Test(expected = TransientDataAccessResourceException.class)
    public void readOnlyTransactionAttribute(){
        testUserService.getAll();
    }

    @Test
    public void transactionSync() {

            userService.deleteAll();
            userService.add(users.get(0));
            userService.add(users.get(1));

    }

    // 포인트컷과 메소드를 비교해주는 테스트 헬퍼 메소드
    public void pointcutMatches(String expression,Boolean expected, Class<?> clazz,
                                String methodName, Class<?>... args) throws Exception{
        AspectJExpressionPointcut pointcut=new AspectJExpressionPointcut();
        pointcut.setExpression(expression);

        assertThat(pointcut.getClassFilter().matches(clazz)
        && pointcut.getMethodMatcher().matches(clazz.getMethod(methodName,
                args),null),is(expected));
    }

    public void targetClassPointcutMatches(String expression,boolean...expected)throws Exception{
        pointcutMatches(expression,expected[0],Target.class,"hello");
        pointcutMatches(expression,expected[1],Target.class,"hello",String.class);
        pointcutMatches(expression,expected[2],Target.class,"plus",int.class,int.class);
        pointcutMatches(expression,expected[3],Target.class,"minus",int.class,int.class);
        pointcutMatches(expression,expected[4],Target.class,"method");
        pointcutMatches(expression,expected[5],Bean.class,"method");
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

    static class TestUserService extends UserServiceImpl{
        private String id="4";

        protected void upgradeLevel(User user){
            if(user.getId().equals(this.id))
                throw new TestUserServiceException();
            super.upgradeLevel(user);
        }

        public List<User> getAll(){
            for (User user : super.getAll()) {
                super.update(user); // 강제로 쓰기 시도 -> 예외 발생
            }
            return null;
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

