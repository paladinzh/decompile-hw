package com.huawei.openalliance.ad.a.e;

import android.content.Context;
import com.huawei.openalliance.ad.a.a.b;
import com.huawei.openalliance.ad.utils.k;
import java.util.ArrayList;
import java.util.List;

/* compiled from: Unknown */
public class h extends d {
    private List<String> c;

    public h() {
        this.c = new ArrayList(4);
        this.a = 2;
    }

    public void b(Context context, b bVar, boolean z) {
        k.c.execute(new j(this, bVar, context, z));
    }
}
