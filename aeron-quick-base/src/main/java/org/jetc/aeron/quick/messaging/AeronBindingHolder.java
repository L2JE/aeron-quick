package org.jetc.aeron.quick.messaging;

import org.jetc.aeron.quick.messaging.subscription.SubscriptionMeta;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AeronBindingHolder {
    private final Map<String, Map<Integer, SubscriptionMeta>> methodBindings = new HashMap<>();

    /**
     * Adds a new binding if not previously set, letting user override bindings defined in system properties
     * @param channel
     * @param streamID
     * @param handler
     */
    public void addBinding(String channel, int streamID, SubscriptionMeta handler){
        Map<Integer, SubscriptionMeta> channelBindings = this.methodBindings.computeIfAbsent(channel, k -> new HashMap<>());
        channelBindings.putIfAbsent(streamID, handler);
    }

    public List<AeronBinding> getBindingsList(){
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
