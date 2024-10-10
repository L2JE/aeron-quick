package org.jetc.aeron.quick.peers.receiver;

import org.jetc.aeron.quick.AeronQuickBuilder;
import org.jetc.aeron.quick.AeronQuickContext;
import org.jetc.aeron.quick.messaging.ReceiverBindingToAeronBindingMapper;

public class AeronQuickReceiverBuilder<T> extends AeronQuickBuilder<AeronQuickReceiverRunner<T>> {
    private final ReceiverAdapterBase<?> serverEntrypoint;
    private String receiverName;
    private final AeronQuickContext context;

    public <E extends T> AeronQuickReceiverBuilder(ReceiverAdapterBase<E> serverEntrypoint, AeronQuickContext context) {
        this.serverEntrypoint = serverEntrypoint;
        this.context = context;
    }

    public ReceiverBindingToAeronBindingMapper getReceiverBindingsProvider(){
        return serverEntrypoint.getBindings();
    }

    @Override
    public AeronQuickReceiverRunner<T> build() {
        serverEntrypoint.setContext(context,receiverName);
        return new AeronQuickReceiverRunner<>(context.getAeron(), getReceiverBindingsProvider(), getAgentIdleStrategy(), getAgentErrorHandler(), getAgentErrorCounter());
    }

    public AeronQuickReceiverBuilder<T> setReceiverName(String receiverName) {
        this.receiverName = receiverName;
        return this;
    }
}
