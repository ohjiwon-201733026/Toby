package springbook.learningtest.spring.embeddeddb;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


public class EmbeddedDbTest {
//
//
//    EmbeddedDatabase db;
//    JdbcTemplate jdbcTemplate;
//
//    @Before
//    public void setUp() {
//        db = new EmbeddedDatabaseBuilder()
//                .setType(EmbeddedDatabaseType.HSQL)
//                .addScript("classpath:/springbook/learningtest/spring/embeddeddb/schema.sql")
//                .addScript("classpath:/springbook/learningtest/spring/embeddeddb/data.sql")
//                .build();
//        jdbcTemplate = new JdbcTemplate(db);
//    }
//
//    @After
//    public void tearDown() {
//        db.shutdown();
//    }
//
//    @Test
//    public void initData() {
//        Integer sqlmapCount = jdbcTemplate.queryForObject("select count(*) from sqlmap", Integer.class);
//        assertThat(sqlmapCount,is(2));
//        List<Map<String, Object>> list = jdbcTemplate.queryForList("select * from sqlmap order by key_");
//        Map<String, Object> firstObject = list.get(0);
//        assertThat(firstObject.get("key_"),is("KEY1"));
//        assertThat(firstObject.get("sql_"),is("SQL1"));
//        Map<String, Object> secondObject = list.get(1);
//        assertThat(secondObject.get("key_"),is("KEY2"));
//        assertThat(secondObject.get("sql_"),is("SQL2"));
//    }
//
//    @Test
//    public void insert() {
//        jdbcTemplate.update("insert into sqlmap(key_, sql_) values(?, ?)", "KEY3", "SQL3");
//        Integer sqlmapCount = jdbcTemplate.queryForObject("select count(*) from sqlmap", Integer.class);
//        assertThat(sqlmapCount,is(3));
//    }
}
