package org.jetc.aeron.quick.annotations.processors.aeron_adapter;

import org.jetc.aeron.quick.annotations.processors.AnnotationLogger;
import org.jetc.aeron.quick.annotations.processors.aeron_adapter.code_writers.AdapterCodeWriter;
import org.jetc.aeron.quick.annotations.processors.aeron_adapter.code_writers.ChannelStreamPerMethodAdapterWriter;
import org.jetc.aeron.quick.annotations.processors.utils.AdaptableMethod;
import org.jetc.aeron.quick.annotations.processors.utils.AdaptingError;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.List;

public class AeronAdapterClassGenerator {
    private final AnnotationLogger log;
    private final Filer filer;

    public AeronAdapterClassGenerator(Messager messager, Filer filer) {
        this.filer = filer;
        this.log = new AnnotationLogger(messager);
    }

    /**
     * Generates an Adapter class for the provided classToAdapt to bind the methods in methodsToAdapt to an Aeron FragmentHandler so the user don't need to deal with them.
     * @param classToAdapt each method in methodsToAdapt will be called through an instance of this class
     * @param methodsToAdapt the list of methods marked with an AeronQuickContract annotation () that are inherited be classToAdapt and must be bound to an Aeron Fragment Handler.
     */
    public void adapt(TypeElement classToAdapt, List<AdaptableMethod> methodsToAdapt) {
        String classToAdaptName = classToAdapt.getSimpleName().toString();
        if(methodsToAdapt == null || methodsToAdapt.isEmpty()) {
            String msg = "There are no methods to bind in the receiver class: " + classToAdapt.getQualifiedName() + ". Please remove the annotation or mark at least one method as QuickContractEndpoint";
            log.warn(msg);
            throw new RuntimeException(new AdaptingError(msg));
        }

        String finalAdapterName = classToAdaptName + "_Adapter";
        PackageElement elementPackage = (PackageElement) classToAdapt.getEnclosingElement();
        AdapterConfiguration config = new AdapterConfiguration(classToAdapt, finalAdapterName, classToAdaptName, methodsToAdapt);

        try (AdapterCodeWriter gen = new ChannelStreamPerMethodAdapterWriter(filer.createSourceFile(elementPackage.getQualifiedName() + "." + finalAdapterName), config)) {
            gen.generateAdapterCode();
        } catch (IOException | AdaptingError ex) {
            String msg = "The was an error while creating an adapter for " + classToAdapt.getQualifiedName() + ": " + ex.getLocalizedMessage();
            log.warnMand(msg);
            throw new RuntimeException(msg);
        }
    }
}
