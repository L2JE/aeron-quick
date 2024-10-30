package org.jetc.aeron.quick.peers.receiver;

import org.jetc.aeron.quick.AeronQuickContext;
import org.jetc.aeron.quick.messaging.AeronBindingHolder;
import org.jetc.aeron.quick.messaging.BindingAppender;
import org.jetc.aeron.quick.messaging.fragment_handling.concurrent.OperationExecutorPoolConfiguration;
import org.jetc.aeron.quick.messaging.subscription.SubscriptionMeta;
import org.jetc.aeron.quick.peers.PeerConfiguration;

public class ReceiverConfiguration<E> implements PeerConfiguration, BindingAppender<SubscriptionMeta> {
    private E targetEndpoint;
    private AeronQuickContext context;
    private String componentName;
    private OperationExecutorPoolConfiguration<E> poolBuilder;
    private final AeronBindingHolder singleThreadedBindings = new AeronBindingHolder();

    @Override
    public AeronQuickContext getContext() {
        return context;
    }

    @Override
    public String getComponentName() {
        return componentName;
    }

    public E getEndpoint() {
        return targetEndpoint;
    }

    @Override
    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    @Override
    public void setContext(AeronQuickContext context) {
        this.context = context;
    }

    public void setEndpoint(E targetInstance) {
        this.targetEndpoint = targetInstance;
    }

    @Override
    public void addBinding(String channel, int streamID, SubscriptionMeta handler) {
        singleThreadedBindings.addBinding(channel, streamID, handler);
    }

    public OperationExecutorPoolConfiguration<E> getEventProcessingPoolConfig() {
        if(this.poolBuilder == null)
            this.poolBuilder = new OperationExecutorPoolConfiguration<>(context, componentName);
        return this.poolBuilder;
    }

    public AeronBindingHolder getUserProvidedBindings() {
        return singleThreadedBindings;
    }
}
