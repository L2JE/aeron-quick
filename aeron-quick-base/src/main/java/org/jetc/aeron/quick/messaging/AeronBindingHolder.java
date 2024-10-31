package org.jetc.aeron.quick.messaging;

import org.jetc.aeron.quick.messaging.subscription.SubscriptionMeta;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AeronBindingHolder implements BindingProvider, BindingAppender<SubscriptionMeta>{
    protected final Map<String, Map<Integer, SubscriptionMeta>> methodBindings = new HashMap<>();

    /**
     * Adds a new binding if not previously set, letting user override bindings defined in system properties
     * @param channel
     * @param streamID
     * @param handler
     */
    @Override
    public void addBinding(String channel, int streamID, SubscriptionMeta handler){
        Map<Integer, SubscriptionMeta> channelBindings = this.methodBindings.computeIfAbsent(channel, k -> new HashMap<>());
        channelBindings.putIfAbsent(streamID, handler);
    }

    @Override
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

    /**
     * Adds each of the binding in the provided holder if not previously set, letting user override bindings defined in system properties
     */
    public BindingProvider addAll(AeronBindingHolder bindingHolder) {
        for(var chanEntry : bindingHolder.methodBindings.entrySet()){
            for (var streamEntry : chanEntry.getValue().entrySet()){
                this.addBinding(chanEntry.getKey(), streamEntry.getKey(), streamEntry.getValue());
            }
        }
        return this;
    }
}
