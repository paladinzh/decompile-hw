package com.huawei.openalliance.ad.a.d;

import android.content.Context;
import com.huawei.openalliance.ad.a.a.a.b;
import com.huawei.openalliance.ad.a.a.a.c;
import com.huawei.openalliance.ad.a.a.f;
import com.huawei.openalliance.ad.a.a.g;
import com.huawei.openalliance.ad.a.g.e;
import com.huawei.openalliance.ad.utils.db.a;
import com.huawei.openalliance.ad.utils.db.bean.AdEventRecord;
import java.util.ArrayList;
import java.util.List;

/* compiled from: Unknown */
class d implements e {
    final /* synthetic */ List a;

    d(List list) {
        this.a = list;
    }

    public void a() {
    }

    public void a(Context context, b bVar, c cVar) {
        if (cVar.responseCode == 0 && (bVar instanceof f) && (cVar instanceof g)) {
            List<com.huawei.openalliance.ad.a.a.b.b> list = ((g) cVar).result__;
            a a = a.a(context);
            if (list != null) {
                try {
                    if (!list.isEmpty()) {
                        List arrayList = new ArrayList(4);
                        List arrayList2 = new ArrayList(4);
                        for (com.huawei.openalliance.ad.a.a.b.b a2 : list) {
                            com.huawei.openalliance.ad.a.e.e.a(a2, arrayList, arrayList2);
                        }
                        a.a(AdEventRecord.class.getSimpleName(), "_id", arrayList);
                        a.a(AdEventRecord.class.getSimpleName(), arrayList2, 0);
                    }
                } catch (Exception e) {
                    com.huawei.openalliance.ad.utils.b.d.c("AdEventManager", "handle event cache report result fail");
                    return;
                } finally {
                    a.close();
                }
            }
            a.close();
            return;
        }
        com.huawei.openalliance.ad.a.e.e.a(context, this.a);
    }

    public void b() {
    }
}
