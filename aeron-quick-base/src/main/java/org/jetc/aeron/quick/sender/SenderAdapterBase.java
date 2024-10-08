package org.jetc.aeron.quick.sender;

import io.aeron.Aeron;

/**
 * Extending this class allows {@link org.jetc.aeron.quick.AeronQuickFactory AeronQuickFactory} provide with message
 * senders that use Aeron to communicate.
 * @param <T> a contract class, a class annotated with
 * {@link org.jetc.aeron.quick.annotations.AeronQuickContract @AeronQuickContract} or at least has one method annotated
 * with {@link org.jetc.aeron.quick.annotations.QuickContractEndpoint @QuickContractEndpoint}
 */
public interface SenderAdapterBase<T> {
    /**
     * @return a client implementing the contract (T)
     */
    T getClient();

    /**
     * Connects to the aeron client, preparing underlying publications and buffers to start sending messages
     * @param aeron to create publications and subscriptions
     * @param clientName to find the configuration properties for this client
     */
    void initSender(Aeron aeron, String clientName);
}
