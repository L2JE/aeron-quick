package org.jetc.aeron.quick.utils.publication;

import io.aeron.Publication;
import org.jetc.aeron.quick.AeronQuickContext;

public class ExclusivePublicationCreator extends AbstractPublicationCreator{
    public ExclusivePublicationCreator(AeronQuickContext ctx, String componentName) {
        super(ctx, componentName);
    }

    @Override
    protected Publication getPublicationInternal(String channel, int stream) {
        return ctx.getAeron().addExclusivePublication(channel,stream);
    }
}
