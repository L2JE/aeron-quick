package org.jetc.aeron.quick.utils.publication;

import org.agrona.MutableDirectBuffer;
import org.jetc.aeron.quick.AeronQuickContext;
import java.util.Queue;
import java.util.function.Supplier;

public class ConcurrentBufferHolderCreator extends BufferHolderCreator<Queue<MutableDirectBuffer>>{
    private final Supplier<MutableDirectBuffer>[] bufferSupplier;

    @SafeVarargs
    public ConcurrentBufferHolderCreator(AeronQuickContext ctx, String componentName, Supplier<MutableDirectBuffer>... bufferSupplier) {
        super(ctx, componentName);
        this.bufferSupplier = bufferSupplier;
    }

    @Override
    public Queue<MutableDirectBuffer> getBufferHolder(String methodName, int bufferNumber) {
        return new ConcurrentBufferQueueProvider(
                ctx.getIntProperty(componentName, methodName, "buffer.poolsize", 10)
        ).getFilledQueue(bufferSupplier[bufferNumber]);
    }
}