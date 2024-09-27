package org.jetc.aeron.quick.server;

import io.aeron.Aeron;
import org.jetc.aeron.quick.AeronQuickBuilder;
import org.jetc.aeron.quick.messaging.ReceiverBindingProvider;
import org.jetc.aeron.quick.server.precompile.ReceiverAdapterBase;

public class AeronQuickReceiverBuilder<T> extends AeronQuickBuilder<AeronQuickReceiverRunner<T>> {
    private final ReceiverAdapterBase<?> serverEntrypoint;

    public <E extends T> AeronQuickReceiverBuilder(ReceiverAdapterBase<E> serverEntrypoint, Class<T> contract, Aeron aeron) {
        super(aeron);
        this.serverEntrypoint = serverEntrypoint;
    }

    public ReceiverBindingProvider getReceiverBindingsProvider(){
        return this.serverEntrypoint.getBindings();
    }

    @Override
    public AeronQuickReceiverRunner<T> build() {
        return new AeronQuickReceiverRunner<>(this.aeron, getReceiverBindingsProvider(), getAgentIdleStrategy(), getAgentErrorHandler(), getAgentErrorCounter());
    }
}
