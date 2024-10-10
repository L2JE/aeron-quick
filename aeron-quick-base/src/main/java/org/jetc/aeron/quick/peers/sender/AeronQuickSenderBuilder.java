package org.jetc.aeron.quick.peers.sender;

import org.jetc.aeron.quick.AeronQuickContext;

public class AeronQuickSenderBuilder<T> {
    private final SenderAdapterBase<T> adaptedContract;
    private String clientName;
    private final AeronQuickContext context;

    public AeronQuickSenderBuilder(SenderAdapterBase<T> adaptedContract, AeronQuickContext context) {
        this.adaptedContract = adaptedContract;
        this.context = context;
    }

    public T build(){
        adaptedContract.setContext(context, clientName);
        return adaptedContract.getClient();
    }

    public AeronQuickSenderBuilder<T> setSenderName(String clientName) {
        this.clientName = clientName;
        return this;
    }
}
