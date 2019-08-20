package com.lincyang.rpc;

import com.lincyang.rpc.config.ReferenceConfig;
import com.lincyang.rpc.context.ApplicationContext;
import com.lincyang.rpc.service.HelloService;
import com.lincyang.rpc.service.TestBean;

import java.util.Collections;

/**
 * @Author lincyang
 * @Date 2019/8/20 3:56 PM
 **/
public class TestConsumer {

    public static void main(String[] args) throws Exception{
        String connectionString = "zookeeper://localhost:2181";
        ReferenceConfig config = new ReferenceConfig(HelloService.class);
        ApplicationContext ctx = new ApplicationContext(connectionString, null, Collections.singletonList(config),
                50070);
        HelloService helloService = (HelloService) ctx.getService(HelloService.class);
        System.out.println("sayHello(TestBean)结果为：" + helloService.sayHello(new TestBean("张三", 20)));
    }
}
