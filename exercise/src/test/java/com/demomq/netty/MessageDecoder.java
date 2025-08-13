package com.demomq.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

// 消息解码器
class MessageDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < 12) { // long + int
            return;
        }

        in.markReaderIndex();
        long sequenceId = in.readLong();
        int contentLength = in.readInt();

        if (in.readableBytes() < contentLength) {
            in.resetReaderIndex();
            return;
        }

        byte[] contentBytes = new byte[contentLength];
        in.readBytes(contentBytes);
        String content = new String(contentBytes, StandardCharsets.UTF_8);

        Message msg = new Message(sequenceId, content);
        out.add(msg);

        System.out.println("[" + Thread.currentThread().getName() + "] Decoded: " + msg);
    }
}
