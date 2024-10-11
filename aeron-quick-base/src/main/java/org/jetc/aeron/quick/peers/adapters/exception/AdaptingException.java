package org.jetc.aeron.quick.peers.adapters.exception;

public class AdaptingException extends RuntimeException{
    public AdaptingException(String message) {
        super(message);
    }

    public AdaptingException(String message, Throwable cause) {
        super(message, cause);
    }
}
