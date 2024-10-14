package org.jetc.aeron.quick.messaging.publication;

import io.aeron.Publication;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.IdleStrategy;
import org.jetc.aeron.quick.exception.PublicationOfferFailedException;

public class RetryOfferingStrategy implements PublicationOfferingStrategy{
    private final IdleStrategy idleStrategy;
    private final int maxRetry;

    public RetryOfferingStrategy(IdleStrategy idleStrategy, int maxRetry) {
        this.idleStrategy = idleStrategy;
        this.maxRetry = maxRetry;
    }


    @Override
    public void offerMessage(Publication publication, MutableDirectBuffer buffer, int offset, int length) throws PublicationOfferFailedException {
        long streamPos = publication.offer(buffer, offset, length);
        long i = 0;
        for (; i < maxRetry && streamPos < 0; i++) {
            idleStrategy.idle();
            streamPos = publication.offer(buffer, offset, length);
        }
        if(i >= maxRetry)
            throw new PublicationOfferFailedException();
    }
}
