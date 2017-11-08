package com.huawei.thermal.event;

import android.content.Intent;

public final class MsgEvent extends Event {
    private Intent mIntent;

    public MsgEvent(int evtId, Intent intent) {
        super(evtId);
        this.mIntent = intent;
    }

    public void resetAs(int evtId, Intent intent) {
        setEventId(evtId);
        this.mIntent = intent;
    }

    public int getType() {
        return 1;
    }

    public Intent getIntent() {
        return this.mIntent;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(" EventID =").append(getEventId());
        builder.append(" Time =").append(getTimeStamp());
        builder.append(" Intent =").append(this.mIntent);
        return builder.toString();
    }
}
