package org.w3c.dom.events;

public class EventException extends RuntimeException {
    public short code;

    public EventException(short code, String message) {
        super(message);
        this.code = code;
    }
}
