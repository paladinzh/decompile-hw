package org.w3c.dom.smil;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

public interface SMILElement extends Element {
    String getId();

    void setId(String str) throws DOMException;
}
