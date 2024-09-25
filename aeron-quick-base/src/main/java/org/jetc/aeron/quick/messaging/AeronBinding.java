package org.jetc.aeron.quick.messaging;

import org.jetc.aeron.quick.messaging.subscription.SubscriptionMeta;

public record AeronBinding(String channel, int streamID, SubscriptionMeta meta){

}