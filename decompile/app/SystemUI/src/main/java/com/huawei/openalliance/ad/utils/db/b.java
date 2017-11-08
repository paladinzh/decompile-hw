package com.huawei.openalliance.ad.utils.db;

import com.huawei.openalliance.ad.utils.i;
import java.io.File;
import java.util.ArrayList;

/* compiled from: Unknown */
class b implements Runnable {
    final /* synthetic */ ArrayList a;
    final /* synthetic */ a b;

    b(a aVar, ArrayList arrayList) {
        this.b = aVar;
        this.a = arrayList;
    }

    public void run() {
        String str = i.b(this.b.c) + File.separator + "hiad" + File.separator;
        String str2 = i.c(this.b.c) + File.separator + "hiad" + File.separator;
        com.huawei.openalliance.ad.utils.b.a(str, this.a);
        com.huawei.openalliance.ad.utils.b.a(str2, this.a);
    }
}
