package org.jetc.aeron.quick.samples.basic_multicast;

import org.jetc.aeron.quick.annotations.AeronQuickContract;

@AeronQuickContract
public interface SenderContract {
    void userAdded(User user, long timestamp);
}
