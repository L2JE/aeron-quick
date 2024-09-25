package org.jetc.aeron.quick.messaging.subscription;

import org.jetc.aeron.quick.messaging.fragment_handling.ContextualHandler;

public record SubscriptionMeta(ContextualHandler handlerProvider, int fragmentLimit){
}
