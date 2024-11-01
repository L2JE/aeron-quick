package org.jetc.aeron.quick.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The method marked with this annotation will be bound to an Aeron internal buffer to receive or send content through it.
 * <p>
 * It will be interpreted as receiver or sender depending on if it is found in a class marked with {@link AeronQuickReceiver @AeronQuickServer} or
 * a method marked with {@link AeronQuickSender @AeronQuickSender}
 * <p>
 * IMPORTANT!!!
 * <p>
 * 1_ All the non-primitive parameters will be de/serialized from/to the channels using <a href="https://github.com/FasterXML/jackson-core">default Jackson object mapper</a> (meaning they have to be compatible)
 * <p>
 * 2_ If there's more than one marked method with the same name you MUST define a name in the annotation to bind them correctly and refer to them when configuring through system properties.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AeronQuickContractEndpoint {
    /**
     * The unique name to identify this endpoint in a receiver/emitter and the on will be used to configure the system property
     * {@code @QuickContractEndpoint(name = "name_example")}
     * <p>
     * will be later available for configuring channel through this property:
     * {@code aeron.quick.receiverExample.nameExample.channel}
     */
    String name() default "";
}