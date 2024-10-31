package org.jetc.aeron.quick.annotations.processors.aeron_adapter.code_writers;

import org.jetc.aeron.quick.annotations.processors.aeron_adapter.AdapterConfiguration;
import org.jetc.aeron.quick.annotations.processors.aeron_adapter.code_writers.fragments.sender.SenderFragmentWriter;
import org.jetc.aeron.quick.annotations.processors.utils.AdaptableMethod;

import javax.lang.model.element.PackageElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import static org.jetc.aeron.quick.annotations.processors.aeron_adapter.code_writers.AeronDirectBufferOperationsWriter.appendPutValueOnBufferStr;

public class ChannelStreamPerMethodSenderAdapterWriter extends AdapterCodeWriter {
    private static final String SENDER_ADAPTER_QUALIFIED_NAME = "org.jetc.aeron.quick.peers.sender.SenderAdapter";
    private final SenderFragmentWriter fragmentWriter;
    private boolean needsJSONMapper = false;

    public ChannelStreamPerMethodSenderAdapterWriter(JavaFileObject sourceFile, AdapterConfiguration config, SenderFragmentWriter fragmentWriter) throws IOException {
        super(sourceFile, config);
        this.fragmentWriter = fragmentWriter;
    }

    @Override
    public void generateAdapterCode() throws IOException {
        append("package ").append(((PackageElement)config.classToAdapt().getEnclosingElement()).getQualifiedName().toString()).append(";");
        newLine();
        append("""
            import io.aeron.Publication;
            import org.agrona.BitUtil;
            import org.agrona.BufferUtil;
            import org.agrona.ExpandableDirectByteBuffer;
            import org.agrona.MutableDirectBuffer;
            import org.agrona.concurrent.UnsafeBuffer;
            import org.jetc.aeron.quick.AeronQuickContext;
            import org.jetc.aeron.quick.messaging.publication.PublicationOfferingStrategy;
            import org.jetc.aeron.quick.messaging.serialization.ObjectStringMapper;
            import org.jetc.aeron.quick.peers.sender.SenderConfiguration;
            import java.nio.ByteOrder;
            """
        );
        fragmentWriter.writeImportsFragment(this);
        newLine();
        append("public class ").append(config.finalAdapterName()).append(" implements ").append(SENDER_ADAPTER_QUALIFIED_NAME + "<").append(config.classToAdaptName()).append(">, ").append(config.classToAdaptName()).append("{");
        startBlock();
        append("""
            private ObjectStringMapper mapper;
            private PublicationOfferingStrategy offeringStrategy;
        """);
        iAppend("private final Publication[] publications = new Publication[").append(String.valueOf(config.methodsToAdapt().size())).append("];");
        newLine();
        writeBuffersAttribute();
        writeConfigureMethod();
        writeGetAdaptedMethod();
        writeContractMethodsImplementations();
        newLine();
        append("}");
    }

    private void writeGetAdaptedMethod() throws IOException {
        iAppendLine("@Override");
        iAppend("public ").append(config.classToAdaptName()).append(" getAdapted() { return this; }");
        newLine();
    }

    private void writeBuffersAttribute() throws IOException {
        iAppend("private final ");
        fragmentWriter.writeDeclareBufferFragment(this, "buffers", config.methodsToAdapt().size());
        newLine();
    }

    private void writeConfigureMethod() throws IOException {
        iAppendLine("@Override");
        iAppend("public void configure(SenderConfiguration config) {");
        startBlock();
            iAppendLine("final AeronQuickContext ctx = config.getContext();");
            iAppendLine("String senderName = config.getComponentName();");
            iAppendLine("mapper = ctx.getObjectMapper();");
            iAppendLine("offeringStrategy = config.getOfferingStrategy();");
            iAppend("final String[] methods = new String[]{");
            long lastMethodIx = config.methodsToAdapt().size() - 1;

            for (var method : config.methodsToAdapt()){
                append("\"").append(method.getPropName()).append("\"");

                if(lastMethodIx-- > 0)
                    append(", ");
            }
            append("};");
            newLine();
            fragmentWriter.writePublicationCreatorFragment(this, "publicationCreator");
            newLine();
            fragmentWriter.writeBufferCreatorFragment(this, "bufferCreator", config.methodsToAdapt());
            newLine();
            append("""
                    for(int i = 0; i < publications.length; i++){
                        publications[i] = publicationCreator.createPublication(methods[i]);
                        buffers[i] = bufferCreator.getBufferHolder(methods[i], i);
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
        iAppend("MutableDirectBuffer buffer = ");
        fragmentWriter.writeAcquireBufferFragment(this, "buffers", methodIxStr);
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
        iAppend("offeringStrategy.offerMessage(publications[").append(methodIxStr).append("], buffer, 0, ").append(paramListLenStr).append(", ");
        fragmentWriter.writeReleaseBufferFragment(this, "buffers", methodIxStr, "buffer");
        append(");");
        newLine();

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
