package org.jetc.aeron.quick.utils.publication;

import io.aeron.Publication;
import org.jetc.aeron.quick.AeronQuickContext;

public abstract class AbstractPublicationCreator {
    protected final AeronQuickContext ctx;
    protected final String componentName;

    public AbstractPublicationCreator(AeronQuickContext ctx, String componentName) {
        this.ctx = ctx;
        this.componentName = componentName;
    }

    public Publication createPublication(String methodName){
        return getPublicationInternal(
            ctx.getProperty(componentName, methodName, "channel"),
            ctx.getIntProperty(componentName, methodName, "stream")
        );
    }

    protected abstract Publication getPublicationInternal(String channel, int stream);
}
