package org.w3c.dom.smil;

import org.w3c.dom.DOMException;

public interface ElementTime {
    boolean beginElement();

    boolean endElement();

    TimeList getBegin();

    float getDur();

    TimeList getEnd();

    short getFill();

    void pauseElement();

    void resumeElement();

    void seekElement(float f);

    void setDur(float f) throws DOMException;
}
