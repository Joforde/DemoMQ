package com.demomq;

import org.apache.pulsar.client.api.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public interface MessageProducer<V> {
  CompletableFuture<MessageId> send(String key, V value) throws PulsarClientException;

  void close() throws PulsarClientException;
}

/**
 * 严格保证顺序的生产方法
 * 适用于需要严格保证消息顺序的场景，如订单处理、状态变更等
 */
class PulsarEnsureSequenceProduce<V> implements MessageProducer<V> {
  private final Producer<V> producer;
  private final Schema<V> schema;

  public PulsarEnsureSequenceProduce(PulsarClient client, String topic, Schema<V> schema) throws PulsarClientException {
    this.schema = schema;
    this.producer = client.newProducer(schema).topic(topic).enableBatching(false).sendTimeout(0, TimeUnit.MILLISECONDS).blockIfQueueFull(true).maxPendingMessages(1).create();
  }

  @Override
  public CompletableFuture<MessageId> send(String key, V value) {
    try {
      TypedMessageBuilder<V> messageBuilder = producer.newMessage(schema).value(value);
      if (key != null) {
        messageBuilder.key(key);
      }
      MessageId messageId = messageBuilder.send();
      return CompletableFuture.completedFuture(messageId);
    } catch (PulsarClientException e) {
      CompletableFuture<MessageId> future = new CompletableFuture<>();
      future.completeExceptionally(e);
      return future;
    }
  }

  @Override
  public void close() throws PulsarClientException {
    if (producer != null) {
      producer.close();
    }
  }
}

/**
 * 低延迟生产方法
 * 适用于对延迟敏感的场景，如实时通知、实时计算等
 */
class PulsarLowLatencyProduce<V> implements MessageProducer<V> {
  private final Producer<V> producer;
  private final Schema<V> schema;

  public PulsarLowLatencyProduce(PulsarClient client, String topic, Schema<V> schema) throws PulsarClientException {
    this.schema = schema;
    this.producer = client.newProducer(schema).topic(topic).enableBatching(true).batchingMaxPublishDelay(5, TimeUnit.MILLISECONDS).sendTimeout(5, TimeUnit.MILLISECONDS).blockIfQueueFull(true).maxPendingMessages(1000).create();
  }

  @Override
  public CompletableFuture<MessageId> send(String key, V value) {
    TypedMessageBuilder<V> messageBuilder = producer.newMessage(schema).value(value);
    if (key != null) {
      messageBuilder.key(key);
    }
    return messageBuilder.sendAsync();
  }

  @Override
  public void close() throws PulsarClientException {
    if (producer != null) {
      producer.close();
    }
  }
}

/**
 * 高吞吐量生产方法
 * 适用于大量数据写入场景，如日志收集、数据同步等
 */
class PulsarHighThroughputProduce<V> implements MessageProducer<V> {
  private final Producer<V> producer;
  private final Schema<V> schema;

  public PulsarHighThroughputProduce(PulsarClient client, String topic, Schema<V> schema) throws PulsarClientException {
    this.schema = schema;
    this.producer = client.newProducer(schema).topic(topic).enableBatching(true).batchingMaxPublishDelay(10, TimeUnit.MILLISECONDS).batchingMaxMessages(1000).batchingMaxBytes(128 * 1024).sendTimeout(30, TimeUnit.SECONDS).blockIfQueueFull(true).maxPendingMessages(10000).create();
  }

  @Override
  public CompletableFuture<MessageId> send(String key, V value) {
    TypedMessageBuilder<V> messageBuilder = producer.newMessage(schema).value(value);
    if (key != null) {
      messageBuilder.key(key);
    }
    return messageBuilder.sendAsync();
  }

  @Override
  public void close() throws PulsarClientException {
    if (producer != null) {
      producer.close();
    }
  }
}