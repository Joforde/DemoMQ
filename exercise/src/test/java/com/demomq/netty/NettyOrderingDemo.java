package com.demomq.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

public class NettyOrderingDemo {

    private static final AtomicLong messageId = new AtomicLong(0);
    private static final EventExecutorGroup businessGroup = new DefaultEventExecutorGroup(1);

    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup(1); // 单线程EventLoop

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                            .addLast(new MessageEncoder())
                            .addLast(new MessageDecoder())
                            .addLast(new ClientHandler());
                    }
                });

            Channel channel = bootstrap.connect("localhost", 8080).sync().channel();

            // 模拟Pulsar的发送逻辑
            simulatePulsarSending(channel);

            // 等待所有消息处理完成
            Thread.sleep(5000);

        } finally {
            group.shutdownGracefully();
            businessGroup.shutdownGracefully();
        }
    }

    private static void simulatePulsarSending(Channel channel) {
        // 模拟快速连续发送多个消息
        for (int i = 0; i < 10; i++) {
            final int index = i;
            // 在EventLoop中发送，模拟Pulsar的发送逻辑
            channel.eventLoop().execute(() -> {
                Message msg = new Message(messageId.incrementAndGet(), "Message-" + index);
                System.out.println("[" + Thread.currentThread().getName() + "] Sending: " + msg);
                channel.writeAndFlush(msg);
            });

            // 模拟网络延迟
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }


    // 客户端处理器
    static class ClientHandler extends SimpleChannelInboundHandler<Message> {
        private final CountDownLatch latch = new CountDownLatch(1);

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
            // 模拟ACK处理
            System.out.println("[" + Thread.currentThread().getName() + "] Received ACK: " + msg);

            // 模拟业务处理
            businessGroup.execute(() -> {
                System.out.println("[" + Thread.currentThread().getName() + "] Processing ACK: " + msg);
                // 模拟处理时间
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("[" + Thread.currentThread().getName() + "] Finished processing ACK: " + msg);
            });
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            System.out.println("[" + Thread.currentThread().getName() + "] Channel connected");
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}


