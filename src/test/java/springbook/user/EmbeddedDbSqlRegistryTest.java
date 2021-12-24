package springbook.user;

import org.junit.After;
import org.junit.Test;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import springbook.user.sqlService.EmbeddedDbSqlRegistry;
import springbook.user.sqlService.SqlUpdateFailureException;
import springbook.user.sqlService.UpdatableSqlRegistry;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.fail;
import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.HSQL;


public class EmbeddedDbSqlRegistryTest
//        extends AbstractUpdatableSqlRegistryTest
{
//    @Override
//    protected UpdatableSqlRegistry createUpdatableSqlRegistry() {
//        return null;
//    }
//    EmbeddedDatabase db;
//    @Override
//    protected UpdatableSqlRegistry createUpdatableSqlRegistry() {
//        db=new EmbeddedDatabaseBuilder()
//                .setType(HSQL).addScript(
//                        "classpath:springbook/learningtest/spring/embeddeddb/schema.sql")
//                .build();
//
//
//        EmbeddedDbSqlRegistry embeddedDbSqlRegistry=new EmbeddedDbSqlRegistry();
//        embeddedDbSqlRegistry.setDataSource(db);
//        return embeddedDbSqlRegistry;
//    }
//
//    @After
//    public void tearDown(){
//        db.shutdown();
//    }
//
//    @Test
//    public void transactionalUpdate(){
//        checkFindResult("SQL1","SQL2","SQL3");
//
//        Map<String,String> sqlmap=new HashMap<>();
//        sqlmap.put("KEY1","Modified1");
//        sqlmap.put("KEY9999!@#$","Modified9999");
//
//        try{
//            sqlRegistry.updateSql(sqlmap);
//            fail();
//        }
//        catch (SqlUpdateFailureException e){}
//
//        checkFindResult("SQL1","SQL2","SQL3");
//    }
}
