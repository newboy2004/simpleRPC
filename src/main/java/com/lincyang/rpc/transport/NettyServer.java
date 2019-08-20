package com.lincyang.rpc.transport;

import com.lincyang.rpc.config.ServiceCofig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * @Author lincyang
 * @Date 2019/8/19 10:37 AM
 **/
public class NettyServer {

    /**
     * 负责调用方法的handler
     */
    private RpcInvokeHandler rpcInvokeHandler;

    public NettyServer(List<ServiceCofig> serviceCofigs, Map<String, Method> interfaceMethods) {
        this.rpcInvokeHandler = new RpcInvokeHandler(serviceCofigs,interfaceMethods);
    }

    public int init(int port) throws  Exception{
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup,workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG,1024)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception{
                        ByteBuf delimiter = Unpooled.copiedBuffer("$$".getBytes());
                        ch.pipeline().addLast(new DelimiterBasedFrameDecoder(10244 * 1024,delimiter));
                        ch.pipeline().addLast(new StringDecoder());
                        ch.pipeline().addLast(rpcInvokeHandler);
                    }
                });
        ChannelFuture sync = b.bind(port).sync();
        System.out.println("启动 NettyService,端口为："+port);
        return port;
    }
}
