package com.huawei.hwid.b;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.hwid.core.d.b;
import com.huawei.hwid.core.d.b.e;
import com.huawei.hwid.core.datatype.HwAccount;
import java.util.ArrayList;
import java.util.HashMap;

public final class a {
    private static a b = null;
    private Context a;
    private HwAccount c;
    private HwAccount d;
    private HashMap<String, String> e = new HashMap();

    public static a a(Context context) {
        a aVar;
        synchronized (a.class) {
            if (b == null) {
                b = new a(context.getApplicationContext());
            }
            aVar = b;
        }
        return aVar;
    }

    private a(Context context) {
        this.a = context;
    }

    public void a() {
        e.a("HwIDMemCache", "initHwAccount");
        ArrayList a = com.huawei.hwid.a.a.a(this.a).a(this.a, "com.huawei.hwid");
        if (a.size() > 0) {
            this.c = (HwAccount) a.get(0);
        }
    }

    public void a(HwAccount hwAccount) {
        e.a("HwIDMemCache", "saveHwAccount");
        if (b.a(hwAccount)) {
            com.huawei.hwid.a.a.a(this.a).a(this.a, hwAccount);
            this.d = null;
            if (b.h(this.a)) {
                a();
                return;
            }
            e.a("HwIDMemCache", "update hwAccount in SDK");
            this.c = hwAccount;
            return;
        }
        e.d("HwIDMemCache", "hwAccount is invalid");
    }

    public HwAccount b() {
        return this.d;
    }

    public HwAccount c() {
        if (this.c == null) {
            a();
        }
        return this.c;
    }

    public void a(String str, String str2) {
        if (!TextUtils.isEmpty(str2)) {
            this.e.put(str, str2);
        }
    }

    public String a(String str) {
        if (this.e.get(str) != null) {
            return (String) this.e.get(str);
        }
        return "";
    }
}
