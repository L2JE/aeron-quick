package org.jetc.aeron.quick.peers.receiver;

import io.aeron.Aeron;
import org.jetc.aeron.quick.AeronQuickBuilder;
import org.jetc.aeron.quick.messaging.ReceiverBindingProvider;

public class AeronQuickReceiverBuilder<T> extends AeronQuickBuilder<AeronQuickReceiverRunner<T>> {
    private final ReceiverAdapterBase<?> serverEntrypoint;
    private String receiverName;

    public <E extends T> AeronQuickReceiverBuilder(ReceiverAdapterBase<E> serverEntrypoint) {
        this.serverEntrypoint = serverEntrypoint;
    }

    public ReceiverBindingProvider getReceiverBindingsProvider(){
        return serverEntrypoint.getBindings();
    }

    @Override
    public AeronQuickReceiverRunner<T> build() {
        serverEntrypoint.init(receiverName);
        return new AeronQuickReceiverRunner<>(aeron, getReceiverBindingsProvider(), getAgentIdleStrategy(), getAgentErrorHandler(), getAgentErrorCounter());
    }

    public AeronQuickReceiverBuilder<T> setReceiverName(String receiverName) {
        this.receiverName = receiverName;
        return this;
    }

    public AeronQuickReceiverBuilder<T> setAeron(Aeron aeron) {
        this.aeron = aeron;
        return this;
    }
}
