package org.jetc.aeron.quick.peers.receiver;

import org.agrona.ErrorHandler;
import org.agrona.concurrent.AgentRunner;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.status.AtomicCounter;
import io.aeron.Aeron;
import org.jetc.aeron.quick.messaging.ReceiverBindingToAeronBindingMapper;

public class AeronQuickReceiverRunner<T> implements AutoCloseable {
    private final AgentRunner serverAgentRunner;

    public AeronQuickReceiverRunner(ReceiverAgentConfiguration<T> config){
        HandlerPerBindingAgent serverAgent = new HandlerPerBindingAgent(config.getContext().getAeron(), config.getBindingsList());
        this.serverAgentRunner = new AgentRunner(config.getAgentIdleStrategy(), config.getAgentErrorHandler(), config.getErrorCounter(), serverAgent);
    }

    public AeronQuickReceiverRunner(Aeron aeron, ReceiverBindingToAeronBindingMapper serverBindings, IdleStrategy idleStrategyClient, ErrorHandler agentErrorHandler, AtomicCounter errorCounter) {
        HandlerPerBindingAgent serverAgent = new HandlerPerBindingAgent(aeron, serverBindings.getBindings());
        this.serverAgentRunner = new AgentRunner(idleStrategyClient, agentErrorHandler, errorCounter, serverAgent);
    }

    public void start() {
        AgentRunner.startOnThread(this.serverAgentRunner);
    }

    /**
     * Stops the receiver agent
     */
    @Override
    public void close() {
        this.serverAgentRunner.close();
    }
}
