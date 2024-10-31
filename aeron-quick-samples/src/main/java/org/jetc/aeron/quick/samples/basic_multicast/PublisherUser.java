package org.jetc.aeron.quick.samples.basic_multicast;

import org.jetc.aeron.quick.AeronQuickFactory;
import org.jetc.aeron.quick.annotations.AeronQuickSender;
import org.jetc.aeron.quick.messaging.publication.OfferingResultSideActions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;

public class PublisherUser {
    private static final Logger log = LoggerFactory.getLogger(PublisherUser.class);
    private static final AeronQuickFactory factory = AeronQuickFactory.builder().removeAeronDirOnShutdown(true).build();
    private static int MAX_MESSAGES = 10000;
    private static Duration idleTime = Duration.ofSeconds(2);

    @AeronQuickSender
    private static SenderContract publisher(){
        if(p == null)
            p = factory.getSender(SenderContract.class, "publisher1", c -> c.setOfferingSideAction(OfferingResultSideActions.logResult(log)));
        return p;
    }
    private static SenderContract p;

    public static void main(String[] args) throws InterruptedException {
        setMockSysProps();
        log.warn("STARTING");

        for (int i = 0; i < MAX_MESSAGES; i++) {
            log.warn("Publishing user %s ".formatted(i));
            publisher().userAdded(
                    new User(i, "user-" + i, new Date(1990, Calendar.NOVEMBER, i + 1)),
                    LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()
            );

            Thread.sleep(idleTime);
        }

        log.warn("STOPPING");
        factory.close();
    }

    private static void setMockSysProps() {
        System.setProperty("aeron.quick.publisher1.userAdded.channel", "aeron:udp?control-mode=dynamic|control=localhost:13000");
        System.setProperty("aeron.quick.publisher1.userAdded.stream", "101");
    }
}
