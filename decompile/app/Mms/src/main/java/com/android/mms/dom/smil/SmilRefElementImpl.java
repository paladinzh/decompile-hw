package com.android.mms.dom.smil;

import org.w3c.dom.smil.SMILMediaElement;

public class SmilRefElementImpl extends SmilRegionMediaElementImpl implements SMILMediaElement {
    SmilRefElementImpl(SmilDocumentImpl owner, String tagName) {
        super(owner, tagName);
    }
}
