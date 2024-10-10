package org.jetc.aeron.quick.messaging.serialization;

public interface ObjectStringMapper {
    String serialize(Object any);
    <T> T deserialize(String content, Class<T> targetClass);
}
