package springbook.user.sqlService;

import springbook.user.dao.UserDao;
import springbook.user.sqlService.jaxb.SqlType;
import springbook.user.sqlService.jaxb.Sqlmap;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class XmlSqlService implements SqlService,SqlRegistry,SqlReader{
    private SqlReader sqlReader;
    private SqlRegistry sqlRegistry;

    private Map<String,String> sqlMap=new HashMap<>();
    private String sqlmapFile;

    public void setSqlmapFile(String sqlmapFile){
        this.sqlmapFile=sqlmapFile;
    }

    public void setSqlReader(SqlReader sqlReader) {
        this.sqlReader = sqlReader;
    }

    public void setSqlRegistry(SqlRegistry sqlRegistry) {
        this.sqlRegistry = sqlRegistry;
    }

    @PostConstruct
    public void loadSql(){
        this.sqlReader.read(this.sqlRegistry);
    }

    public XmlSqlService(){

    }

    private File getXmlFile(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(fileName).getFile());
    }

    @Override
    public String getSql(String key) throws SqlRetrievalFailureException {
        try{
            return this.sqlRegistry.findSql(key);
        }
        catch (SqlNotFoundException e){
            throw new SqlRetrievalFailureException(e);
        }
    }

    @Override
    public void registerSql(String key, String sql) {
        sqlMap.put(key,sql);
    }

    @Override
    public String findSql(String key) throws SqlNotFoundException {
        String sql=sqlMap.get(key);
        if(sql==null) throw new SqlNotFoundException(key+
                " sqlNotFound");
        else return sql;
    }

    @Override
    public void read(SqlRegistry sqlRegistry) {
        String contextPath= Sqlmap.class.getPackage().getName();
        try{
            JAXBContext context= JAXBContext.newInstance(contextPath);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            Sqlmap sqlmap = (Sqlmap) unmarshaller.unmarshal(getXmlFile(this.sqlmapFile));

            for (SqlType sql : sqlmap.getSql()) {
                sqlRegistry.registerSql(sql.getKey(),sql.getValue());
            }
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
