package com.lincyang.rpc.context;

import com.alibaba.fastjson.JSONObject;
import com.lincyang.rpc.config.ReferenceConfig;
import com.lincyang.rpc.config.ServiceCofig;
import com.lincyang.rpc.invoke.DefaultInvoker;
import com.lincyang.rpc.invoke.Invoker;
import com.lincyang.rpc.loadbalance.LoadBalancer;
import com.lincyang.rpc.loadbalance.RandomLoadBalancer;
import com.lincyang.rpc.registry.Registry;
import com.lincyang.rpc.registry.RegistryInfo;
import com.lincyang.rpc.registry.zk.ZookeeperRegistry;
import com.lincyang.rpc.transport.NettyClient;
import com.lincyang.rpc.transport.NettyServer;
import com.lincyang.rpc.transport.RpcResponse;
import com.lincyang.rpc.util.InvokeUtils;
import io.netty.channel.ChannelHandlerContext;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.LongAdder;

/**
 * 框架统一入口
 * @Author lincyang
 * @Date 2019/8/19 10:26 AM
 **/
public class ApplicationContext<T> {

    private List<ServiceCofig> serviceCofigs;
    private List<ReferenceConfig> referenceConfigs;
    private NettyServer nettyServer;

    // 注册中心
    private Registry registry;
    private  LoadBalancer loadBalancer;

    private Map<String, Method> interfaceMethods = new ConcurrentHashMap<>();
    private Map<Class, List<RegistryInfo>> interfacesMethodRegistryList = new ConcurrentHashMap<>();
    private Map<RegistryInfo, ChannelHandlerContext> channels = new ConcurrentHashMap<>();
    private ConcurrentLinkedQueue<RpcResponse> responses = new ConcurrentLinkedQueue();
    private ConcurrentHashMap<String,Invoker> inProgressInvoker = new ConcurrentHashMap<>();



    public ApplicationContext(String registryUrl,List<ServiceCofig> serviceCofigs,List<ReferenceConfig> referenceConfigs,Integer port) throws Exception {
        // 保存需要暴露的接口配置
        this.serviceCofigs = serviceCofigs == null ? new ArrayList<>():serviceCofigs;
        this.referenceConfigs = referenceConfigs == null ? new ArrayList<>():referenceConfigs;
        this.loadBalancer = new RandomLoadBalancer();

        //实例化注册中心
        initRegistry(registryUrl);

        // 将接口注册到注册中心，从注册中心获取接口，初始化服务接口列表
        RegistryInfo registryInfo = null;
        InetAddress addr = InetAddress.getLocalHost();
        String hostName = addr.getHostName();
        String hostAddress = addr.getHostAddress();
        registryInfo = new RegistryInfo(hostName,hostAddress,port);
        doRegistry(registryInfo);

        // 初始化 netty 服务器，接受到请求，直接打到服务提供者的 service 方法中
        if(!this.serviceCofigs.isEmpty()){
            nettyServer = new NettyServer(this.serviceCofigs,interfaceMethods);
            nettyServer.init(port);
            initProcessor();
        }


    }

    private void initProcessor() {
        // 事实上，这里可以通过配置文件读取，启动多少个processor
        ApplicationContext.ResponseProcessor responseProcessors[] = new ApplicationContext.ResponseProcessor[3];
        for (int i = 0; i < 3; i++) {
            ResponseProcessor processors = createProcessor(i);
        }

    }

    private ResponseProcessor createProcessor(int num) {
        ResponseProcessor responseProcessor = new ResponseProcessor(num);
        responseProcessor.start();
        return  responseProcessor;
    }

    private void doRegistry(RegistryInfo registryInfo) throws  Exception {
        for(ServiceCofig config : serviceCofigs){
            Class type = config.getType();
            registry.register(type,registryInfo);

            Method[] declareMethods = type.getDeclaredMethods();
            for(Method method : declareMethods){
                String identity = InvokeUtils.buildIntegerfaceMethodIdentify(type,method);
                interfaceMethods.put(identity,method);
            }
        }

        for(ReferenceConfig config : this.referenceConfigs){
            List registryInfos = registry.fetchRegistry(config.getType());
            if(registryInfos != null){
                interfacesMethodRegistryList.put(config.getType(),registryInfos);
                initChannel(registryInfos);
            }
        }
    }


    private void initRegistry(String registryUrl) {
        if(registryUrl.startsWith("zookeeper://")){
           registryUrl = registryUrl.substring(12);
           registry = new ZookeeperRegistry(registryUrl);
        }
    }

    private void initChannel(List<RegistryInfo> registryInfos) throws  InterruptedException{
        for(RegistryInfo registryInfo : registryInfos){
            if(!channels.containsKey(registryInfo)){
                System.out.println("开始建立连接："+registryInfo.getIp()+","+registryInfo.getPort());
                NettyClient client = new NettyClient(registryInfo.getIp(),registryInfo.getPort());
                client.setMessageCallBack(message -> {
                    RpcResponse response = JSONObject.parseObject(message,RpcResponse.class);
                    responses.offer(response);
                    synchronized (ApplicationContext.this){
                        ApplicationContext.this.notifyAll();
                    }
                });

                //等待连接建立
                ChannelHandlerContext ctx = client.getCtx();
                channels.put(registryInfo,ctx);
            }
        }
    }

    private LongAdder requestIdWorker = new LongAdder();

    public T getService(Class clazz){
        return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{clazz}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String methodName = method.getName();
                if("equals".equals(methodName) || "hashCode".equals(methodName)){
                    throw new IllegalAccessException("不能访问"+methodName+" 方法");
                }
                if("toString".equals(methodName)){
                    return clazz.getName() + "#" + methodName;
                }

                // 1获取服务地址列表
                List<RegistryInfo> registryInfos = interfacesMethodRegistryList.get(clazz);
                if(registryInfos == null){
                    throw  new RuntimeException("无法找到服务提供者");
                }

                // 2负载均衡
                RegistryInfo registryInfo = loadBalancer.choose(registryInfos);

                ChannelHandlerContext ctx = channels.get(registryInfo);
                String identify = InvokeUtils.buildIntegerfaceMethodIdentify(clazz,method);
                String requestId;
                synchronized (ApplicationContext.this){
                    requestIdWorker.increment();
                    requestId = String.valueOf(requestIdWorker.longValue());
                }
                Invoker invoker  = new DefaultInvoker(method.getReturnType(),ctx,requestId,identify);
                inProgressInvoker.put(identify+"#"+requestId,invoker);
                return invoker.invoke(args);
            }
        });
    }

    private  class ResponseProcessor extends Thread{

        private int index;

        ResponseProcessor(){}

        ResponseProcessor(int index){
            this.index = index;
        }

        @Override
        public void run() {
            System.out.println("启动响应处理线程："+getName());
            while(true){
                RpcResponse response = responses.poll();
                if(response == null){
                    try{
                        synchronized (ApplicationContext.this){
                            ApplicationContext.this.wait();
                        }
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }else {
                    System.out.println("收到一个响应："+response);
                    String interfaceMethodIndentify = response.getInterfaceMethodIdentify();
                    String requestId = response.getRequestId();
                    String key = interfaceMethodIndentify +"#"+requestId;
                    Invoker invoker = inProgressInvoker.remove(key);
                    invoker.setResult(response.getResult());
                }
            }
        }
    }


}
