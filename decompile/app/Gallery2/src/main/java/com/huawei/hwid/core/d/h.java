package com.huawei.hwid.core.d;

import android.content.Context;
import com.huawei.hwid.core.c.a;
import com.huawei.hwid.core.d.b.e;
import com.huawei.hwid.core.datatype.SMSKeyInfo;
import com.huawei.hwid.core.datatype.SiteCountryInfo;
import com.huawei.hwid.core.datatype.SiteListInfo;
import java.util.HashMap;

public class h {
    private static CharSequence[] a = null;
    private static CharSequence[] b = null;
    private static HashMap<CharSequence, CharSequence> c = new HashMap();
    private static SiteCountryInfo d = null;
    private static SMSKeyInfo e = null;
    private static SiteListInfo f = null;
    private static boolean g = false;

    public static int a(Context context) {
        int i = 0;
        try {
            i = a.a(context).a("ip_countrySiteID", 0);
        } catch (Exception e) {
            e.d("IpCountryUtil", "siteID in prefrence maybe err");
        }
        return i;
    }
}
