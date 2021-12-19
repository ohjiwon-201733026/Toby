package springbook.learningtest.spring.oxm;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.Unmarshaller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import springbook.user.sqlService.jaxb.SqlType;
import springbook.user.sqlService.jaxb.Sqlmap;

import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration
public class OxmTest {

//    @Autowired
//    Unmarshaller unmarshaller;
//
//    @Test
//    public void unmarshallSqlMap() throws  IOException, JAXBException {
//        Source xmlSource=new StreamSource(
//                getClass().getResourceAsStream("sqlmapTest.xml"));
//
//        Sqlmap sqlmap=(Sqlmap)this.unmarshaller.unmarshal(xmlSource);
//
//        List<SqlType> sqlList=sqlmap.getSql();
//        assertThat(sqlList.size(),is(3));
//        assertThat(sqlList.get(0).getKey(), is("add"));
//        assertThat(sqlList.get(0).getValue(), is("insert"));
//        assertThat(sqlList.get(1).getKey(), is("get"));
//        assertThat(sqlList.get(1).getValue(), is("select"));
//        assertThat(sqlList.get(2).getKey(), is("delete"));
//        assertThat(sqlList.get(2).getValue(), is("delete"));
//    }

    private File getXmlFile(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(fileName).getFile());
    }
}
