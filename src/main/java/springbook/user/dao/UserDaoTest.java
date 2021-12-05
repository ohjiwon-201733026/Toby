package springbook.user.dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import springbook.user.domain.Level;
import springbook.user.domain.User;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThrows;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/test-applicationContext.xml")
//@DirtiesContext
public class UserDaoTest {

    @Autowired UserDao dao;
    @Autowired DataSource dataSource;
    private User user1;
    private User user2;
    private User user3;

    @Before
    public void setUp(){
//        ApplicationContext context=new AnnotationConfigApplicationContext(DaoFactory.class);
//        ApplicationContext context=new GenericXmlApplicationContext("applicationContext.xml");
//        this.dao=context.getBean("userDao",UserDao.class);
//        this.dao=new UserDaoJdbc();
//        JdbcTemplate jdbcTemplate=new JdbcTemplate();
//        DataSource dataSource=new SingleConnectionDataSource(
//                "jdbc:mysql://localhost/testdb?serverTimezone=UTC","root","test1234",true
//        );
//        jdbcTemplate.setDataSource(dataSource);
//        dao.setDataSource(dataSource);
        this.user1=new User("1","1","test1", Level.BASIC,1,0,"1@a.com");
        this.user2=new User("2","2","test2",Level.SILVER,55,10,"2@b.com");
        this.user3=new User("3","3","test3",Level.GOLD,100,40,"3@c.com");
    }

    @Test
    public void addAndGet() throws SQLException, ClassNotFoundException {

        dao.deleteAll();
        assertThat(dao.getCount(),is(0));


        dao.add(user1);
        dao.add(user2);
        assertThat(dao.getCount(),is(2));

        User userGet1=dao.get(user1.getId());
        checkSameUser(userGet1,user1);

        User userGet2=dao.get(user2.getId());
        checkSameUser(userGet2,user2);

    }

    @Test
    public void count() throws SQLException, ClassNotFoundException {


        dao.deleteAll();
        assertThat(dao.getCount(),is(0));

        dao.add(user1);
        assertThat(dao.getCount(),is(1));

        dao.add(user2);
        assertThat(dao.getCount(),is(2));

        dao.add(user3);
        assertThat(dao.getCount(),is(3));
    }

    @Test(expected= EmptyResultDataAccessException.class)
    public void getUserFailure() throws SQLException, ClassNotFoundException {

        dao.deleteAll();
        assertThat(dao.getCount(),is(0));

        dao.get("unknown_id");
    }

    @Test
    public void getAll() throws SQLException, ClassNotFoundException {
        dao.deleteAll();

        List<User> user0=dao.getAll();
        assertThat(user0.size(),is(0));

        dao.add(user1);
        List<User> users1=dao.getAll();
        assertThat(users1.size(),is(1));
        checkSameUser(user1,users1.get(0));

        dao.add(user2);
        List<User> users2=dao.getAll();
        assertThat(users2.size(),is(2));
        checkSameUser(user1,users2.get(0));
        checkSameUser(user2,users2.get(1));

        dao.add(user3);
        List<User> users3=dao.getAll();
        assertThat(users3.size(),is(3));
        checkSameUser(user1,users3.get(0));
        checkSameUser(user2,users3.get(1));
        checkSameUser(user3,users3.get(2));

    }

    private void checkSameUser(User user1, User user2){
        assertThat(user1.getId(),is(user2.getId()));
        assertThat(user1.getName(),is(user2.getName()));
        assertThat(user1.getPassword(),is(user2.getPassword()));
        assertThat(user1.getLevel(),is(user2.getLevel()));
        assertThat(user1.getLogin(),is(user2.getLogin()));
        assertThat(user1.getRecommend(),is(user2.getRecommend()));
        assertThat(user1.getEmail(),is(user2.getEmail()));
    }

    @Test(expected = DuplicateKeyException.class)
    public void duplicateKey(){
        dao.deleteAll();

        dao.add(user1);
        dao.add(user1);
    }

    @Test
    public void sqlExceptionTranslate(){
        dao.deleteAll();

        try{
            dao.add(user1);
            dao.add(user1);
        }catch(DuplicateKeyException ex){
            SQLException sqlEx=(SQLException)ex.getRootCause();
            SQLExceptionTranslator set = new SQLErrorCodeSQLExceptionTranslator(this.dataSource);
            assertThat(set.translate(null,null,sqlEx).getClass(),is(DuplicateKeyException.class));
        }

    }

    @Test
    public void update(){
        dao.deleteAll();

        dao.add(user1); // 수정할 사용자
        dao.add(user2); // 수정하지 않을 사용자
        
        user1.setName("오민규");
        user1.setPassword("springno6");
        user1.setLevel(Level.GOLD);
        user1.setLogin(1000);
        user1.setRecommend(999);
        dao.update(user1);

        User user1update=dao.get(user1.getId());
        checkSameUser(user1,user1update);
        User user2same=dao.get(user2.getId());
        checkSameUser(user2,user2same);
    }

}
