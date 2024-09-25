package org.jetc.aeron.quick.messaging.fragment_handling;

import io.aeron.Aeron;
import io.aeron.logbuffer.FragmentHandler;

public interface ContextualHandler {
    FragmentHandler getHandler(Aeron aeron);
}