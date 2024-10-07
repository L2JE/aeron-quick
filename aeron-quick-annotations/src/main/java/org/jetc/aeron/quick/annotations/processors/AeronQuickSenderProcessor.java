package org.jetc.aeron.quick.annotations.processors;

import org.jetc.aeron.quick.annotations.AeronQuickSender;
import org.jetc.aeron.quick.annotations.processors.utils.AdaptableMethod;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.List;

@SupportedAnnotationTypes({
    "org.jetc.aeron.quick.annotations.AeronQuickSender"
})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class AeronQuickSenderProcessor extends AeronQuickContractProcessor {
    /**
     * @return null if the element is can't be processed or the target class to be processed
     */
    @Override
    protected TypeElement getMessagingRoleElement(Element e){
        TypeMirror mirror = null;
        if(e.getKind().equals(ElementKind.METHOD) && e instanceof ExecutableElement method)
            mirror = method.getReturnType();

        if(e.getKind().equals(ElementKind.FIELD))
            mirror = e.asType();

        return mirror != null ? (TypeElement) processingEnv.getTypeUtils().asElement(mirror) : null;
    }

    /**
     * @return returns the contract annotation that is being processed
     */
    @Override
    protected  Class<? extends Annotation> getMessagingRoleAnnotation(){
        return AeronQuickSender.class;
    }

    @Override
    protected void processContractMethods(Element annotatedEl, TypeElement classToAdapt, List<AdaptableMethod> methodsToAdapt) {
        creator.adaptSender(annotatedEl, classToAdapt, methodsToAdapt);
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "INITIALIZING Processor SENDER");
    }
}
