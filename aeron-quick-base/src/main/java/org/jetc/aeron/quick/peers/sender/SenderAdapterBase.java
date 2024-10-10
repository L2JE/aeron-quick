package org.jetc.aeron.quick.peers.sender;

import org.jetc.aeron.quick.peers.ContextDependantComponent;

/**
 * Extending this class allows {@link org.jetc.aeron.quick.AeronQuickFactory AeronQuickFactory} provide with message
 * senders that use Aeron to communicate.
 * @param <T> a contract class, a class annotated with
 * {@link org.jetc.aeron.quick.annotations.AeronQuickContract @AeronQuickContract} or at least has one method annotated
 * with {@link org.jetc.aeron.quick.annotations.QuickContractEndpoint @QuickContractEndpoint}
 */
public interface SenderAdapterBase<T> extends ContextDependantComponent {
    /**
     * @return a client implementing the contract (T)
     */
    T getClient();
}
