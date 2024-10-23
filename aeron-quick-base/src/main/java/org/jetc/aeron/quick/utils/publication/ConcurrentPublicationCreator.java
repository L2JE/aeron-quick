package org.jetc.aeron.quick.utils.publication;

import io.aeron.Publication;
import org.jetc.aeron.quick.AeronQuickContext;

public class ConcurrentPublicationCreator extends AbstractPublicationCreator{
    public ConcurrentPublicationCreator(AeronQuickContext ctx, String componentName) {
        super(ctx, componentName);
    }

    @Override
    protected Publication getPublicationInternal(String channel, int stream) {
        return ctx.getAeron().addPublication(channel,stream);
    }
}
