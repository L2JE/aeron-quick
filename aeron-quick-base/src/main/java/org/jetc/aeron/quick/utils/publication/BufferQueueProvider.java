package org.jetc.aeron.quick.utils.publication;

import org.agrona.MutableDirectBuffer;

import java.util.Queue;
import java.util.function.Supplier;

public interface BufferQueueProvider {
    Queue<MutableDirectBuffer> getFilledQueue(Supplier<MutableDirectBuffer> supplier);
}
