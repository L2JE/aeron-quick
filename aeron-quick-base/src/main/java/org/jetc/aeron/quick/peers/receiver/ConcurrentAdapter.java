package org.jetc.aeron.quick.peers.receiver;

import org.jetc.aeron.quick.messaging.AeronBinding;
import org.jetc.aeron.quick.messaging.BindingAppender;
import org.jetc.aeron.quick.messaging.fragment_handling.concurrent.OperationExecutorPool;
import org.jetc.aeron.quick.messaging.fragment_handling.concurrent.OperationExecutorPoolConfiguration;
import org.jetc.aeron.quick.messaging.subscription.ConcurrentSubscriptionMeta;
import java.util.List;

public abstract class ConcurrentAdapter<E> extends ReceiverAdapter<E, ConcurrentSubscriptionMeta<E>> {
    private OperationExecutorPoolConfiguration<E> poolConfiguration;
    private OperationExecutorPool pool;

    @Override
    protected BindingAppender<ConcurrentSubscriptionMeta<E>> getBindingAppender(ReceiverConfiguration<E> config) {
        poolConfiguration = config.getEventProcessingPoolConfig();
        return poolConfiguration;
    }

    @Override
    protected void finishConfiguration(E server) {
        pool = poolConfiguration
                .setGlobalEventHandler((event, sequence, endOfBatch) -> event.getExecutor().runMethod(server))
                .apply();
    }

    @Override
    public List<AeronBinding> getBindings() {
        allBindings.addAll(pool.getBindingHolder());
        pool.startPool();
        return allBindings.getBindings();
    }
}
