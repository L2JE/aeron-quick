package org.jetc.aeron.quick.samples.annotation_receiver_and_sender;

import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.SigInt;
import org.agrona.concurrent.SleepingMillisIdleStrategy;
import org.jetc.aeron.quick.AeronQuickFactory;
import org.jetc.aeron.quick.annotations.AeronQuickSender;
import org.jetc.aeron.quick.exception.PublicationOfferFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

public class MyGeneralSender {
    private static final Logger log = LoggerFactory.getLogger(MyGeneralSender.class);
    private static final AeronQuickFactory factory = AeronQuickFactory.builder().removeAeronDirOnShutdown(true).build();
    private static final AtomicBoolean running = new AtomicBoolean(true);

    @AeronQuickSender//to have the sending logic generated at compile time (if not used, a java proxy will be generated at runtime which will add a higher latency per message)
    private static AeronGeneralServiceContract sender;

    private static void aeronQuickSenderExample() {
        SigInt.register(() -> running.set(false));
        IdleStrategy idleStrategy = new SleepingMillisIdleStrategy(1000);
        sender = factory.getSenderBuilder(AeronGeneralServiceContract.class, "senderExample").orElseThrow().build();

        for(long it = 0; running.get(); it++) {
            try {
                if(it % 2 == 0)
                    sender.duplicateNumber(it);
                else
                    sender.notifyOperationDone(new ExamplePojo("iteration-"+it, (int)it), (int)(it +1));

                log.info("Sent message %s successfully".formatted(it));
            } catch (PublicationOfferFailedException e){
                log.error("Message %s timed out".formatted(it));
            } catch (Exception e){
                log.error("There was an error with message %s".formatted(it), e);
            }

            idleStrategy.idle();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        setMockSysProps();
        log.warn("STARTING CLIENT");

        Thread.startVirtualThread(MyGeneralSender::aeronQuickSenderExample);

        Thread.sleep(Duration.ofMinutes(5));
        log.warn("STOPPING CLIENT");
        closeExample();
    }

    public static final String GLOBAL_CHANNEL = "aeron:udp?endpoint=localhost:20121";
    public static final int PRICE_CHANGED_STREAM = 1;
    public static final int NOTIFY_OPERATION_STREAM = 2;
    public static final int DUPLICATE_NUMBER_STREAM = 3;

    private static void setMockSysProps() {
        System.setProperty("aeron.quick.senderExample.priceChanged.channel", GLOBAL_CHANNEL);
        System.setProperty("aeron.quick.senderExample.priceChanged.stream", Integer.toString(PRICE_CHANGED_STREAM));
        System.setProperty("aeron.quick.senderExample.notifyOperationDone.channel", GLOBAL_CHANNEL);
        System.setProperty("aeron.quick.senderExample.notifyOperationDone.stream", Integer.toString(NOTIFY_OPERATION_STREAM));
        System.setProperty("aeron.quick.senderExample.duplicateNumber.channel", GLOBAL_CHANNEL);
        System.setProperty("aeron.quick.senderExample.duplicateNumber.stream", Integer.toString(DUPLICATE_NUMBER_STREAM));
    }

    private static void closeExample(){
        running.set(false);
        factory.close();
    }
}
