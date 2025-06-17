package com.demomq;

import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;

public class ProducerFactory {
    public static <V> MessageProducer createMessageProducer(ProducerType producerType, PulsarClient pulsarClient,
                                                            String topic, Schema<V> schema)
        throws PulsarClientException {
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
         * Creates a producer with strict message ordering
         * Suitable for scenarios requiring strict message ordering, like order processing, state changes etc.
         */
        SEQUENCE,

        /**
         * Creates a low latency producer
         * Suitable for latency-sensitive scenarios like real-time notifications, real-time computing etc.
         */
        LOW_LATENCY,

        /**
         * Creates a high throughput producer
         * Suitable for high volume data writing scenarios like log collection, data synchronization etc.
         */
        HIGH_THROUGHPUT
    }
}
