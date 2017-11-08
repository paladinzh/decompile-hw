package com.huawei.hwid.manager;

import android.content.Context;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.manager.a.a;

/* compiled from: HwAccountManagerBuilder */
public abstract class f {
    public static g a(Context context) {
        if (d.h(context)) {
            return a.a(context);
        }
        return com.huawei.hwid.manager.b.a.a(context);
    }
}
