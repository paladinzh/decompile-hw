package org.w3c.dom.smil;

import org.w3c.dom.NodeList;

public interface ElementTimeContainer extends ElementTime {
    NodeList getActiveChildrenAt(float f);

    NodeList getTimeChildren();
}
