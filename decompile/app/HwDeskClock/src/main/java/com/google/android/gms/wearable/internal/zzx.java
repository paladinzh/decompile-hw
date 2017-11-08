package com.google.android.gms.wearable.internal;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataItem;

/* compiled from: Unknown */
public class zzx implements DataEvent {
    private int zzUS;
    private DataItem zzbap;

    public DataItem getDataItem() {
        return this.zzbap;
    }

    public int getType() {
        return this.zzUS;
    }

    public String toString() {
        String str = getType() != 1 ? getType() != 2 ? "unknown" : "deleted" : "changed";
        return "DataEventEntity{ type=" + str + ", dataitem=" + getDataItem() + " }";
    }
}
