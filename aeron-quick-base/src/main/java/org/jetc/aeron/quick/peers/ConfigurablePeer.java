package org.jetc.aeron.quick.peers;

public interface ConfigurablePeer<T extends PeerConfiguration> {
    void configure(T config);
}
