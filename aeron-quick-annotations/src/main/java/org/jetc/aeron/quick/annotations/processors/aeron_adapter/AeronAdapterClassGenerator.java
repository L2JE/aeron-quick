package org.jetc.aeron.quick.annotations.processors.aeron_adapter;

import org.jetc.aeron.quick.annotations.AeronQuickSender;
import org.jetc.aeron.quick.annotations.processors.AnnotationLogger;
import org.jetc.aeron.quick.annotations.processors.aeron_adapter.code_writers.AdapterCodeWriter;
import org.jetc.aeron.quick.annotations.processors.aeron_adapter.code_writers.ChannelStreamPerMethodReceiverAdapterWriter;
import org.jetc.aeron.quick.annotations.processors.aeron_adapter.code_writers.ChannelStreamPerMethodSenderAdapterWriter;
import org.jetc.aeron.quick.annotations.processors.aeron_adapter.code_writers.fragments.sender.MultiThreadFragmentWriter;
import org.jetc.aeron.quick.annotations.processors.aeron_adapter.code_writers.fragments.sender.SenderFragmentWriter;
import org.jetc.aeron.quick.annotations.processors.aeron_adapter.code_writers.fragments.sender.SingleThreadFragmentWriter;
import org.jetc.aeron.quick.annotations.processors.utils.AdaptableMethod;
import org.jetc.aeron.quick.annotations.processors.utils.AdaptingError;
import org.jetc.aeron.quick.annotations.processors.utils.ThrowingBiFunction;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.List;

public class AeronAdapterClassGenerator {
    private final AnnotationLogger log;
    private final Filer filer;

    public AeronAdapterClassGenerator(Messager messager, Filer filer) {
        this.filer = filer;
        this.log = new AnnotationLogger(messager);
    }

    public void adaptSender(TypeElement classToAdapt, List<AdaptableMethod> methodsToAdapt, AeronQuickSender config) {
        final SenderFragmentWriter fragmentWriter = config.concurrent() ?
            new MultiThreadFragmentWriter("ctx", "senderName") :
            new SingleThreadFragmentWriter("ctx", "senderName");

        adapt(classToAdapt, methodsToAdapt, AdapterConfiguration.SENDER_SUFFIX, (source, cfg) -> new ChannelStreamPerMethodSenderAdapterWriter(source, cfg, fragmentWriter));
    }

    public void adaptReceiver(TypeElement classToAdapt, List<AdaptableMethod> methodsToAdapt) {
        adapt(classToAdapt, methodsToAdapt, AdapterConfiguration.RECEIVER_SUFFIX, ChannelStreamPerMethodReceiverAdapterWriter::new);
    }

    /**
     * Generates an Adapter class for the provided classToAdapt to bind the methods in methodsToAdapt to an Aeron FragmentHandler so the user don't need to deal with them.
     * @param classToAdapt each method in methodsToAdapt will be called through an instance of this class
     * @param methodsToAdapt the list of methods marked with an AeronQuickContract annotation () that are inherited be classToAdapt and must be bound to an Aeron Fragment Handler.
     */
    private void adapt(TypeElement classToAdapt, List<AdaptableMethod> methodsToAdapt, String adapterSuffix, ThrowingBiFunction<JavaFileObject, AdapterConfiguration, AdapterCodeWriter>  codeGenerator) {
        String classToAdaptName = classToAdapt.getSimpleName().toString();
        if(methodsToAdapt == null || methodsToAdapt.isEmpty()) {
            String msg = "There are no methods to bind in the receiver class: " + classToAdapt.getQualifiedName() + ". Please remove the annotation or mark at least one method as AeronQuickContractEndpoint";
            log.warn(msg);
            throw new RuntimeException(new AdaptingError(msg));
        }

        String finalAdapterName = classToAdaptName + adapterSuffix;
        PackageElement elementPackage = (PackageElement) classToAdapt.getEnclosingElement();
        AdapterConfiguration config = new AdapterConfiguration(classToAdapt, finalAdapterName, classToAdaptName, methodsToAdapt, null);

        try (AdapterCodeWriter gen = codeGenerator.apply(filer.createSourceFile(elementPackage.getQualifiedName() + "." + finalAdapterName), config)) {
            gen.generateAdapterCode();
        } catch (IOException | AdaptingError ex) {
            String msg = "There was an error while creating an adapter for " + classToAdapt.getQualifiedName() + ": " + ex.getLocalizedMessage();
            log.warnMand(msg);
            throw new RuntimeException(msg);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
