package org.jetc.aeron.quick.annotations.processors.aeron_adapter.code_writers.fragments.sender;

import org.jetc.aeron.quick.annotations.processors.aeron_adapter.code_writers.AdapterCodeWriter;
import org.jetc.aeron.quick.annotations.processors.utils.AdaptableMethod;
import java.io.IOException;
import java.util.List;

public interface SenderFragmentWriter {
    void writePublicationCreatorFragment(AdapterCodeWriter writer, String publicationCreatorName) throws IOException;
    void writeBufferCreatorFragment(AdapterCodeWriter writer, String bufferCreatorName, List<AdaptableMethod> methods) throws IOException;
    void writeDeclareBufferFragment(AdapterCodeWriter writer, String buffersVarName, int size) throws IOException;
    void writeAcquireBufferFragment(AdapterCodeWriter writer, String buffersVarName, String endpointIx) throws IOException;
    void writeReleaseBufferFragment(AdapterCodeWriter writer, String buffersVarName, String endpointIx, String freedBufferName) throws IOException;
    void writeImportsFragment(AdapterCodeWriter writer) throws IOException;
}
