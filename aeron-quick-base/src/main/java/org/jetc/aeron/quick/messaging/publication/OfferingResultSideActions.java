package org.jetc.aeron.quick.messaging.publication;

import io.aeron.Publication;
import org.jetc.aeron.quick.messaging.publication.exception.PublicationOfferFailedException;
import org.jetc.aeron.quick.peers.sender.SenderConfiguration;
import org.slf4j.Logger;
import java.util.function.Consumer;

public class OfferingResultSideActions {
    public static Consumer<Long> logResult(Logger log){
        return result -> {
            String msg = "Offer failed: ";
            if (result > 0){
                msg = "Offer Succeeded";
            } else if (result == Publication.BACK_PRESSURED){
                msg += "back pressured";
            } else if (result == Publication.NOT_CONNECTED) {
                msg += "publisher is not connected to a subscriber";
            } else if (result == Publication.ADMIN_ACTION) {
                msg += "an administration action in the system caused it";
            } else if (result == Publication.CLOSED) {
                msg += "publication is closed";
            } else if (result == Publication.MAX_POSITION_EXCEEDED) {
                msg += "publication reached its max result";
            } else {
                msg += "unknown reason: " + result;
            }

            if(log != null)
                log.warn(msg);
            else
                System.out.println(msg);
        };
    }

    public static Consumer<Long> throwOnFailed(){
        return i -> {
            if(i < 0)
                throw new PublicationOfferFailedException();
        };
    }

    public static void logResultConfig(SenderConfiguration cfg){
        cfg.setOfferingSideAction(logResult(null));
    }
}
