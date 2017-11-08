package com.android.mms.dom.smil;

import java.util.ArrayList;
import org.w3c.dom.smil.Time;
import org.w3c.dom.smil.TimeList;

public class TimeListImpl implements TimeList {
    private final ArrayList<Time> mTimes;

    TimeListImpl(ArrayList<Time> times) {
        this.mTimes = times;
    }

    public int getLength() {
        return this.mTimes.size();
    }

    public Time item(int index) {
        Time time = null;
        try {
            return (Time) this.mTimes.get(index);
        } catch (IndexOutOfBoundsException e) {
            return time;
        }
    }
}
