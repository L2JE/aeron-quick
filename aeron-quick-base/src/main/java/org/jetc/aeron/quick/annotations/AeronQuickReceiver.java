package org.jetc.aeron.quick.annotations;

import javax.lang.model.type.NullType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the class that have the implementation of methods that can be bound with Aeron streams.
 * If a class is marked with this annotation it must have methods marked with {@link org.jetc.aeron.quick.annotations.UnicastMessage} or
 * {@link org.jetc.aeron.quick.annotations.MulticastMessage} (directly in the class or indirectly from an interface).
 **/
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AeronQuickReceiver {
    /**
     * receiver name where to look for properties
     */
    String name();

    /**
     * If not defined it is assumed that the class is a direct implementor of an {@link AeronQuickContract}
     * @return
     */
    Class<?> contract() default NullType.class;
}