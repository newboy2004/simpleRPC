package com.lincyang.rpc.loadbalance;

import com.lincyang.rpc.registry.RegistryInfo;

import java.util.List;

/**
 * 负载均衡
 * @Author lincyang
 * @Date 2019/8/20 3:06 PM
 **/
public interface LoadBalancer {

    RegistryInfo choose(List<RegistryInfo> registryInfos);
}
