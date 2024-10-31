package org.jetc.aeron.quick.messaging.fragment_handling.concurrent;

import com.lmax.disruptor.EventHandler;
import org.jetc.aeron.quick.AeronQuickContext;
import org.jetc.aeron.quick.deferred_exec.ExecutionRequestEvent;
import org.jetc.aeron.quick.messaging.AeronConcurrentBinding;
import org.jetc.aeron.quick.messaging.BindingAppender;
import org.jetc.aeron.quick.messaging.fragment_handling.concurrent.disruptor.DisruptorExecutorPoolFactory;
import org.jetc.aeron.quick.messaging.subscription.ConcurrentSubscriptionMeta;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadFactory;

/**
 * @param <T> the target class that will end-up running from the ExecutionRequestEvents
 */
public class OperationExecutorPoolConfiguration<T> implements BindingAppender<ConcurrentSubscriptionMeta<T>> {
    protected PoolWaitStrategy idleStrategy;
    protected long idleTimeNanos;
    protected int poolSize;
    protected ThreadFactory threadFactory;
    protected int queueSize;
    protected List<AeronConcurrentBinding<T>> paramsExtractors;
    protected EventHandler<ExecutionRequestEvent<T>> globalEventHandler;
    protected OperationExecutorPoolFactory<T> poolFactory;

    public OperationExecutorPoolConfiguration(AeronQuickContext context, String componentName){
        String POOL_PROPS_BASE = "events.pool";
        idleStrategy = PoolWaitStrategy.fromProp(context.getProperty(componentName, POOL_PROPS_BASE, "idleStrategy"), PoolWaitStrategy.SLEEP);
        idleTimeNanos = context.getIntProperty(componentName, POOL_PROPS_BASE, "idleTimeMillis", 100);
        poolSize = context.getIntProperty(componentName, POOL_PROPS_BASE, "poolSize", 1);
        threadFactory = ThreadFactoryOpts.fromProp(context.getProperty(componentName, POOL_PROPS_BASE, "threadFactory"), ThreadFactoryOpts.VIRTUAL);
        queueSize = context.getIntProperty(componentName, POOL_PROPS_BASE, "queueSize", 2048);
        poolFactory = new DisruptorExecutorPoolFactory<>();
        paramsExtractors = new LinkedList<>();
    }

    public OperationExecutorPool apply(){
        return poolFactory.create(this);
    }

    public PoolWaitStrategy getIdleStrategy() {
        if(idleStrategy == null)
            idleStrategy = PoolWaitStrategy.SLEEP;
        return idleStrategy;
    }

    public long getIdleTimeNanos() {
        if(idleTimeNanos < 0)
            idleTimeNanos = 1;
        return idleTimeNanos;
    }

    public int getPoolSize() {
        if(poolSize < 0)
            poolSize = 1;
        return poolSize;
    }

    public ThreadFactory getThreadFactory() {
        if(threadFactory == null)
            threadFactory = Thread.ofVirtual().factory();
        return threadFactory;
    }

    public int getQueueSize() {
        if(queueSize < 1024)
            queueSize = 1024;
        return queueSize;
    }

    public List<AeronConcurrentBinding<T>> getParamsExtractors() {
        return paramsExtractors;
    }

    public EventHandler<ExecutionRequestEvent<T>> getGlobalEventHandler() {
        return globalEventHandler;
    }

    public OperationExecutorPoolFactory<T> getPoolFactory() {
        return poolFactory;
    }

    public OperationExecutorPoolConfiguration<T> setPoolFactory(OperationExecutorPoolFactory<T> poolFactory) {
        this.poolFactory = poolFactory;
        return this;
    }

    public OperationExecutorPoolConfiguration<T> setIdleStrategy(PoolWaitStrategy idleStrategy) {
        this.idleStrategy = idleStrategy;
        return this;
    }

    public OperationExecutorPoolConfiguration<T> setIdleTimeNanos(long idleTimeNanos) {
        this.idleTimeNanos = idleTimeNanos;
        return this;
    }

    public OperationExecutorPoolConfiguration<T> setPoolSize(int poolSize) {
        this.poolSize = poolSize;
        return this;
    }

    public OperationExecutorPoolConfiguration<T> setThreadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        return this;
    }

    public OperationExecutorPoolConfiguration<T> setQueueSize(int queueSize) {
        this.queueSize = queueSize;
        return this;
    }

    /**
     * @param globalEventHandler to run the method executor on a target object from multiple threads. Eg: {@code (event, sequence, endOfBatch) -> event.getExecutor().runMethod(server)}
     */
    public OperationExecutorPoolConfiguration<T> setGlobalEventHandler(EventHandler<ExecutionRequestEvent<T>> globalEventHandler) {
        this.globalEventHandler = globalEventHandler;
        return this;
    }

    @Override
    public void addBinding(String channel, int streamID, ConcurrentSubscriptionMeta<T> handler) {
        paramsExtractors.add(new AeronConcurrentBinding<>(channel, streamID, handler));
    }

    public enum PoolWaitStrategy {
        BUSY_SPIN, YIELD, SLEEP;

        public static PoolWaitStrategy fromProp(String prop, PoolWaitStrategy defaultValue){
            PoolWaitStrategy value;
            try {
                value = PoolWaitStrategy.valueOf(prop.toLowerCase());
            } catch (Exception ignored){
                value = defaultValue;
            }
            return value;
        }
    }

    public enum ThreadFactoryOpts {
        PLATFORM, VIRTUAL;

        public static ThreadFactory fromProp(String prop, ThreadFactoryOpts defaultValue){
            ThreadFactoryOpts value;
            try {
                value = ThreadFactoryOpts.valueOf(prop.toLowerCase());
            } catch (Exception ignored){
                value = defaultValue;
            }

            Thread.Builder b = value.equals(PLATFORM) ? Thread.ofPlatform() : Thread.ofVirtual();
            return b.name("aq_event_pool_worker", 0).factory();
        }
    }

}
