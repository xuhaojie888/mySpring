package com.xhj.user;

import com.springframework.BeanPostProcessor;
import com.springframework.Component;
import com.springframework.Transactional;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

@Component
public class MyBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("BeanPostProcessor 初始化前操作");
        System.out.println("BeanPostProcessor bean："+bean);
        System.out.println("BeanPostProcessor beanName："+beanName);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Class<?> clazz = bean.getClass();
        for (Method declaredMethod : clazz.getDeclaredMethods()) {
            if(declaredMethod.isAnnotationPresent(Transactional.class)){
                Enhancer enhancer = new Enhancer();
                enhancer.setSuperclass(bean.getClass());
                Object target = bean;
                enhancer.setCallback(new MethodInterceptor() {
                    @Override
                    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                        if(method.isAnnotationPresent(Transactional.class)) {
                            System.out.println("开启事务");
                            Object result = method.invoke(target, objects);
                            System.out.println("结束事务");
                            return result;
                        }else {
                            return method.invoke(target, objects);
                        }
                    }
                });
                bean = enhancer.create();
            }
        }
        System.out.println("BeanPostProcessor 初始化后操作");
        System.out.println("BeanPostProcessor bean："+bean);
        System.out.println("BeanPostProcessor beanName："+beanName);
        return bean;
    }
}
