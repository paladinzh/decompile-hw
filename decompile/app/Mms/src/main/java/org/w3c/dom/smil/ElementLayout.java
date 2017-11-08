package org.w3c.dom.smil;

import org.w3c.dom.DOMException;

public interface ElementLayout {
    String getBackgroundColor();

    int getHeight();

    int getWidth();

    void setBackgroundColor(String str) throws DOMException;

    void setHeight(int i) throws DOMException;

    void setWidth(int i) throws DOMException;
}
