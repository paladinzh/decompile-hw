package com.huawei.hwid.core.d.a;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.hwid.core.d.b;
import com.huawei.hwid.core.d.b.e;

public class a implements b {
    public void a(Context context, int i, int i2) {
        if (i < i2) {
            if (b.h(context)) {
                e.b("DBGrade", "update vip databse when version update");
                a(context);
            }
            return;
        }
        e.d("DBGrade", "newVersion is less then oldVersion, onUpgrade error");
    }

    private void a(Context context) {
        int i = 0;
        String[] strArr = new String[]{"deviceVipUserId", "curUserId"};
        e.b("DBGrade", "update deviceVipUserId and curUserId");
        int length = strArr.length;
        while (i < length) {
            String str = strArr[i];
            Object a = com.huawei.hwid.d.a.a(context, str);
            if (TextUtils.isEmpty(a)) {
                e.d("DBGrade", str + " get error");
            } else {
                a = com.huawei.hwid.core.encrypt.e.a(context, a);
                if (TextUtils.isEmpty(a)) {
                    e.d("DBGrade", str + " ecbDecrypter error");
                } else {
                    a = com.huawei.hwid.core.encrypt.e.b(context, a);
                    if (TextUtils.isEmpty(a)) {
                        com.huawei.hwid.d.a.a(context, str, "");
                        e.d("DBGrade", str + " cbcEncrypter error");
                    } else {
                        com.huawei.hwid.d.a.a(context, str, a);
                    }
                }
            }
            i++;
        }
    }
}
