package org.jetc.aeron.quick.samples.spring.basic_multicast.sender;

import org.jetc.aeron.quick.annotations.AeronQuickContract;
import org.jetc.aeron.quick.samples.basic_multicast.User;

@AeronQuickContract
public interface SenderContract {
    void userAdded(User user, long timestamp);
}
