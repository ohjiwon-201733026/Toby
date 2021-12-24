package springbook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import com.mysql.jdbc.Driver;
import org.springframework.mail.MailSender;
import org.springframework.transaction.PlatformTransactionManager;
import springbook.user.dao.UserDao;
import springbook.user.dao.UserDaoJdbc;
import springbook.user.service.DummyMailSender;
import springbook.user.service.UserService;
import springbook.user.service.UserServiceImpl;
import springbook.user.service.UserServiceTest;
import springbook.user.sqlService.BaseSqlService;
import springbook.user.sqlService.DefaultSqlService;
import springbook.user.sqlService.SqlRegistry;
import springbook.user.sqlService.SqlService;

import static springbook.user.service.UserServiceTest.*;

@Configuration
//@ImportResource("/test-applicationContext.xml")
public class TestApplicationContext {

    @Autowired
    SqlService sqlService;

    @Bean
    public DataSource dataSource(){
        SimpleDriverDataSource dataSource=new SimpleDriverDataSource();

        dataSource.setDriverClass(Driver.class);
        dataSource.setUrl("jdbc:mysql://localhost/toby_spring?serverTimezone=UTC");
        dataSource.setUsername("root");
        dataSource.setPassword("test1234");

        return dataSource;
    }

    @Bean
    public PlatformTransactionManager transactionManager(){
        DataSourceTransactionManager tm=new DataSourceTransactionManager();
        tm.setDataSource(dataSource());
        return tm;
    }

    @Bean
    public UserDao userDao(){
        UserDaoJdbc dao= new UserDaoJdbc();
        dao.setDataSource(dataSource());
        dao.setSqlService(this.sqlService);
        return dao;
    }

    @Bean
    public UserService userService(){
        UserServiceImpl service=new UserServiceImpl();
        service.setUserDao(userDao());
        service.setMailSender(mailSender());
        return service;
    }

    @Bean
    public UserService testUserService(){
        TestUserService testService=new TestUserService();
        testService.setUserDao(userDao());
        testService.setMailSender(mailSender());
        return testService;
    }

    @Bean
    public MailSender mailSender(){
        return new DummyMailSender();
    }

    @Bean
    public SqlService sqlService(){
        DefaultSqlService sqlService=new DefaultSqlService();
        return sqlService;
    }


}
