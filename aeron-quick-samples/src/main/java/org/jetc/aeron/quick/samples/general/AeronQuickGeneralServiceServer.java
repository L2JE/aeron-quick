package org.jetc.aeron.quick.samples.general;

import org.agrona.concurrent.SleepingMillisIdleStrategy;
import org.jetc.aeron.quick.AeronQuickFactory;
import org.jetc.aeron.quick.annotations.AeronQuickReceiver;
import org.jetc.aeron.quick.server.AeronQuickReceiverRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;

@AeronQuickReceiver(name = "generalReceiver")
public class AeronQuickGeneralServiceServer implements AeronGeneralServiceContract {

    private static final Logger log = LoggerFactory.getLogger(AeronQuickGeneralServiceServer.class);
    private static final AeronQuickFactory factory = AeronQuickFactory.builder().unplugged(true).removeAeronDirOnShutdown(true).build();

    public static void main(String[] args) throws InterruptedException {
        log.warn("STARTING SERVER FROM - %s".formatted(Thread.currentThread().getName()));
        AeronQuickGeneralServiceServer server = new AeronQuickGeneralServiceServer().startServer();
        Thread.sleep(Duration.ofMinutes(5));
        log.warn("STOPPING SERVER FROM - %s".formatted(Thread.currentThread().getName()));
        server.stopServer();
        factory.close();
    }

    private AeronQuickReceiverRunner<AeronQuickGeneralServiceServer> serverRunner;

    public AeronQuickGeneralServiceServer startServer(){
        factory.connectClient();
        factory.getReceiverBuilder(this)
            .ifPresentOrElse( builder -> {
                builder.setAgentIdleStrategy(new SleepingMillisIdleStrategy(1000*2))
                    .setAgentErrorHandler(e -> {
                        log.error("Agent error handler captured error ....", e);
                        this.serverRunner.close();
                        factory.close();
                        log.warn("Agent error handler finished...");
                    });
                this.serverRunner = builder.build();
                this.serverRunner.start();
                log.warn("FINISHED STARTING SERVER - %s".formatted(Thread.currentThread().getName()));
            }, factory::close);
        return this;
    }

    public void stopServer(){
        if(this.serverRunner != null)
            this.serverRunner.close();
    }


    @Override
    public void priceChanged(double newPrice) {
        log.warn("priceChanged - with %s".formatted(newPrice));
        heavyOperation();
        log.warn("priceChanged FINISHED");
    }

    @Override
    public long duplicateNumber(long target) {
        log.warn("duplicateNumber - with %s and result %s".formatted(target, target * 2));
        long result = target * 2;
        heavyOperation();
        log.warn("duplicateNumber FINISHED with result %s".formatted(result));
        return result;
    }

    @Override
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
