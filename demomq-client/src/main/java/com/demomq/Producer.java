package com.demomq;

import com.demomq.v0.Message;
import com.demomq.v0.MessagingServiceGrpc;
import com.demomq.v0.SendMessageRequest;
import com.demomq.v0.SendMessageResponse;
import com.google.protobuf.ByteString;
import io.grpc.Channel;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Producer {

    private static final Logger logger = Logger.getLogger(Producer.class.getName());

    private final MessagingServiceGrpc.MessagingServiceBlockingStub blockingStub;


    /**
     * Construct client for accessing HelloWorld server using the existing channel.
     */
    public Producer(Channel channel) {
        // 'channel' here is a Channel, not a ManagedChannel, so it is not this code's responsibility to
        // shut it down.

        // Passing Channels to code makes code easier to test and makes it easier to reuse Channels.
        blockingStub = MessagingServiceGrpc.newBlockingStub(channel);
    }

    public static void main(String[] args) throws Exception {
        String target = "localhost:6650";
        // Create a communication channel to the server, known as a Channel. Channels are thread-safe
        // and reusable. It is common to create channels at the beginning of your application and reuse
        // them until the application shuts down.
        //
        // For the example we use plaintext insecure credentials to avoid needing TLS certificates. To
        // use TLS, use TlsChannelCredentials instead.
        ManagedChannel channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create()).build();
        try {
            Producer client = new Producer(channel);
            while (true){
                client.sendMessage("hello world");
                TimeUnit.SECONDS.sleep(1);
            }
        } finally {
            // ManagedChannels use resources like threads and TCP connections. To prevent leaking these
            // resources the channel should be shut down when it will no longer be used. If it may be used
            // again leave it running.
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    public void sendMessage(String message) {
        SendMessageResponse response = blockingStub.sendMessage(SendMessageRequest.newBuilder()
                .addMessages(Message.newBuilder()
                        .setBody(ByteString.copyFromUtf8(message))
                        .build()).build());
        logger.info(response.toString());
    }
}
