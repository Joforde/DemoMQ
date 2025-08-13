package com.demomq.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;

// 消息编码器
class MessageEncoder extends MessageToByteEncoder<Message> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) {
        // 写入序列号和内容长度
        out.writeLong(msg.getSequenceId());
        byte[] contentBytes = msg.getContent().getBytes(StandardCharsets.UTF_8);
        out.writeInt(contentBytes.length);
        out.writeBytes(contentBytes);

        System.out.println("[" + Thread.currentThread().getName() + "] Encoded: " + msg);
    }
}
