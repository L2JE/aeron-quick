package org.jetc.aeron.quick.messaging;

import org.jetc.aeron.quick.messaging.subscription.SubscriptionMeta;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ReceiverBindingToAeronBindingMapper {
    private final Map<String, Map<Integer, SubscriptionMeta>> methodBindings;

    public ReceiverBindingToAeronBindingMapper(Map<String, Map<Integer, SubscriptionMeta>> methodBindings) {
        this.methodBindings = methodBindings;
    }

    public Object setBinding(String channel, int streamID, SubscriptionMeta handler){
        Map<Integer, SubscriptionMeta> channelBindings = this.methodBindings.computeIfAbsent(channel, k -> new HashMap<>());
        return channelBindings.put(streamID, handler);
    }

    public List<AeronBinding> getBindings(){
        List<AeronBinding> bindings = new LinkedList<>();
        for(var chanEntry : methodBindings.entrySet()){
            String channel = chanEntry.getKey();
            for (var streamEntry : chanEntry.getValue().entrySet()){
                bindings.add(new AeronBinding(channel, streamEntry.getKey(), streamEntry.getValue()));
            }
        }
        return bindings;
    }
}
