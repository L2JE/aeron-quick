package org.jetc.aeron.quick.peers.sender;

import io.aeron.Aeron;

public class AeronQuickSenderBuilder<T> {
    private final SenderAdapterBase<T> adaptedContract;
    private String clientName;
    private Aeron aeron;

    public AeronQuickSenderBuilder(SenderAdapterBase<T> adaptedContract) {
        this.adaptedContract = adaptedContract;
    }

    public T build(){
        adaptedContract.initSender(aeron, clientName);
        return adaptedContract.getClient();
    }

    public AeronQuickSenderBuilder<T> setSenderName(String clientName) {
        this.clientName = clientName;
        return this;
    }

    public AeronQuickSenderBuilder<T> setAeron(Aeron aeron) {
        this.aeron = aeron;
        return this;
    }
}
