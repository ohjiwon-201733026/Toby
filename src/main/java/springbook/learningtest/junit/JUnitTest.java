package springbook.learningtest.junit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/JUnit-applicationContext.xml")
public class JUnitTest {
    @Autowired
    ApplicationContext context;
    static Set<JUnitTest> testObjects=new HashSet<>();

    static ApplicationContext contextObejct=null;

    @Test
    public void test1(){
        assertThat(testObjects,is(not(hasItem(this))));
        testObjects.add(this);
        assertThat(contextObejct==null || contextObejct==this.context ,is(true));
        contextObejct=this.context;
    }

    @Test
    public void test2(){
        assertThat(testObjects,is(not(hasItem(this))));
        testObjects.add(this);

        assertThat(contextObejct==null || contextObejct==this.context ,is(true));
        contextObejct=this.context;
    }

    @Test
    public void test3() {
        assertThat(testObjects, is(not(hasItem(this))));
        testObjects.add(this);

        assertThat(contextObejct,
                either(is(nullValue())).or(is(this.context)));
        contextObejct = this.context;
    }
}
