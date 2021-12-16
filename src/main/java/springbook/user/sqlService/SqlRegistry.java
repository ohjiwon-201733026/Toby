package springbook.user.sqlService;

public interface SqlRegistry {

    void registerSql(String key,String sql); // SQL을 키와 함께 등록

    String findSql(String key) throws SqlNotFoundException;
}
