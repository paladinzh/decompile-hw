package com.huawei.hwid.vermanager;

import android.content.Context;
import android.webkit.WebViewClient;
import com.huawei.cloudservice.a;

public abstract class b {
    public static c a() {
        return VersionManager.h();
    }

    public static WebViewClient a(Context context, a aVar) {
        return new a(context, aVar);
    }
}
