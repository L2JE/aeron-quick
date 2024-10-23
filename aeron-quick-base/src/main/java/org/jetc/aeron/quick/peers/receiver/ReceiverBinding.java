package org.jetc.aeron.quick.peers.receiver;

import io.aeron.logbuffer.FragmentHandler;

public record ReceiverBinding(String method, FragmentHandler handler){}