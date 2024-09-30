package org.jetc.aeron.quick.annotations.processors.utils;

import javax.lang.model.element.ExecutableElement;

public class InterfaceCompatibilityUtils {
    private static final String[] fragmentHandlerParams = new String[]{"org.agrona.DirectBuffer", "int", "int", "io.aeron.logbuffer.Header"};
    public static boolean methodIsFragmentHandler(ExecutableElement method){
        var params = method.getParameters();

        if(params.size() != fragmentHandlerParams.length)
            return false;

        int i = 0;
        for (var param : params){
            if(!param.asType().toString().equals(fragmentHandlerParams[i++]))
                return false;
        }
        return true;
    }
    public static boolean methodIsContextualFragmentHandler(ExecutableElement method){
        var params = method.getParameters();
        return method.getReturnType().toString().equals("io.aeron.logbuffer.FragmentHandler") &&
                params != null && params.size() == 1 && params.getFirst().asType().toString().equals("io.aeron.Aeron");
    }
}
