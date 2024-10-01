package org.jetc.aeron.quick.samples.just_annotation_receiver;

import io.aeron.Aeron;
import io.aeron.Publication;
import io.aeron.driver.MediaDriver;
import org.agrona.BitUtil;
import org.agrona.BufferUtil;
import org.agrona.CloseHelper;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import java.nio.ByteOrder;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Basic Aeron publisher application.
 * <p>
 * This publisher sends a fixed number of messages on a group of channels and stream IDs,
 * This example is derived from
 * <a href="https://github.com/real-logic/aeron/blob/47c6ea7177f5995f20c20b7eebdca85a1c88f857/aeron-samples/src/main/java/io/aeron/samples/BasicPublisher.java">
 *     Aeron's BasicPublisher sample
 * </a>
 * to send messages to the three channel-stream pairs defined in {@link MyGeneralReceiver}
 */
public class AnyAeronPublisherExample
{
    private static final long NUMBER_OF_MESSAGES = 100;
    private static Publication[] PUBLICATIONS;
    /**
     * Main method for launching the process.
     *
     * @param args passed to the process.
     * @throws InterruptedException if the thread sleep delay is interrupted.
     */
    public static void main(final String[] args) throws InterruptedException
    {
        final MediaDriver driver = MediaDriver.launchEmbedded();

        // Connect a new Aeron instance to the media driver and create a publication on
        // the given channel and stream ID.
        // The Aeron and Publication classes implement "AutoCloseable" and will automatically
        // clean up resources when this try block is finished
        try (Aeron aeron = Aeron.connect(new Aeron.Context().aeronDirectoryName(driver.aeronDirectoryName())))
        {
            final UnsafeBuffer buffer = new UnsafeBuffer(BufferUtil.allocateDirectAligned(256, 64));
            PUBLICATIONS = new Publication[]{
                    aeron.addPublication(MyGeneralReceiver.GLOBAL_CHANNEL, MyGeneralReceiver.PRICE_CHANGED_STREAM),
                    aeron.addPublication(MyGeneralReceiver.GLOBAL_CHANNEL, MyGeneralReceiver.NOTIFY_OPERATION_STREAM),
                    aeron.addPublication(MyGeneralReceiver.GLOBAL_CHANNEL, MyGeneralReceiver.DUPLICATE_NUMBER_STREAM)
            };

            for (long i = 0; i < NUMBER_OF_MESSAGES; i++)
            {
                PublicationResult result = offerMessage(buffer, i, aeron);
                final long position = result.value();

                if (position > 0)
                {
                    System.out.println("yay!");
                    result.postAction().ifPresent(action -> action.accept(position));
                }
                else if (position == Publication.BACK_PRESSURED)
                {
                    System.out.println("Offer failed due to back pressure");
                }
                else if (position == Publication.NOT_CONNECTED)
                {
                    System.out.println("Offer failed because publisher is not connected to a subscriber");
                }
                else if (position == Publication.ADMIN_ACTION)
                {
                    System.out.println("Offer failed because of an administration action in the system");
                }
                else if (position == Publication.CLOSED)
                {
                    System.out.println("Offer failed because publication is closed");
                    break;
                }
                else if (position == Publication.MAX_POSITION_EXCEEDED)
                {
                    System.out.println("Offer failed due to publication reaching its max position");
                    break;
                }
                else
                {
                    System.out.println("Offer failed due to unknown reason: " + position);
                }

                Thread.sleep(TimeUnit.SECONDS.toMillis(2));
            }

            System.out.println("Done sending.");
        }

        CloseHelper.close(driver);
    }

    private static PublicationResult offerMessage(MutableDirectBuffer buffer, long i, Aeron aeron) {
        int target = (int) (i % PUBLICATIONS.length);
        int length;
        final String messageCount = i + "/" + NUMBER_OF_MESSAGES;
        Consumer<Long> postAction = null;
        switch (target) {
            case 0:
                buffer.putDouble(0, i + 0.5, ByteOrder.LITTLE_ENDIAN);
                length = BitUtil.SIZE_OF_DOUBLE;
                break;
            case 1:
                String content = "{\"value1\":\"hello-%s\",\"value2\":%s}".formatted(i, i);
                length = buffer.putStringUtf8(0, content, ByteOrder.LITTLE_ENDIAN);
                break;
            case 2:
                buffer.putLong(0, i, ByteOrder.LITTLE_ENDIAN);
                length = BitUtil.SIZE_OF_LONG;
                break;
            default:
                System.out.printf("Invalid Target: %s%n", target);
                return PublicationResult.error(-15);
        }

        System.out.print("Offering " + messageCount + " - ");
        if (!PUBLICATIONS[target].isConnected())
        {
            System.out.println("No active subscribers detected");
            return PublicationResult.error(Publication.NOT_CONNECTED);
        }

        return new PublicationResult(PUBLICATIONS[target].offer(buffer, 0, length), postAction != null ? Optional.of(postAction) : Optional.empty());
    }

    public record PublicationResult(long value, Optional<Consumer<Long>> postAction){
        public static PublicationResult error(long value){
            return new PublicationResult(value, null);
        }
    }
}
