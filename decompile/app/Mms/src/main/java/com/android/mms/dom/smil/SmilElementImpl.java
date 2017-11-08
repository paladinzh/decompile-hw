package com.android.mms.dom.smil;

import com.android.mms.dom.ElementImpl;
import org.w3c.dom.DOMException;
import org.w3c.dom.smil.SMILElement;

public class SmilElementImpl extends ElementImpl implements SMILElement {
    SmilElementImpl(SmilDocumentImpl owner, String tagName) {
        super(owner, tagName.toLowerCase());
    }

    public String getId() {
        return null;
    }

    public void setId(String id) throws DOMException {
    }
}
