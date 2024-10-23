package org.jetc.aeron.quick.messaging.publication;

import io.aeron.Publication;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.IdleStrategy;
import org.jetc.aeron.quick.exception.PublicationOfferFailedException;
import java.util.function.Consumer;

public class RetryOfferingStrategy implements PublicationOfferingStrategy{
    private final IdleStrategy idleStrategy;
    private final int maxRetry;
    private final Consumer<Long> sideAction;

    public RetryOfferingStrategy(IdleStrategy idleStrategy, Consumer<Long> publishingResultSideEffect, int maxRetry) {
        this.idleStrategy = idleStrategy;
        this.maxRetry = maxRetry;
        this.sideAction = publishingResultSideEffect;
    }


    @Override
    public void offerMessage(Publication publication, MutableDirectBuffer buffer, int offset, int length, Consumer<MutableDirectBuffer> releaseBufferAction) throws PublicationOfferFailedException {
        long streamPos = publication.offer(buffer, offset, length);
        long i = 0;
        for (; i < maxRetry && streamPos < 0; i++) {
            idleStrategy.idle();
            streamPos = publication.offer(buffer, offset, length);
        }

        if(releaseBufferAction != null)
            releaseBufferAction.accept(buffer);

        if(sideAction != null)
            sideAction.accept(streamPos);
    }
}
