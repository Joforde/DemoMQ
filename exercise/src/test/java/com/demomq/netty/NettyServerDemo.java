package com.demomq.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class NettyServerDemo {

    private static final EventExecutorGroup businessGroup = new DefaultEventExecutorGroup(4); // 多线程业务处理

    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(1); // 单线程处理连接

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                            .addLast(new MessageDecoder())
                            .addLast(new MessageEncoder())
                            .addLast(new ServerHandler());
                    }
                });

            ChannelFuture future = bootstrap.bind(8080).sync();
            System.out.println("Server started on port 8080");

            future.channel().closeFuture().sync();

        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            businessGroup.shutdownGracefully();
        }
    }

    // 服务端消息处理器
    static class ServerHandler extends SimpleChannelInboundHandler<Message> {
        private final AtomicLong counter = new AtomicLong(0);

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
            System.out.println("[" + Thread.currentThread().getName() + "] Received: " + msg);

            // 模拟服务端处理时间（可能不同）
            long processingTime = (msg.getSequenceId() % 3 + 1) * 100; // 100ms, 200ms, 300ms

            // 在业务线程中处理，模拟Pulsar broker的处理
            businessGroup.execute(() -> {
                try {
                    System.out.println("[" + Thread.currentThread().getName() + "] Processing: " + msg +
                        ", time: " + processingTime + "ms");
                    Thread.sleep(processingTime);

                    // 发送ACK响应
                    ctx.channel().eventLoop().execute(() -> {
                        Message ack = new Message(msg.getSequenceId(), "ACK-" + msg.getContent());
                        System.out.println("[" + Thread.currentThread().getName() + "] Sending ACK: " + ack);
                        ctx.writeAndFlush(ack);
                    });

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            System.out.println("[" + Thread.currentThread().getName() + "] Client connected");
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}