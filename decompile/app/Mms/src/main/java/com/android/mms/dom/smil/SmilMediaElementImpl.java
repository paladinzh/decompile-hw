package com.android.mms.dom.smil;

import android.util.Log;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.android.mms.dom.events.EventImpl;
import org.w3c.dom.DOMException;
import org.w3c.dom.events.DocumentEvent;
import org.w3c.dom.events.Event;
import org.w3c.dom.smil.ElementTime;
import org.w3c.dom.smil.SMILMediaElement;
import org.w3c.dom.smil.TimeList;

public class SmilMediaElementImpl extends SmilElementImpl implements SMILMediaElement {
    ElementTime mElementTime = new ElementTimeImpl(this) {
        private Event createEvent(String eventType) {
            Event event = ((DocumentEvent) SmilMediaElementImpl.this.getOwnerDocument()).createEvent("Event");
            event.initEvent(eventType, false, false);
            return event;
        }

        private Event createEvent(String eventType, int seekTo) {
            EventImpl event = (EventImpl) ((DocumentEvent) SmilMediaElementImpl.this.getOwnerDocument()).createEvent("Event");
            event.initEvent(eventType, false, false, seekTo);
            return event;
        }

        public boolean beginElement() {
            SmilMediaElementImpl.this.dispatchEvent(createEvent("SmilMediaStart"));
            return true;
        }

        public boolean endElement() {
            SmilMediaElementImpl.this.dispatchEvent(createEvent("SmilMediaEnd"));
            return true;
        }

        public void resumeElement() {
            SmilMediaElementImpl.this.dispatchEvent(createEvent("SmilMediaStart"));
        }

        public void pauseElement() {
            SmilMediaElementImpl.this.dispatchEvent(createEvent("SmilMediaPause"));
        }

        public void seekElement(float seekTo) {
            SmilMediaElementImpl.this.dispatchEvent(createEvent("SmilMediaSeek", (int) seekTo));
        }

        public float getDur() {
            float dur = super.getDur();
            if (dur != 0.0f) {
                return dur;
            }
            String tag = SmilMediaElementImpl.this.getTagName();
            if (tag.equals("video") || tag.equals("audio")) {
                return -1.0f;
            }
            if (tag.equals("text") || tag.equals("img") || tag.equals("vcard") || tag.equals("vcalendar")) {
                return 0.0f;
            }
            Log.w("Mms:smil", "Unknown media type");
            return dur;
        }

        ElementTime getParentElementTime() {
            return ((SmilParElementImpl) this.mSmilElement.getParentNode()).mParTimeContainer;
        }
    };

    SmilMediaElementImpl(SmilDocumentImpl owner, String tagName) {
        super(owner, tagName);
    }

    public String getSrc() {
        return getAttribute(NumberInfo.SOURCE_KEY);
    }

    public void setSrc(String src) throws DOMException {
        setAttribute(NumberInfo.SOURCE_KEY, src);
    }

    public boolean beginElement() {
        return this.mElementTime.beginElement();
    }

    public boolean endElement() {
        return this.mElementTime.endElement();
    }

    public TimeList getBegin() {
        return this.mElementTime.getBegin();
    }

    public float getDur() {
        return this.mElementTime.getDur();
    }

    public TimeList getEnd() {
        return this.mElementTime.getEnd();
    }

    public short getFill() {
        return this.mElementTime.getFill();
    }

    public void pauseElement() {
        this.mElementTime.pauseElement();
    }

    public void resumeElement() {
        this.mElementTime.resumeElement();
    }

    public void seekElement(float seekTo) {
        this.mElementTime.seekElement(seekTo);
    }

    public void setDur(float dur) throws DOMException {
        this.mElementTime.setDur(dur);
    }
}
