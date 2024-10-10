package org.jetc.aeron.quick.annotations.processors.aeron_adapter.code_writers;

import org.jetc.aeron.quick.annotations.processors.aeron_adapter.AdapterConfiguration;
import org.jetc.aeron.quick.annotations.processors.utils.AdaptableMethod;
import org.jetc.aeron.quick.annotations.processors.utils.AdaptingError;
import javax.lang.model.element.PackageElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import static org.jetc.aeron.quick.annotations.processors.aeron_adapter.code_writers.AeronDirectBufferOperationsWriter.appendPutValueOnBufferStr;

public class ChannelStreamPerMethodSenderAdapterWriter extends AdapterCodeWriter {
    private static final String SENDER_ADAPTER_QUALIFIED_NAME = "org.jetc.aeron.quick.peers.sender.SenderAdapterBase";
    private boolean needsJSONMapper = false;

    public ChannelStreamPerMethodSenderAdapterWriter(JavaFileObject sourceFile, AdapterConfiguration config) throws IOException {
        super(sourceFile, config);
    }

    @Override
    public void generateAdapterCode() throws IOException, AdaptingError {
        append("package ").append(((PackageElement)config.classToAdapt().getEnclosingElement()).getQualifiedName().toString()).append(";");
        newLine();
        append("""
            import com.fasterxml.jackson.core.JsonProcessingException;
            import org.jetc.aeron.quick.messaging.serialization.ObjectStringMapper;
            import org.jetc.aeron.quick.AeronQuickContext;
            import io.aeron.Aeron;
            import io.aeron.Publication;
            import org.agrona.BitUtil;
            import org.agrona.BufferUtil;
            import org.agrona.ExpandableDirectByteBuffer;
            import org.agrona.MutableDirectBuffer;
            import org.agrona.concurrent.IdleStrategy;
            import org.agrona.concurrent.SleepingMillisIdleStrategy;
            import org.agrona.concurrent.UnsafeBuffer;
            import org.jetc.aeron.quick.exception.PublicationOfferFailedException;
            import java.nio.ByteOrder;
            import java.time.Duration;
            """
        );
        newLine();
        append("public class ").append(config.finalAdapterName()).append(" implements ").append(SENDER_ADAPTER_QUALIFIED_NAME + "<").append(config.classToAdaptName()).append(">, ").append(config.classToAdaptName()).append("{");
        startBlock();
        append("    private static final String PROPS_SUFFIX = \"aeron.quick.\";");
        newLine();
        append("""
            private final Duration idleTime = Duration.ofMillis(100);
            private final Duration connectionTimeout = Duration.ofSeconds(5);
            private final IdleStrategy idleStrategy = new SleepingMillisIdleStrategy(idleTime.toMillis());
            private final long maxRetries = connectionTimeout.toMillis() / idleTime.toMillis();
            private final int DEFAULT_INIT_BUFFER_SIZE = 256;
            private AeronQuickContext context;
            private ObjectStringMapper mapper;
        
            private void offerMsg(Publication publication, MutableDirectBuffer buffer, int offset, int length) {
                long streamPos = publication.offer(buffer, offset, length);
                long i = 0;
                for (; i < maxRetries && streamPos < 0; i++) {
                    idleStrategy.idle();
                    streamPos = publication.offer(buffer, offset, length);
                }
                if(i >= maxRetries)
                    throw new PublicationOfferFailedException();
            }
        
            @Override
            public AeronGeneralServiceContract getClient() {
                return this;
            }
        """);
        iAppend("private final Publication[] publications = new Publication[").append(String.valueOf(config.methodsToAdapt().size())).append("];");
        newLine();
        writeBuffersAttribute();
        writeInitSenderMethod();
        writeContractMethodsImplementations();
        newLine();
        append("}");
    }

    private void writeBuffersAttribute() throws IOException {
        iAppend("private final MutableDirectBuffer[] buffers = new MutableDirectBuffer[]{");
        startBlock();
        long lastMethodIx = config.methodsToAdapt().size() - 1;
        for (var method : config.methodsToAdapt()){
            iAppend("new ").append(
                    method.isPrimitive() ?
                    "UnsafeBuffer(BufferUtil.allocateDirectAligned(DEFAULT_INIT_BUFFER_SIZE, 64))" :
                    "ExpandableDirectByteBuffer(DEFAULT_INIT_BUFFER_SIZE)");

            if(lastMethodIx-- > 0) {
                append(", ");
                newLine();
            }
        }
        endBlock();
        iAppendLine("};");
    }

    private void writeInitSenderMethod() throws IOException {
        iAppendLine("@Override");
        iAppend("public void setContext(AeronQuickContext context, String componentName) {");
        startBlock();
            iAppendLine("this.context = context;");
            iAppendLine("mapper = context.getObjectMapper();");
            iAppend("final String[] methods = new String[]{");
            long lastMethodIx = config.methodsToAdapt().size() - 1;
            for (var method : config.methodsToAdapt()){
                append("\"").append(method.getPropName()).append("\"");

                if(lastMethodIx-- > 0)
                    append(", ");
            }
            append("};");
            newLine();
            append("""
                    for(int i = 0; i < publications.length; i++){
                        publications[i] = context.getAeron().addExclusivePublication(
                            context.getProperty(componentName, methods[i], "channel"),
                            context.getIntProperty(componentName, methods[i], "stream")
                        );
                    }
            """);
        endBlock();
        iAppendLine("}");
    }

    private void writeContractMethodsImplementations() throws IOException {
        int methodIx = 0;
        for (var method : config.methodsToAdapt()){
            iAppendLine("@Override");
            iAppend("public ").append(method.getSignature()).append("{"); //only public methods will be adapted (ensured by processor)
            startBlock();
            try {
                writeMethodBody(method, methodIx);
            } catch (Exception e){
                throw new IOException(e);
            }
            endBlock();
            iAppend("}");
            methodIx++;
            newLine();
        }
    }

    private void writeMethodBody(AdaptableMethod method, int methodIx) throws Exception {
        String methodIxStr = String.valueOf(methodIx);
        iAppend("MutableDirectBuffer buffer = buffers[").append(methodIxStr).append("];");
        newLine();
        StringBuilder paramListLenStr = new StringBuilder("0");
        method.forEachParam((param, ix) -> {
            String paramValue = param.getParamName();

            if(!param.isPrimitive()) {
                iAppend("int ").append(paramValue).append("Length = ");
                if(param.isNoStringObject()) {
                    paramValue = "mapper.serialize(" + paramValue + ")";
                    needsJSONMapper = true;
                }
            } else
                iAppend("");

            appendPutValueOnBufferStr(this, "buffer", paramListLenStr.toString(), param.getElement(), paramValue).append(";");
            newLine();

            paramListLenStr.append(" + ");
            if(!param.isPrimitive())
                paramListLenStr.append(param.getParamName()).append("Length");
            else
                paramListLenStr.append(param.getLengthStr());

        });
        iAppend("offerMsg(publications[").append(methodIxStr).append("], buffer, 0, ").append(paramListLenStr).append(");");

        switch (method.getReturnType()){
            case BYTE:
            case SHORT:
            case INT:
            case LONG:
            case CHAR:
            case FLOAT:
            case DOUBLE:
                newLine();
                iAppend("return 0;");
                break;
            case BOOLEAN:
                newLine();
                iAppend("return false;");
                break;
            case DECLARED:
                newLine();
                iAppend("return null;");
        }
    }
}
