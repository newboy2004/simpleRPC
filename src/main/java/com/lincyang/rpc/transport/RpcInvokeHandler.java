package com.lincyang.rpc.transport;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lincyang.rpc.config.ServiceCofig;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * rpc 请求处理 handler
 * @Author lincyang
 * @Date 2019/8/19 12:13 PM
 **/
public class RpcInvokeHandler extends ChannelInboundHandlerAdapter {

    private Map<String, Method> interfaceMethods;

    private Map<Class,Object> interfaceToInstance;

    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 50, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(100), new ThreadFactory() {

        AtomicInteger m = new AtomicInteger(0);
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r,"IO-thread" + m.incrementAndGet());
        }
    });


    public RpcInvokeHandler(List<ServiceCofig> serviceCofigList, Map<String, Method> interfaceMethods) {
        this.interfaceToInstance = new ConcurrentHashMap<>();
        this.interfaceMethods = interfaceMethods;
        for(ServiceCofig config : serviceCofigList){
            interfaceToInstance.put(config.getType(),config.getInstance());
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try{
            String message = (String) msg;
            // 这里拿到的是一串JSON数据，解析为Request对象，
            // 事实上这里解析网络数据，可以用序列化方式，定一个接口，可以实现JSON格式序列化，或者其他序列化
            // 但是demo版本就算了。
            System.out.println("接收到消息： "+message);
            RpcRequest request = RpcRequest.parse(message,ctx);
            threadPoolExecutor.execute(new RpcInvokeTask(request));
        }finally {

        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("发生了异常。。。。"+cause);
        cause.printStackTrace();
        ctx.close();
    }

    public  class RpcInvokeTask implements Runnable{
        private RpcRequest request;

        RpcInvokeTask(RpcRequest request){
            this.request = request;
        }

        @Override
        public void run() {
            try {
                /*
                 * 数据大概是这样子的
                 * {"interfaces":"interface=com.study.rpc.test.producer.HelloService&method=sayHello¶meter=com
                 * .study.rpc.test.producer.TestBean","requestId":"3","parameter":{"com.study.rpc.test.producer
                 * .TestBean":{"age":20,"name":"张三"}}}
                 */
                // 这里希望能拿到每一个服务对象的每一个接口的特定声明

                String interfaceIdentify = request.getInterfaceIdentity();
                Method method = interfaceMethods.get(interfaceIdentify);
                Map<String, String> map = string2Map(interfaceIdentify);
                String interfaceName = map.get("interface");
                Class interfaceClass = Class.forName(interfaceName);
                Object o = interfaceToInstance.get(interfaceClass);
                String parameterString = map.get("parameter");
                Object result;

                if(parameterString != null){
                    String[] parameterTypeClass = parameterString.split(",");
                    Map<String,Object> paramterMap = request.getParamterMap();
                    Object[] parameterInstance = new Object[parameterTypeClass.length];
                    for (int i = 0; i < parameterTypeClass.length; i++) {
                        String parameterClazz = parameterTypeClass[i];
                        parameterInstance[i] = paramterMap.get(parameterClazz);
                    }
                    result = method.invoke(o,parameterInstance);
                }else{
                    result = method.invoke(o);
                }

                ChannelHandlerContext ctx = request.getCtx();
                String requestId = request.getRequestId();
                RpcResponse response = RpcResponse.create(JSONArray.toJSONString(result),interfaceIdentify,requestId);
                String s = JSONObject.toJSONString(response) + "$$";
                ByteBuf byteBuf = Unpooled.copiedBuffer(s.getBytes());
                ctx.writeAndFlush(byteBuf);
                System.out.println("响应给客户端："+s);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        private Map<String, String> string2Map(String str) {
            String[] split = str.split("&");
            Map<String,String> map = new HashMap<>(16);
            for(String s : split){
                String[] split1 = s.split("=");
                map.put(split1[0],split1[1]);
            }
            return map;
        }
    }
}
