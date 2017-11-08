package com.huawei.hwid.core.d.a;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.hwid.core.d.b.e;
import com.huawei.hwid.core.d.f;
import java.util.ArrayList;
import java.util.List;

public class d {
    private static List<b> a = new ArrayList();

    static {
        a.add(new a());
        a.add(new e());
        a.add(new c());
    }

    public static synchronized void a(Context context) {
        synchronized (d.class) {
            int i;
            String b = f.b(context, "encryptversion");
            int i2 = -1;
            if (TextUtils.isEmpty(b)) {
                i = i2;
            } else {
                try {
                    i = Integer.parseInt(b);
                } catch (Exception e) {
                    e.d("VersionUpdateHelper", "parse encryptversion error:" + b);
                    i = i2;
                }
            }
            e.d("VersionUpdateHelper", "old version is " + i + ", current version is " + 1);
            if (i < 1) {
                for (b a : a) {
                    a.a(context, i, 1);
                }
            }
            f.a(context, "encryptversion", String.valueOf(1));
        }
    }
}
