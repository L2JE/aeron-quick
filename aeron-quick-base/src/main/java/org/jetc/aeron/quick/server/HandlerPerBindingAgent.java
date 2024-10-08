package org.jetc.aeron.quick.server;

import io.aeron.Aeron;
import io.aeron.Subscription;
import io.aeron.logbuffer.FragmentHandler;
import org.agrona.CloseHelper;
import org.agrona.concurrent.Agent;
import org.jetc.aeron.quick.messaging.AeronBinding;
import org.jetc.aeron.quick.messaging.subscription.SubscriptionMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.UUID;

public class HandlerPerBindingAgent implements Agent {
    private static final Logger log = LoggerFactory.getLogger(HandlerPerBindingAgent.class);
    private final String roleName;
    private final SubscriptionData[] subscriptions;

    public HandlerPerBindingAgent(Aeron aeron, List<AeronBinding> channelBindings) {
        this.roleName = "hpbAgent(" + UUID.randomUUID() + ")";
        this.subscriptions = channelBindings.stream().map(b ->
                new SubscriptionData(
                    aeron.addSubscription(b.channel(), b.streamID()),
                    b.meta().handlerProvider().getHandler(aeron),
                    b.meta()
                )
        ).toArray(SubscriptionData[]::new);
    }

    @Override
    public int doWork() throws Exception {
        log.warn("Iterating subscriptions and polling.");
        for(SubscriptionData subscriptionData : this.subscriptions){
            SubscriptionMeta meta = subscriptionData.meta();
            log.warn("Polling from %s - stream(%s) - limit(%s)".formatted(subscriptionData.subscription().channel(), subscriptionData.subscription().streamId(), meta.fragmentLimit()));
            int fragmentsReceived = subscriptionData.subscription().poll(
                    subscriptionData.handler(),
                    meta.fragmentLimit()
            );

            if(fragmentsReceived != 0){
                log.warn("Received fragments: %s ".formatted(fragmentsReceived));
            }
        }
        return 0;
    }

    @Override
    public String roleName() {
        return this.roleName;
    }

    @Override
    public void onClose() {
        log.warn("Closing Agent");
        for (final SubscriptionData data : subscriptions){
            CloseHelper.quietClose(data.subscription());
        }
    }

    private record SubscriptionData(Subscription subscription, FragmentHandler handler, SubscriptionMeta meta){}
}
