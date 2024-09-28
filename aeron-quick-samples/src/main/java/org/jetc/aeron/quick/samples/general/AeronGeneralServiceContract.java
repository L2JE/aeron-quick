package org.jetc.aeron.quick.samples.general;

import org.jetc.aeron.quick.annotations.QuickContractEndpoint;

public interface AeronGeneralServiceContract {

    @QuickContractEndpoint
    long duplicateNumber(long target);

    void notifyOperationDone(AeronQuickGeneralServiceServer.ExampleParamClass extraData);

    @QuickContractEndpoint
    void priceChanged(double newPrice);
}
