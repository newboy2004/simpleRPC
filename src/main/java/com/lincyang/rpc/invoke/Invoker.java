package com.lincyang.rpc.invoke;

/**
 * @Author lincyang
 * @Date 2019/8/20 3:20 PM
 **/
public interface Invoker<T> {

    T invoke(Object[] args);

    void setResult(String result);
}
