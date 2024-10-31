package org.jetc.aeron.quick.annotations.processors.aeron_adapter.code_writers.fragments.receiver;

import org.jetc.aeron.quick.annotations.processors.aeron_adapter.code_writers.AdapterCodeWriter;
import org.jetc.aeron.quick.annotations.processors.utils.AdaptableMethod;
import java.io.IOException;
import java.util.List;

public class ConcurrentFragmentBindingAppenderMetaWriter implements ReceiverFragmentBindingAppenderMetaWriter{
    private final String METHOD_EXECUTOR_NAME = "MethodExecutor";

    @Override
    public void writeImports(AdapterCodeWriter writer) throws IOException {
        writer.iAppendLine("import org.jetc.aeron.quick.messaging.subscription.ConcurrentSubscriptionMeta;");
        writer.iAppendLine("import org.jetc.aeron.quick.deferred_exec.MethodExecutor;");
    }

    @Override
    public void writeSuperClassName(AdapterCodeWriter writer) throws IOException {
        writer.append("ConcurrentAdapter");
    }

    @Override
    public void writeBindingAppenderMetaArgDeclaration(AdapterCodeWriter writer, String classToAdaptName) throws IOException {
        writer.append("ConcurrentSubscriptionMeta<").append(classToAdaptName).append(">");
    }

    @Override
    public void writeInstantiation(AdapterCodeWriter writer) throws IOException {
        writer.iAppend("new ConcurrentSubscriptionMeta<>(");
    }

    @Override
    public void writeHandlerLambda1stLine(AdapterCodeWriter writer) throws IOException {
        writer.iAppend("(event, sequence, buffer, offset, length) -> {");
    }

    @Override
    public void writeDispatchAction(AdapterCodeWriter writer, String serverParamName, AdaptableMethod method, int methodCount) throws IOException {
        writer.iAppend("event.setExecutor(new ").append(METHOD_EXECUTOR_NAME).append(String.valueOf(methodCount)).append("(");
        writeArgList(writer, method, false);
        writer.append("));");
    }


    @Override
    public void writeExtraInnerClasses(AdapterCodeWriter writer, String classToAdaptName, List<AdaptableMethod> methods) throws IOException {
        int count = 0;
        for(var method :methods){
            writer.iAppend("public record ").append(METHOD_EXECUTOR_NAME).append(String.valueOf(count++)).append("(");
            writeArgList(writer, method, true);
            writer.append(")");
            writer.append(" implements MethodExecutor<").append(classToAdaptName).append(">{");
            writer.startBlock();

            writer.iAppendLine("@Override");
            writer.iAppend("public void runMethod(").append(classToAdaptName).append(" target) {");
            writer.startBlock();

            writer.iAppend("target.").append(method.getSimpleName()).append("(");
            writeArgList(writer, method, false);
            writer.append(");");

            writer.endBlock();
            writer.iAppend("}");
            writer.endBlock();
            writer.iAppendLine("}");
        }
    }

    private void writeArgList(AdapterCodeWriter writer, AdaptableMethod method, boolean prefixType) throws IOException {
        final int lastParamIx = method.getParamsCount() - 1;

        try {
            method.forEachParam((param, ix) -> {
                if(prefixType)
                    writer.append(param.getDeclaredType().toString()).append(" ");

                writer.append(param.getParamId());
                if (ix < lastParamIx)
                    writer.append(", ");
            });
        } catch (Exception e){
            throw new IOException(e);
        }
    }

}
