package org.jetc.aeron.quick.peers;

import org.jetc.aeron.quick.annotations.AeronQuickReceiver;
import org.jetc.aeron.quick.peers.sender.SenderAdapterBase;
import org.jetc.aeron.quick.peers.receiver.ReceiverAdapterBase;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

public class Adapters {
    private static final String ADAPTER_SUFFIX = "_Adapter";
    private static final String SENDER_ADAPTER_SUFFIX = "_SAdapter";

    /**
     * Loads and retrieves an adapter for a class marked with {@link AeronQuickReceiver @AeronQuickServer} which will bind the stream and channels to the corresponding method
     * @param targetServer to adapt with the compile time generated adapter
     * @return the corresponding receiver adapter for the targetServer or null if the class wasn't marked with {@link AeronQuickReceiver @AeronQuickServer}
     * @param <T> Any class marked with {@link AeronQuickReceiver @AeronQuickServer}
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<ReceiverAdapterBase<T>> adaptReceiver(T targetServer){
        Class<T> serverClass = (Class<T>) targetServer.getClass();

        if(serverClass.getAnnotation(AeronQuickReceiver.class) != null) {
            try {
                return Optional.of(
                        (ReceiverAdapterBase<T>) Class.forName(serverClass.getCanonicalName() + ADAPTER_SUFFIX)
                                .getDeclaredConstructor(serverClass)
                                .newInstance(targetServer)
                );
            } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                     InstantiationException | IllegalAccessException ignored) {
                // IF THE CLASS IS MARKED WITH @AeronQuickServer IT WILL ALWAYS HAVE AN ADAPTER GENERATED BY THE PROCESSOR IN THE SAME PACKAGE
            }
        }
        return Optional.empty();
    }

    /**
     * Loads and retrieves an adapter for a class (contract class) that has methods marked with {@link org.jetc.aeron.quick.annotations.QuickContractEndpoint @QuickContractEndpoint} which will bind the stream and channels to the corresponding method
     * @param <T> Any class marked {@link org.jetc.aeron.quick.annotations.AeronQuickContract @AeronQuickContract} or having at least one method marked with {@link org.jetc.aeron.quick.annotations.QuickContractEndpoint @QuickContractEndpoint}
     * @param contract to adapt with the compile time generated adapter
     * @return the corresponding sender adapter for the target contract or null if the class is not a valid contract
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<SenderAdapterBase<T>> adaptSender(Class<T> contract) {
        SenderAdapterBase<T> adapter = null;
        try {
            adapter = (SenderAdapterBase<T>) Class.forName(contract.getCanonicalName() + SENDER_ADAPTER_SUFFIX)
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException | NoSuchMethodException | InstantiationException ignored) {
            throw new RuntimeException();
        }

        return Optional.of(adapter);
    }
}