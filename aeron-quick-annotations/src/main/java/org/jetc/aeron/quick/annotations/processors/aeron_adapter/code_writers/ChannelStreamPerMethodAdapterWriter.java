package org.jetc.aeron.quick.annotations.processors.aeron_adapter.code_writers;

import org.jetc.aeron.quick.annotations.processors.aeron_adapter.AdapterConfiguration;
import org.jetc.aeron.quick.annotations.processors.utils.AdaptableMethod;
import org.jetc.aeron.quick.annotations.processors.utils.AdaptingError;
import javax.lang.model.element.PackageElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.List;
import static org.jetc.aeron.quick.annotations.processors.aeron_adapter.code_writers.AeronFragmentHandlerWriter.appendParamValueFromBufferStr;

public class ChannelStreamPerMethodAdapterWriter extends AdapterCodeWriter {
    private static final String ADAPTER_BASE_CLASS_NAME = "ReceiverAdapterBase";
    private static final int DEFAULT_FRAGMENT_LIMIT = 3;
    private static final String CONTEXTUAL_HANDLER_1ST_LINE = "aeron -> (DirectBuffer buffer, int offset, int length, Header header) -> {";
    private static final String DEFAULT_SERVER_PARAM_NAME = "server";

    private final PackageElement elementPackage;

    public ChannelStreamPerMethodAdapterWriter(JavaFileObject sourceFile, AdapterConfiguration config) throws IOException, AdaptingError {
        super(sourceFile, config);
        this.elementPackage = (PackageElement) config.classToAdapt().getEnclosingElement();
    }

    @Override
    public void generateAdapterCode() throws IOException {
        append("package ").append(String.valueOf(elementPackage.getQualifiedName())).append(";");
        newLine();
        append("import org.jetc.aeron.quick.server.precompile.").append(ADAPTER_BASE_CLASS_NAME).append(";");
        newLine();
        append("""
                import org.jetc.aeron.quick.messaging.ReceiverBindingProvider;
                import org.jetc.aeron.quick.messaging.fragment_handling.ContextualHandler;
                import org.jetc.aeron.quick.messaging.subscription.SubscriptionMeta;
                import com.fasterxml.jackson.core.JsonProcessingException;
                import com.fasterxml.jackson.databind.ObjectMapper;
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
        append("class ").append(config.finalAdapterName()).append(" implements ").append(ADAPTER_BASE_CLASS_NAME + "<").append(config.classToAdaptName()).append(">").append("{");
        startBlock();
        append("    private static final Logger log = LoggerFactory.getLogger(").append(config.finalAdapterName()).append(".class);");
        newLine();
        append("    private static final String PROPS_SUFFIX = \"aeron.quick.").append(config.receiverName()).append(".\";");
        newLine();
        append("""
                    private record Binding(String methodName, int fragmentLimit, ContextualHandler handler){}
                    private final List<Binding> bindingsToCompute;
                
                    private static String getPropForMethod(String method, String prop){
                            String value = System.getProperty(PROPS_SUFFIX + method + "." + prop);
                            if(value == null || value.isBlank())
                                value = System.getProperty(PROPS_SUFFIX + prop);
                            return value;
                    }
                
                    @Override
                    public ReceiverBindingProvider getBindings() {
                        ReceiverBindingProvider computedBindings = new ReceiverBindingProvider(new HashMap<>());
            
                        for (Binding binding : this.bindingsToCompute){
                            boolean isRepeatedBinding = computedBindings.setBinding(
                                    getPropForMethod(binding.methodName() ,"channel"),
                                    Integer.parseInt(getPropForMethod(binding.methodName(), "stream")),
                                    new SubscriptionMeta(binding.handler(), binding.fragmentLimit())
                            ) != null;
            
                            if(isRepeatedBinding)
                                throw new IllegalStateException("Only unique channel-stream pairs are allowed. Check properties for: %s".formatted(PROPS_SUFFIX + binding.methodName()));
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
        iAppend("new Binding(");
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

        iAppend(DEFAULT_SERVER_PARAM_NAME + "." + method.getSimpleName() + "(");

        if(lastParamIx > -1) {
            StringBuilder paramListLenStr = new StringBuilder();
            startBlock();
            method.forEachParam((param, ix) -> {
                appendParamValueFromBufferStr(this, "buffer", "offset" + paramListLenStr, param.getElement());

                if (ix < lastParamIx) {
                    append(",");
                    newLine();
                }

                paramListLenStr.append(" + ").append(param.getLengthStr());
            });
            endBlock();
            iAppend("");
        }
        append(");");
    }

}
