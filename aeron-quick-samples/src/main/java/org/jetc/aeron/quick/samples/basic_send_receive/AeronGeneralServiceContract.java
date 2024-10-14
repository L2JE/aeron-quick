package org.jetc.aeron.quick.samples.basic_send_receive;

import org.jetc.aeron.quick.annotations.AeronQuickContract;

@AeronQuickContract
public interface AeronGeneralServiceContract {
    long duplicateNumber(long target);
    void notifyOperationDone(ExamplePojo extraData, int id);
}
