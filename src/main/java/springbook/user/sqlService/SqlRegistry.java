package springbook.user.sqlService;

public interface SqlRegistry {

    void registerSql(String key,String sql); // SQL�� Ű�� �Բ� ���

    String findSql(String key) throws SqlNotFoundException;
}
