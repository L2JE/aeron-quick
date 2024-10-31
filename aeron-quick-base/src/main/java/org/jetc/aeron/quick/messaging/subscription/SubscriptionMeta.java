package org.jetc.aeron.quick.messaging.subscription;

import io.aeron.logbuffer.FragmentHandler;

public record SubscriptionMeta(FragmentHandler handler, int fragmentLimit){
}
