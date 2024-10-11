package org.jetc.aeron.quick.peers.receiver;

import org.jetc.aeron.quick.AeronQuickContext;
import org.jetc.aeron.quick.messaging.AeronBindingHolder;
import org.jetc.aeron.quick.peers.PeerConfiguration;

public class ReceiverConfiguration<E> extends AeronBindingHolder implements PeerConfiguration{
    private E targetEndpoint;
    private AeronQuickContext context;
    private String componentName;

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
}
