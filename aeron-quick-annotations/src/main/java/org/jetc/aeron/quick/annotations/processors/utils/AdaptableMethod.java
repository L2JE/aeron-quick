package org.jetc.aeron.quick.annotations.processors.utils;

import org.jetc.aeron.quick.annotations.QuickContractEndpoint;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.LinkedList;
import java.util.List;
import static org.jetc.aeron.quick.annotations.processors.utils.InterfaceCompatibilityUtils.methodIsContextualFragmentHandler;
import static org.jetc.aeron.quick.annotations.processors.utils.InterfaceCompatibilityUtils.methodIsFragmentHandler;
import static org.jetc.aeron.quick.annotations.processors.utils.FullyQualifiedClassNames.STRING_TYPE;

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

    /**
     * @return true if all parameters are primitive types
     */
    public boolean isPrimitive() {
        for(VariableElement param : method.getParameters())
            if(!param.asType().getKind().isPrimitive())
                return false;
        return true;
    }

    public TypeKind getReturnType(){
        return method.getReturnType().getKind();
    }

    public CharSequence getSignature() {
        StringBuilder b = new StringBuilder();
        b.append(method.getReturnType()).append(" ").append(method.getSimpleName()).append("(");

        long lastMethodIx = method.getParameters().size() - 1;
        for (var param : method.getParameters()) {
            b.append(param.asType()).append(" ").append(param.getSimpleName());

            if(lastMethodIx-- > 0)
                b.append(", ");

        }
        return b.append(")");
    }

    public static class AdaptableParam {
        private final VariableElement param;
        private String paramId;
        private final boolean isNoStringObject;

        public AdaptableParam(VariableElement param, int paramId) {
            this.param = param;
            this.paramId = "param" + paramId;
            isNoStringObject = !this.param.asType().getKind().isPrimitive() && !this.param.asType().toString().equals(STRING_TYPE);
        }

        public VariableElement getElement(){
            return param;
        }

        public TypeMirror getDeclaredType(){
            return this.param.asType();
        }

        public boolean isNoStringObject(){
            return isNoStringObject;
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

        public void setParamId(String newParamId) {
            paramId = newParamId;
        }

        /**
         * @return the name of the parameter in the method signature
         */
        public String getParamName() {
            return param.getSimpleName().toString();
        }

        public boolean isPrimitive() {
            return param.asType().getKind().isPrimitive();
        }
    }

    public enum MethodKind {
        FRAGMENT_HANDLER,
        CONTEXTUAL_HANDLER,
        COMMON
    }
}
