package org.jetc.aeron.quick.utils.publication;

import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.ManyToManyConcurrentArrayQueue;

import java.util.Queue;
import java.util.function.Supplier;

public class ConcurrentBufferQueueProvider implements BufferQueueProvider {
    private final int poolSize;

    public ConcurrentBufferQueueProvider(int poolSize) {
        this.poolSize = poolSize;
    }

    @Override
    public Queue<MutableDirectBuffer> getFilledQueue(Supplier<MutableDirectBuffer> supplier) {
        Queue<MutableDirectBuffer> result = new ManyToManyConcurrentArrayQueue<>(poolSize);

        for(int i = 0; i < poolSize; i++)
            result.offer(supplier.get());

        return result;
    }
}
