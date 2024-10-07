package org.jetc.aeron.quick.samples.annotation_receiver_and_sender;

import org.jetc.aeron.quick.annotations.AeronQuickContract;

@AeronQuickContract
public interface AeronGeneralServiceContract {
    long duplicateNumber(long target);
    void notifyOperationDone(ExamplePojo extraData, int id);
}
