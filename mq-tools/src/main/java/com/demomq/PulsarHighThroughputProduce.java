package com.demomq;

import org.apache.pulsar.client.api.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * High throughput producer
 * Suitable for high volume data writing scenarios like log collection, data synchronization etc.
 */
class PulsarHighThroughputProduce<V> implements MessageProducer<V> {
    private final Producer<V> producer;
    private final Schema<V> schema;

    public PulsarHighThroughputProduce(PulsarClient client, String topic, Schema<V> schema)
        throws PulsarClientException {
        this.schema = schema;
        this.producer = client.newProducer(schema).topic(topic).enableBatching(true)
            .batchingMaxPublishDelay(10, TimeUnit.MILLISECONDS)
            .batchingMaxMessages(1000)
            .batchingMaxBytes(128 * 1024).sendTimeout(30, TimeUnit.SECONDS).blockIfQueueFull(true)
            .maxPendingMessages(10000).create();
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
