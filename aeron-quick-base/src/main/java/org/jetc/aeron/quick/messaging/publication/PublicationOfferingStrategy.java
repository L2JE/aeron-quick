package org.jetc.aeron.quick.messaging.publication;

import io.aeron.Publication;
import org.agrona.MutableDirectBuffer;
import org.jetc.aeron.quick.exception.PublicationOfferFailedException;

public interface PublicationOfferingStrategy {
    void offerMessage(Publication publication, MutableDirectBuffer buffer, int offset, int length) throws PublicationOfferFailedException;
}
