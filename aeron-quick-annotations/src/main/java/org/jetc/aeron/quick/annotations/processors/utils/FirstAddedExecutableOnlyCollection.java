package org.jetc.aeron.quick.annotations.processors.utils;

import javax.lang.model.element.ExecutableElement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class FirstAddedExecutableOnlyCollection {
    private final Map<String, ExecutableElement> els = new HashMap<>();
    private final List<ExecutableElement> elsList = new LinkedList<>();

    public void add(ExecutableElement el){
        String elName = el.getSimpleName().toString();
        String elParams = serializeParams(el);
        String key = elName + elParams;

        if(!els.containsKey(key)) {
            els.put(key, el);
            elsList.addLast(el);
        }
    }

    public <A> List<A> asList(Function<ExecutableElement, A> converter){
        List<A> convertedEls = new LinkedList<>();
        for (var m : elsList)
            convertedEls.add(converter.apply(m));
        return convertedEls;
    }

    private String serializeParams(ExecutableElement el) {
        var params = el.getParameters();
        String[] strParams = new String[params.size()];
        int i = 0;
        for(var param : params){
            strParams[i++] = param.asType().toString() + param.getSimpleName().toString();
        }
        return String.join(",", strParams);
    }
}