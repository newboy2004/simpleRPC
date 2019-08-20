package com.lincyang.rpc;

import com.lincyang.rpc.config.ServiceCofig;
import com.lincyang.rpc.context.ApplicationContext;
import com.lincyang.rpc.service.HelloService;
import com.lincyang.rpc.service.impl.HelloServiceImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author lincyang
 * @Date 2019/8/19 5:42 PM
 **/
public class TestProducer {

    public static void main(String[] args) throws Exception {
        String connectString="zookeeper://localhost:2181";
        HelloService service = new HelloServiceImpl();
        ServiceCofig<HelloService> config = new ServiceCofig<>(HelloService.class,service);
        List<ServiceCofig> list = new ArrayList<>();
        list.add(config);
        ApplicationContext ctx = new ApplicationContext(connectString,list,null,50071);
    }
}
