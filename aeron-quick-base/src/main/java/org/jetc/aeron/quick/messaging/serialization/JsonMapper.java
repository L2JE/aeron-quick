package org.jetc.aeron.quick.messaging.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonMapper implements ObjectStringMapper{
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String serialize(Object any) {
        try {
            return mapper.writeValueAsString(any);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T deserialize(String content, Class<T> targetClass) {
        try {
            return mapper.readValue(content, targetClass);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
