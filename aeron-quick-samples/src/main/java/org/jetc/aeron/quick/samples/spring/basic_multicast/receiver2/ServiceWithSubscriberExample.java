package org.jetc.aeron.quick.samples.spring.basic_multicast.receiver2;

import org.jetc.aeron.quick.annotations.AeronQuickReceiver;
import org.jetc.aeron.quick.annotations.AeronQuickContractEndpoint;
import org.jetc.aeron.quick.samples.basic_multicast.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
@AeronQuickReceiver
public class ServiceWithSubscriberExample {
    private static final Logger log = LoggerFactory.getLogger(ServiceWithSubscriberExample.class);

    @AeronQuickContractEndpoint//you can also implement an interface having this method as in "org.jetc.aeron.quick.samples.basic_multicast" example
    void otherActionToTrigger(User user, long timestamp){
        LocalDateTime t = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC);
        log.warn("Received user: %s, created at: %s".formatted(user, t));
    }
}
