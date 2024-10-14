package org.jetc.aeron.quick.samples.basic_multicast;

import org.agrona.concurrent.SleepingMillisIdleStrategy;
import org.jetc.aeron.quick.AeronQuickFactory;
import org.jetc.aeron.quick.annotations.AeronQuickReceiver;
import org.jetc.aeron.quick.peers.receiver.AeronQuickReceiverRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@AeronQuickReceiver
public class Subscriber2 implements SenderContract {
    private static final Logger log = LoggerFactory.getLogger(Subscriber2.class);
    private static final AeronQuickFactory factory = AeronQuickFactory.builder().removeAeronDirOnShutdown(true).build();

    @Override
    public void userAdded(User user, long timestamp) {
        LocalDateTime t = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC);
        log.warn("Received user: %s, created at: %s".formatted(user, t));
    }

    public static void main(String[] args) throws InterruptedException {
        setMockSysProps();
        log.warn("STARTING");
        AeronQuickReceiverRunner<Subscriber2> r =  factory.getReceiver(new Subscriber2(), "subscriber2", c -> c.setAgentIdleStrategy(new SleepingMillisIdleStrategy(1000*4)));
        r.start();

        Thread.sleep(Duration.ofMinutes(5));
        log.warn("STOPPING");
        r.close();
        factory.close();
    }

    private static void setMockSysProps() {
        System.setProperty("aeron.quick.subscriber2.userAdded.channel", "aeron:udp?endpoint=localhost:12002|control=localhost:13000|control-mode=dynamic");
        System.setProperty("aeron.quick.subscriber2.userAdded.stream", "101");
    }
}
