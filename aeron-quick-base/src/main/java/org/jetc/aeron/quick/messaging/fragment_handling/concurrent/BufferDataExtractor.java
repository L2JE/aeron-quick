package org.jetc.aeron.quick.messaging.fragment_handling.concurrent;

import com.lmax.disruptor.EventTranslatorThreeArg;
import org.agrona.DirectBuffer;
import org.jetc.aeron.quick.deferred_exec.ExecutionRequestEvent;

/**
 * Extracts the data from the received buffer and inserts it in the given {@link ExecutionRequestEvent}
 * @param <T> target class where {@link ExecutionRequestEvent ExecutionRequestEvents} correspond
 */
public interface BufferDataExtractor<T> extends EventTranslatorThreeArg<ExecutionRequestEvent<T>, DirectBuffer, Integer, Integer> {
}
