package org.jetc.aeron.quick.server.precompile;

import org.jetc.aeron.quick.messaging.ReceiverBindingProvider;

public interface ServerAdapterBase<T> {
    ReceiverBindingProvider getBindings();
}
