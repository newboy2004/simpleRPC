package com.lincyang.rpc.registry;

import java.util.Objects;

/**
 * 注册中心配置信息
 * @Author lincyang
 * @Date 2019/8/19 9:49 AM
 **/
public class RegistryInfo {

    private String hostName;
    private String ip;
    private Integer port;

    public RegistryInfo(String hostName, String ip,Integer port) {
        this.hostName = hostName;
        this.ip = ip;
        this.port = port;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "RegistryInfo{" + "hostName='" + hostName + '\'' + ", ip='" + ip + '\'' + ", port=" + port + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegistryInfo that = (RegistryInfo) o;
        return Objects.equals(hostName, that.hostName) &&
                Objects.equals(ip, that.ip) &&
                Objects.equals(port, that.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostName, ip, port);
    }
}
