package org.jetc.aeron.quick.annotations.processors.aeron_adapter.code_writers;

import org.jetc.aeron.quick.annotations.processors.utils.AdaptingError;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import java.io.IOException;

public class AeronFragmentHandlerWriter {
    private static final String DEFAULT_BYTE_ORDER = "ByteOrder.LITTLE_ENDIAN";

    public static AdapterCodeWriter appendParamValueFromBufferStr(AdapterCodeWriter writer, String bufferName, String offsetStr, VariableElement param) throws IOException, AdaptingError {
        TypeKind kind = param.asType().getKind();
        writer.append(bufferName).append(".");

        switch (kind) {
            case SHORT -> writer.append("getShort(");
            case INT -> writer.append("getInt(");
            case LONG -> writer.append("getLong(");
            case CHAR -> writer.append("getChar(");
            case FLOAT -> writer.append("getFloat(");
            case DOUBLE -> writer.append("getDouble(");
            case DECLARED -> writer.append("getStringUtf8(");
            default -> throw new AdaptingError("Unsupported/Not-Resolved parameter type on method "+ param.getEnclosingElement().getSimpleName() + ": "+ param.asType() + " " + param.getSimpleName() + ". Should be an imported class, interface or primitive type");
        }
        writer.append(offsetStr).append(", " + DEFAULT_BYTE_ORDER + ")");
        return writer;
    }
}
