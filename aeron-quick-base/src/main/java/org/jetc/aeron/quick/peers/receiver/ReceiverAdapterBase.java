package org.jetc.aeron.quick.peers.receiver;

import org.jetc.aeron.quick.messaging.ReceiverBindingProvider;

public interface ReceiverAdapterBase<T> {
    ReceiverBindingProvider getBindings();
    void init(String name);
}
