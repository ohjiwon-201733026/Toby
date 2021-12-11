package springbook.learningtest.jdk;

import javassist.util.proxy.ProxyFactory;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.Test;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;
import org.springframework.security.access.method.P;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Locale;

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
        assertThat(hello.sayThankYou("Toby"),is("Thank You Toby"));

        /*
        Hello proxiedHello= new HelloUppercase(new HelloTarget());// 프록시 통해 타깃에 접근
        assertThat(proxiedHello.sayHello("Toby"),is("HELLO TOBY"));
        assertThat(proxiedHello.sayHi("Toby"),is("HI TOBY"));
        assertThat(proxiedHello.sayThankYou("Toby"),is("THANK YOU TOBY"));
        */
        // 프록시 생성
        Hello proxiedHello=(Hello) Proxy.newProxyInstance( // 생성된 다이내믹 프록시 오브젝트는 Hello 인터페이스를 구현하고 있으므로 Hello타입으로 캐스팅해도 안정
                getClass().getClassLoader(), // 동적으로 생성되는 다이내믹 프록시 클래스의 로딩에 사용할 클래스 로더
                new Class[]{Hello.class}, // 구현할 인터페이스
                new UppercaseHandler(new HelloTarget())
        );
        assertThat(proxiedHello.sayHello("Toby"),is("HELLO TOBY"));
        assertThat(proxiedHello.sayHi("Toby"),is("HI TOBY"));
        assertThat(proxiedHello.sayThankYou("Toby"),is("THANK YOU TOBY"));
    }

    @Test
    public void proxyFactoryBean(){
        ProxyFactoryBean pfBean=new ProxyFactoryBean();
        pfBean.setTarget(new HelloTarget()); // 타깃 설정
        pfBean.addAdvice(new UppercaseAdvice()); // 부가기능을 담은 어드바이스 추가, 여러개 추가 가능
        Hello proxiedHello=(Hello)pfBean.getObject(); // FactoryBean이므로 getObject()로 생성된 프록시를 가져옴

        assertThat(proxiedHello.sayHello("Toby"),is("HELLO TOBY"));
        assertThat(proxiedHello.sayHi("Toby"),is("HI TOBY"));
        assertThat(proxiedHello.sayThankYou("Toby"),is("THANK YOU TOBY"));
    }

    @Test
    public void pointcutAdvisor(){
        ProxyFactoryBean pfBean=new ProxyFactoryBean();
        pfBean.setTarget(new HelloTarget());

        NameMatchMethodPointcut pointcut=new NameMatchMethodPointcut();
        pointcut.setMappedName("sayH*");

        pfBean.addAdvisor( new DefaultPointcutAdvisor(pointcut,new UppercaseAdvice())); // 포인트 컷과 어드바이서 묶어서

        Hello proxiedHello=(Hello)pfBean.getObject(); // FactoryBean이므로 getObject()로 생성된 프록시를 가져옴

        assertThat(proxiedHello.sayHello("Toby"),is("HELLO TOBY"));
        assertThat(proxiedHello.sayHi("Toby"),is("HI TOBY"));
        assertThat(proxiedHello.sayThankYou("Toby"),is("Thank You Toby"));
    }
    
    @Test
    public void classNamePointcutAdvisor(){
        // 포인트 컷 준비
        NameMatchMethodPointcut classMethodPointcut=new NameMatchMethodPointcut(){
            public ClassFilter getClassFilter(){ // 익명 내부 클래스 방식으로 클래스 정의
                return new ClassFilter() {
                    @Override
                    public boolean matches(Class<?> clazz) {
                        return clazz.getSimpleName().startsWith("HelloT");
                        // 클래스 이름이 HelloT로 시작하는 것만 선정
                    }
                };
            }
        };
        classMethodPointcut.setMappedName("sayH*"); // sayH로 시작하는 메소드 이름을 가진 메소드만 선정
        
        // 테스트
        checkAdviced(new HelloTarget(),classMethodPointcut,true);
        class HelloWorld extends HelloTarget{};
        checkAdviced(new HelloWorld(),classMethodPointcut,false);
        class HelloToy extends HelloTarget{};
        checkAdviced(new HelloToy(),classMethodPointcut,true);
    }

    private void checkAdviced(Object target, Pointcut pointcut, boolean adviced){
        ProxyFactoryBean pfBean=new ProxyFactoryBean();
        pfBean.setTarget(target);
        pfBean.addAdvisor(new DefaultPointcutAdvisor(pointcut,new UppercaseAdvice()));
        Hello proxiedHello=(Hello)pfBean.getObject();

        if(adviced){
            assertThat(proxiedHello.sayHello("Toby"),is("HELLO TOBY"));
            assertThat(proxiedHello.sayHi("Toby"),is("HI TOBY"));
            assertThat(proxiedHello.sayThankYou("Toby"),is("Thank You Toby"));
        }
        else{
            assertThat(proxiedHello.sayHello("Toby"),is("Hello Toby"));
            assertThat(proxiedHello.sayHi("Toby"),is("Hi Toby"));
            assertThat(proxiedHello.sayThankYou("Toby"),is("Thank You Toby"));
        }
    }

    static class UppercaseAdvice implements MethodInterceptor{

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            String ret=(String)invocation.proceed(); // 리플렉션의 Method와 달리 메소드 실행 시 타깃 오브젝트를 전달할 필요 없음,
            // MethodInvocation이이미 다 알고있기 때문
            return ret.toUpperCase();
        }
    }

    static interface Hello{
        String sayHello(String name);
        String sayHi(String name);
        String sayThankYou(String name);
    }

    static class HelloTarget implements Hello{

        @Override
        public String sayHello(String name) {
            return "Hello " +name;
        }

        @Override
        public String sayHi(String name) {
            return "Hi "+name;
        }

        @Override
        public String sayThankYou(String name) {
            return "Thank You "+name;
        }
    }
}
