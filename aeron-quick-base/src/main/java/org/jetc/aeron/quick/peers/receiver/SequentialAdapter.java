package org.jetc.aeron.quick.peers.receiver;

import org.jetc.aeron.quick.messaging.AeronBinding;
import org.jetc.aeron.quick.messaging.BindingAppender;
import org.jetc.aeron.quick.messaging.subscription.SubscriptionMeta;

import java.util.List;

public abstract class SequentialAdapter<E> extends ReceiverAdapter<E, SubscriptionMeta> {
    @Override
    protected BindingAppender<SubscriptionMeta> getBindingAppender(ReceiverConfiguration<E> config) {
        return allBindings;
    }

    @Override
    protected void finishConfiguration(E server) {}

    @Override
    public List<AeronBinding> getBindings() {
        return allBindings.getBindings();
    }
}
