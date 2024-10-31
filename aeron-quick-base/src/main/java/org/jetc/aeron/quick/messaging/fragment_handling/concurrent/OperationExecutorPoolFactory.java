package org.jetc.aeron.quick.messaging.fragment_handling.concurrent;

public interface OperationExecutorPoolFactory<T> {
    OperationExecutorPool create(OperationExecutorPoolConfiguration<T> config);
}
