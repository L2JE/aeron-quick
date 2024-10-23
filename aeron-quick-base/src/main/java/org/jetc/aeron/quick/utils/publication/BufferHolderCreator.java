package org.jetc.aeron.quick.utils.publication;

import org.agrona.MutableDirectBuffer;
import org.jetc.aeron.quick.AeronQuickContext;
import java.util.function.Supplier;

public abstract class BufferHolderCreator<T>{
    protected final Supplier<MutableDirectBuffer>[] bufferSupplier;
    protected final String componentName;
    protected final AeronQuickContext ctx;

    @SafeVarargs
    public BufferHolderCreator(AeronQuickContext ctx, String componentName, Supplier<MutableDirectBuffer>... bufferSupplier) {
        this.bufferSupplier = bufferSupplier;
        this.componentName = componentName;
        this.ctx = ctx;
    }

    public abstract T getBufferHolder(String methodName, int bufferNumber);
}
