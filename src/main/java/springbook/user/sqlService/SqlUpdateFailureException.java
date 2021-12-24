package springbook.user.sqlService;

public class SqlUpdateFailureException extends RuntimeException {

    public SqlUpdateFailureException(String message){
        super(message);
    }

    public SqlUpdateFailureException(String message,Throwable cause){
        super(message, cause);
    }

    public SqlUpdateFailureException(SqlNotFoundException e) {
    }
}
