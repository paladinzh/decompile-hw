package com.huawei.hwid.ui.common.login;

import android.content.Context;
import android.os.Bundle;
import cn.com.xy.sms.sdk.net.NetUtil;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.datatype.HwAccount;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.manager.g;
import com.huawei.hwid.ui.common.c;

/* compiled from: LoginRegisterCommonActivity */
public class l extends c {
    private boolean b = false;
    final /* synthetic */ LoginRegisterCommonActivity d;
    private g e;
    private HwAccount f;

    public l(LoginRegisterCommonActivity loginRegisterCommonActivity, Context context, g gVar) {
        this.d = loginRegisterCommonActivity;
        super(loginRegisterCommonActivity, context);
        this.e = gVar;
    }

    public void onSuccess(Bundle bundle) {
        super.onSuccess(bundle);
        String string = bundle.getString(NetUtil.REQ_QUERY_TOEKN);
        String string2 = bundle.getString("userId");
        String string3 = bundle.getString("cookie");
        int i = bundle.getInt("siteId");
        String string4 = bundle.getString("userName");
        String string5 = bundle.getString("tokenType");
        String string6 = bundle.getString("deviceId");
        String string7 = bundle.getString("deviceType");
        String string8 = bundle.getString("accountType");
        a.b("LoginRegisterCommonActivity", "BaseLoginCallback:" + f.a(bundle));
        this.f = d.a(string4, string5, string, string2, i, string3, string6, string7, string8);
        this.b = this.e.a(this.d, this.f);
    }

    public void onFail(Bundle bundle) {
        super.onFail(bundle);
    }

    protected boolean a() {
        return this.b;
    }

    protected HwAccount b() {
        return this.f;
    }
}
