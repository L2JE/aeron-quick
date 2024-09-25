package org.jetc.aeron.quick;

import io.aeron.Aeron;
import org.agrona.ErrorHandler;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.SleepingMillisIdleStrategy;
import org.agrona.concurrent.status.AtomicCounter;

public abstract class AeronQuickBuilder<R> {
    protected Aeron aeron;
    protected IdleStrategy agentIdleStrategy;
    protected ErrorHandler agentErrorHandler;
    protected AtomicCounter agentErrorCounter;

    protected AeronQuickBuilder(Aeron aeron){
        this.aeron = aeron;
    }

    public ErrorHandler getAgentErrorHandler(){
        if(agentErrorHandler == null)
            agentErrorHandler = Throwable::printStackTrace;

        return agentErrorHandler;
    }

    public AtomicCounter getAgentErrorCounter(){
        return agentErrorCounter;
    }

    public IdleStrategy getAgentIdleStrategy() {
        if(agentIdleStrategy == null)
            agentIdleStrategy = new SleepingMillisIdleStrategy();

        return agentIdleStrategy;
    }

    public AeronQuickBuilder<R> setAgentIdleStrategy(IdleStrategy idleStrategy) {
        this.agentIdleStrategy = idleStrategy;
        return this;
    }

    public AeronQuickBuilder<R> setAgentErrorHandler(ErrorHandler agentErrorHandler) {
        this.agentErrorHandler = agentErrorHandler;
        return this;
    }

    public AeronQuickBuilder<R> setAgentErrorCounter(AtomicCounter agentErrorCounter) {
        this.agentErrorCounter = agentErrorCounter;
        return this;
    }

    public abstract R build();
}
