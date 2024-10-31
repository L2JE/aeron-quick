package org.jetc.aeron.quick.utils.events;

/**
 * An event requiring to run a method on a target class
 * @param <T> target class where a method will be run
 */
public class ExecutionRequestEvent<T> {
    private MethodExecutor<T> executor;

    public MethodExecutor<T> getExecutor() {
        return executor;
    }

    public void setExecutor(MethodExecutor<T> executor) {
        this.executor = executor;
    }

    public void clear() {
        executor = null;
    }
}