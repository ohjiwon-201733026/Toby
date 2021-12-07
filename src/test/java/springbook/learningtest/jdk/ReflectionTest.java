package springbook.learningtest.jdk;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ReflectionTest {

    @Test
    public void invokeMethod() throws Exception{
        String name="Spring";

        // length()
        assertThat(name.length(),is(6));

        Method lengthMethod=String.class.getMethod("length");
        assertThat((Integer)lengthMethod.invoke(name),is(6));

        // charAt()
        assertThat(name.charAt(0),is('S'));

        Method charAtMethod=String.class.getMethod("charAt", int.class);
        assertThat((Character)charAtMethod.invoke(name,0),is('S'));
    }

    @Test
    public void simpleProxy(){
        Hello hello=new HelloTarget(); // 타깃은 인터페이스를 통해 접근하는 습관
        assertThat(hello.sayHello("Toby"),is("Hello Toby"));
        assertThat(hello.sayHi("Toby"),is("Hi Toby"));
        assertThat(hello.sayThankYou("Toby"),is("Thank you Toby"));

        Hello proxiedHello= new HelloUppercase(new HelloTarget());// 프록시 통해 타깃에 접근
        assertThat(proxiedHello.sayHello("Toby"),is("HELLO TOBY"));
        assertThat(proxiedHello.sayHi("Toby"),is("HI TOBY"));
        assertThat(proxiedHello.sayThankYou("Toby"),is("THANK YOU TOBY"));
    }
}
