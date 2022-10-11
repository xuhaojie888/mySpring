package com.xhj.user;

import com.springframework.*;

@Component
@Scope
public class UserService implements BeanNameAware, InitializingBean {

    @Autowired
    private OrderService orderService;

    @Transactional
    public void test(){
        System.out.println("UserService test orderService:"+orderService);
        System.out.println("UserService test invoke:"+this);
    }





    @Override
    public void setBeanName(String beanName) {
        System.out.println("BeanNameAware 回调："+beanName);
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("UserService 初始化 InitializingBean.afterPropertiesSet：");
    }
}
