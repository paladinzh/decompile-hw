package com.huawei.hwid.core.c.a;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.encrypt.e;

/* compiled from: DBGrade */
public class a implements b {
    public void a(Context context, int i, int i2) {
        if (i < i2) {
            if (d.h(context)) {
                com.huawei.hwid.core.c.b.a.b("DBGrade", "update vip databse when version update");
                a(context);
            }
            return;
        }
        com.huawei.hwid.core.c.b.a.d("DBGrade", "newVersion is less then oldVersion, onUpgrade error");
    }

    private void a(Context context) {
        int i = 0;
        String[] strArr = new String[]{"deviceVipUserId", "curUserId"};
        com.huawei.hwid.core.c.b.a.b("DBGrade", "update deviceVipUserId and curUserId");
        int length = strArr.length;
        while (i < length) {
            String str = strArr[i];
            Object a = com.huawei.hwid.c.a.a.a(context, str);
            if (TextUtils.isEmpty(a)) {
                com.huawei.hwid.core.c.b.a.d("DBGrade", str + " get error");
            } else {
                a = e.a(context, a);
                if (TextUtils.isEmpty(a)) {
                    com.huawei.hwid.core.c.b.a.d("DBGrade", str + " ecbDecrypter error");
                } else {
                    String b = e.b(context, a);
                    if (TextUtils.isEmpty(b)) {
                        com.huawei.hwid.c.a.a.a(context, str, "");
                        com.huawei.hwid.core.c.b.a.d("DBGrade", str + " cbcEncrypter error");
                    } else {
                        com.huawei.hwid.c.a.a.a(context, str, b);
                    }
                }
            }
            i++;
        }
    }
}
