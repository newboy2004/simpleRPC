package com.lincyang.rpc.transport;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 请求信息包装类
 * @Author lincyang
 * @Date 2019/8/19 12:25 PM
 **/
public class RpcRequest {

    private String interfaceIdentity;

    private Map<String,Object> paramterMap = new HashMap<>();

    private ChannelHandlerContext ctx;

    private String requestId;

    public static RpcRequest parse(String message,ChannelHandlerContext ctx) throws ClassNotFoundException{
        /*
         * {
         *   "interfaces":"interface=com.study.rpc.test.producer.HelloService&method=sayHello2¶meter=java.lang
         * .String,com.study.rpc.test.producer.TestBean",
         *   "parameter":{
         *      "java.lang.String":"haha",
         *      "com.study.rpc.test.producer.TestBean":{
         *              "name":"小王",
         *              "age":20
         *        }
         *    }
         * }
         */

        JSONObject jsonObject = JSONObject.parseObject(message);
        String interfaces = jsonObject.getString("interfaces");

        JSONObject parameter = jsonObject.getJSONObject("parameter");
        Set<String> strings = parameter.keySet();
        RpcRequest request = new RpcRequest();
        request.setInterfaceIdentity(interfaces);
        Map<String,Object> parameterMap = new HashMap<>(16);

        String requestId = jsonObject.getString("requestId");

        for(String key : strings){
            if(key.equals("java.lang.String")){
                parameterMap.put(key,parameter.getString(key));
            }else{
                Class clazz = Class.forName(key);
                Object object = parameter.getObject(key,clazz);
                parameterMap.put(key,object);
            }
        }

        request.setParamterMap(parameterMap);
        request.setCtx(ctx);
        request.setRequestId(requestId);
        return request;
    }


    public String getInterfaceIdentity() {
        return interfaceIdentity;
    }

    public void setInterfaceIdentity(String interfaceIdentity) {
        this.interfaceIdentity = interfaceIdentity;
    }

    public Map<String, Object> getParamterMap() {
        return paramterMap;
    }

    public void setParamterMap(Map<String, Object> paramterMap) {
        this.paramterMap = paramterMap;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
