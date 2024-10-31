package org.jetc.aeron.quick.messaging.fragment_handling.concurrent;

import org.jetc.aeron.quick.messaging.AeronBindingHolder;

public interface OperationExecutorPool {

    /**
     * Start the workers to be ready for aeron messages
     */
    void startPool();

    /**
     * @return a container with the channel-stream-handler bindings that redirect messages to the thread pool
     */
    AeronBindingHolder getBindingHolder();
}
