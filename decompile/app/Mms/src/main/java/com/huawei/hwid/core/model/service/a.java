package com.huawei.hwid.core.model.service;

import android.content.Context;
import android.os.Bundle;
import com.huawei.hwid.core.model.http.request.i;

/* compiled from: FetchCountryIPService */
final class a implements Runnable {
    final /* synthetic */ Context a;

    a(Context context) {
        this.a = context;
    }

    public void run() {
        com.huawei.hwid.core.model.http.a iVar = new i(this.a, new Bundle());
        iVar.a(this.a, iVar, null, null);
    }
}
