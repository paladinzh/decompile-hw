package com.android.mms.dom.smil;

import java.util.ArrayList;
import org.w3c.dom.DOMException;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.DocumentEvent;
import org.w3c.dom.events.Event;
import org.w3c.dom.smil.ElementParallelTimeContainer;
import org.w3c.dom.smil.ElementTime;
import org.w3c.dom.smil.SMILParElement;
import org.w3c.dom.smil.Time;
import org.w3c.dom.smil.TimeList;

public class SmilParElementImpl extends SmilElementImpl implements SMILParElement {
    ElementParallelTimeContainer mParTimeContainer = new ElementParallelTimeContainerImpl(this) {
        public TimeList getBegin() {
            TimeList beginTimeList = super.getBegin();
            if (beginTimeList.getLength() <= 1) {
                return beginTimeList;
            }
            ArrayList<Time> singleTimeContainer = new ArrayList();
            singleTimeContainer.add(beginTimeList.item(0));
            return new TimeListImpl(singleTimeContainer);
        }

        public NodeList getTimeChildren() {
            return SmilParElementImpl.this.getChildNodes();
        }

        public boolean beginElement() {
            Event startEvent = ((DocumentEvent) SmilParElementImpl.this.getOwnerDocument()).createEvent("Event");
            startEvent.initEvent("SmilSlideStart", false, false);
            SmilParElementImpl.this.dispatchEvent(startEvent);
            return true;
        }

        public boolean endElement() {
            Event endEvent = ((DocumentEvent) SmilParElementImpl.this.getOwnerDocument()).createEvent("Event");
            endEvent.initEvent("SmilSlideEnd", false, false);
            SmilParElementImpl.this.dispatchEvent(endEvent);
            return true;
        }

        public void pauseElement() {
        }

        public void resumeElement() {
        }

        public void seekElement(float seekTo) {
        }

        ElementTime getParentElementTime() {
            return ((SmilDocumentImpl) this.mSmilElement.getOwnerDocument()).mSeqTimeContainer;
        }
    };

    SmilParElementImpl(SmilDocumentImpl owner, String tagName) {
        super(owner, tagName.toUpperCase());
    }

    public NodeList getActiveChildrenAt(float instant) {
        return this.mParTimeContainer.getActiveChildrenAt(instant);
    }

    public NodeList getTimeChildren() {
        return this.mParTimeContainer.getTimeChildren();
    }

    public boolean beginElement() {
        return this.mParTimeContainer.beginElement();
    }

    public boolean endElement() {
        return this.mParTimeContainer.endElement();
    }

    public TimeList getBegin() {
        return this.mParTimeContainer.getBegin();
    }

    public float getDur() {
        return this.mParTimeContainer.getDur();
    }

    public TimeList getEnd() {
        return this.mParTimeContainer.getEnd();
    }

    public short getFill() {
        return this.mParTimeContainer.getFill();
    }

    public void pauseElement() {
        this.mParTimeContainer.pauseElement();
    }

    public void resumeElement() {
        this.mParTimeContainer.resumeElement();
    }

    public void seekElement(float seekTo) {
        this.mParTimeContainer.seekElement(seekTo);
    }

    public void setDur(float dur) throws DOMException {
        this.mParTimeContainer.setDur(dur);
    }
}
