package com.android.mms.dom.smil;

import org.w3c.dom.DOMException;
import org.w3c.dom.smil.SMILRootLayoutElement;

public class SmilRootLayoutElementImpl extends SmilElementImpl implements SMILRootLayoutElement {
    SmilRootLayoutElementImpl(SmilDocumentImpl owner, String tagName) {
        super(owner, tagName);
    }

    public String getBackgroundColor() {
        return getAttribute("backgroundColor");
    }

    public int getHeight() {
        return parseAbsoluteLength(getAttribute("height"));
    }

    public int getWidth() {
        return parseAbsoluteLength(getAttribute("width"));
    }

    public void setBackgroundColor(String backgroundColor) throws DOMException {
        setAttribute("backgroundColor", backgroundColor);
    }

    public void setHeight(int height) throws DOMException {
        setAttribute("height", String.valueOf(height) + "px");
    }

    public void setWidth(int width) throws DOMException {
        setAttribute("width", String.valueOf(width) + "px");
    }

    private int parseAbsoluteLength(String length) {
        if (length.endsWith("px")) {
            length = length.substring(0, length.indexOf("px"));
        }
        try {
            return Integer.parseInt(length);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
