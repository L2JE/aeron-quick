package org.jetc.aeron.quick.peers;

import org.jetc.aeron.quick.AeronQuickContext;

public interface ContextDependantComponent {
    void setContext(AeronQuickContext context, String componentName);
}
