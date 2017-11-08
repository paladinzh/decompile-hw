package org.w3c.dom.smil;

import org.w3c.dom.DOMException;

public interface SMILRegionElement extends SMILElement, ElementLayout {
    String getFit();

    int getLeft();

    int getTop();

    void setFit(String str) throws DOMException;

    void setLeft(int i) throws DOMException;

    void setTop(int i) throws DOMException;
}
