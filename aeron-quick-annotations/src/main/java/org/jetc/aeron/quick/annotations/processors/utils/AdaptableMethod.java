package org.jetc.aeron.quick.annotations.processors.utils;

import org.jetc.aeron.quick.annotations.QuickContractEndpoint;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.LinkedList;
import java.util.List;

import static org.jetc.aeron.quick.annotations.processors.utils.InterfaceCompatibilityUtils.methodIsContextualFragmentHandler;
import static org.jetc.aeron.quick.annotations.processors.utils.InterfaceCompatibilityUtils.methodIsFragmentHandler;

public class AdaptableMethod {
    private final ExecutableElement method;
    private List<AdaptableParam> params;

    public AdaptableMethod(ExecutableElement method) {
        this.method = method;
    }

    public String getPropName() {
        QuickContractEndpoint markData = method.getAnnotation(QuickContractEndpoint.class);
        return (markData != null && !markData.name().isBlank()) ? markData.name() : method.getSimpleName().toString();
    }

    public void forEachParam(ThrowableBiConsumer<AdaptableParam, Integer> consumer) throws Exception {
        int ix = 0;

        if(params == null) {
            params = new LinkedList<>();
            for (var param : method.getParameters()) {
                var adaptable = new AdaptableParam(param, ix);
                params.add(adaptable);
                consumer.accept(adaptable, ix);
                ix++;
            }
        } else {
            for (var param : params) {
                consumer.accept(param, ix);
                ix++;
            }
        }
    }

    public String getSimpleName() {
        return method.getSimpleName().toString();
    }

    public int getParamsCount() {
        return this.method.getParameters().size();
    }

    public MethodKind getMethodKind(){
        MethodKind kind = MethodKind.COMMON;
        if(methodIsFragmentHandler(this.method))
            kind = MethodKind.FRAGMENT_HANDLER;

        if(methodIsContextualFragmentHandler(this.method))
            kind = MethodKind.CONTEXTUAL_HANDLER;

        return kind;
    }

    public static class AdaptableParam {
        private final VariableElement param;
        private final String paramId;
        public AdaptableParam(VariableElement param, int paramId) {
            this.param = param;
            this.paramId = "param" + paramId;
        }

        public VariableElement getElement(){
            return param;
        }

        public TypeMirror getDeclaredType(){
            return this.param.asType();
        }

        public String getLengthStr(){
            return switch (getDeclaredType().getKind()){
                case SHORT -> "BitUtil.SIZE_OF_SHORT";
                case INT -> "BitUtil.SIZE_OF_INT";
                case LONG ->"BitUtil.SIZE_OF_LONG";
                case CHAR -> "BitUtil.SIZE_OF_CHAR";
                case FLOAT -> "BitUtil.SIZE_OF_FLOAT";
                case DOUBLE -> "BitUtil.SIZE_OF_DOUBLE";
                case DECLARED -> paramId + ".length() + BitUtil.SIZE_OF_INT"; //includes the prefixed length in the buffer
                default -> null; //TODO: ADD CASES FOR STRING OR OTHER TYPES
            };
        }

        public String getParamId() {
            return paramId;
        }
    }

    public enum MethodKind {
        FRAGMENT_HANDLER,
        CONTEXTUAL_HANDLER,
        COMMON
    }
}
