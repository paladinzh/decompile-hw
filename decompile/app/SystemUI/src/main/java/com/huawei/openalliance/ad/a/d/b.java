package com.huawei.openalliance.ad.a.d;

import android.content.Context;
import com.huawei.openalliance.ad.a.a.a.c;
import com.huawei.openalliance.ad.a.a.h;
import com.huawei.openalliance.ad.a.g.e;
import com.huawei.openalliance.ad.utils.b.d;
import com.huawei.openalliance.ad.utils.db.a;
import com.huawei.openalliance.ad.utils.db.bean.ThirdPartyEventRecord;

/* compiled from: Unknown */
class b implements e {
    final /* synthetic */ int a;

    b(int i) {
        this.a = i;
    }

    public void a() {
    }

    public void a(Context context, com.huawei.openalliance.ad.a.a.a.b bVar, c cVar) {
        if (!(!(bVar instanceof h) || cVar.responseCode == 302 || cVar.responseCode == 200)) {
            h hVar = (h) bVar;
            a a = a.a(context);
            try {
                a.a(ThirdPartyEventRecord.class.getSimpleName(), new ThirdPartyEventRecord(this.a, hVar.getUrl()).q());
            } catch (Exception e) {
                d.c("AdEventManager", "insert third party event fail");
            } finally {
                a.close();
            }
        }
        a.b(context, this.a);
    }

    public void b() {
    }
}
