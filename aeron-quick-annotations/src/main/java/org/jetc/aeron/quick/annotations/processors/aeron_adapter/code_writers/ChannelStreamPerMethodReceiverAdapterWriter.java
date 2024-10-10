package org.jetc.aeron.quick.annotations.processors.aeron_adapter.code_writers;

import org.jetc.aeron.quick.annotations.processors.aeron_adapter.AdapterConfiguration;
import org.jetc.aeron.quick.annotations.processors.utils.AdaptableMethod;
import org.jetc.aeron.quick.annotations.processors.utils.AdaptingError;
import javax.lang.model.element.PackageElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.List;
import static org.jetc.aeron.quick.annotations.processors.aeron_adapter.code_writers.AeronDirectBufferOperationsWriter.appendParamValueFromBufferStr;
import static org.jetc.aeron.quick.annotations.processors.utils.FullyQualifiedClassNames.STRING_TYPE;

public class ChannelStreamPerMethodReceiverAdapterWriter extends AdapterCodeWriter {
    private static final String RECEIVER_ADAPTER_QUALIFIED_NAME = "org.jetc.aeron.quick.peers.receiver.ReceiverAdapterBase";
    private static final int DEFAULT_FRAGMENT_LIMIT = 3;
    private static final String CONTEXTUAL_HANDLER_1ST_LINE = "aeron -> (DirectBuffer buffer, int offset, int length, Header header) -> {";
    private static final String DEFAULT_SERVER_PARAM_NAME = "server";
    private boolean needsJSONMapper = false;

    private final PackageElement elementPackage;

    public ChannelStreamPerMethodReceiverAdapterWriter(JavaFileObject sourceFile, AdapterConfiguration config) throws IOException, AdaptingError {
        super(sourceFile, config);
        this.elementPackage = (PackageElement) config.classToAdapt().getEnclosingElement();
    }

    @Override
    public void generateAdapterCode() throws IOException {
        append("package ").append(String.valueOf(elementPackage.getQualifiedName())).append(";");
        newLine();
        append("""
                import org.jetc.aeron.quick.messaging.ReceiverBindingToAeronBindingMapper;
                import org.jetc.aeron.quick.messaging.serialization.ObjectStringMapper;
                import org.jetc.aeron.quick.peers.receiver.ReceiverBinding;
                import org.jetc.aeron.quick.AeronQuickContext;
                import org.jetc.aeron.quick.messaging.fragment_handling.ContextualHandler;
                import org.jetc.aeron.quick.messaging.subscription.SubscriptionMeta;
                import io.aeron.Publication;
                import io.aeron.logbuffer.Header;
                import org.agrona.DirectBuffer;
                import org.agrona.ExpandableDirectByteBuffer;
                import org.agrona.MutableDirectBuffer;
                import org.agrona.BitUtil;
                import org.slf4j.Logger;
                import org.slf4j.LoggerFactory;
                import java.nio.ByteOrder;
                import java.util.HashMap;
                import java.util.List;
                import java.util.Map;
                """);
        newLine();
        append("public class ").append(config.finalAdapterName()).append(" implements ").append(RECEIVER_ADAPTER_QUALIFIED_NAME + "<").append(config.classToAdaptName()).append(">").append("{");
        startBlock();
        append("    private static final Logger log = LoggerFactory.getLogger(").append(config.finalAdapterName()).append(".class);");
        newLine();
        append("""
                    private final List<ReceiverBinding> bindingsToCompute;
                    private String receiverName;
                    private AeronQuickContext context;
                    private ObjectStringMapper mapper;
                
                    @Override
                    public void setContext(AeronQuickContext context, String componentName) {
                        receiverName = componentName;
                        this.context = context;
                        mapper = context.getObjectMapper();
                    }
                
                    @Override
                    public ReceiverBindingToAeronBindingMapper getBindings() {
                        ReceiverBindingToAeronBindingMapper computedBindings = new ReceiverBindingToAeronBindingMapper(new HashMap<>());
            
                        for (ReceiverBinding binding : this.bindingsToCompute){
                            boolean isRepeatedBinding = computedBindings.setBinding(
                                    context.getProperty(receiverName, binding.methodName() ,"channel"),
                                    context.getIntProperty(receiverName, binding.methodName(), "stream"),
                                    new SubscriptionMeta(binding.handler(), binding.fragmentLimit())
                            ) != null;
            
                            if(isRepeatedBinding)
                                throw new IllegalStateException("Only unique channel-stream pairs are allowed. Check properties for: %s.%s".formatted(receiverName, binding.methodName()));
                        }
            
                        return computedBindings;
                    }
                """);
        try {
            writeConstructorMethod();
        } catch (Exception e){
            throw new IOException(e);
        }
        newLine();
        append("}");
    }

    private void writeConstructorMethod() throws Exception {
        iAppend("public ").append(config.finalAdapterName()).append("(").append(config.classToAdaptName()).append(" "+DEFAULT_SERVER_PARAM_NAME+"){");
        startBlock();
        iAppend("MutableDirectBuffer rspBuffer = ").append(config.bufferForResponsesCreationStr());
        newLine();
        iAppend("bindingsToCompute = List.of(");
        startBlock();
        
        List<AdaptableMethod> methodsToAdapt = config.methodsToAdapt();
        int pos = methodsToAdapt.size() - 1;
        for(AdaptableMethod method : methodsToAdapt){
            writeBindingForMethod(method);
            if(pos-- > 0)
                append(",");
            newLine();
        }
        endBlock();
        iAppend(");");
        endBlock();
        iAppend("}");
    }

    private void writeBindingForMethod(AdaptableMethod method) throws Exception {
        iAppend("new ReceiverBinding(");
        startBlock();
        iAppend("\"").append(method.getPropName()).append("\",");
        newLine();
        iAppend(DEFAULT_FRAGMENT_LIMIT + ",");
        newLine();

        switch (method.getMethodKind()){
            case CONTEXTUAL_HANDLER -> iAppend(DEFAULT_SERVER_PARAM_NAME + "::" + method.getSimpleName());
            case FRAGMENT_HANDLER -> iAppend("aeron -> "+ DEFAULT_SERVER_PARAM_NAME + "::" + method.getSimpleName());
            case COMMON -> {
                iAppend(CONTEXTUAL_HANDLER_1ST_LINE);
                startBlock();

                writeHandlerBody(method);

                endBlock();
                iAppend("}");
            }
        }

        endBlock();
        iAppend(")");
    }

    private void writeHandlerBody(AdaptableMethod method) throws Exception{
        final int lastParamIx = method.getParamsCount() - 1;

        if(lastParamIx > -1) {
            StringBuilder paramListLenStr = new StringBuilder();
            method.forEachParam((param, ix) -> {
                String originalParamId = param.getParamId();
                String declaredType = param.getDeclaredType().toString();

                if(param.isNoStringObject()) {
                    param.setParamId(originalParamId + "_str");
                    declaredType = STRING_TYPE;
                }

                iAppend(declaredType + " " + param.getParamId() + " = ");

                appendParamValueFromBufferStr(this, "buffer", "offset" + paramListLenStr, param.getElement()).append(";");

                if(param.isNoStringObject()) {
                    needsJSONMapper = true;
                    newLine();
                    iAppend(param.getDeclaredType() + " " + originalParamId + " = ").append("mapper.deserialize(").append(param.getParamId()).append(", ").append(param.getDeclaredType().toString()).append(".class);");
                }

                newLine();

                paramListLenStr.append(" + ").append(param.getLengthStr());
                param.setParamId(originalParamId);
            });
        }

        iAppend(DEFAULT_SERVER_PARAM_NAME + "." + method.getSimpleName() + "(");
        method.forEachParam((param, ix)->{
            append(param.getParamId());
            if(ix < lastParamIx)
                append(", ");
        });
        append(");");
    }

}
