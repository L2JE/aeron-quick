package org.jetc.aeron.quick.utils.events;

/**
 * @param <T> the target class where a method will be run with the given parameters
 */
public interface MethodExecutor<T> {
    void runMethod(T target);
}
