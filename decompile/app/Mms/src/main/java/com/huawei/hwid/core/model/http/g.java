package com.huawei.hwid.core.model.http;

import android.content.Context;
import android.os.Bundle;
import android.util.SparseIntArray;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.m;

/* compiled from: HttpStatusCode */
public class g {
    private static final SparseIntArray a = new SparseIntArray();
    private static final SparseIntArray b = new SparseIntArray();

    static {
        b.put(70002057, 70002003);
        b.put(70002059, 70002001);
        b.put(70002060, 70002001);
    }

    public static String a(Context context, int i) {
        a.put(70002044, m.a(context, "CS_bind_devices_excess"));
        a.put(70002019, m.a(context, "CS_email_already_verified"));
        a.put(70001104, m.a(context, "CS_overload_message"));
        a.put(70002067, m.a(context, "CS_area_not_support_service"));
        String str = "";
        if (a.get(i) == 0) {
            return str;
        }
        if (70002044 != i) {
            return context.getString(a.get(i));
        }
        return context.getString(a.get(i), new Object[]{"http://www1.hicloud.com/"});
    }

    public static int a(Bundle bundle, int i) {
        if (bundle == null || i == 0) {
            a.b("HttpStatusCode", "bundle or errorCode is null");
            return i;
        }
        if (SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE == bundle.getInt("responseCode") && b.get(i) != 0) {
            i = b.get(i);
            a.e("HttpStatusCode", "transform errorCode = " + i + ", to " + i);
        }
        return i;
    }
}
