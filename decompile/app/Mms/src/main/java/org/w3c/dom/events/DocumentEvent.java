package org.w3c.dom.events;

import org.w3c.dom.DOMException;

public interface DocumentEvent {
    Event createEvent(String str) throws DOMException;
}
