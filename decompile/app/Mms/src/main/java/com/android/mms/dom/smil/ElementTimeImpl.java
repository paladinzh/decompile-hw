package com.android.mms.dom.smil;

import android.util.Log;
import java.util.ArrayList;
import org.w3c.dom.DOMException;
import org.w3c.dom.smil.ElementTime;
import org.w3c.dom.smil.SMILElement;
import org.w3c.dom.smil.Time;
import org.w3c.dom.smil.TimeList;

public abstract class ElementTimeImpl implements ElementTime {
    final SMILElement mSmilElement;

    abstract ElementTime getParentElementTime();

    ElementTimeImpl(SMILElement element) {
        this.mSmilElement = element;
    }

    int getBeginConstraints() {
        return 255;
    }

    int getEndConstraints() {
        return 255;
    }

    public TimeList getBegin() {
        String[] beginTimeStringList = this.mSmilElement.getAttribute("begin").split(";");
        ArrayList<Time> beginTimeList = new ArrayList();
        for (String timeImpl : beginTimeStringList) {
            try {
                beginTimeList.add(new TimeImpl(timeImpl, getBeginConstraints()));
            } catch (IllegalArgumentException e) {
            }
        }
        if (beginTimeList.size() == 0) {
            beginTimeList.add(new TimeImpl("0", 255));
        }
        return new TimeListImpl(beginTimeList);
    }

    public float getDur() {
        try {
            String durString = this.mSmilElement.getAttribute("dur");
            if (durString != null) {
                return TimeImpl.parseClockValue(durString) / 1000.0f;
            }
            return 0.0f;
        } catch (IllegalArgumentException e) {
            return 0.0f;
        }
    }

    public TimeList getEnd() {
        int i;
        ArrayList<Time> endTimeList = new ArrayList();
        String[] endTimeStringList = this.mSmilElement.getAttribute("end").split(";");
        if (!(len == 1 && endTimeStringList[0].length() == 0)) {
            for (String timeImpl : endTimeStringList) {
                try {
                    endTimeList.add(new TimeImpl(timeImpl, getEndConstraints()));
                } catch (IllegalArgumentException e) {
                    Log.e("ElementTimeImpl", "Malformed time value.", e);
                }
            }
        }
        if (endTimeList.size() == 0) {
            float duration = getDur();
            if (duration < 0.0f) {
                endTimeList.add(new TimeImpl("indefinite", getEndConstraints()));
            } else {
                TimeList begin = getBegin();
                for (i = 0; i < begin.getLength(); i++) {
                    endTimeList.add(new TimeImpl((begin.item(i).getResolvedOffset() + ((double) duration)) + "s", getEndConstraints()));
                }
            }
        }
        return new TimeListImpl(endTimeList);
    }

    private boolean beginAndEndAreZero() {
        boolean z = true;
        TimeList begin = getBegin();
        TimeList end = getEnd();
        if (begin.getLength() != 1 || end.getLength() != 1) {
            return false;
        }
        Time beginTime = begin.item(0);
        Time endTime = end.item(0);
        if (!(beginTime.getOffset() == 0.0d && endTime.getOffset() == 0.0d)) {
            z = false;
        }
        return z;
    }

    public short getFill() {
        String fill = this.mSmilElement.getAttribute("fill");
        if (fill.equalsIgnoreCase("freeze")) {
            return (short) 1;
        }
        if (fill.equalsIgnoreCase("remove")) {
            return (short) 0;
        }
        if (fill.equalsIgnoreCase("hold") || fill.equalsIgnoreCase("transition")) {
            return (short) 1;
        }
        if (!fill.equalsIgnoreCase("auto")) {
            short fillDefault = getFillDefault();
            if (fillDefault != (short) 2) {
                return fillDefault;
            }
        }
        return ((this.mSmilElement.getAttribute("dur").length() == 0 && this.mSmilElement.getAttribute("end").length() == 0 && this.mSmilElement.getAttribute("repeatCount").length() == 0 && this.mSmilElement.getAttribute("repeatDur").length() == 0) || beginAndEndAreZero()) ? (short) 1 : (short) 0;
    }

    public short getFillDefault() {
        String fillDefault = this.mSmilElement.getAttribute("fillDefault");
        if (fillDefault.equalsIgnoreCase("remove")) {
            return (short) 0;
        }
        if (fillDefault.equalsIgnoreCase("freeze")) {
            return (short) 1;
        }
        if (fillDefault.equalsIgnoreCase("auto")) {
            return (short) 2;
        }
        if (fillDefault.equalsIgnoreCase("hold") || fillDefault.equalsIgnoreCase("transition")) {
            return (short) 1;
        }
        ElementTime parent = getParentElementTime();
        if (parent == null) {
            return (short) 2;
        }
        return ((ElementTimeImpl) parent).getFillDefault();
    }

    public void setDur(float dur) throws DOMException {
        this.mSmilElement.setAttribute("dur", Integer.toString((int) (1000.0f * dur)) + "ms");
    }
}
