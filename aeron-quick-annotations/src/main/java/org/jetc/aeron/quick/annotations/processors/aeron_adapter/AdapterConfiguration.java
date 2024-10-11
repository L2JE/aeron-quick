package org.jetc.aeron.quick.annotations.processors.aeron_adapter;

import org.jetc.aeron.quick.annotations.processors.utils.AdaptableMethod;
import javax.lang.model.element.TypeElement;
import java.util.List;

public class AdapterConfiguration {
    public static final String SENDER_SUFFIX = "_SAdapter";
    public static final String RECEIVER_SUFFIX = "_RAdapter";

    private final TypeElement classToAdapt;
    private final String finalAdapterName;
    private final String classToAdaptName;
    private final List<AdaptableMethod> methodsToAdapt;

    public AdapterConfiguration(TypeElement classToAdapt, String finalAdapterName, String classToAdaptName, List<AdaptableMethod> methodsToAdapt, String adapterCfgName) {
        this.classToAdapt = classToAdapt;
        this.finalAdapterName = finalAdapterName;
        this.classToAdaptName = classToAdaptName;
        this.methodsToAdapt = methodsToAdapt;
    }

    public String finalAdapterName() {
        return finalAdapterName;
    }

    public String classToAdaptName() {
        return classToAdaptName;
    }

    public TypeElement classToAdapt() {
        return classToAdapt;
    }

    public String bufferForResponsesCreationStr() {
        return "new ExpandableDirectByteBuffer(256);";
    }

    public List<AdaptableMethod> methodsToAdapt() {
        return methodsToAdapt;
    }
}
