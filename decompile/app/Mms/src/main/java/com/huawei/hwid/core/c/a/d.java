package com.huawei.hwid.core.c.a;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.i;
import java.util.ArrayList;
import java.util.List;

/* compiled from: VersionUpdateHelper */
public class d {
    private static List a = new ArrayList();

    static {
        a.add(new a());
        a.add(new e());
        a.add(new c());
    }

    public static synchronized void a(Context context) {
        synchronized (d.class) {
            int i;
            String b = i.b(context, "encryptversion");
            int i2 = -1;
            if (TextUtils.isEmpty(b)) {
                i = i2;
            } else {
                try {
                    i = Integer.parseInt(b);
                } catch (Exception e) {
                    a.d("VersionUpdateHelper", "parse encryptversion error:" + b);
                    i = i2;
                }
            }
            a.d("VersionUpdateHelper", "old version is " + i + ", current version is " + 1);
            if (i < 1) {
                for (b a : a) {
                    a.a(context, i, 1);
                }
            }
            i.a(context, "encryptversion", String.valueOf(1));
        }
    }
}
