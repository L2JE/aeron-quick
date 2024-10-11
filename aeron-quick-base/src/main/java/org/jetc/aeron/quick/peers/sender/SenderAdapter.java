package org.jetc.aeron.quick.peers.sender;

import org.jetc.aeron.quick.peers.ConfigurablePeer;

public interface SenderAdapter<T> extends ConfigurablePeer<SenderConfiguration> {
    T getAdapted();
}
