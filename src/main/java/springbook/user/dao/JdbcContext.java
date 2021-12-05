package springbook.user.dao;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JdbcContext {
    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void workWithStatementStrategy(StatementStrategy stmt) throws SQLException{
        Connection c=null;
        PreparedStatement ps=null;
        try{
            c=dataSource.getConnection();

            ps=stmt.makePreparedStatement(c);

            ps.executeUpdate();
        }catch (SQLException e) { // 2. 예외 처리해주기
            throw e;

        } finally { // 3. try 수행 후 반드시 실행되는 코드
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                }
            }
            if (c != null) {
                try {
                    c.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    public void executeSql(final String query) throws SQLException{
        workWithStatementStrategy(
                new StatementStrategy(){
                    public PreparedStatement makePreparedStatement(Connection c) throws SQLException {
                        return c.prepareStatement(query);
                    }}
        );
    }
}
