package cn.com.xy.sms.sdk.net;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.util.SdkCallBack;
import java.util.Map;

/* compiled from: Unknown */
final class i implements SdkCallBack {
    private final /* synthetic */ String a;
    private final /* synthetic */ String b;
    private final /* synthetic */ Map c;
    private final /* synthetic */ String d;
    private final /* synthetic */ XyCallBack e;

    i(String str, String str2, Map map, String str3, XyCallBack xyCallBack) {
        this.a = str;
        this.b = str2;
        this.c = map;
        this.d = str3;
        this.e = xyCallBack;
    }

    private static boolean a(Object... objArr) {
        return objArr == null || objArr.length < 2 || !objArr[0].toString().equals("0");
    }

    private static boolean b(Object... objArr) {
        return objArr != null && objArr.length > 0 && objArr[0].toString().equals("1");
    }

    public final void execute(Object... objArr) {
        int i = (objArr != null && objArr.length > 0 && objArr[0].toString().equals("1")) ? 1 : 0;
        if (i == 0) {
            i = (objArr != null && objArr.length >= 2 && objArr[0].toString().equals("0")) ? 0 : 1;
            if (i == 0) {
                try {
                    XyUtil.doXycallBackResult(this.e, Integer.valueOf(3), objArr[1]);
                    return;
                } catch (Throwable th) {
                    XyUtil.doXycallBackResult(this.e, Integer.valueOf(-10), th.getMessage());
                    return;
                }
            }
            throw new Exception("params error");
        }
        NetUtil.requestNewTokenAndPostRequestAgain(this.a, this.b, Constant.FIVE_MINUTES, false, false, false, this.c, this.d, this);
    }
}
