package com.lincyang.rpc.config;

/**
 * 服务配置类
 * @Author lincyang
 * @Date 2019/8/19 10:22 AM
 **/
public class ServiceCofig<T> {
    public  Class type;

    public T instance;


    public ServiceCofig(Class type, T instance) {
        this.type = type;
        this.instance = instance;
    }

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public T getInstance() {
        return instance;
    }

    public void setInstance(T instance) {
        this.instance = instance;
    }
}
