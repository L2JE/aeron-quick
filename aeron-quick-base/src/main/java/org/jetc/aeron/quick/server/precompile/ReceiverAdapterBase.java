package org.jetc.aeron.quick.server.precompile;

import org.jetc.aeron.quick.messaging.ReceiverBindingProvider;

public interface ReceiverAdapterBase<T> {
    ReceiverBindingProvider getBindings();
}
