package org.jetc.aeron.quick.samples.just_annotation_receiver;

import org.agrona.concurrent.SleepingMillisIdleStrategy;
import org.jetc.aeron.quick.AeronQuickFactory;
import org.jetc.aeron.quick.annotations.AeronQuickReceiver;
import org.jetc.aeron.quick.annotations.QuickContractEndpoint;
import org.jetc.aeron.quick.server.AeronQuickReceiverRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;

@AeronQuickReceiver(name = "annotatedReceiver")
public class MyGeneralReceiver {
    private static final Logger log = LoggerFactory.getLogger(MyGeneralReceiver.class);
    private static final AeronQuickFactory factory = AeronQuickFactory.builder().removeAeronDirOnShutdown(true).build();
    private static AeronQuickReceiverRunner<MyGeneralReceiver> serverRunner;

    @QuickContractEndpoint
    public void priceChanged(double newPrice) {
        log.warn("priceChanged - with %s".formatted(newPrice));
        heavyOperation();
        log.warn("priceChanged FINISHED");
    }

    @QuickContractEndpoint
    public long duplicateNumber(long target) {
        log.warn("duplicateNumber - with %s".formatted(target));
        long result = target * 2;
        heavyOperation();
        log.warn("duplicateNumber FINISHED with result %s".formatted(result));
        return result;
    }

    @QuickContractEndpoint
    public void notifyOperationDone(ExampleParamClass extraData, int id) {
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

    public static void main(String[] args) throws InterruptedException {
        setMockSysProps(); //Set system properties for channels and streams (just for the example)
        log.warn("STARTING SERVER FROM ");

        factory.getReceiverBuilder(new MyGeneralReceiver()).ifPresent(builder -> {
            serverRunner = builder
                    .setAgentIdleStrategy(new SleepingMillisIdleStrategy(1000 * 2))
                    .build();
            serverRunner.start();
        });

        Thread.sleep(Duration.ofMinutes(5));
        log.warn("STOPPING SERVER FROM");
        closeExample();
    }

    public static final String GLOBAL_CHANNEL = "aeron:udp?endpoint=localhost:20121";
    public static final int PRICE_CHANGED_STREAM = 1;
    public static final int NOTIFY_OPERATION_STREAM = 2;
    public static final int DUPLICATE_NUMBER_STREAM = 3;

    private static void setMockSysProps() {
        System.setProperty("aeron.quick.annotatedReceiver.priceChanged.channel", GLOBAL_CHANNEL);
        System.setProperty("aeron.quick.annotatedReceiver.priceChanged.stream", Integer.toString(PRICE_CHANGED_STREAM));
        System.setProperty("aeron.quick.annotatedReceiver.notifyOperationDone.channel", GLOBAL_CHANNEL);
        System.setProperty("aeron.quick.annotatedReceiver.notifyOperationDone.stream", Integer.toString(NOTIFY_OPERATION_STREAM));
        System.setProperty("aeron.quick.annotatedReceiver.duplicateNumber.channel", GLOBAL_CHANNEL);
        System.setProperty("aeron.quick.annotatedReceiver.duplicateNumber.stream", Integer.toString(DUPLICATE_NUMBER_STREAM));
    }

    private static void closeExample(){
        factory.close();
        if(serverRunner != null)
            serverRunner.close();
    }
}
