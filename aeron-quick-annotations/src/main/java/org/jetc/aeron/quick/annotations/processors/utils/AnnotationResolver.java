package org.jetc.aeron.quick.annotations.processors.utils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Map;

public class AnnotationResolver {
    private ProcessingEnvironment processingEnv;

    public void init(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    private static AnnotationMirror getAnnotationMirror(TypeElement typeElement, Class<?> clazz) {
        String clazzName = clazz.getName();
        for(AnnotationMirror m : typeElement.getAnnotationMirrors()) {
            if(m.getAnnotationType().toString().equals(clazzName)) {
                return m;
            }
        }
        return null;
    }

    private static AnnotationValue getAnnotationValue(AnnotationMirror annotationMirror, String key) {
        for(Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet() ) {
            if(entry.getKey().getSimpleName().toString().equals(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * @param valueKey must be something that can be converted to TypeElement
     * @return only type element values
     */
    public TypeElement getAnnotationValue(TypeElement typeElement, Class<?> clazz, String valueKey){
        AnnotationMirror am = getAnnotationMirror(typeElement, clazz);
        if(am == null) {
            return null;
        }
        AnnotationValue av = getAnnotationValue(am, valueKey);
        if(av == null) {
            return null;
        } else {
            return (TypeElement) this.processingEnv.getTypeUtils().asElement((TypeMirror)av.getValue());
        }
    }
}
