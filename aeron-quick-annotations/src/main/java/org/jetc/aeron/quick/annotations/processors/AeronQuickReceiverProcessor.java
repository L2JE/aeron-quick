package org.jetc.aeron.quick.annotations.processors;

import org.jetc.aeron.quick.annotations.AeronQuickReceiver;
import org.jetc.aeron.quick.annotations.processors.utils.AdaptableMethod;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.List;

@SupportedAnnotationTypes({
    "org.jetc.aeron.quick.annotations.AeronQuickReceiver"
})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class AeronQuickReceiverProcessor extends AeronQuickContractProcessor {

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "INITIALIZING Processor Receiver");
    }

    /**
     * @param e an element annotated with {@link AeronQuickContractProcessor#getMessagingRoleAnnotation()}
     * @return null if the element is can't be processed or the target class to be processed
     */
    @Override
    protected TypeElement getMessagingRoleElement(Element e){
        return e.getKind().isClass() ? (TypeElement) e : null;
    }

    /**
     * @return returns the contract annotation that is being processed
     */
    @Override
    protected  Class<? extends Annotation> getMessagingRoleAnnotation(){
        return AeronQuickReceiver.class;
    }

    @Override
    protected void processContractMethods(TypeElement classToAdapt, List<AdaptableMethod> methodsToAdapt) {
        creator.adaptReceiver(classToAdapt, methodsToAdapt);
    }
}
