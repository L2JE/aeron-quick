package org.jetc.aeron.quick.samples.first_basic_example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aeron.Publication;
import io.aeron.logbuffer.Header;
import org.agrona.DirectBuffer;
import org.agrona.ExpandableDirectByteBuffer;
import org.agrona.MutableDirectBuffer;
import org.jetc.aeron.quick.messaging.ReceiverBindingProvider;
import org.jetc.aeron.quick.messaging.fragment_handling.ContextualHandler;
import org.jetc.aeron.quick.messaging.subscription.SubscriptionMeta;
import org.jetc.aeron.quick.samples.general.AeronQuickGeneralServiceServer;
import org.jetc.aeron.quick.server.precompile.ServerAdapterBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyGeneralReceiverAdapter implements ServerAdapterBase<MyGeneralReceiver> {
    public static final String GLOBAL_CHANNEL = "aeron:udp?endpoint=localhost:20121";
    public static final Integer PRICE_CHANGED_STREAM = 1;
    public static final Integer NOTIFY_OPERATION_STREAM = 2;
    public static final Integer DUPLICATE_NUMBER_STREAM = 3;

    private static final Map<String, String> SYS_PROPS_MOCKUP = Map.of(
            "aeron.quick.generalReceiver.priceChanged.channel", GLOBAL_CHANNEL,
            "aeron.quick.generalReceiver.priceChanged.stream", PRICE_CHANGED_STREAM.toString(),
            "aeron.quick.generalReceiver.notifyOperationDone.channel", GLOBAL_CHANNEL,
            "aeron.quick.generalReceiver.notifyOperationDone.stream", NOTIFY_OPERATION_STREAM.toString(),
            "aeron.quick.generalReceiver.duplicateNumber.channel", GLOBAL_CHANNEL,
            "aeron.quick.generalReceiver.duplicateNumber.stream", DUPLICATE_NUMBER_STREAM.toString()
    );
    private static final Logger log = LoggerFactory.getLogger(MyGeneralReceiverAdapter.class);

    @Override
    public ReceiverBindingProvider getBindings() {
        ReceiverBindingProvider computedBindings = new ReceiverBindingProvider(new HashMap<>());

        for (Binding binding : this.bindingsToCompute){
            boolean isRepeatedBinding = computedBindings.setBinding(
                    SYS_PROPS_MOCKUP.get(binding.propsPrefix() + ".channel"),
                    Integer.parseInt(SYS_PROPS_MOCKUP.get(binding.propsPrefix() + ".stream")),
                    new SubscriptionMeta(binding.handler(), binding.fragmentLimit())
            ) != null;

            if(isRepeatedBinding)
                throw new IllegalStateException("Only unique channel-stream pairs are allowed. Check properties for: %s".formatted(binding.propsPrefix()));
        }

        return computedBindings;
    }
    private record Binding(String propsPrefix, int fragmentLimit, ContextualHandler handler){}

    private final List<Binding> bindingsToCompute;
    private static final ObjectMapper mapper = new ObjectMapper();

    private static <T> T mapToObject(String content, Class<T> targetClass){
        try {
            return mapper.readValue(content, targetClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    // To be computed on Processor:
    public MyGeneralReceiverAdapter(MyGeneralReceiver server){
        MutableDirectBuffer rspBuffer = new ExpandableDirectByteBuffer(256);
        bindingsToCompute = List.of(
            new Binding(
                    "aeron.quick.generalReceiver.priceChanged",
                    3,
                    aeron -> (DirectBuffer buffer, int offset, int length, Header header) -> {
                        server.priceChanged(buffer.getDouble(offset, ByteOrder.LITTLE_ENDIAN));
                    }
            ),
            new Binding(
                    "aeron.quick.generalReceiver.notifyOperationDone",
                    3,
                    aeron -> (DirectBuffer buffer, int offset, int length, Header header) -> {
                        String content = buffer.getStringWithoutLengthUtf8(offset, length);
                        log.warn("Received: %s".formatted(content));
                        server.notifyOperationDone(mapToObject(content, MyGeneralReceiver.ExampleParamClass.class));
                    }
            ),
            new Binding(
                    "aeron.quick.generalReceiver.duplicateNumber",
                    3,
                    aeron -> (DirectBuffer buffer, int offset, int length, Header header) -> {
                        long param1 = buffer.getLong(offset, ByteOrder.LITTLE_ENDIAN);
                        rspBuffer.putLong(0, server.duplicateNumber(param1), ByteOrder.LITTLE_ENDIAN);

                        int paramListLen = Long.BYTES;
                        int endParamListPos = offset + paramListLen;
                        int streamIDLen = Integer.BYTES;
                        int channelLen = length - paramListLen - streamIDLen;
                        String rspChannel = buffer.getStringWithoutLengthUtf8(endParamListPos, channelLen);
                        int rspStreamID = buffer.getInt(offset + paramListLen + channelLen, ByteOrder.LITTLE_ENDIAN);

                        Publication rsp = aeron.addExclusivePublication(rspChannel, rspStreamID);
                        while (!rsp.isConnected()){
                            aeron.context().idleStrategy().idle();
                        }
                        rsp.offer(rspBuffer, 0, Long.BYTES);
                    }
            )
        );
    }
}
