package com.demomq;

import com.demomq.v0.Message;
import com.demomq.v0.MessagingServiceGrpc;
import com.demomq.v0.SendMessageRequest;
import com.demomq.v0.SendMessageResponse;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;

import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MessagingServiceImpl extends MessagingServiceGrpc.MessagingServiceImplBase {
    private static final Logger logger = Logger.getLogger(MessagingServiceImpl.class.getName());

    @Override
    public void sendMessage(SendMessageRequest request, StreamObserver<SendMessageResponse> responseObserver) {
        logger.info("received messages:" + String.join(",", request.getMessagesList().stream().map(Message::getBody).map(ByteString::toString).collect(Collectors.toSet())));
        responseObserver.onNext(SendMessageResponse.newBuilder().setStatus("200").build());
        responseObserver.onCompleted();
    }
}
