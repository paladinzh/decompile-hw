package cn.com.xy.sms.sdk.iccid;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.IccidInfoManager;
import cn.com.xy.sms.sdk.db.entity.a;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.net.util.j;
import cn.com.xy.sms.sdk.util.StringUtils;

/* compiled from: Unknown */
final class b implements XyCallBack {
    private final /* synthetic */ String a;
    private final /* synthetic */ String b;

    b(String str, String str2) {
        this.a = str;
        this.b = str2;
    }

    public final void execute(Object... objArr) {
        if (objArr != null && objArr[0].toString().equals("0") && objArr.length == 2) {
            a c = j.c(objArr[1].toString());
            if (c.a == 0) {
                c.b = StringUtils.getSubString(this.a);
                c.g = System.currentTimeMillis();
                if (StringUtils.isNull(this.a)) {
                    if (!StringUtils.isNull(this.b)) {
                        IccidInfoManager.insertIccid(this.b, true, c.d, c.c, c.e, c.f, Constant.getContext());
                    }
                } else if (cn.com.xy.sms.sdk.db.entity.a.a.a(c)) {
                    cn.com.xy.sms.sdk.db.entity.a.a.a(c.b, c);
                }
            } else if (c.a == j.b) {
                NetUtil.QueryTokenRequest(this.b);
            }
        }
    }
}
