package com.demomq.fileStore;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.concurrent.TimeUnit;

public class ReadFileBenchmark {
    // 读取缓冲区大小（单位：字节）
    static final int readCapacity = 102400;
    static final ByteBuf readBuffer = Unpooled.buffer(readCapacity);

    // 当前缓冲区对应的文件起始位置
    static long readBufferStartPosition = -1;

    public static void main(String[] args) throws IOException, InterruptedException {
        // 替换为实际文件路径
        String filePath = "/path/data.log";

        // 打开文件
        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("文件不存在: " + filePath);
            return;
        }

        try (RandomAccessFile raf = new RandomAccessFile(file, "r");
             FileChannel fileChannel = raf.getChannel()) {

            // 目标缓冲区（用于接收读取的数据）
            ByteBuf dest = Unpooled.buffer(1024); // 假设最多读取 1024 字节
            long pos = 0; // 从文件开头读取
            int length = 10240; // 读取 1024 字节

            // 调用读取方法
            int bytesRead = read(dest, pos, length, fileChannel);
            System.out.println("实际读取字节数: " + bytesRead);
            System.out.println("每次读取的数据块大小（readCapacity）: " + readCapacity + " 字节");

            // 打印前 10 字节验证读取内容
            byte[] data = new byte[Math.min(dest.readableBytes(), 10)];
            dest.getBytes(0, data);
            System.out.println("读取内容（前 10 字节）: " + new String(data));
        }
    }

    /**
     * 从文件中读取数据到目标缓冲区
     *
     * @param dest     目标缓冲区
     * @param pos      文件读取起始位置
     * @param length   要读取的字节数
     * @param fileChannel 文件通道
     * @return 实际读取的字节数，或 -1 表示 EOF
     * @throws IOException 读取异常
     */
    public static int read(ByteBuf dest, long pos, int length, FileChannel fileChannel) throws IOException, InterruptedException {
        long currentPosition = pos;
        int totalBytesRead = 0;

        while (length > 0) {
            // 检查是否命中缓存
            if (readBufferStartPosition != -1 &&
                currentPosition >= readBufferStartPosition &&
                currentPosition < readBufferStartPosition + readBuffer.readableBytes()) {
                // 计算在缓冲区中的偏移量
                int posInBuffer = (int) (currentPosition - readBufferStartPosition);
                int bytesToCopy = Math.min(length, readBuffer.readableBytes() - posInBuffer);
                dest.writeBytes(readBuffer, posInBuffer, bytesToCopy);
                currentPosition += bytesToCopy;
                length -= bytesToCopy;
                totalBytesRead += bytesToCopy;
                System.out.println("命中缓存，复制 " + bytesToCopy + " 字节");
            } else {
                // 缓存未命中，重新填充缓冲区
                readBufferStartPosition = currentPosition;
                readBuffer.clear(); // 清空缓冲区，准备写入新数据
                int readBytes = fileChannel.read(readBuffer.internalNioBuffer(0, readCapacity), currentPosition);
                if (readBytes <= 0) {
                    // EOF 或读取失败
                    break;
                }
                readBuffer.writerIndex(readBytes);
                System.out.println("读取文件，填充 " + readBytes + " 字节到缓冲区");
//                TimeUnit.SECONDS.sleep(1);
                // 从缓冲区复制数据到目标
                int bytesToCopy = Math.min(length, readBytes);
                dest.writeBytes(readBuffer, 0, bytesToCopy);
                currentPosition += bytesToCopy;
                length -= bytesToCopy;
                totalBytesRead += bytesToCopy;
            }
        }

        return totalBytesRead;
    }
}
