package org.jetc.aeron.quick.peers.receiver;

import org.jetc.aeron.quick.messaging.fragment_handling.ContextualHandler;

public record ReceiverBinding(String methodName, int fragmentLimit, ContextualHandler handler){}
