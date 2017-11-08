package cn.com.xy.sms.sdk.service.number;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.util.SdkCallBack;
import java.util.Set;
import org.json.JSONArray;

/* compiled from: Unknown */
final class n implements SdkCallBack {
    private final /* synthetic */ XyCallBack a;
    private final /* synthetic */ Set b;

    n(XyCallBack xyCallBack, Set set) {
        this.a = xyCallBack;
        this.b = set;
    }

    public final void execute(Object... objArr) {
        try {
            int intValue = ((Integer) objArr[0]).intValue();
            Object obj = objArr[1];
            if (intValue == 3) {
                JSONArray a = m.a(obj);
                XyUtil.doXycallBackResult(this.a, Integer.valueOf(4), this.b, a);
                return;
            }
            XyUtil.doXycallBackResult(this.a, Integer.valueOf(intValue), this.b, obj);
        } catch (Throwable th) {
            XyUtil.doXycallBackResult(this.a, Integer.valueOf(-10), this.b, th.getMessage());
        }
    }
}
