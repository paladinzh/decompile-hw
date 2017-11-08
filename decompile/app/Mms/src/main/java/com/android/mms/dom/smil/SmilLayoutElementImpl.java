package com.android.mms.dom.smil;

import com.android.mms.layout.LayoutManager;
import org.w3c.dom.NodeList;
import org.w3c.dom.smil.SMILLayoutElement;
import org.w3c.dom.smil.SMILRootLayoutElement;

public class SmilLayoutElementImpl extends SmilElementImpl implements SMILLayoutElement {
    SmilLayoutElementImpl(SmilDocumentImpl owner, String tagName) {
        super(owner, tagName);
    }

    public NodeList getRegions() {
        return getElementsByTagName("region");
    }

    public SMILRootLayoutElement getRootLayout() {
        NodeList childNodes = getChildNodes();
        SMILRootLayoutElement rootLayoutNode = null;
        int childrenCount = childNodes.getLength();
        for (int i = 0; i < childrenCount; i++) {
            if (childNodes.item(i).getNodeName().equals("root-layout")) {
                rootLayoutNode = (SMILRootLayoutElement) childNodes.item(i);
            }
        }
        if (rootLayoutNode != null) {
            return rootLayoutNode;
        }
        rootLayoutNode = (SMILRootLayoutElement) getOwnerDocument().createElement("root-layout");
        rootLayoutNode.setWidth(LayoutManager.getInstance().getLayoutParameters().getWidth());
        rootLayoutNode.setHeight(LayoutManager.getInstance().getLayoutParameters().getHeight());
        appendChild(rootLayoutNode);
        return rootLayoutNode;
    }
}
