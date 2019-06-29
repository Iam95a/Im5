package com.example.im5.im.client;

import com.chen.im.common.protobuf.RequestMessageProto;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;

public class ImClient {
    private final String host;
    private final int port;

    public static Channel channel;

    public static void sendRequest(RequestMessageProto.RequestMessage requestMessage) {
        channel.writeAndFlush(requestMessage);
    }

    public static EventLoopGroup group = new NioEventLoopGroup();

    public static Thread exeThread;

    public static void startIm() {
        if (channel != null && channel.isOpen()) {
            //todo 判断是否登陆
        } else {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        new ImClient("192.168.1.24", 8888).start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            exeThread = thread;
            thread.start();
        }
    }


    public ImClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();                //1
            b.group(group)                                //2
                    .channel(NioSocketChannel.class)            //3
                    .remoteAddress(new InetSocketAddress(host, port))    //4
                    .handler(new ChannelInitializer<SocketChannel>() {    //5
                        @Override
                        public void initChannel(SocketChannel ch)
                                throws Exception {
                            // 实体类传输数据，protobuf序列化
                            ch.pipeline().addLast("decoder",
                                    new ProtobufDecoder(RequestMessageProto.RequestMessage.getDefaultInstance()));
                            ch.pipeline().addLast("encoder",
                                    new ProtobufEncoder());
                            ch.pipeline().addLast(
                                    new EchoClientHandler());
                        }
                    });

            ChannelFuture f = b.connect().sync();        //6
            f.channel().closeFuture().sync();            //7
        } finally {
            group.shutdownGracefully().sync();            //8
        }
    }

    public static void main(String[] args) throws Exception {


    }
}
