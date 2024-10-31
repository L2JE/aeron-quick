package org.jetc.aeron.quick.messaging;

import java.util.List;

public interface BindingProvider {
    /**
     * @return the list of bindings with fragment handlers that take the aeron messages and distribute them among the threads on the pool
     */
    List<AeronBinding> getBindings();
}
