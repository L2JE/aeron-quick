package org.jetc.aeron.quick.messaging.fragment_handling.concurrent.disruptor;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.jetc.aeron.quick.utils.events.ExecutionRequestEvent;
import org.jetc.aeron.quick.utils.events.MethodExecutor;
import org.jetc.aeron.quick.messaging.AeronBindingHolder;
import org.jetc.aeron.quick.messaging.AeronConcurrentBinding;
import org.jetc.aeron.quick.messaging.fragment_handling.concurrent.OperationExecutorPool;
import org.jetc.aeron.quick.messaging.subscription.SubscriptionMeta;
import java.util.List;
import java.util.concurrent.ThreadFactory;

/**
 * A pool for {@link ExecutionRequestEvent ExecutionRequestEvents} for the same target class (T) that uses Disruptor pattern to enqueue events on each worker thread
 */
public class DisruptorExecutorPool<T> implements OperationExecutorPool {
    private final AeronBindingHolder aeronToDisruptorBindings;
    private final Disruptor<ExecutionRequestEvent<T>>[] handlerPool;

    /**
     * @param paramsExtractors One object per target method that must extract the data from the aeron buffer and put them in a {@link MethodExecutor} that will run the target method later
     * @param globalEventHandler to run the method executor on a target object from multiple threads. Eg: {@code (event, sequence, endOfBatch) -> event.getExecutor().runMethod(server)}
     * @param queueSize of the event queue
     * @param threadFactory to create the threads where events will be taken to run method executors.
     * @param waitStrategy for consumer threads to wait for new events
     * @param poolSize the amount of threads that will be listening for new aeron messages
     */
    @SuppressWarnings("unchecked")
    public DisruptorExecutorPool(List<AeronConcurrentBinding<T>> paramsExtractors, EventHandler<ExecutionRequestEvent<T>> globalEventHandler, int queueSize, ThreadFactory threadFactory, WaitStrategy waitStrategy, int poolSize){
        this.aeronToDisruptorBindings = new AeronBindingHolder();
        this.handlerPool = new Disruptor[poolSize];

        RingBuffer<ExecutionRequestEvent<T>>[] ringBuffers = new RingBuffer[poolSize];
        for(var b : paramsExtractors){
            this.aeronToDisruptorBindings.addBinding(
                b.channel(), b.streamID(),
                new SubscriptionMeta(
                    new SingleThreadRemoteOperationDispatcher<>(ringBuffers, b.meta().extractor()),
                    b.meta().fragmentLimit()
                )
            );
        }

        for(int i = 0; i < poolSize; i++){
            final Disruptor<ExecutionRequestEvent<T>> disruptor = new Disruptor<>(ExecutionRequestEvent::new, queueSize, threadFactory, ProducerType.SINGLE, waitStrategy);
            disruptor.handleEventsWith(globalEventHandler).then((event, sequence, endOfBatch) -> event.clear());
            ringBuffers[i] = disruptor.getRingBuffer();
            handlerPool[i] = disruptor;
        }
    }

    @Override
    public void startPool(){
        for(var h : handlerPool) {
            h.start();
        }
    }

    @Override
    public AeronBindingHolder getBindingHolder() {
        return aeronToDisruptorBindings;
    }
}
