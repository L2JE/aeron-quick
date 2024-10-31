package org.jetc.aeron.quick.messaging.fragment_handling.concurrent.disruptor;

import com.lmax.disruptor.RingBuffer;
import io.aeron.logbuffer.FragmentHandler;
import io.aeron.logbuffer.Header;
import org.agrona.DirectBuffer;
import org.jetc.aeron.quick.utils.events.ExecutionRequestEvent;
import org.jetc.aeron.quick.messaging.fragment_handling.concurrent.BufferDataExtractor;

public class SingleThreadRemoteOperationDispatcher<T> implements FragmentHandler {
    private int currentTurn = 0;
    private final RingBuffer<ExecutionRequestEvent<T>>[] handlerPool;
    private final BufferDataExtractor<T> eventTranslator;

    public SingleThreadRemoteOperationDispatcher(RingBuffer<ExecutionRequestEvent<T>>[] operationQueuePool, BufferDataExtractor<T> eventTranslator) {
        this.handlerPool = operationQueuePool;
        this.eventTranslator = eventTranslator;
    }

    @Override
    public void onFragment(DirectBuffer buffer, int offset, int length, Header header) {
        if(currentTurn >= handlerPool.length)
            currentTurn = 0;

        handlerPool[currentTurn++].publishEvent(eventTranslator, buffer, offset, length);
    }
}
