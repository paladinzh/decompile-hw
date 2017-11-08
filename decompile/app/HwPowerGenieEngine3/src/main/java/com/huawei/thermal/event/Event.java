package com.huawei.thermal.event;

public abstract class Event {
    private int mEventId;
    private long mTimestamp = System.currentTimeMillis();

    public abstract int getType();

    public Event(int evtId) {
        this.mEventId = evtId;
    }

    protected void setEventId(int evtId) {
        this.mTimestamp = System.currentTimeMillis();
        this.mEventId = evtId;
    }

    public int getEventId() {
        return this.mEventId;
    }

    public long getTimeStamp() {
        return this.mTimestamp;
    }
}
