package org.jetc.aeron.quick.peers.receiver;

import org.agrona.concurrent.Agent;
import org.agrona.concurrent.AgentRunner;

public class AeronQuickReceiverRunner<T> implements AutoCloseable {
    private final AgentRunner serverAgentRunner;

    public AeronQuickReceiverRunner(ReceiverAgentConfiguration<T> config, Agent agent){
        this.serverAgentRunner = new AgentRunner(config.getAgentIdleStrategy(), config.getAgentErrorHandler(), config.getErrorCounter(), agent);
    }

    public AeronQuickReceiverRunner<T> start() {
        AgentRunner.startOnThread(this.serverAgentRunner);
        return this;
    }

    /**
     * Stops the receiver agent
     */
    @Override
    public void close() {
        this.serverAgentRunner.close();
    }
}
