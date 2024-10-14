package org.jetc.aeron.quick.peers.receiver;

import org.agrona.concurrent.AgentRunner;

public class AeronQuickReceiverRunner<T> implements AutoCloseable {
    private final AgentRunner serverAgentRunner;

    public AeronQuickReceiverRunner(ReceiverAgentConfiguration<T> config){
        HandlerPerBindingAgent serverAgent = new HandlerPerBindingAgent(config.getContext().getAeron(), config.getBindingsList());
        this.serverAgentRunner = new AgentRunner(config.getAgentIdleStrategy(), config.getAgentErrorHandler(), config.getErrorCounter(), serverAgent);
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
