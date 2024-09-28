package org.jetc.aeron.quick.annotations.processors.utils;

public class AdaptingError extends Exception{
    public AdaptingError(String ex) {
        super(ex);
    }
}
