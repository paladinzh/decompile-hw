package com.huawei.openalliance.ad.a.c;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.openalliance.ad.a.a.b.e;
import com.huawei.openalliance.ad.a.e.k;
import com.huawei.openalliance.ad.a.g.d;
import com.huawei.openalliance.ad.inter.HiAdMagLock;
import com.huawei.openalliance.ad.inter.MagLockAdInfo;
import com.huawei.openalliance.ad.inter.constant.EventType;
import com.huawei.openalliance.ad.utils.h;
import fyusion.vislib.BuildConfig;

/* compiled from: Unknown */
public class a implements HiAdMagLock {
    public void reportEvent(Context context, String str, EventType eventType) {
        if (context != null && !TextUtils.isEmpty(str) && eventType != null) {
            e a = com.huawei.openalliance.ad.a.d.e.a(context, str.trim());
            if (a != null) {
                h a2 = h.a(context);
                if (EventType.IMPRESSION.value().equals(eventType.value())) {
                    a2.a(BuildConfig.FLAVOR + d.c());
                }
                a.setShowid__(a2.k());
                com.huawei.openalliance.ad.a.d.a.a(context, 2, eventType, a);
            }
        }
    }

    public void updateMagLockInfo(Context context, MagLockAdInfo magLockAdInfo, boolean z) {
        k.a(context, magLockAdInfo, z);
    }
}
