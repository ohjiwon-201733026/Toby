package springbook.user;

import org.junit.Before;
import org.junit.Test;
import springbook.user.sqlService.SqlNotFoundException;
import springbook.user.sqlService.SqlUpdateFailureException;
import springbook.user.sqlService.UpdatableSqlRegistry;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public abstract class AbstractUpdatableSqlRegistryTest {

    UpdatableSqlRegistry sqlRegistry;

    @Before
    public void setUp(){
        sqlRegistry=createUpdatableSqlRegistry();
        sqlRegistry.registerSql("KEY1","SQL1");
        sqlRegistry.registerSql("KEY2","SQL2");
        sqlRegistry.registerSql("KEY3","SQL3");
    }

    abstract protected UpdatableSqlRegistry createUpdatableSqlRegistry();

    protected void checkFindResult(String expected1,String expected2,String expected3){
        assertThat(sqlRegistry.findSql("KEY1"),is(expected1));
        assertThat(sqlRegistry.findSql("KEY2"),is(expected2));
        assertThat(sqlRegistry.findSql("KEY3"),is(expected3));
    }

    @Test
    public void find(){
        checkFindResult("SQL1","SQL2","SQL3");
    }

    @Test(expected = SqlNotFoundException.class)
    public void unknownKey(){
        sqlRegistry.findSql("SQL9999!@#$");
    }

    @Test
    public void updateSingle(){
        sqlRegistry.updateSql("KEY2","Modified2");
        checkFindResult("SQL1","Modified2","SQL3");
    }

    @Test
    public void updateMulti(){
        Map<String,String> sqlmap=new HashMap<>();
        sqlmap.put("KEY1","Modified1");
        sqlmap.put("KEY3","Modified3");

        sqlRegistry.updateSql(sqlmap);
        checkFindResult("Modified1","SQL2","Modified3");
    }

    @Test(expected = SqlUpdateFailureException.class)
    public void updateWithNotExistingKey(){
        sqlRegistry.updateSql("SQL9999!@#$","Modified2");
    }
}
