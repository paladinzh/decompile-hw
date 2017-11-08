package com.android.mms.dom.smil;

import org.w3c.dom.NodeList;
import org.w3c.dom.smil.SMILDocument;
import org.w3c.dom.smil.SMILRegionElement;
import org.w3c.dom.smil.SMILRegionMediaElement;

public class SmilRegionMediaElementImpl extends SmilMediaElementImpl implements SMILRegionMediaElement {
    private SMILRegionElement mRegion;

    SmilRegionMediaElementImpl(SmilDocumentImpl owner, String tagName) {
        super(owner, tagName);
    }

    public SMILRegionElement getRegion() {
        if (this.mRegion == null) {
            NodeList regions = ((SMILDocument) getOwnerDocument()).getLayout().getElementsByTagName("region");
            for (int i = 0; i < regions.getLength(); i++) {
                SMILRegionElement region = (SMILRegionElement) regions.item(i);
                if (region.getId().equals(getAttribute("region"))) {
                    this.mRegion = region;
                }
            }
        }
        return this.mRegion;
    }

    public void setRegion(SMILRegionElement region) {
        setAttribute("region", region.getId());
        this.mRegion = region;
    }
}
