package com.demomq;

import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;

public class ProducerFactory {
  public static <V> MessageProducer createMessageProducer(ProducerType producerType, PulsarClient pulsarClient, String topic, Schema<V> schema) throws PulsarClientException {
    switch (producerType) {
      case SEQUENCE:
        return new PulsarEnsureSequenceProduce(pulsarClient, topic, schema);
      case LOW_LATENCY:
        return new PulsarLowLatencyProduce(pulsarClient, topic, schema);
      case HIGH_THROUGHPUT:
        return new PulsarHighThroughputProduce(pulsarClient, topic, schema);
      default:
        throw new IllegalArgumentException("Unsupported producer type: " + producerType);
    }
  }

  public enum ProducerType {

    /**
     * 创建严格保证顺序的生产者
     * 适用于需要严格保证消息顺序的场景，如订单处理、状态变更等
     */
    SEQUENCE,

    /**
     * 创建低延迟生产者
     * 适用于对延迟敏感的场景，如实时通知、实时计算等
     */
    LOW_LATENCY,

    /**
     * 创建高吞吐量生产者
     * 适用于大量数据写入场景，如日志收集、数据同步等
     */
    HIGH_THROUGHPUT
  }
}