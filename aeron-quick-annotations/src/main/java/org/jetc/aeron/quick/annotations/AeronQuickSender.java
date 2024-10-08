package org.jetc.aeron.quick.annotations;

import javax.lang.model.type.NullType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the class that have the implementation of methods that can be bound with Aeron streams.
 * <p>
 * If a class is marked with this annotation it must have methods marked with {@link org.jetc.aeron.quick.annotations.QuickContractEndpoint} (directly in the class or indirectly from an interface).
 **/
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface AeronQuickSender {
    /**
     * If not defined it will be taken from the type of the method return value, local variable or class attribute
     */
    Class<?> contract() default NullType.class;
}