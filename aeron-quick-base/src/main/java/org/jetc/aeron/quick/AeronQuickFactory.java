package org.jetc.aeron.quick;

import io.aeron.Aeron;
import io.aeron.CommonContext;
import io.aeron.driver.MediaDriver;
import io.aeron.exceptions.AeronException;
import org.agrona.CloseHelper;
import org.jetc.aeron.quick.annotations.AeronQuickContract;
import org.jetc.aeron.quick.annotations.AeronQuickReceiver;
import org.jetc.aeron.quick.annotations.QuickContractEndpoint;
import org.jetc.aeron.quick.peers.PeerConfiguration;
import org.jetc.aeron.quick.peers.adapters.Adapters;
import org.jetc.aeron.quick.peers.adapters.exception.AdaptingException;
import org.jetc.aeron.quick.peers.receiver.AeronQuickReceiverRunner;
import org.jetc.aeron.quick.peers.receiver.ReceiverAdapter;
import org.jetc.aeron.quick.peers.receiver.ReceiverAgentConfiguration;
import org.jetc.aeron.quick.peers.sender.AeronQuickSenderBuilder;
import org.jetc.aeron.quick.peers.receiver.AeronQuickReceiverBuilder;
import org.jetc.aeron.quick.peers.receiver.ReceiverAdapterBase;
import org.jetc.aeron.quick.peers.sender.SenderAdapter;
import org.jetc.aeron.quick.peers.sender.SenderConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * This is the 'entrypoint' for the AeronQuick library.
 * <p>
 * This factory class takes an {@link Aeron.Context} and a {@link MediaDriver.Context} to create a new {@link Aeron Aeron client} and an embedded {@link MediaDriver} if necessary.
 * <p>
 * From here it is possible to create Receivers and Emitters of messages that use the {@link Aeron Aeron client} instance.
 */
public class AeronQuickFactory implements AutoCloseable{
    private static final Logger log = LoggerFactory.getLogger(AeronQuickFactory.class);
    private final Aeron.Context aeronContext;
    private final MediaDriver driver;
    private final AeronQuickContext context;

    private AeronQuickFactory(Aeron.Context aeronContext, MediaDriver.Context driverContext, boolean clientConnected){
        this.context = new AeronQuickContext();

        if(driverContext != null){
            driver = MediaDriver.launch(driverContext);
            aeronContext.aeronDirectoryName(driverContext.aeronDirectoryName());
        } else
            driver = null;

        if(clientConnected)
            this.context.setAeron(Aeron.connect(aeronContext));

        this.aeronContext = aeronContext;
        log.warn("Preparing Aeron with driver at directory: %s".formatted(aeronContext.aeronDirectoryName()));
    }

    public void connectClient(){
        if(this.context.getAeron() == null || this.context.getAeron().isClosed())
            this.context.setAeron(Aeron.connect(aeronContext));
    }

    /**
     *
     * @param targetServer any class marked with {@link AeronQuickReceiver @AeronQuickReceiver} will receive messages through aeron on the methods marked with {@link QuickContractEndpoint @QuickContractEndpoint}
     * @return an {@link AeronQuickReceiverBuilder} wrapping the target server to allow any extra fine grain configuration
     * @param <T> Any class marked with {@link AeronQuickReceiver @AeronQuickReceiver}
     * @throws SetupNotReadyException if Aeron client is not connected
     */
    public <T> Optional<AeronQuickReceiverBuilder<T>> getReceiverBuilder(T targetServer, String receiverName) throws AeronException {
        return this.getReceiverBuilder(
            Adapters.adaptReceiver(targetServer).orElseThrow(() -> new IllegalStateException("No Adapter class could be loaded for: " + targetServer.getClass().getCanonicalName())),
            receiverName
        );
    }

    public <T> Optional<AeronQuickReceiverBuilder<T>> getReceiverBuilder(ReceiverAdapterBase<T> serverEntrypoint, String receiverName){
        try {
            assertFactoryIsReady();
            return Optional.of(new AeronQuickReceiverBuilder<>(serverEntrypoint, context).setReceiverName(receiverName));
        } catch (Exception error) {
            log.error("Could not create a receiver builder: ", error);
        }
        return Optional.empty();
    }

    /**
     * @param contract an Aeron Quick Contract, any class annotated with
     * {@link AeronQuickContract @AeronQuickContract} or at least has one method
     * annotated with {@link QuickContractEndpoint @QuickContractEndpoint}
     * <p>
     * @param senderName used to find configuration properties at
     * <p>
     * {@code aeron.quick.<senderName>} eg:
     * <p>
     * {@code aeron.quick.sampleSender.methodName.channel = aeron:udp?endpoint=localhost:20121}
     * @return a client implementing the given contract, so calling a method in the client results in sending a message through Aeron
     */
    public <T> Optional<AeronQuickSenderBuilder<T>> getSenderBuilder(Class<T> contract, String senderName){
        return Optional.of(
            new AeronQuickSenderBuilder<>(
                Adapters.adaptSender(contract).orElseThrow(() -> new IllegalStateException("No Adapter class could be loaded for: " + contract.getCanonicalName())),
                context
            ).setSenderName(senderName)
        );
    }

    @SafeVarargs
    public final <T> AeronQuickReceiverRunner<T> getReceiver(T targetInstance, String componentName, Consumer<ReceiverAgentConfiguration<T>>... configDigest) throws AdaptingException {
        ReceiverAgentConfiguration<T> config = initCommonConfig(new ReceiverAgentConfiguration<>(), componentName);
        config.setEndpoint(targetInstance);

        for(var digest : configDigest) {
            digest.accept(config);
        }

        ReceiverAdapter<T> adapter = Adapters.adaptReceiver1(targetInstance);
        adapter.configure(config);

        return new AeronQuickReceiverRunner<>(config);
    }

    @SafeVarargs
    public final <T> T getSender(Class<T> targetContract, String componentName, Consumer<SenderConfiguration>... configDigest) throws AdaptingException {
        SenderConfiguration config = initCommonConfig(new SenderConfiguration(), componentName);

        for(var digest : configDigest) {
            digest.accept(config);
        }

        SenderAdapter<T> adapter = Adapters.adaptSender1(targetContract);
        adapter.configure(config);

        return adapter.getAdapted();
    }

    private <T extends PeerConfiguration> T initCommonConfig(T config, String componentName){
        config.setComponentName(componentName);
        config.setContext(context);
        return config;
    }


    /**
     * @return A helper class for building an {@link AeronQuickFactory}
     */
    public static Builder builder(){
        return new Builder();
    }

    /**
     * Closes Aeron Client and Media Driver if it's not external
     */
    @Override
    public void close() {
        CloseHelper.closeAll(this.context.getAeron(), driver);
    }

    private void assertFactoryIsReady(){
        if(this.context.getAeron() == null || this.context.getAeron().isClosed())
            throw new SetupNotReadyException("Aeron client is not connected", AeronException.Category.FATAL);
    }

    public static class SetupNotReadyException extends AeronException {

        public SetupNotReadyException(String message, Category category) {
            super(message, category);
        }
    }

    public static class Builder {
        private String embeddedDriverPath;
        private String externalDriverPath;
        private Aeron.Context aeronContext;
        private MediaDriver.Context mediaDriverContext;
        private boolean clientDisconnected;
        private boolean removeAeronDirOnShutdown = false;

        /**
         * @return {@link AeronQuickFactory}
         */
        public AeronQuickFactory build(){
            if(externalDriverPath == null && mediaDriverContext == null)
                mediaDriverContext = new MediaDriver.Context();

            if(embeddedDriverPath != null)
                mediaDriverContext.aeronDirectoryName(embeddedDriverPath);

            if(aeronContext == null)
                aeronContext = new Aeron.Context();

            if(mediaDriverContext != null) {
                mediaDriverContext.dirDeleteOnShutdown(removeAeronDirOnShutdown);
                if(CommonContext.AERON_DIR_PROP_DEFAULT.equals(mediaDriverContext.aeronDirectoryName()))
                    mediaDriverContext.aeronDirectoryName(CommonContext.generateRandomDirName());
            }

            aeronContext.aeronDirectoryName(externalDriverPath != null ? externalDriverPath : mediaDriverContext.aeronDirectoryName());

            return new AeronQuickFactory(aeronContext, externalDriverPath != null ? null : mediaDriverContext, !clientDisconnected);
        }

        public Builder embeddedDriverPath(String mediaDriverPath){
            this.embeddedDriverPath = mediaDriverPath;
            this.externalDriverPath = null;

            if(this.mediaDriverContext != null){
                this.mediaDriverContext.aeronDirectoryName(mediaDriverPath);
            }

            return this;
        }

        public Builder externalDriverPath(String mediaDriverPath){
            this.externalDriverPath = mediaDriverPath;
            this.embeddedDriverPath = null;

            if(this.mediaDriverContext != null){
                this.mediaDriverContext.aeronDirectoryName(mediaDriverPath);
            }

            return this;
        }

        public Builder aeronContext(Aeron.Context context){
            this.aeronContext = context;
            return this;
        }

        public Builder driverContext(MediaDriver.Context context){
            embeddedDriverPath(context.aeronDirectoryName());
            this.mediaDriverContext = context;
            return this;
        }

        public Builder unplugged(boolean clientDisconnected) {
            this.clientDisconnected = clientDisconnected;
            return this;
        }

        public Builder removeAeronDirOnShutdown(boolean remove) {
            this.removeAeronDirOnShutdown = remove;
            return this;
        }
    }
}