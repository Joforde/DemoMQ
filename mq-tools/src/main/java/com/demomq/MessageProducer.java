package com.demomq;

import org.apache.pulsar.client.api.*;

import java.util.concurrent.CompletableFuture;

public interface MessageProducer<V> {
  CompletableFuture<MessageId> send(String key, V value) throws PulsarClientException;

  void close() throws PulsarClientException;
}

