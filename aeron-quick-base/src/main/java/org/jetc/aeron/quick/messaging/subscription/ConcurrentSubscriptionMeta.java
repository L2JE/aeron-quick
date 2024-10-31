package org.jetc.aeron.quick.messaging.subscription;

import org.jetc.aeron.quick.messaging.fragment_handling.concurrent.BufferDataExtractor;

public record ConcurrentSubscriptionMeta<T>(BufferDataExtractor<T> extractor, int fragmentLimit) {}
