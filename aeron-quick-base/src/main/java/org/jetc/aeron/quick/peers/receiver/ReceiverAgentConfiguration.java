package org.jetc.aeron.quick.peers.receiver;

import org.agrona.ErrorHandler;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.SleepingMillisIdleStrategy;
import org.agrona.concurrent.status.AtomicCounter;

public class ReceiverAgentConfiguration<E> extends ReceiverConfiguration<E>{
    private IdleStrategy agentIdleStrategy;
    private ErrorHandler agentErrorHandler;
    private AtomicCounter agentErrorCounter;

    public ErrorHandler getAgentErrorHandler(){
        if(agentErrorHandler == null)
            agentErrorHandler = Throwable::printStackTrace;

        return agentErrorHandler;
    }

    public AtomicCounter getErrorCounter(){
        return agentErrorCounter;
    }

    public IdleStrategy getAgentIdleStrategy() {
        if(agentIdleStrategy == null)
            agentIdleStrategy = new SleepingMillisIdleStrategy();

        return agentIdleStrategy;
    }

    public void setAgentIdleStrategy(IdleStrategy idleStrategy) {
        this.agentIdleStrategy = idleStrategy;
    }

    public void setAgentErrorHandler(ErrorHandler agentErrorHandler) {
        this.agentErrorHandler = agentErrorHandler;
    }

    public void setAgentErrorCounter(AtomicCounter agentErrorCounter) {
        this.agentErrorCounter = agentErrorCounter;
    }
}
