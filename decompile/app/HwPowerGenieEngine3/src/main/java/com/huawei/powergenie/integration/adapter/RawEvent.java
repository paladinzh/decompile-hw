package com.huawei.powergenie.integration.adapter;

public final class RawEvent {
    private int mEventId;
    private String mPayload;
    private int mPid;
    private long mTimestamp;

    public void reset() {
        this.mPid = 0;
        this.mTimestamp = 0;
        this.mEventId = 0;
        this.mPayload = null;
    }

    public int getPid() {
        return this.mPid;
    }

    public long getTimestamp() {
        return this.mTimestamp;
    }

    public int getEventId() {
        return this.mEventId;
    }

    public String getPayload() {
        return this.mPayload;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("pid=").append(this.mPid);
        builder.append(" spent=").append(System.currentTimeMillis() - this.mTimestamp).append("ms");
        builder.append(" eventId=").append(this.mEventId);
        builder.append(" payload=").append(this.mPayload);
        return builder.toString();
    }
}
