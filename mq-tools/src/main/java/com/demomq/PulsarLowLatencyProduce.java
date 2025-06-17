package com.demomq;

import org.apache.pulsar.client.api.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Low latency producer
 * Suitable for latency-sensitive scenarios like real-time notifications, real-time computing etc.
 */
class PulsarLowLatencyProduce<V> implements MessageProducer<V> {
    private final Producer<V> producer;
    private final Schema<V> schema;

    public PulsarLowLatencyProduce(PulsarClient client, String topic, Schema<V> schema)
        throws PulsarClientException {
        this.schema = schema;
        this.producer = client.newProducer(schema).topic(topic).enableBatching(true)
            .batchingMaxPublishDelay(1, TimeUnit.MILLISECONDS)
            .sendTimeout(5, TimeUnit.MILLISECONDS).blockIfQueueFull(true).maxPendingMessages(100).create();
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
