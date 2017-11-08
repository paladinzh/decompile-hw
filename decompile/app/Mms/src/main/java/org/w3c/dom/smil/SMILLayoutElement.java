package org.w3c.dom.smil;

import org.w3c.dom.NodeList;

public interface SMILLayoutElement extends SMILElement {
    NodeList getRegions();

    SMILRootLayoutElement getRootLayout();
}
