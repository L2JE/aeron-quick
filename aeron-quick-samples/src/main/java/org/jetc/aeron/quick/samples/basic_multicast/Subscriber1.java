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
public class Subscriber1 implements SenderContract {
    private static final Logger log = LoggerFactory.getLogger(Subscriber1.class);
    private static final AeronQuickFactory factory = AeronQuickFactory.builder().removeAeronDirOnShutdown(true).build();

    @Override
    public void userAdded(User user, long timestamp) {
        LocalDateTime t = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC);
        log.warn("Received user: %s, created at: %s".formatted(user, t));
    }

    public static void main(String[] args) throws InterruptedException {
        setMockSysProps();
        log.warn("STARTING");
        AeronQuickReceiverRunner<Subscriber1> r =  factory.getReceiver(new Subscriber1(), "subscriber1", c -> c.setAgentIdleStrategy(new SleepingMillisIdleStrategy(1000*4)));
        r.start();

        Thread.sleep(Duration.ofMinutes(5));
        log.warn("STOPPING");
        r.close();
        factory.close();
    }

    private static void setMockSysProps() {
        System.setProperty("aeron.quick.subscriber1.userAdded.channel", "aeron:udp?endpoint=localhost:12001|control=localhost:13000|control-mode=dynamic");
        System.setProperty("aeron.quick.subscriber1.userAdded.stream", "101");
    }
}
