package com.google.android.gms.wearable.internal;

import com.google.android.gms.common.data.DataHolder;
import com.google.android.gms.common.data.zzc;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataItem;

/* compiled from: Unknown */
public final class zzy extends zzc implements DataEvent {
    private final int zzasB;

    public zzy(DataHolder dataHolder, int i, int i2) {
        super(dataHolder, i);
        this.zzasB = i2;
    }

    public DataItem getDataItem() {
        return new zzae(this.zzYX, this.zzabh, this.zzasB);
    }

    public int getType() {
        return getInteger("event_type");
    }

    public String toString() {
        String str = getType() != 1 ? getType() != 2 ? "unknown" : "deleted" : "changed";
        return "DataEventRef{ type=" + str + ", dataitem=" + getDataItem() + " }";
    }
}
