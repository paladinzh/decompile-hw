package com.huawei.openalliance.ad.a.d;

import android.content.ContentValues;
import android.content.Context;
import com.huawei.openalliance.ad.a.a.a.b;
import com.huawei.openalliance.ad.a.a.h;
import com.huawei.openalliance.ad.a.g.d;
import com.huawei.openalliance.ad.a.g.e;
import com.huawei.openalliance.ad.utils.db.a;
import com.huawei.openalliance.ad.utils.db.bean.ThirdPartyEventRecord;

/* compiled from: Unknown */
class c implements e {
    c() {
    }

    public void a() {
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void a(Context context, b bVar, com.huawei.openalliance.ad.a.a.a.c cVar) {
        a aVar = null;
        if (bVar instanceof h) {
            try {
                aVar = a.a(context);
                h hVar = (h) bVar;
                if (cVar.responseCode != 302) {
                    if (cVar.responseCode != 200) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("time", Long.valueOf(d.c()));
                        contentValues.put("lockTime", Integer.valueOf(0));
                        aVar.a(ThirdPartyEventRecord.class.getSimpleName(), contentValues, "_id = ?", new String[]{hVar.get_id()});
                        if (aVar != null) {
                            aVar.close();
                        }
                    }
                }
                aVar.a(ThirdPartyEventRecord.class.getSimpleName(), "_id = ?", new String[]{hVar.get_id()});
                if (aVar != null) {
                    aVar.close();
                }
            } catch (Exception e) {
                com.huawei.openalliance.ad.utils.b.d.c("AdEventManager", "handle third party report result fail");
                if (aVar != null) {
                    aVar.close();
                }
            } catch (Throwable th) {
                Throwable th2 = th;
                a aVar2 = aVar;
                Throwable th3 = th2;
                if (aVar2 != null) {
                    aVar2.close();
                }
                throw th3;
            }
        }
    }

    public void b() {
    }
}
