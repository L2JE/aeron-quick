package org.jetc.aeron.quick.annotations.processors.aeron_adapter.code_writers.fragments.receiver;

import org.jetc.aeron.quick.annotations.processors.aeron_adapter.code_writers.AdapterCodeWriter;
import org.jetc.aeron.quick.annotations.processors.utils.AdaptableMethod;
import java.io.IOException;
import java.util.List;

public class SequentialFragmentBindingAppenderMetaWriter implements ReceiverFragmentBindingAppenderMetaWriter{

    @Override
    public void writeImports(AdapterCodeWriter writer) throws IOException {
        writer.iAppendLine("import org.jetc.aeron.quick.messaging.subscription.SubscriptionMeta;");
    }

    @Override
    public void writeSuperClassName(AdapterCodeWriter writer) throws IOException {
        writer.append("SequentialAdapter");
    }

    @Override
    public void writeBindingAppenderMetaArgDeclaration(AdapterCodeWriter writer, String classToAdaptName) throws IOException {
        writer.append("SubscriptionMeta");
    }

    @Override
    public void writeInstantiation(AdapterCodeWriter writer) throws IOException {
        writer.iAppend("new SubscriptionMeta(");
    }

    @Override
    public void writeHandlerLambda1stLine(AdapterCodeWriter writer) throws IOException {
        writer.iAppend("(buffer, offset, length, header) -> {");
    }

    @Override
    public void writeDispatchAction(AdapterCodeWriter writer, String serverParamName, AdaptableMethod method, int methodCount) throws IOException {
        writer.iAppend(serverParamName + "." + method.getSimpleName() + "(");
        final int lastParamIx = method.getParamsCount() - 1;

        try {
            method.forEachParam((param, ix) -> {
                writer.append(param.getParamId());
                if (ix < lastParamIx)
                    writer.append(", ");
            });
        } catch (Exception e){
            throw new IOException(e);
        }
        writer.append(");");
    }

    @Override
    public void writeExtraInnerClasses(AdapterCodeWriter writer, String classToAdaptName, List<AdaptableMethod> methods) {}
}
