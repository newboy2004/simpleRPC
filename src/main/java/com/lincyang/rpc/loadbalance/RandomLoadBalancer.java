package com.lincyang.rpc.loadbalance;

import com.lincyang.rpc.registry.RegistryInfo;

import java.util.List;
import java.util.Random;

/**
 * 随机算法
 * @Author lincyang
 * @Date 2019/8/20 3:07 PM
 **/
public class RandomLoadBalancer  implements LoadBalancer{

    @Override
    public RegistryInfo choose(List<RegistryInfo> registryInfos) {
        Random random = new Random();
        int index = random.nextInt(registryInfos.size());
        return registryInfos.get(index);
    }
}
