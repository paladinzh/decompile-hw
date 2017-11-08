package org.w3c.dom.smil;

import org.w3c.dom.DOMException;

public interface SMILMediaElement extends ElementTime, SMILElement {
    String getSrc();

    void setSrc(String str) throws DOMException;
}
