package com.lincyang.rpc.service.impl;

import com.lincyang.rpc.service.HelloService;
import com.lincyang.rpc.service.TestBean;

/**
 * @Author lincyang
 * @Date 2019/8/19 5:40 PM
 **/
public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(TestBean testBean) {
        return "牛逼,我收到了消息：" + testBean;
    }
}
