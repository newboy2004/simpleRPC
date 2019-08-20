package com.lincyang.rpc.transport;

/**
 * 相应请求包装
 * @Author lincyang
 * @Date 2019/8/19 12:33 PM
 **/
public class RpcResponse {

    private String result;

    private String interfaceMethodIdentify;

    private String requestId;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getInterfaceMethodIdentify() {
        return interfaceMethodIdentify;
    }

    public void setInterfaceMethodIdentify(String interfaceMethodIdentify) {
        this.interfaceMethodIdentify = interfaceMethodIdentify;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public static RpcResponse create(String result,String interfaceMethodIdentify,String requestId){
        RpcResponse response = new RpcResponse();
        response.setRequestId(requestId);
        response.setInterfaceMethodIdentify(interfaceMethodIdentify);
        response.setResult(result);
        return response;
    }
}
