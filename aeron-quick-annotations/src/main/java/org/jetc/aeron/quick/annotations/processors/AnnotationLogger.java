package org.jetc.aeron.quick.annotations.processors;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;

public record AnnotationLogger(Messager messager){
    public void error(String msg){
        messager.printMessage(Diagnostic.Kind.ERROR, msg);
    }
    public void warn(String msg){
        messager.printMessage(Diagnostic.Kind.WARNING, msg);
    }
    public void warnMand(String msg){
        messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, msg);
    }
    public void note(String msg){
        messager.printMessage(Diagnostic.Kind.NOTE, msg);
    }
    public void other(String msg){
        messager.printMessage(Diagnostic.Kind.OTHER, msg);
    }
}
