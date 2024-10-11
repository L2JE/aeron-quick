package org.jetc.aeron.quick.peers;

import org.jetc.aeron.quick.AeronQuickContext;

public interface PeerConfiguration {
    String getComponentName();
    void setComponentName(String componentName);
    void setContext(AeronQuickContext context);
    AeronQuickContext getContext();
}
