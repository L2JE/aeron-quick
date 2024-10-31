package org.jetc.aeron.quick.annotations.processors.aeron_adapter.code_writers.fragments.receiver;

import org.jetc.aeron.quick.annotations.processors.aeron_adapter.code_writers.AdapterCodeWriter;
import org.jetc.aeron.quick.annotations.processors.utils.AdaptableMethod;

import java.io.IOException;
import java.util.List;

public interface ReceiverFragmentBindingAppenderMetaWriter {
    void writeImports(AdapterCodeWriter writer) throws IOException;
    void writeSuperClassName(AdapterCodeWriter writer) throws IOException;
    void writeBindingAppenderMetaArgDeclaration(AdapterCodeWriter writer, String classToAdaptName) throws IOException;
    void writeInstantiation(AdapterCodeWriter writer) throws IOException;
    void writeHandlerLambda1stLine(AdapterCodeWriter writer) throws IOException;
    void writeDispatchAction(AdapterCodeWriter writer, String serverParamName, AdaptableMethod method, int methodCount) throws IOException;
    void writeExtraInnerClasses(AdapterCodeWriter writer, String classToAdaptName, List<AdaptableMethod> methods) throws IOException;
}
