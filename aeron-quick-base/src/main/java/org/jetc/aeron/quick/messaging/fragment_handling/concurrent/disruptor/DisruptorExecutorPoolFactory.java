package org.jetc.aeron.quick.messaging.fragment_handling.concurrent.disruptor;

import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import org.jetc.aeron.quick.messaging.fragment_handling.concurrent.OperationExecutorPool;
import org.jetc.aeron.quick.messaging.fragment_handling.concurrent.OperationExecutorPoolConfiguration;
import org.jetc.aeron.quick.messaging.fragment_handling.concurrent.OperationExecutorPoolFactory;

public class DisruptorExecutorPoolFactory<T> implements OperationExecutorPoolFactory<T> {
    @Override
    public OperationExecutorPool create(OperationExecutorPoolConfiguration<T> config) {
        WaitStrategy waitStrategy = switch (config.getIdleStrategy()){
            case BUSY_SPIN -> new BusySpinWaitStrategy();
            case YIELD -> new YieldingWaitStrategy();
            case SLEEP -> new SleepingWaitStrategy(200, config.getIdleTimeMillis() > 0 ? config.getIdleTimeMillis() * 1000 : 100);
        };

        return new DisruptorExecutorPool<>(config.getParamsExtractors(), config.getGlobalEventHandler(), config.getQueueSize(), config.getThreadFactory(), waitStrategy, config.getPoolSize());
    }
}