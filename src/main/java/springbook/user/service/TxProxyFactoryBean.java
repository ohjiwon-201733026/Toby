package springbook.user.service;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import java.lang.reflect.Proxy;

public class TxProxyFactoryBean implements FactoryBean<Object> {
    // TransactionHandler를 생성할 때 필요
    Object target;
    PlatformTransactionManager transactionManager;
    String pattern;
    Class<?> serviceInterface; // 다이내믹 프록시 생성할 떄 필요, UserService외의 인터페이스를 가진 타깃에도 적용 가능

    public void setTarget(Object target) {
        this.target = target;
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public void setServiceInterface(Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    // FactoryBean 인터페이스 구현메소드
    @Override
    public Object getObject() throws Exception { // DI 받은 정보를 이용해 TransactionHandler를 사용하는 다이내믹 프록시 생성
        TransactionHandler txHandler=new TransactionHandler();
        txHandler.setTarget(target);
        txHandler.setTransactionManager(transactionManager);
        txHandler.setPattern(pattern);
        return Proxy.newProxyInstance(
                getClass().getClassLoader(),new Class[]{serviceInterface},
                txHandler);
    }

    @Override
    public Class<?> getObjectType() {
        return serviceInterface;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
