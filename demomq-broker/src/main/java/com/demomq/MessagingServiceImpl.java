package com.demomq;

import com.demomq.v0.Message;
import com.demomq.v0.MessagingServiceGrpc;
import com.demomq.v0.SendMessageRequest;
import com.demomq.v0.SendMessageResponse;
import io.grpc.stub.StreamObserver;

import java.util.List;
import java.util.logging.Logger;

public class MessagingServiceImpl extends MessagingServiceGrpc.MessagingServiceImplBase {
    private static final Logger logger = Logger.getLogger(MessagingServiceImpl.class.getName());

    @Override
    public void sendMessage(SendMessageRequest request, StreamObserver<SendMessageResponse> responseObserver) {
        List<Message> messagesList = request.getMessagesList();
        messagesList.forEach(message -> {
            String topic = message.getTopic();
            String value = message.getBody().toStringUtf8();
            logger.info("received messages:" + topic + ":" + value);
        });
        responseObserver.onNext(SendMessageResponse.newBuilder().setStatus("200").build());
        responseObserver.onCompleted();
    }
}
