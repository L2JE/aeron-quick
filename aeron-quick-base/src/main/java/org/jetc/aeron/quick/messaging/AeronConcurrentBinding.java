package org.jetc.aeron.quick.messaging;

import org.jetc.aeron.quick.messaging.subscription.ConcurrentSubscriptionMeta;

public record AeronConcurrentBinding<T>(String channel, int streamID, ConcurrentSubscriptionMeta<T> meta) {
}
