package org.jetc.aeron.quick.messaging;

public interface BindingAppender<T> {
    void addBinding(String channel, int streamID, T handler);
}
