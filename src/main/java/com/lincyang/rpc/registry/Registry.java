package com.lincyang.rpc.registry;

import java.util.List;

/**
 * 注册接口
 * @Author lincyang
 * @Date 2019/8/19 9:49 AM
 **/
public interface Registry {
    void register(Class clazz, RegistryInfo registryInfo) throws Exception;

    /**
     * 为服务提供者抓取注册表
     * @param clazz
     * @return
     * @throws Exception
     */
    List fetchRegistry(Class clazz) throws  Exception;
}
