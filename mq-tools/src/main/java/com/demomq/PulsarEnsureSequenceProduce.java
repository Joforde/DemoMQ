package com.demomq;

import org.apache.pulsar.client.api.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Producer with strict message ordering
 * Suitable for scenarios requiring strict message ordering, like order processing, state changes etc.
 */
class PulsarEnsureSequenceProduce<V> implements MessageProducer<V> {
    private final Producer<V> producer;
    private final Schema<V> schema;

    public PulsarEnsureSequenceProduce(PulsarClient client, String topic, Schema<V> schema)
        throws PulsarClientException {
        this.schema = schema;
        this.producer = client.newProducer(schema).topic(topic).enableBatching(false)
            .messageRoutingMode(MessageRoutingMode.SinglePartition).sendTimeout(0, TimeUnit.MILLISECONDS)
            .blockIfQueueFull(true).maxPendingMessages(1).create();
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

