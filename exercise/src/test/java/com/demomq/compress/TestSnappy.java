package com.demomq.compress;

/**
 * 复现问题
 * snappy 执行时，从 jar 包中提取并验证 native 库，写入 /tmp 目录。/tmp 目录的权限问题，导致开启压缩的客户端写入失败
 * chmod 000 /home/infra/tmp
 * su infra
 * javac -cp "/path/libs/snappy-java-1.1.8.1.jar" TestSnappy.java
 * java -Dorg.xerial.snappy.tempdir=/home/infra/tmp -Dorg.xerial.snappy.debug=true -cp ".:/path/libs/snappy-java-1.1.8.1.jar" TestSnappy


 java.io.FileNotFoundException: /home/infra/tmp/snappy-1.1.8-9ecccdc0-d0ef-44b6-b58d-b86adbb43380-libsnappyjava.so (Permission denied)
 at java.base/java.io.FileOutputStream.open0(Native Method)
 at java.base/java.io.FileOutputStream.open(FileOutputStream.java:293)
 at java.base/java.io.FileOutputStream.<init>(FileOutputStream.java:235)
 at java.base/java.io.FileOutputStream.<init>(FileOutputStream.java:184)
 at org.xerial.snappy.SnappyLoader.extractLibraryFile(SnappyLoader.java:258)
 at org.xerial.snappy.SnappyLoader.findNativeLibrary(SnappyLoader.java:374)
 at org.xerial.snappy.SnappyLoader.loadNativeLibrary(SnappyLoader.java:195)
 at org.xerial.snappy.SnappyLoader.loadSnappyApi(SnappyLoader.java:167)
 at org.xerial.snappy.Snappy.init(Snappy.java:69)
 at org.xerial.snappy.Snappy.<clinit>(Snappy.java:46)
 at TestSnappy.main(TestSnappy.java:22)
 Exception in thread "main" java.lang.UnsatisfiedLinkError: 'int org.xerial.snappy.SnappyNative.maxCompressedLength(int)'
 at org.xerial.snappy.SnappyNative.maxCompressedLength(Native Method)
 at org.xerial.snappy.Snappy.maxCompressedLength(Snappy.java:380)
 at org.xerial.snappy.Snappy.rawCompress(Snappy.java:423)
 at org.xerial.snappy.Snappy.compress(Snappy.java:105)
 at TestSnappy.main(TestSnappy.java:22)
 */
public class TestSnappy {
    public static void main(String[] args) {
        try {
            System.out.println("测试 Snappy 压缩功能...");
            byte[] testData = "Hello, Snappy!".getBytes();
            byte[] compressed = org.xerial.snappy.Snappy.compress(testData);
            byte[] decompressed = org.xerial.snappy.Snappy.uncompress(compressed);

            System.out.println("原始数据长度: " + testData.length);
            System.out.println("压缩后长度: " + compressed.length);
            System.out.println("解压后长度: " + decompressed.length);
            System.out.println("测试成功！");

        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

