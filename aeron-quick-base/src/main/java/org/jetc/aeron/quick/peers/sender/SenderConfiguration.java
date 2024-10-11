package org.jetc.aeron.quick.peers.sender;

import org.jetc.aeron.quick.AeronQuickContext;
import org.jetc.aeron.quick.peers.PeerConfiguration;

public class SenderConfiguration implements PeerConfiguration {
    private AeronQuickContext context;
    private String componentName;

    @Override
    public AeronQuickContext getContext() {
        return context;
    }

    @Override
    public String getComponentName() {
        return componentName;
    }

    @Override
    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    @Override
    public void setContext(AeronQuickContext context) {
        this.context = context;
    }
}
