package com.lincyang.rpc.config;

/**
 * 消费端配置
 * @Author lincyang
 * @Date 2019/8/20 9:58 AM
 **/
public class ReferenceConfig {

    private Class  type;

    public ReferenceConfig(Class type) {
        this.type = type;
    }

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }
}
