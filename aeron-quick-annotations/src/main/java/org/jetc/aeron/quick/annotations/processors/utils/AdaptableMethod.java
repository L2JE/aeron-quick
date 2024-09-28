package org.jetc.aeron.quick.annotations.processors.utils;

import org.jetc.aeron.quick.annotations.QuickContractEndpoint;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;

public class AdaptableMethod {
    private final ExecutableElement method;

    public AdaptableMethod(ExecutableElement method) {
        this.method = method;
    }

    public String getPropName() {
        QuickContractEndpoint markData = method.getAnnotation(QuickContractEndpoint.class);
        return (markData != null && !markData.name().isBlank()) ? markData.name() : method.getSimpleName().toString();
    }

    public void forEachParam(ThrowableBiConsumer<AdaptableParam, Integer> consumer) throws Exception {
        int ix = 0;
        for(var param : method.getParameters())
            consumer.accept(new AdaptableParam(param), ix++);
    }

    public String getSimpleName() {
        return method.getSimpleName().toString();
    }

    public int getParamsCount() {
        return this.method.getParameters().size();
    }

    public static class AdaptableParam {
        private final VariableElement param;
        public AdaptableParam(VariableElement param) {
            this.param = param;
        }

        public VariableElement getElement(){
            return param;
        }

        public TypeKind getType(){
            return this.param.asType().getKind();
        }

        public String getLengthStr(){
            return switch (param.asType().getKind()){
                case SHORT -> "BitUtil.SIZE_OF_SHORT";
                case INT -> "BitUtil.SIZE_OF_INT";
                case LONG ->"BitUtil.SIZE_OF_LONG";
                case CHAR -> "BitUtil.SIZE_OF_CHAR";
                case FLOAT -> "BitUtil.SIZE_OF_FLOAT";
                case DOUBLE -> "BitUtil.SIZE_OF_DOUBLE";
                default -> null; //TODO: ADD CASES FOR STRING OR OTHER TYPES
            };
        }
    }
}
