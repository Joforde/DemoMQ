package com.demomq.bucket;

import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.options.PutOptions;

import java.io.File;

public class JCloudsMinIOUploadExample {

    public static void main(String[] args) {
        // MinIO 配置信息
        String provider = "aws-s3"; // MinIO 兼容 AWS S3 API
        String identity = "identity";; // MinIO 用户的 Access Key
        String credential = "credential";; // MinIO 用户的 Secret Key
        String containerName = "pulsar-s3-test"; // 存储桶名称
        String blobName = "example-folder/uploaded-file.txt"; // 文件在存储桶中的路径
        String localFilePath = "/Users/macbookpro/test.txt"; // 本地文件路径
        String endpoint = "http://localhost:8080";

        // 初始化 BlobStore 上下文
        BlobStoreContext context = ContextBuilder.newBuilder(provider)
            .endpoint(endpoint) // 指定 MinIO 的 endpoint
            .credentials(identity, credential)
            .buildView(BlobStoreContext.class);

        try {
            BlobStore blobStore = context.getBlobStore();

            // 创建容器（如果不存在）
            if (!blobStore.containerExists(containerName)) {
                blobStore.createContainerInLocation(null, containerName);
                System.out.println("✅ 存储桶已创建：" + containerName);
            }

            // 创建并上传 Blob
            Blob blob = blobStore.blobBuilder(blobName)
                .payload(new File(localFilePath))
                .build();

            // 上传到 MinIO
            blobStore.putBlob(containerName, blob, PutOptions.Builder.multipart());
            System.out.println("✅ 文件上传成功！");
        } catch (Exception e) {
            System.err.println("❌ 上传失败: " + e.getMessage());
        } finally {
            context.close(); // 关闭上下文
        }
    }
}