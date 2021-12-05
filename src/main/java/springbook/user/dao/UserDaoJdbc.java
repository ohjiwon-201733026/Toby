package springbook.user.dao;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.SimpleConnectionHandle;
import springbook.user.domain.Level;
import springbook.user.domain.User;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

public  class UserDaoJdbc implements UserDao {
    private RowMapper<User> userMapper=
            new RowMapper<User>() {
                public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                    User user=new User();
                    user.setId(rs.getString("id"));
                    user.setName(rs.getString("name"));
                    user.setPassword(rs.getString("password"));
                    user.setLevel(Level.valueOf(rs.getInt("level")));
                    user.setLogin(rs.getInt("login"));
                    user.setRecommend(rs.getInt("recommend"));
                    user.setEmail(rs.getString("email"));
                    return user;
                }
            };

     private JdbcTemplate jdbcTemplate;

     /**
      * setter 주입
      */
     public void setDataSource(DataSource dataSource) {
         this.jdbcTemplate = new JdbcTemplate(dataSource);
     }


     public void add(final User user)  {

         this.jdbcTemplate.update(
                 "insert into users(id,name,password,level,login,recommend,email)"+
                         "values(?,?,?,?,?,?,?)",
                 user.getId(), user.getName(), user.getPassword(),user.getLevel().intValue(),user.getLogin(),user.getRecommend(),user.getEmail());

     }

     public void deleteAll()  {
//        this.jdbcContext.executeSql("delete from users");
         this.jdbcTemplate.update("delete from users");
     }


     public User get(String id)  {
         return this.jdbcTemplate.queryForObject("select * from users where id=?",
                 new Object[]{id}, this.userMapper);
     }


    public int getCount ()  {
        return this.jdbcTemplate.queryForObject("select count(*) from users",Integer.class);
    }

    @Override
    public void update(User user) {
        this.jdbcTemplate.update(
                "update users set name=?,password=?,level=?,login=?,"+
                        "recommend=?,email=? where id=?",user.getName(),user.getPassword(),
                user.getLevel().intValue(),user.getLogin(),user.getRecommend(),user.getEmail(),user.getId()
        );
    }

    public List<User> getAll(){
         return this.jdbcTemplate.query("select * from users order by id", this.userMapper);

    }


 }