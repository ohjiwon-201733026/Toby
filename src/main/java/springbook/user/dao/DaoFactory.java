package springbook.user.dao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;

@Configuration
public class DaoFactory {

    @Bean
    public UserDaoJdbc userDao(){
        UserDaoJdbc userDao=new UserDaoJdbc();
        userDao.setDataSource(dataSource());
        return userDao;
    }

//    @Bean
//    public ConnectionMaker connectionMaker(){
//        return new SimpleConnectionMaker();
//    }

    @Bean
    public DataSource dataSource(){
        SimpleDriverDataSource dataSource=new SimpleDriverDataSource();

        dataSource.setDriverClass(com.mysql.cj.jdbc.Driver.class);
        dataSource.setUrl("jdbc:mysql://localhost/toby_spring?serverTimezone=UTC");
        dataSource.setUsername("root");
        dataSource.setPassword("test1234");

        return dataSource;
    }
}
