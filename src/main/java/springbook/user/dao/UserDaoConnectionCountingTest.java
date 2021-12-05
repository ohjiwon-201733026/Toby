package springbook.user.dao;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import springbook.user.domain.User;

import java.sql.SQLException;

public class UserDaoConnectionCountingTest {

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
//        AnnotationConfigApplicationContext context=
//                new AnnotationConfigApplicationContext(CountingDaoFactory.class);
//        UserDao dao=context.getBean("userDao",UserDao.class);
//
//        /**
//         * DAO 사용 코드
//         */
//        User user=new User();
//        user.setId("a");
//        user.setPassword("b");
//        user.setName("c");
//        dao.add(user);
//
//        CountingConnectionMaker ccm=context.getBean("connectionMaker",CountingConnectionMaker.class);
//        System.out.println("Connection counter : "+ccm.getCounter());
    }
}
