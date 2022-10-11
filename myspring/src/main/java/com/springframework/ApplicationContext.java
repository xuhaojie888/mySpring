package com.springframework;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationContext {
    private Class configClazz;
    Map<String, BeanDefination> beanDefinationMap = new HashMap<>();
    Map<String,Object> singletonObjects = new HashMap<>();
    List<BeanPostProcessor> beanPostProcessorList = new ArrayList();

    public ApplicationContext(Class configClazz) throws Exception{
        this.configClazz = configClazz;

        scanPackage(configClazz);
        for (Map.Entry<String, BeanDefination> entry : beanDefinationMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefination beanDefination = entry.getValue();
            if(!beanDefination.isLazy() && beanDefination.getScope().equals("singleton")){
                Object object = createBean(beanName,beanDefination);
                singletonObjects.put(beanName,object);
            }
        }

    }

    private Object createBean(String beanName, BeanDefination beanDefination) throws Exception{
        Class clazz = beanDefination.getClazz();
        //构造方法
        Object object = clazz.newInstance();

        //依赖注入
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if(field.isAnnotationPresent(Autowired.class)){
                Object bean = getBean(field.getName());
                field.setAccessible(true);
                field.set(object,bean);
            }
        }

        //Aware回调
        if(object instanceof BeanNameAware){
            ((BeanNameAware) object).setBeanName(beanName);
        }

        //初始化前
        for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
            object = beanPostProcessor.postProcessBeforeInitialization(object,beanName);
        }

        //初始化
        if(object instanceof InitializingBean){
            ((InitializingBean) object).afterPropertiesSet();
        }

        //初始化后
        for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
            object = beanPostProcessor.postProcessAfterInitialization(object,beanName);
        }

        return object;
    }

    private void scanPackage(Class configClazz) throws Exception {
        if(configClazz.isAnnotationPresent(ComponentScan.class)){
            ComponentScan componentScan = ((ComponentScan) configClazz.getAnnotation(ComponentScan.class));
            String path = componentScan.value();
            path = path.replace(".","/");
            URL resource = this.getClass().getClassLoader().getResource(path);
            File file = new File(resource.getFile());
            if(file.isDirectory()){
                for (File f : file.listFiles()) {
                    if(!f.isDirectory()){
                        String absolutePath = f.getAbsolutePath();
                        String className = absolutePath.substring(absolutePath.indexOf("com"),absolutePath.indexOf(".class")).replace("/",".");
                        Class<?> clazz = this.getClass().getClassLoader().loadClass(className);
                        if(clazz.isAnnotationPresent(Component.class)){
                            String beanName = clazz.getAnnotation(Component.class).value();
                            if ("".equals(beanName)) {
                                beanName = Introspector.decapitalize(clazz.getSimpleName());
                            }

                            BeanDefination beanDefination = new BeanDefination();
                            beanDefination.setClazz(clazz);
                            beanDefination.setLazy(clazz.isAnnotationPresent(Lazy.class));
                            beanDefination.setScope("singleton");
                            if (clazz.isAnnotationPresent(Scope.class)) {
                                String scope = clazz.getAnnotation(Scope.class).value();
                                if (!scope.equals("")) {
                                    beanDefination.setScope(scope);
                                }
                            }

                            if(BeanPostProcessor.class.isAssignableFrom(clazz)){
                                //实现了BeanPostProcessor的bean
                                BeanPostProcessor beanPostProcessor = ((BeanPostProcessor) createBean(beanName, beanDefination));
                                beanPostProcessorList.add(beanPostProcessor);
                            }else {
                                //普通bean
                                beanDefinationMap.put(beanName, beanDefination);
                            }

                        }
                    }
                }
            }
        }
    }


    public Object getBean(String beanName) throws Exception{
        Object object = singletonObjects.get(beanName);
        if(object != null){
            return object;
        }
        BeanDefination beanDefination = beanDefinationMap.get(beanName);
        if(beanDefination != null){
            object = createBean(beanName,beanDefination);
            if(beanDefination.getScope().equals("singleton")){
                singletonObjects.put(beanName,object);
            }
            return object;
        }

         throw new RuntimeException("spring容器未找到此bean的定义");
    }
}
