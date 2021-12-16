package springbook.user.sqlService;

public interface SqlReader {
    void read(SqlRegistry sqlRegistry); 
    // SQL을 외부에서 가져와 SqlREgistry에 등록
    // 다양한 예외가 발생할 수 있겠지만 대부분 불가능한 예외 -> 예외 선언하지 않음
}
