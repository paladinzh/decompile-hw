package com.huawei.hwid.core.model.http.request;

import android.content.Context;
import android.os.Bundle;
import cn.com.xy.sms.sdk.util.StringUtils;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.k;
import com.huawei.hwid.core.c.p;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.hwid.core.helper.handler.c;
import java.util.Date;

/* compiled from: GetIpCountryRequest */
class j extends c {
    private Context b;

    public j(Context context) {
        super(context);
        this.b = context;
    }

    public void onSuccess(Bundle bundle) {
        super.onSuccess(bundle);
        a.e("getIpCountryRequest", "GetIpCallBack execute success");
        String string = bundle.getString("callingCode");
        String string2 = bundle.getString("nativeName");
        String string3 = bundle.getString("englishName");
        String string4 = bundle.getString("countryCode");
        int i = bundle.getInt("siteID");
        if (string != null) {
            a.b("GetIpThread", "STR =" + string.substring(string.indexOf("+") + 1));
        }
        a(string, string2, string3, string4, i);
        a(i);
        k.a(this.b);
    }

    private void a(int i) {
        Bundle bundle = new Bundle();
        bundle.putInt("siteId", i);
        com.huawei.hwid.core.model.http.a nVar = new n(this.b, bundle);
        nVar.d(2);
        nVar.a(this.b, nVar, null, null);
    }

    private void a(String str, String str2, String str3, String str4, int i) {
        com.huawei.hwid.core.b.a.a(this.b).b("lastCheckDate", new Date().getTime());
        if (p.e(str)) {
            str = StringUtils.MPLUG86;
        }
        if (p.e(str2)) {
            String str5 = "中国";
        }
        if (p.e(str3)) {
            str3 = "CHINA";
        }
        if (p.e(str4)) {
            str5 = "cn";
        }
        k.b(this.b, str);
        k.c(this.b, str3);
        k.a(this.b, i);
    }

    public void onFail(Bundle bundle) {
        super.onFail(bundle);
        ErrorStatus errorStatus = (ErrorStatus) bundle.getParcelable("requestError");
        if (errorStatus != null) {
            a.d("getIpCountryRequest", "GetIpHelper execute error:" + f.a(errorStatus.getErrorReason()));
        }
    }
}
