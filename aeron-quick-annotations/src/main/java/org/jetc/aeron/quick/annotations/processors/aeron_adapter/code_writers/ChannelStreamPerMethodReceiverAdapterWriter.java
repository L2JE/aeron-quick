package org.jetc.aeron.quick.annotations.processors.aeron_adapter.code_writers;

import org.jetc.aeron.quick.annotations.processors.aeron_adapter.AdapterConfiguration;
import org.jetc.aeron.quick.annotations.processors.aeron_adapter.code_writers.fragments.receiver.ReceiverFragmentBindingAppenderMetaWriter;
import org.jetc.aeron.quick.annotations.processors.utils.AdaptableMethod;
import org.jetc.aeron.quick.annotations.processors.utils.AdaptingError;
import javax.lang.model.element.PackageElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.List;
import static org.jetc.aeron.quick.annotations.processors.aeron_adapter.code_writers.AeronDirectBufferOperationsWriter.appendParamValueFromBufferStr;
import static org.jetc.aeron.quick.annotations.processors.utils.FullyQualifiedClassNames.STRING_TYPE;

public class ChannelStreamPerMethodReceiverAdapterWriter extends AdapterCodeWriter {
    private static final String RECEIVER_ADAPTER_PACKAGE_NAME = "org.jetc.aeron.quick.peers.receiver.";
    private static final int DEFAULT_FRAGMENT_LIMIT = 3;
    private static final String DEFAULT_SERVER_PARAM_NAME = "server";
    private final PackageElement elementPackage;
    private final ReceiverFragmentBindingAppenderMetaWriter bindingAppenderWriter;

    public ChannelStreamPerMethodReceiverAdapterWriter(JavaFileObject sourceFile, AdapterConfiguration config, ReceiverFragmentBindingAppenderMetaWriter bindingAppenderWriter) throws IOException, AdaptingError {
        super(sourceFile, config);
        this.elementPackage = (PackageElement) config.classToAdapt().getEnclosingElement();
        this.bindingAppenderWriter = bindingAppenderWriter;
    }

    @Override
    public void generateAdapterCode() throws IOException {
        append("package ").append(String.valueOf(elementPackage.getQualifiedName())).append(";");
        newLine();
        append("""
                import org.agrona.BitUtil;
                import org.jetc.aeron.quick.AeronQuickContext;
                import org.jetc.aeron.quick.messaging.BindingAppender;
                import org.jetc.aeron.quick.messaging.serialization.ObjectStringMapper;
                import java.nio.ByteOrder;
                """);
        bindingAppenderWriter.writeImports(this);
        newLine();
        append("public class ").append(config.finalAdapterName()).append(" extends ").append(RECEIVER_ADAPTER_PACKAGE_NAME);
        bindingAppenderWriter.writeSuperClassName(this);
        append("<").append(config.classToAdaptName()).append(">").append("{");
        startBlock();
        writeRegisterBindingsMethod();
        newLine();
        bindingAppenderWriter.writeExtraInnerClasses(this, config.classToAdaptName(),config.methodsToAdapt());
        append("}");
    }

    private void writeRegisterBindingsMethod() throws IOException {
        iAppendLine("@Override");
        iAppend("protected void registerBindings(BindingAppender<");
        bindingAppenderWriter.writeBindingAppenderMetaArgDeclaration(this, config.classToAdaptName());
        append("> bindings, AeronQuickContext ctx, ObjectStringMapper mapper, String receiverName, ").append(config.classToAdaptName()).append(" ").append(DEFAULT_SERVER_PARAM_NAME).append(") {");
        startBlock();
        
        List<AdaptableMethod> methodsToAdapt = config.methodsToAdapt();
        int count = 0;
        for(AdaptableMethod method : methodsToAdapt){
            writeBindingForMethod(method, count++);
        }
        endBlock();
        iAppend("}");
    }

    private void writeBindingForMethod(AdaptableMethod method, int methodCount) throws IOException {
        newLine();
        iAppend("bindings.addBinding(");
        startBlock();

        iAppend("ctx.getProperty(receiverName, \"").append(method.getPropName()).append("\", \"channel\"),");
        newLine();
        iAppend("ctx.getIntProperty(receiverName, \"").append(method.getPropName()).append("\", \"stream\"),");
        newLine();

        bindingAppenderWriter.writeInstantiation(this);
        startBlock();

        switch (method.getMethodKind()){
            case FRAGMENT_HANDLER -> iAppend(DEFAULT_SERVER_PARAM_NAME + "::" + method.getSimpleName());
            case COMMON -> {
                bindingAppenderWriter.writeHandlerLambda1stLine(this);
                startBlock();

                try {
                    writeHandlerBody(method, methodCount);
                } catch (Exception e){
                    throw new IOException(e);
                }

                endBlock();
                iAppend("}");
            }
        }

        append(", ").append(String.valueOf(DEFAULT_FRAGMENT_LIMIT));

        endBlock();
        iAppend(")");
        endBlock();
        iAppend(");");
    }

    private void writeHandlerBody(AdaptableMethod method, int methodCount) throws Exception {
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
                    newLine();
                    iAppend(param.getDeclaredType() + " " + originalParamId + " = ").append("mapper.deserialize(").append(param.getParamId()).append(", ").append(param.getDeclaredType().toString()).append(".class);");
                }

                newLine();

                paramListLenStr.append(" + ").append(param.getLengthStr());
                param.setParamId(originalParamId);
            });
        }

        bindingAppenderWriter.writeDispatchAction(this, DEFAULT_SERVER_PARAM_NAME, method, methodCount);
    }

}
