package org.jetc.aeron.quick.annotations.processors.aeron_adapter.code_writers;

import org.jetc.aeron.quick.annotations.processors.utils.AdaptingError;
import javax.lang.model.element.VariableElement;
import java.io.IOException;

public class AeronDirectBufferOperationsWriter {
    private static final String DEFAULT_BYTE_ORDER = "ByteOrder.LITTLE_ENDIAN";

    public static AdapterCodeWriter appendParamValueFromBufferStr(AdapterCodeWriter writer, String bufferName, String offsetStr, VariableElement param) throws IOException, AdaptingError {
        writer.append(bufferName).append(".get");
        writeTypeFragmentToBuffer(writer, param);
        writer.append(offsetStr).append(", " + DEFAULT_BYTE_ORDER + ")");
        return writer;
    }

    public static AdapterCodeWriter appendPutValueOnBufferStr(AdapterCodeWriter writer, String bufferName, String offsetStr, VariableElement param, String value) throws IOException, AdaptingError {
        writer.append(bufferName).append(".put");
        writeTypeFragmentToBuffer(writer, param);
        writer.append(offsetStr).append(", ").append(value).append(", " +DEFAULT_BYTE_ORDER + ")");
        return writer;
    }

    private static void writeTypeFragmentToBuffer(AdapterCodeWriter writer, VariableElement param) throws IOException, AdaptingError {
        switch (param.asType().getKind()) {
            case SHORT -> writer.append("Short(");
            case INT -> writer.append("Int(");
            case LONG -> writer.append("Long(");
            case CHAR -> writer.append("Char(");
            case FLOAT -> writer.append("Float(");
            case DOUBLE -> writer.append("Double(");
            case DECLARED -> writer.append("StringUtf8(");
            default -> throw new AdaptingError("Unsupported/Not-Resolved parameter type on method "+ param.getEnclosingElement().getSimpleName() + ": "+ param.asType() + " " + param.getSimpleName() + ". Should be an imported class, interface or primitive type");
        }
    }
}
