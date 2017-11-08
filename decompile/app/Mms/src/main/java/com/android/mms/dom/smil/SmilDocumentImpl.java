package com.android.mms.dom.smil;

import com.android.mms.dom.DocumentImpl;
import com.android.mms.dom.events.EventImpl;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.DocumentEvent;
import org.w3c.dom.events.Event;
import org.w3c.dom.smil.ElementSequentialTimeContainer;
import org.w3c.dom.smil.ElementTime;
import org.w3c.dom.smil.SMILDocument;
import org.w3c.dom.smil.SMILElement;
import org.w3c.dom.smil.SMILLayoutElement;
import org.w3c.dom.smil.TimeList;

public class SmilDocumentImpl extends DocumentImpl implements SMILDocument, DocumentEvent {
    ElementSequentialTimeContainer mSeqTimeContainer;

    public NodeList getActiveChildrenAt(float instant) {
        return this.mSeqTimeContainer.getActiveChildrenAt(instant);
    }

    public NodeList getTimeChildren() {
        return this.mSeqTimeContainer.getTimeChildren();
    }

    public boolean beginElement() {
        return this.mSeqTimeContainer.beginElement();
    }

    public boolean endElement() {
        return this.mSeqTimeContainer.endElement();
    }

    public TimeList getBegin() {
        return this.mSeqTimeContainer.getBegin();
    }

    public float getDur() {
        return this.mSeqTimeContainer.getDur();
    }

    public TimeList getEnd() {
        return this.mSeqTimeContainer.getEnd();
    }

    public short getFill() {
        return this.mSeqTimeContainer.getFill();
    }

    public void pauseElement() {
        this.mSeqTimeContainer.pauseElement();
    }

    public void resumeElement() {
        this.mSeqTimeContainer.resumeElement();
    }

    public void seekElement(float seekTo) {
        this.mSeqTimeContainer.seekElement(seekTo);
    }

    public void setDur(float dur) throws DOMException {
        this.mSeqTimeContainer.setDur(dur);
    }

    public Element createElement(String tagName) throws DOMException {
        tagName = tagName.toLowerCase();
        if (tagName.equals("text") || tagName.equals("img") || tagName.equals("video")) {
            return new SmilRegionMediaElementImpl(this, tagName);
        }
        if (tagName.equals("audio") || tagName.equals("vcard") || tagName.equals("vcalendar")) {
            return new SmilMediaElementImpl(this, tagName);
        }
        if (tagName.equals("layout")) {
            return new SmilLayoutElementImpl(this, tagName);
        }
        if (tagName.equals("root-layout")) {
            return new SmilRootLayoutElementImpl(this, tagName);
        }
        if (tagName.equals("region")) {
            return new SmilRegionElementImpl(this, tagName);
        }
        if (tagName.equals("ref")) {
            return new SmilRefElementImpl(this, tagName);
        }
        if (tagName.equals("par")) {
            return new SmilParElementImpl(this, tagName);
        }
        return new SmilElementImpl(this, tagName);
    }

    public SMILElement getDocumentElement() {
        Node rootElement = getFirstChild();
        if (rootElement == null || !(rootElement instanceof SMILElement)) {
            rootElement = createElement("smil");
            appendChild(rootElement);
        }
        return (SMILElement) rootElement;
    }

    public SMILElement getHead() {
        Node rootElement = getDocumentElement();
        Node headElement = rootElement.getFirstChild();
        if (headElement == null || !(headElement instanceof SMILElement)) {
            headElement = createElement("head");
            rootElement.appendChild(headElement);
        }
        return (SMILElement) headElement;
    }

    public SMILElement getBody() {
        Node rootElement = getDocumentElement();
        Node bodyElement = getHead().getNextSibling();
        if (bodyElement == null || !(bodyElement instanceof SMILElement)) {
            bodyElement = createElement("body");
            rootElement.appendChild(bodyElement);
        }
        this.mSeqTimeContainer = new ElementSequentialTimeContainerImpl((SMILElement) bodyElement) {
            public NodeList getTimeChildren() {
                return SmilDocumentImpl.this.getBody().getElementsByTagName("par");
            }

            public boolean beginElement() {
                Event startEvent = SmilDocumentImpl.this.createEvent("Event");
                startEvent.initEvent("SmilDocumentStart", false, false);
                SmilDocumentImpl.this.dispatchEvent(startEvent);
                return true;
            }

            public boolean endElement() {
                Event endEvent = SmilDocumentImpl.this.createEvent("Event");
                endEvent.initEvent("SimlDocumentEnd", false, false);
                SmilDocumentImpl.this.dispatchEvent(endEvent);
                return true;
            }

            public void pauseElement() {
            }

            public void resumeElement() {
            }

            public void seekElement(float seekTo) {
            }

            ElementTime getParentElementTime() {
                return null;
            }
        };
        return (SMILElement) bodyElement;
    }

    public SMILLayoutElement getLayout() {
        Node headElement = getHead();
        Node layoutElement = headElement.getFirstChild();
        while (layoutElement != null && !(layoutElement instanceof SMILLayoutElement)) {
            layoutElement = layoutElement.getNextSibling();
        }
        if (layoutElement == null) {
            layoutElement = new SmilLayoutElementImpl(this, "layout");
            headElement.appendChild(layoutElement);
        }
        return (SMILLayoutElement) layoutElement;
    }

    public Event createEvent(String eventType) throws DOMException {
        if ("Event".equals(eventType)) {
            return new EventImpl();
        }
        throw new DOMException((short) 9, "Not supported interface");
    }
}
