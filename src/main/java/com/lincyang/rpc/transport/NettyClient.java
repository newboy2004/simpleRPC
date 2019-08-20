package com.lincyang.rpc.transport;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.ReferenceCountUtil;

/**
 * @Author lincyang
 * @Date 2019/8/20 1:56 PM
 **/
public class NettyClient {

    private ChannelHandlerContext ctx;

    private MessageCallback messageCallBack;

    public NettyClient(String ip,Integer port){
        EventLoopGroup group = new NioEventLoopGroup();
        try{
            Bootstrap b  = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ByteBuf delimeter = Unpooled.copiedBuffer("$$".getBytes());
                            ch.pipeline().addLast(new DelimiterBasedFrameDecoder(2014 * 1024,delimeter));
                            ch.pipeline().addLast(new StringDecoder());
                            ch.pipeline().addLast(new NettyClientHandler());
                        }
                    });
            ChannelFuture sync = b.connect(ip, port).sync();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setMessageCallBack(MessageCallback messageCallBack) {
        this.messageCallBack = messageCallBack;
    }

    public ChannelHandlerContext getCtx() throws InterruptedException {
        System.out.println("等待连接成功。。。。");
        if(ctx == null){
            synchronized (this){
                wait();
            }
        }
        return ctx;
    }

    private class NettyClientHandler extends ChannelInboundHandlerAdapter{
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            try{
                String message = (String) msg;
                if(messageCallBack != null){
                    messageCallBack.onMessage(message);
                }
            }finally {
                ReferenceCountUtil.release(msg);
            }
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            NettyClient.this.ctx = ctx;
            System.out.println("连接成功："+ctx);
            synchronized (NettyClient.this){
                NettyClient.this.notifyAll();
            }
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
        }
    }



    public interface MessageCallback{
        void onMessage(String message);
    }
}
