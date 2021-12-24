package springbook.user;

import springbook.user.sqlService.ConcurrentHashMapSqlRegistry;
import springbook.user.sqlService.UpdatableSqlRegistry;


public class ConcurrentHashMapSqlRegistryTest extends AbstractUpdatableSqlRegistryTest{

    @Override
    protected UpdatableSqlRegistry createUpdatableSqlRegistry() {
        return new ConcurrentHashMapSqlRegistry();
    }

}
