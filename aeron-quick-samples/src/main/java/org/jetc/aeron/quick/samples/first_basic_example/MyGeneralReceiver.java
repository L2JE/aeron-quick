package org.jetc.aeron.quick.samples.first_basic_example;

import org.agrona.concurrent.SleepingMillisIdleStrategy;
import org.jetc.aeron.quick.AeronQuickFactory;
import org.jetc.aeron.quick.server.AeronQuickReceiverRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;

public class MyGeneralReceiver {

    private static final Logger log = LoggerFactory.getLogger(MyGeneralReceiver.class);
    private static final AeronQuickFactory factory = AeronQuickFactory.builder().removeAeronDirOnShutdown(true).build();
    private static AeronQuickReceiverRunner<MyGeneralReceiver> serverRunner;

    public static void main(String[] args) throws InterruptedException {
        log.warn("STARTING SERVER FROM ");

        factory.getReceiverBuilder(new MyGeneralReceiverAdapter(new MyGeneralReceiver()), "").ifPresent( builder -> {
            serverRunner = builder
                    .setAgentIdleStrategy(new SleepingMillisIdleStrategy(1000 * 2))
                    .build();
            serverRunner.start();
        });

        Thread.sleep(Duration.ofMinutes(5));
        log.warn("STOPPING SERVER FROM");
        closeExample();
    }

    private static void closeExample(){
        factory.close();
        if(serverRunner != null)
            serverRunner.close();
    }

    public void priceChanged(double newPrice) {
        log.warn("priceChanged - with %s".formatted(newPrice));
        heavyOperation();
        log.warn("priceChanged FINISHED");
    }

    public long duplicateNumber(long target) {
        log.warn("duplicateNumber - with %s".formatted(target));
        long result = target * 2;
        heavyOperation();
        log.warn("duplicateNumber FINISHED with result %s".formatted(result));
        return result;
    }

    public void notifyOperationDone(ExampleParamClass extraData) {
        log.warn("notifyOperationDone - with %s".formatted(extraData));
        heavyOperation();
        log.warn("notifyOperationDone FINISHED");
    }

    private void heavyOperation(){
        try {
            Thread.sleep(Duration.ofSeconds(5));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public record ExampleParamClass(String value1, int value2){}
}
