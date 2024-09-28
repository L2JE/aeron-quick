package org.jetc.aeron.quick.annotations.processors;

import org.jetc.aeron.quick.annotations.AeronQuickContract;
import org.jetc.aeron.quick.annotations.AeronQuickReceiver;
import org.jetc.aeron.quick.annotations.QuickContractEndpoint;
import org.jetc.aeron.quick.annotations.processors.aeron_adapter.AeronAdapterClassGenerator;
import org.jetc.aeron.quick.annotations.processors.utils.AdaptableMethod;
import org.jetc.aeron.quick.annotations.processors.utils.AnnotationResolver;
import org.jetc.aeron.quick.annotations.processors.utils.FirstAddedExecutableOnlyCollection;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.function.Consumer;

@SupportedAnnotationTypes({
    "org.jetc.aeron.quick.annotations.AeronQuickReceiver"
})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class AeronQuickReceiverProcessor extends AbstractProcessor {
    private static final Class<Annotation>[] CONTRACT_ANNOTATIONS = new Class[]{QuickContractEndpoint.class};

    private static final String RECEIVER_CONTRACT_FIELD = "contract";
    private final AnnotationResolver annotationResolver = new AnnotationResolver();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(AeronQuickReceiver.class);

        if(annotatedElements.isEmpty())
            return false;

        AeronAdapterClassGenerator creator = new AeronAdapterClassGenerator(processingEnv.getMessager(), processingEnv.getFiler());

        roundEnv.getRootElements().forEach(e -> {
            if(e.getAnnotation(AeronQuickReceiver.class) != null && e.getKind().isClass()) {
                TypeElement receiverClass = (TypeElement) e;
                FirstAddedExecutableOnlyCollection methodsToAdapt = new FirstAddedExecutableOnlyCollection();
                forEachContractMethod(receiverClass, methodsToAdapt::add);

                creator.adapt(receiverClass, methodsToAdapt.asList(AdaptableMethod::new));
            }
        });

        return true;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.annotationResolver.init(processingEnv);
    }

    /**
     * Allows consuming every method directly (in the receiverClass) or indirectly (in an interface) marked with a contract annotation (eg: {@link QuickContractEndpoint @QuickContractEndpoint} or
     * any annotation that marks a method that will be bound to an aeron stream)
     * @param targetClass a class marked with {@link @AeronQuickReceiver}
     * @param consumer that will consume each method that is part of the receiver contract
     */
    private void forEachContractMethod(TypeElement targetClass, Consumer<ExecutableElement> consumer) {
        boolean isGloballyMarked = targetClass.getAnnotation(AeronQuickContract.class) != null;
        targetClass.getEnclosedElements().forEach(enclosed -> {
            if(enclosed.getKind() == ElementKind.METHOD){
                boolean isLocallyMarked = isGloballyMarked;

                if(!isLocallyMarked) {
                    for (var contractAnnotation : CONTRACT_ANNOTATIONS) {
                        var isMarkedResult = enclosed.getAnnotationsByType(contractAnnotation);

                        if (isMarkedResult != null && isMarkedResult.length > 0) {
                            isLocallyMarked = true;
                            break;
                        }
                    }
                }

                if(isLocallyMarked)
                    consumer.accept((ExecutableElement) enclosed);
            }
        });
        forEachContractMethodInParents(targetClass, consumer);
    }

    /**
     * Looks for possible contracts in the {@link AeronQuickReceiver#contract() AeronQuickReceiver.contract} attribute or implemented interfaces by receiverClass and then apply {@link AeronQuickReceiverProcessor#forEachContractMethod(TypeElement, Consumer) forEachContractMethod}
     */
    private void forEachContractMethodInParents(TypeElement receiverClass, Consumer<ExecutableElement> consumer) {
        TypeElement[] contractCandidates = getContractCandidates(receiverClass);

        for(var candidate : contractCandidates){
            forEachContractMethod(candidate, consumer);
        }
    }

    /**
     * @return The value of the {@link AeronQuickReceiver#contract() AeronQuickReceiver.contract} attribute or all the interfaces that are implemented by the receiverClass any of which could contain the contract annotations
     */
    private TypeElement[] getContractCandidates(TypeElement receiverClass) {
        TypeElement receiverContract = this.annotationResolver.getAnnotationValue(receiverClass, AeronQuickReceiver.class, RECEIVER_CONTRACT_FIELD);
        if(receiverContract != null)
            return new TypeElement[]{ receiverContract };

        return receiverClass.getInterfaces().stream().map(mirror -> (TypeElement) processingEnv.getTypeUtils().asElement(mirror)).toArray(TypeElement[]::new);
    }
}
