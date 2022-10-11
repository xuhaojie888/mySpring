package com.xhj;

import com.springframework.ApplicationContext;
import com.xhj.user.UserService;

public class MyApplicationContext {

    public static void main(String[] args) throws Exception{
        ApplicationContext applicationContext = new ApplicationContext(AppConfig.class);
        UserService userService = (UserService) applicationContext.getBean("userService");
//        UserService userService2 = (UserService) applicationContext.getBean("userService");
//        UserService userService3 = (UserService) applicationContext.getBean("userService");
//        OrderService orderService = ((OrderService) applicationContext.getBean("orderService"));
//        OrderService orderService2 = ((OrderService) applicationContext.getBean("orderService"));
//        OrderService orderService3 = ((OrderService) applicationContext.getBean("orderService"));
//        System.out.println(userService);
//        System.out.println(userService2);
//        System.out.println(userService3);
//        System.out.println(orderService);
//        System.out.println(orderService2);
//        System.out.println(orderService3);
        userService.test();
    }
}
