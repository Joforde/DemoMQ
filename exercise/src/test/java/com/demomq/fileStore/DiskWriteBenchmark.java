package com.demomq.fileStore;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicLong;

public class DiskWriteBenchmark {

    // 配置参数
    private static final String FILE_PATH = "/data3/testfile";  // 测试文件路径
    private static final int BLOCK_SIZE = 1024;                 // 写入块大小 (1KB)
    private static final long TEST_DURATION_MS = 10000;         // 测试持续时间 (60秒)
    private static final boolean FLUSH_TO_DISK = true;          // 是否强制刷盘

    public static void main(String[] args) {
        try (FileOutputStream fos = new FileOutputStream(FILE_PATH);
             FileChannel channel = fos.getChannel()) {

            // 初始化写入数据块
            ByteBuffer buffer = ByteBuffer.allocate(BLOCK_SIZE);
            for (int i = 0; i < buffer.capacity(); i++) {
                buffer.put((byte) 'a');  // 填充测试数据
            }
            buffer.flip();

            // 初始化统计
            AtomicLong totalBytes = new AtomicLong(0);
            AtomicLong totalLatency = new AtomicLong(0);
            AtomicLong writeCount = new AtomicLong(0);

            // 启动测试
            Instant startTime = Instant.now();
            Thread writerThread = new Thread(() -> {
                try {
                    while (Duration.between(startTime, Instant.now()).toMillis() < TEST_DURATION_MS) {
                        long start = System.nanoTime();
                        channel.write(buffer);
                        if (FLUSH_TO_DISK) {
                            channel.force(true);  // 强制刷盘
                        }
                        long end = System.nanoTime();
                        long latency = end - start;

                        totalBytes.addAndGet(BLOCK_SIZE);
                        totalLatency.addAndGet(latency);
                        writeCount.incrementAndGet();

                        buffer.rewind();  // 重置 buffer
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            writerThread.start();
            writerThread.join();

            // 输出结果
            long totalWrites = writeCount.get();
            long totalTimeMs = Duration.between(startTime, Instant.now()).toMillis();
            double iops = totalWrites / (totalTimeMs / 1000.0);
            double avgLatencyUs = (totalLatency.get() / 1000.0) / totalWrites;  // 转换为微秒

            System.out.println("=== 测试结果 ===");
            System.out.println("总写入次数: " + totalWrites);
            System.out.println("总耗时 (ms): " + totalTimeMs);
            System.out.println("IOPS: " + String.format("%.2f", iops));
            System.out.println("平均延迟 (µs): " + String.format("%.2f", avgLatencyUs));
            System.out.println("总写入数据量: " + (totalBytes.get() / (1024 * 1024)) + " MB");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 计算两个时间点之间的时间差
    public static Duration Duration(Instant start, Instant end) {
        return Duration.ofMillis(ChronoUnit.MILLIS.between(start, end));
    }
}