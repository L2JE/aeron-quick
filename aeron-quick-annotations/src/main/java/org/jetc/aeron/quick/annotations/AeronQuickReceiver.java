package org.jetc.aeron.quick.annotations;

import javax.lang.model.type.NullType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the class that have the implementation of methods that can be bound with Aeron streams.
 * If a class is marked with this annotation it must have methods marked with {@link org.jetc.aeron.quick.annotations.AeronQuickContractEndpoint} or
 * inherit from a class having the method annotated with it.
 **/
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AeronQuickReceiver {
    /**
     * If not defined it is assumed that the class is a direct implementor of an interface/class that has methods annotated with {@link AeronQuickContractEndpoint}
     */
    Class<?> contract() default NullType.class;

    boolean concurrent() default false;
}