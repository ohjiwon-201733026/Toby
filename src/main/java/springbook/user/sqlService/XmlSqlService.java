package springbook.user.sqlService;

import springbook.user.dao.UserDao;
import springbook.user.sqlService.jaxb.SqlType;
import springbook.user.sqlService.jaxb.Sqlmap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class XmlSqlService implements SqlService{

    private Map<String,String> sqlMap=new HashMap<>();
    private String sqlmapFile;

    public void setSqlmapFile(String sqlmapFile){
        this.sqlmapFile=sqlmapFile;
    }

    public void loadSql(){
        String contextPath= Sqlmap.class.getPackage().getName();
        try{
            JAXBContext context= JAXBContext.newInstance(contextPath);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            Sqlmap sqlmap = (Sqlmap) unmarshaller.unmarshal(getXmlFile(this.sqlmapFile));

            for (SqlType sql : sqlmap.getSql()) {
                sqlMap.put(sql.getKey(),sql.getValue());
            }
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public XmlSqlService(){

    }

    private File getXmlFile(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(fileName).getFile());
    }

    @Override
    public String getSql(String key) throws SqlRetrievalFailureException {
        String sql=sqlMap.get(key);
        if(sql==null){
            throw new SqlRetrievalFailureException(key);
        }
        else return sql;
    }
}
