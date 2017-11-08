package com.huawei.openalliance.ad.inter;

import android.content.Context;
import com.huawei.openalliance.ad.a.c.a;
import com.huawei.openalliance.ad.inter.constant.EventType;

/* compiled from: Unknown */
public interface HiAdMagLock {

    /* compiled from: Unknown */
    public static final class Builder {
        public final HiAdMagLock build() {
            return new a();
        }
    }

    void reportEvent(Context context, String str, EventType eventType);

    void updateMagLockInfo(Context context, MagLockAdInfo magLockAdInfo, boolean z);
}
