package com.lincyang.rpc.registry.zk;

import com.alibaba.fastjson.JSONArray;
import com.lincyang.rpc.registry.Registry;
import com.lincyang.rpc.registry.RegistryInfo;
import com.lincyang.rpc.util.InvokeUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * zk注册接口实现
 * @Author lincyang
 * @Date 2019/8/19 9:50 AM
 **/
public class ZookeeperRegistry implements Registry {

    private CuratorFramework client;

    public ZookeeperRegistry(String connectStrinig) {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,3);
        client = CuratorFrameworkFactory.newClient(connectStrinig,retryPolicy);
        client.start();

        try{
            Stat myRPC = client.checkExists().forPath("/myRPC");
            if(myRPC == null){
                client.create()
                        .creatingParentsIfNeeded()
                        .forPath("/myRPC");
            }
            System.out.println("Zookeeper Client初始化完毕。。。。");
        }catch (Exception e){

        }
    }

    public void register(Class clazz, RegistryInfo registryInfo) throws Exception {
        // 1. 注册的时候，先从zk中获取数据
        // 2. 将自己的服务器地址加入注册中心中

        // 为每一个接口的每一个方法注册一个临时节点，然后key为接口方法的唯一标识，data为服务地址列表
        Method[] declareMethods = clazz.getDeclaredMethods();
        for(Method method : declareMethods){
            String key = InvokeUtils.buildIntegerfaceMethodIdentify(clazz,method);
            String path = "/myRPC/" + key;
            System.out.println("server path: "+path);
            Stat stat = client.checkExists().forPath(path);

            List<RegistryInfo> registryInfos;
            if(stat != null){
                // 如果这个接口已经有人注册过了，把数据拿回来，然后将自己的信息保存进去
                byte[] bytes = client.getData().forPath(path);
                String data = new String(bytes, StandardCharsets.UTF_8);
                registryInfos = JSONArray.parseArray(data,RegistryInfo.class);
                if(registryInfos.contains(registryInfo)){
                    // 正常来说，zk的临时节点，断开连接后，直接就没了，但是重启会经常发现存在节点，所以有了这样的代码
                    System.out.println("地址列表已经包含本机【" + key + "】，不注册了");
                }else{
                    registryInfos.add(registryInfo);
                    client.setData().forPath(path,JSONArray.toJSONString(registryInfos).getBytes());
                    System.out.println("注册到注册中心，路径为：【" + path + "】 信息为：" + registryInfo);
                }
            }else{
                registryInfos = new ArrayList<>();
                registryInfos.add(registryInfo);
                client.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.EPHEMERAL)
                        .forPath(path,JSONArray.toJSONString(registryInfos).getBytes());
                System.out.println("注册到注册中心，路径为：【" + path + "】 信息为：" + registryInfo);
            }
        }
    }

    @Override
    public List fetchRegistry(Class clazz) throws Exception {
        Method[] declareMethods = clazz.getDeclaredMethods();
        List<RegistryInfo> registryInfos = null;
        for(Method method : declareMethods){
            String key = InvokeUtils.buildIntegerfaceMethodIdentify(clazz,method);
            String path = "/myRPC/" + key;
            System.out.println("client path: "+path);
            Stat stat = client.checkExists().forPath(path);
            if(stat == null){
                // 此处可以添加 watcher 来监听变化
                System.out.println("无法找到服务接口："+path);
                continue;
            }
            if(registryInfos == null){
                byte[] bytes = client.getData().forPath(path);
                String data = new String(bytes,StandardCharsets.UTF_8);
                registryInfos = JSONArray.parseArray(data,RegistryInfo.class);
            }
        }
        return registryInfos;
    }
}
