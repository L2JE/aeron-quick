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
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public abstract class AeronQuickContractProcessor extends AbstractProcessor {
    private final AnnotationResolver annotationResolver = new AnnotationResolver();
    protected AeronAdapterClassGenerator creator;
    private static final Class<Annotation>[] CONTRACT_ANNOTATIONS = new Class[]{QuickContractEndpoint.class};
    private static final String ANNOTATION_CONTRACT_FIELD = "contract";
    private final Set<String> processedContracts = new HashSet<>();

    /**
     * @param e an element annotated with {@link AeronQuickContractProcessor#getMessagingRoleAnnotation()}
     * @return null if the element is can't be processed or the target class to be processed
     */
    protected abstract TypeElement getMessagingRoleElement(Element e);

    /**
     * @return returns the contract annotation that is being processed
     */
    protected abstract Class<? extends Annotation> getMessagingRoleAnnotation();

    protected abstract void processContractMethods(Element annotatedEl, TypeElement classToAdapt, List<AdaptableMethod> methodsToAdapt);

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(getMessagingRoleAnnotation());

        if(annotatedElements.isEmpty())
            return false;

        annotatedElements.forEach(e -> {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Checking!!! " + e.asType().toString());
            TypeElement contractElement = getMessagingRoleElement(e);
            if(contractElement != null) {
                String contractName = contractElement.toString();
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Processing!!! " + contractName);
                if(!processedContracts.contains(contractName)){
                    FirstAddedExecutableOnlyCollection methodsToAdapt = new FirstAddedExecutableOnlyCollection();
                    forEachContractMethod(contractElement, methodsToAdapt::add);

                    processContractMethods(e, contractElement, methodsToAdapt.asList(AdaptableMethod::new));
                    processedContracts.add(contractName);
                }
            }
        });

        return true;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.annotationResolver.init(processingEnv);
        this.creator = new AeronAdapterClassGenerator(processingEnv.getMessager(), processingEnv.getFiler());
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
     * Looks for possible contracts in the {@link AeronQuickReceiver#contract() AeronQuickReceiver.contract} attribute or implemented interfaces by receiverClass and then apply {@link AeronQuickContractProcessor#forEachContractMethod(TypeElement, Consumer) forEachContractMethod}
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
        TypeElement receiverContract = this.annotationResolver.getAnnotationValue(receiverClass, getMessagingRoleAnnotation(), ANNOTATION_CONTRACT_FIELD);
        if(receiverContract != null)
            return new TypeElement[]{ receiverContract };

        return receiverClass.getInterfaces().stream().map(mirror -> (TypeElement) processingEnv.getTypeUtils().asElement(mirror)).toArray(TypeElement[]::new);
    }
}
