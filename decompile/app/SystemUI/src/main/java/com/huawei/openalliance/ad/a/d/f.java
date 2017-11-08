package com.huawei.openalliance.ad.a.d;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import com.huawei.openalliance.ad.utils.b;
import com.huawei.openalliance.ad.utils.b.d;
import com.huawei.openalliance.ad.utils.c.a.b.a;
import com.huawei.openalliance.ad.utils.db.bean.MaterialRecord;
import java.io.File;

/* compiled from: Unknown */
class f implements a {
    final /* synthetic */ MaterialRecord a;
    final /* synthetic */ String b;
    final /* synthetic */ Handler c;

    f(MaterialRecord materialRecord, String str, Handler handler) {
        this.a = materialRecord;
        this.b = str;
        this.c = handler;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void a(Context context, String str) {
        com.huawei.openalliance.ad.utils.db.a aVar = null;
        try {
            aVar = com.huawei.openalliance.ad.utils.db.a.a(context);
            if (!TextUtils.isEmpty(str)) {
                if (b.b(new File(str))) {
                    this.a.h(this.a.i().replace(this.b, "file:///" + str));
                    aVar.a(MaterialRecord.class.getSimpleName(), this.a.q());
                }
            }
            if (aVar != null) {
                aVar.close();
            }
            if (this.c != null) {
                this.c.sendEmptyMessage(1002);
            }
        } catch (Exception e) {
            d.c("MaterialManager", "insert material fail");
            if (aVar != null) {
                aVar.close();
            }
            if (this.c != null) {
                this.c.sendEmptyMessage(1002);
            }
        } catch (Throwable th) {
            Throwable th2 = th;
            com.huawei.openalliance.ad.utils.db.a aVar2 = aVar;
            Throwable th3 = th2;
            if (aVar2 != null) {
                aVar2.close();
            }
            if (this.c != null) {
                this.c.sendEmptyMessage(1002);
            }
            throw th3;
        }
    }
}
