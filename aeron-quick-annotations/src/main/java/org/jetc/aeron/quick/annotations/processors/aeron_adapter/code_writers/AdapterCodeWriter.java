package org.jetc.aeron.quick.annotations.processors.aeron_adapter.code_writers;

import org.jetc.aeron.quick.annotations.processors.aeron_adapter.AdapterConfiguration;
import org.jetc.aeron.quick.annotations.processors.utils.AdaptingError;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

public abstract class AdapterCodeWriter extends BufferedWriter {
    protected final AdapterConfiguration config;
    private int indent = 0;

    protected AdapterCodeWriter(JavaFileObject sourceFile, AdapterConfiguration config) throws IOException {
        super(sourceFile.openWriter());
        this.config = config;
    }

    public void startBlock() throws IOException {
        indent++;
        newLine();
    }

    public void endBlock() throws IOException {
        indent--;
        newLine();
    }

    public Writer iAppendLine(CharSequence csq) throws IOException {
        iAppend(csq);
        newLine();
        return this;
    }

    /**
     * Appends indentation spaces and then the provided character sequence
     * @param  csq
     *         The character sequence to append.  If {@code csq} is
     *         {@code null}, then the four characters {@code "null"} are
     *         appended to this writer.
     *
     * @return  This writer
     *
     * @throws  IOException
     *          If an I/O error occurs
     */
    public Writer iAppend(CharSequence csq) throws IOException {
        ind();
        return append(csq);
    }

    protected void ind() throws IOException {
        for(int i = 0; i < indent; i++)
            append("    ");
    }

    public abstract void generateAdapterCode() throws IOException, AdaptingError;
}
