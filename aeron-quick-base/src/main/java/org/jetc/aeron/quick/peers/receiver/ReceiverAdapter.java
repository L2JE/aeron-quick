package org.jetc.aeron.quick.peers.receiver;

import org.jetc.aeron.quick.AeronQuickContext;
import org.jetc.aeron.quick.messaging.AeronBindingHolder;
import org.jetc.aeron.quick.messaging.BindingAppender;
import org.jetc.aeron.quick.messaging.BindingProvider;
import org.jetc.aeron.quick.messaging.serialization.ObjectStringMapper;
import org.jetc.aeron.quick.peers.ConfigurablePeer;

public abstract class ReceiverAdapter<E, B> implements ConfigurablePeer<ReceiverConfiguration<E>>, BindingProvider {
    protected AeronBindingHolder allBindings;

    @Override
    public void configure(ReceiverConfiguration<E> config) {
        final AeronQuickContext ctx = config.getContext();
        final ObjectStringMapper mapper = ctx.getObjectMapper();
        final String receiverName = config.getComponentName();
        final E server = config.getEndpoint();
        allBindings = config.getUserProvidedBindings();

        registerBindings(getBindingAppender(config), ctx, mapper, receiverName, server);
        finishConfiguration(server);
    }

    abstract protected BindingAppender<B> getBindingAppender(ReceiverConfiguration<E> config);
    abstract protected void registerBindings(BindingAppender<B> bindings, AeronQuickContext ctx, ObjectStringMapper mapper, String receiverName, E server);
    abstract protected void finishConfiguration(E server);

}
