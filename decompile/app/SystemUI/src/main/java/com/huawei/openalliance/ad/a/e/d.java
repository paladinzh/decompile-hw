package com.huawei.openalliance.ad.a.e;

import android.content.Context;
import com.huawei.openalliance.ad.a.a.b;
import com.huawei.openalliance.ad.a.a.b.a;
import com.huawei.openalliance.ad.a.a.b.e;

/* compiled from: Unknown */
public abstract class d {
    protected int a = 1;

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void a(Context context, a aVar, e eVar, boolean z) {
        if (context != null && eVar != null) {
            if (1 == this.a) {
                if (2 != eVar.getCreativetype__()) {
                    if (4 == eVar.getCreativetype__()) {
                    }
                }
                com.huawei.openalliance.ad.a.d.e.a(context, aVar.getSlotid__(), this.a, eVar, z, null, false);
            }
        }
    }

    protected void a(Context context, b bVar, boolean z) {
        for (a aVar : bVar.getMultiad__()) {
            if (!(aVar == null || 200 != aVar.getRetcode30__() || aVar.getContent__() == null || aVar.getContent__().isEmpty())) {
                for (e a : aVar.getContent__()) {
                    a(context, aVar, a, z);
                }
            }
        }
    }
}
