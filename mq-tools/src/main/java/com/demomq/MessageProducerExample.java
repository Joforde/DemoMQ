package com.demomq;

import org.apache.pulsar.client.api.*;

import java.util.concurrent.CompletableFuture;

public class MessageProducerExample {
  public static void main(String[] args) {
    try {
      PulsarClient pulsarClient = PulsarClient.builder().serviceUrl("pulsar://localhost:6650").memoryLimit(100, SizeUnit.MEGA_BYTES).connectionsPerBroker(1).build();
      MessageProducer messageProducer = ProducerFactory.createMessageProducer(ProducerFactory.ProducerType.SEQUENCE, pulsarClient, "my-topic", Schema.STRING);
      for (int i = 0; i < 10; i++) {
        CompletableFuture<MessageId> res = messageProducer.send("order-1", "order data");
        res.whenComplete((messageId, throwable) -> {
          if (throwable != null) {
            throwable.printStackTrace();
          } else {
            System.out.println(messageId);
          }
        });
      }
      messageProducer.close();
    } catch (PulsarClientException e) {
      e.printStackTrace();
    }
  }
}
