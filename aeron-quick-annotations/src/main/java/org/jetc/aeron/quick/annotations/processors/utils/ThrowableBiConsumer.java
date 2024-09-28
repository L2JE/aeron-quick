package org.jetc.aeron.quick.annotations.processors.utils;


public interface ThrowableBiConsumer<T, U> {
    void accept(T t, U u) throws Exception;
}
