package org.jetc.aeron.quick.annotations.processors.aeron_adapter.code_writers.fragments.sender;

import org.jetc.aeron.quick.annotations.processors.aeron_adapter.code_writers.AdapterCodeWriter;
import org.jetc.aeron.quick.annotations.processors.utils.AdaptableMethod;
import java.io.IOException;
import java.util.List;

public class SingleThreadFragmentWriter implements SenderFragmentWriter {
    private final String aeronCtxVarName;
    private final String senderNameVarName;

    public SingleThreadFragmentWriter(String aeronCtxVarName, String senderNameVarName) {
        this.aeronCtxVarName = aeronCtxVarName;
        this.senderNameVarName = senderNameVarName;
    }

    @Override
    public void writePublicationCreatorFragment(AdapterCodeWriter writer, String publicationCreatorName) throws IOException {
        writer.iAppend("AbstractPublicationCreator ").append(publicationCreatorName).append(" = new ExclusivePublicationCreator(").append(aeronCtxVarName).append(", ").append(senderNameVarName).append(");");
    }

    @Override
    public void writeBufferCreatorFragment(AdapterCodeWriter writer, String bufferCreatorName, List<AdaptableMethod> methods) throws IOException {
        writer.iAppend("SimpleBufferHolderCreator ").append(bufferCreatorName).append(" = new SimpleBufferHolderCreator(");
        writer.startBlock();
        writer.iAppend(aeronCtxVarName).append(",");
        writer.newLine();
        writer.iAppend(senderNameVarName).append(",");
        writer.newLine();

        int lastIx = methods.size() - 1;
        int i = 0;
        for(AdaptableMethod method : methods){
            writer.iAppend("() -> ").append(method.getEquivalentParameterBufferStr());

            if(i++ < lastIx){
                writer.append(",");
                writer.newLine();
            }
        }

        writer.endBlock();
        writer.iAppend(");");
    }

    @Override
    public void writeDeclareBufferFragment(AdapterCodeWriter writer, String buffersVarName, int size) throws IOException {
        writer.append("MutableDirectBuffer[] buffers = new MutableDirectBuffer[").append(String.valueOf(size)).append("];");
    }


    @Override
    public void writeAcquireBufferFragment(AdapterCodeWriter writer, String buffersVarName, String endpointIx) throws IOException {
        writer.append(buffersVarName).append("[").append(endpointIx).append("];");
    }

    @Override
    public void writeReleaseBufferFragment(AdapterCodeWriter writer, String buffersVarName, String endpointIx, String freedBufferName) throws IOException {
        writer.append("null");
    }

    @Override
    public void writeImportsFragment(AdapterCodeWriter writer) throws IOException {
        writer.iAppendLine("import org.jetc.aeron.quick.utils.publication.AbstractPublicationCreator;");
        writer.iAppendLine("import org.jetc.aeron.quick.utils.publication.ExclusivePublicationCreator;");
        writer.iAppendLine("import org.jetc.aeron.quick.utils.publication.SimpleBufferHolderCreator;");
    }

}
