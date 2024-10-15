package org.jetc.aeron.quick.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Shorthand for marking all methods as {@link AeronQuickContractEndpoint @QuickContractEndpoint}
 **/
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AeronQuickContract {
}