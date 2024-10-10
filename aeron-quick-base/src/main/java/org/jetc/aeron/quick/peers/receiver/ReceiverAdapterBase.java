package org.jetc.aeron.quick.peers.receiver;

import org.jetc.aeron.quick.messaging.ReceiverBindingToAeronBindingMapper;
import org.jetc.aeron.quick.peers.ContextDependantComponent;

public interface ReceiverAdapterBase<T> extends ContextDependantComponent {
    ReceiverBindingToAeronBindingMapper getBindings();
}
