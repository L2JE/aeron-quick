package org.jetc.aeron.quick.utils.publication;

import org.agrona.MutableDirectBuffer;
import org.jetc.aeron.quick.AeronQuickContext;
import java.util.function.Supplier;

public class SimpleBufferHolderCreator extends BufferHolderCreator<MutableDirectBuffer> {
    @SafeVarargs
    public SimpleBufferHolderCreator(AeronQuickContext ctx, String componentName, Supplier<MutableDirectBuffer>... bufferSupplier) {
        super(ctx, componentName, bufferSupplier);
    }

    @Override
    public MutableDirectBuffer getBufferHolder(String methodName, int bufferNumber) {
        return bufferSupplier[bufferNumber].get();
    }
}
