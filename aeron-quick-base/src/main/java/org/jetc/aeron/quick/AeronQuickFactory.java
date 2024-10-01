package org.jetc.aeron.quick;

import io.aeron.Aeron;
import io.aeron.CommonContext;
import io.aeron.driver.MediaDriver;
import io.aeron.exceptions.AeronException;
import org.agrona.CloseHelper;
import org.jetc.aeron.quick.server.Adapters;
import org.jetc.aeron.quick.server.AeronQuickReceiverBuilder;
import org.jetc.aeron.quick.server.precompile.ReceiverAdapterBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;

/**
 * This is the 'entrypoint' for the AeronQuick library.
 * <p>
 * This factory class takes an {@link Aeron.Context} and a {@link MediaDriver.Context} to create a new {@link Aeron Aeron client} and an embedded {@link MediaDriver} if necessary.
 * <p>
 * From here it is possible to create Receivers and Emitters of messages that use the {@link Aeron Aeron client} instance.
 */
public class AeronQuickFactory implements AutoCloseable{
    private static final Logger log = LoggerFactory.getLogger(AeronQuickFactory.class);

    private Aeron aeron;
    private final Aeron.Context aeronContext;
    private final MediaDriver driver;

    private AeronQuickFactory(Aeron.Context aeronContext, MediaDriver.Context driverContext, boolean clientConnected){
        if(driverContext != null){
            driver = MediaDriver.launch(driverContext);
            aeronContext.aeronDirectoryName(driverContext.aeronDirectoryName());
        } else
            driver = null;

        if(clientConnected)
            aeron = Aeron.connect(aeronContext);
        else
            aeron = null;

        this.aeronContext = aeronContext;
        log.warn("Preparing Aeron with driver at directory: %s".formatted(aeronContext.aeronDirectoryName()));
    }

    public void connectClient(){
        if(aeron == null || aeron.isClosed())
            aeron = Aeron.connect(aeronContext);
    }

    /**
     *
     * @param targetServer any class marked with {@link org.jetc.aeron.quick.annotations.AeronQuickReceiver @AeronQuickReceiver} will receive messages through aeron on the methods marked with {@link org.jetc.aeron.quick.annotations.QuickContractEndpoint @QuickContractEndpoint}
     * @return an {@link AeronQuickReceiverBuilder} wrapping the target server to allow any extra fine grain configuration
     * @param <T> Any class marked with {@link org.jetc.aeron.quick.annotations.AeronQuickReceiver @AeronQuickReceiver}
     * @throws SetupNotReadyException if Aeron client is not connected
     */
    public <T> Optional<AeronQuickReceiverBuilder<T>> getReceiverBuilder(T targetServer) throws AeronException {
        return this.getReceiverBuilder(
            Adapters.adapt(targetServer).orElseThrow(() -> new IllegalStateException("No Adapter class could be loaded for: " + targetServer.getClass().getCanonicalName()))
        );
    }

    public <T> Optional<AeronQuickReceiverBuilder<T>> getReceiverBuilder(ReceiverAdapterBase<T> serverEntrypoint){
        try {
            assertFactoryIsReady();
            return Optional.of(new AeronQuickReceiverBuilder<>(serverEntrypoint, null, this.aeron));
        } catch (Exception error) {
            log.error("Could not create a receiver builder: ", error);
        }
        return Optional.empty();
    }

    public <T> AeronQuickReceiverBuilder<T> getClientBuilder(Class<T> contract){
        return null;
    }

    /**
     * @return A helper class for building an {@link AeronQuickFactory}
     */
    public static AeronQuickFactory.Builder builder(){
        return new Builder();
    }

    /**
     * Closes Aeron Client and Media Driver if it's not external
     */
    @Override
    public void close() {
        CloseHelper.closeAll(aeron, driver);
    }

    private void assertFactoryIsReady(){
        if(aeron == null || aeron.isClosed())
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