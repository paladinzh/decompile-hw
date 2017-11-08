package com.huawei.hwid.api.common;

import android.content.Context;
import android.content.Intent;
import com.huawei.hwid.core.c.k;
import com.huawei.hwid.core.model.service.FetchCountryIPService;

/* compiled from: CloudAccountImpl */
final class b implements Runnable {
    final /* synthetic */ Context a;

    b(Context context) {
        this.a = context;
    }

    public void run() {
        Intent intent = new Intent();
        intent.setClass(this.a, FetchCountryIPService.class);
        this.a.startService(intent);
        k.a(this.a);
    }
}
