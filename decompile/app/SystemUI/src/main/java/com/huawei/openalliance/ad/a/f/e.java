package com.huawei.openalliance.ad.a.f;

import android.content.Context;
import com.huawei.openalliance.ad.a.g.d;
import com.huawei.openalliance.ad.utils.db.a;
import com.huawei.openalliance.ad.utils.db.bean.AdEventRecord;
import com.huawei.openalliance.ad.utils.db.bean.ThirdPartyEventRecord;

/* compiled from: Unknown */
public class e extends a {
    public void a(Context context) {
        if ((d.c() - 172800000 <= 0 ? 1 : 0) == 0) {
            a a = a.a(context);
            a.a(ThirdPartyEventRecord.class.getSimpleName(), " time < ? and adType = ?", new String[]{String.valueOf(r4), String.valueOf(1)});
            a.a(AdEventRecord.class.getSimpleName(), " time < ? and adType = ?", new String[]{String.valueOf(r4), String.valueOf(1)});
            a.close();
        }
    }
}
