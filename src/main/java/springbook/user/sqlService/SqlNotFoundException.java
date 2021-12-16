package springbook.user.sqlService;

public class SqlNotFoundException extends RuntimeException{

    public SqlNotFoundException(String message){
        super(message);
    }
}
