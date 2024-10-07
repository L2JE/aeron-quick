package org.jetc.aeron.quick.sender;

public class AeronQuickSenderBuilder<T> {
    private final SenderAdapterBase<T> adaptedContract;
    public AeronQuickSenderBuilder(SenderAdapterBase<T> adaptedContract) {
        this.adaptedContract = adaptedContract;
    }

    public T build(){
        return this.adaptedContract.getClient();
    }
}
