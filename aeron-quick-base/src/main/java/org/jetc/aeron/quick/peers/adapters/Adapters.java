package org.jetc.aeron.quick.peers.adapters;

import org.jetc.aeron.quick.annotations.AeronQuickReceiver;
import org.jetc.aeron.quick.peers.adapters.exception.AdaptingException;
import org.jetc.aeron.quick.peers.receiver.ReceiverAdapter;
import org.jetc.aeron.quick.peers.sender.SenderAdapter;
import java.lang.reflect.InvocationTargetException;

public class Adapters {
    private static final String SENDER_ADAPTER_SUFFIX = "_SAdapter";
    private static final String RECEIVER_ADAPTER_SUFFIX = "_RAdapter";

    /**
     * Loads and retrieves an adapter for a class marked with {@link AeronQuickReceiver @AeronQuickServer} which will bind the stream and channels to the corresponding method
     * @param targetServer to adapt with the compile time generated adapter
     * @return the corresponding receiver adapter for the targetServer or null if the class wasn't marked with {@link AeronQuickReceiver @AeronQuickServer}
     * @param <T> Any class marked with {@link AeronQuickReceiver @AeronQuickServer}
     */
    @SuppressWarnings("unchecked")
    public static <T> ReceiverAdapter<T> adaptReceiver(T receiverInstance) throws AdaptingException {
        Class<T> receiverClass = (Class<T>) receiverInstance.getClass();
        if(receiverClass.getAnnotation(AeronQuickReceiver.class) == null)
            throw new RuntimeException("The target receiver (%s) must be annotated with %s in order to have a compile time generated adapter".formatted(receiverClass.getCanonicalName(), AeronQuickReceiver.class.getCanonicalName()));

        return (ReceiverAdapter<T>) instantiateAdapter(receiverClass, ReceiverAdapter.class ,RECEIVER_ADAPTER_SUFFIX);
    }

    /**
     * Loads and retrieves an adapter for a class (contract class) that has methods marked with {@link org.jetc.aeron.quick.annotations.QuickContractEndpoint @QuickContractEndpoint} which will bind the stream and channels to the corresponding method
     * @param <T> Any class marked {@link org.jetc.aeron.quick.annotations.AeronQuickContract @AeronQuickContract} or having at least one method marked with {@link org.jetc.aeron.quick.annotations.QuickContractEndpoint @QuickContractEndpoint}
     * @param contract to adapt with the compile time generated adapter
     * @return the corresponding sender adapter for the target contract or null if the class is not a valid contract
     */
    @SuppressWarnings("unchecked")
    public static <T> SenderAdapter<T> adaptSender(Class<T> senderClass) throws AdaptingException {
        return (SenderAdapter<T>) instantiateAdapter(senderClass, SenderAdapter.class, SENDER_ADAPTER_SUFFIX);
    }

    private static Object instantiateAdapter(Class<?> adapted, Class<?> adapterClass, String adapterID) throws AdaptingException {
        Object retrieved;
        String adapterName = adapted.getCanonicalName() + adapterID;
        try {
            retrieved = Class.forName(adapterName)
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                 ClassNotFoundException e) {
            throw new AdaptingException("Could not load class (%s) or instantiate it using no-args constructor.".formatted(adapterName), e);
        }

        if(!adapterClass.isAssignableFrom(retrieved.getClass()))
            throw new AdaptingException("This is really unexpected: tried to load an adapter for class (%s) and adapter found (%s) is not implementing the expected adapter interface (%s).".formatted(adapted.getCanonicalName(), adapterName, adapterClass));

        return retrieved;
    }
}
