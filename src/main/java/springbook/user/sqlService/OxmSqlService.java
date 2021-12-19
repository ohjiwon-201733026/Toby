package springbook.user.sqlService;

import org.springframework.oxm.Unmarshaller;
import springbook.user.dao.UserDao;
import springbook.user.sqlService.jaxb.SqlType;
import springbook.user.sqlService.jaxb.Sqlmap;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;


public class OxmSqlService implements SqlService{
    private final OxmSqlReader oxmSqlReader=new OxmSqlReader();

    private SqlRegistry sqlRegistry=new HashMapSqlRegistry();

    public void setSqlRegistry(SqlRegistry sqlRegistry) {
        this.sqlRegistry = sqlRegistry;
    }

    public void setUnmarshaller(Unmarshaller unmarshaller){
        this.oxmSqlReader.setUnmarshaller(unmarshaller);
    }

    public void setSqlmapFile(String sqlmapFile){
        this.oxmSqlReader.setSqlmapFile(sqlmapFile);
    }

    @PostConstruct
    public void loadSql(){
        this.oxmSqlReader.read(this.sqlRegistry);
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


    private class OxmSqlReader implements SqlReader{

        private Unmarshaller unmarshaller;
        private static final String DEFAULT_SQLMAP_FILE="sqlmap.xml";
        private String sqlmapFile=DEFAULT_SQLMAP_FILE;

        public void setUnmarshaller(Unmarshaller unmarshaller) {
            this.unmarshaller = unmarshaller;
        }

        public void setSqlmapFile(String sqlmapFile) {
            this.sqlmapFile = sqlmapFile;
        }

        @Override
        public void read(SqlRegistry sqlRegistry) {
//            String contextPath= Sqlmap.class.getPackage().getName();
            try{
                Source source=new StreamSource(UserDao.class.getResourceAsStream(this.sqlmapFile));

                Sqlmap sqlmap=(Sqlmap) this.unmarshaller.unmarshal(source);

                for (SqlType sql : sqlmap.getSql()) {
                    sqlRegistry.registerSql(sql.getKey(),sql.getValue());
                }
            } catch (IOException e) {
                throw new IllegalArgumentException(this.sqlmapFile);
            }
        }

        private File getXmlFile(String fileName) {
            ClassLoader classLoader = getClass().getClassLoader();
            return new File(classLoader.getResource(fileName).getFile());
        }
    }


}
