package org.jetc.aeron.quick.peers.sender;

import org.agrona.concurrent.SleepingMillisIdleStrategy;
import org.jetc.aeron.quick.AeronQuickContext;
import org.jetc.aeron.quick.messaging.publication.PublicationOfferingStrategy;
import org.jetc.aeron.quick.peers.PeerConfiguration;
import org.jetc.aeron.quick.messaging.publication.RetryOfferingStrategy;
import java.util.function.Consumer;

public class SenderConfiguration implements PeerConfiguration {
    private AeronQuickContext context;
    private String componentName;
    private PublicationOfferingStrategy offeringStrategy;
    private Consumer<Long> resultSideEffect;

    @Override
    public AeronQuickContext getContext() {
        return context;
    }

    @Override
    public String getComponentName() {
        return componentName;
    }

    @Override
    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    @Override
    public void setContext(AeronQuickContext context) {
        this.context = context;
    }

    public PublicationOfferingStrategy getOfferingStrategy() {
        if(offeringStrategy == null)
            offeringStrategy = new RetryOfferingStrategy(new SleepingMillisIdleStrategy(), getPublishResultHandler(), 50);
        return offeringStrategy;
    }

    public void setOfferingStrategy(PublicationOfferingStrategy offeringStrategy) {
        this.offeringStrategy = offeringStrategy;
    }

    public void setOfferingSideAction(Consumer<Long> resultSideEffect) {
        this.resultSideEffect = resultSideEffect;
    }

    public Consumer<Long> getPublishResultHandler() {
        return resultSideEffect;
    }
}
