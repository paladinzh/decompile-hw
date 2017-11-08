package com.android.mms.dom.smil;

import com.android.mms.dom.NodeListImpl;
import java.util.ArrayList;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.smil.ElementParallelTimeContainer;
import org.w3c.dom.smil.ElementTime;
import org.w3c.dom.smil.SMILElement;
import org.w3c.dom.smil.Time;
import org.w3c.dom.smil.TimeList;

public abstract class ElementParallelTimeContainerImpl extends ElementTimeContainerImpl implements ElementParallelTimeContainer {
    ElementParallelTimeContainerImpl(SMILElement element) {
        super(element);
    }

    public String getEndSync() {
        String endsync = this.mSmilElement.getAttribute("endsync");
        if (endsync == null || endsync.length() == 0) {
            setEndSync("last");
            return "last";
        } else if ("first".equals(endsync) || "last".equals(endsync) || "all".equals(endsync) || "media".equals(endsync)) {
            return endsync;
        } else {
            setEndSync("last");
            return "last";
        }
    }

    public void setEndSync(String endSync) throws DOMException {
        if ("first".equals(endSync) || "last".equals(endSync) || "all".equals(endSync) || "media".equals(endSync)) {
            this.mSmilElement.setAttribute("endsync", endSync);
            return;
        }
        throw new DOMException((short) 9, "Unsupported endsync value" + endSync);
    }

    public float getDur() {
        float dur = super.getDur();
        if (dur == 0.0f) {
            return getImplicitDuration();
        }
        return dur;
    }

    public float getImplicitDuration() {
        float dur = -1.0f;
        if ("last".equals(getEndSync())) {
            NodeList children = getTimeChildren();
            for (int i = 0; i < children.getLength(); i++) {
                TimeList endTimeList = ((ElementTime) children.item(i)).getEnd();
                for (int j = 0; j < endTimeList.getLength(); j++) {
                    Time endTime = endTimeList.item(j);
                    if (endTime.getTimeType() == (short) 0) {
                        return -1.0f;
                    }
                    if (endTime.getResolved()) {
                        float end = (float) endTime.getResolvedOffset();
                        if (end > dur) {
                            dur = end;
                        }
                    }
                }
            }
        }
        return dur;
    }

    public NodeList getActiveChildrenAt(float instant) {
        ArrayList<Node> activeChildren = new ArrayList();
        NodeList children = getTimeChildren();
        int childrenLen = children.getLength();
        for (int i = 0; i < childrenLen; i++) {
            int j;
            double maxOffset = 0.0d;
            boolean active = false;
            ElementTime child = (ElementTime) children.item(i);
            TimeList beginList = child.getBegin();
            int len = beginList.getLength();
            for (j = 0; j < len; j++) {
                double resolvedOffset;
                Time begin = beginList.item(j);
                if (begin.getResolved()) {
                    resolvedOffset = begin.getResolvedOffset() * 1000.0d;
                    if (resolvedOffset <= ((double) instant) && resolvedOffset >= maxOffset) {
                        maxOffset = resolvedOffset;
                        active = true;
                    }
                }
            }
            TimeList endList = child.getEnd();
            len = endList.getLength();
            for (j = 0; j < len; j++) {
                Time end = endList.item(j);
                if (end.getResolved()) {
                    resolvedOffset = end.getResolvedOffset() * 1000.0d;
                    if (resolvedOffset <= ((double) instant) && resolvedOffset >= maxOffset) {
                        maxOffset = resolvedOffset;
                        active = false;
                    }
                }
            }
            if (active) {
                activeChildren.add((Node) child);
            }
        }
        return new NodeListImpl(activeChildren);
    }
}
