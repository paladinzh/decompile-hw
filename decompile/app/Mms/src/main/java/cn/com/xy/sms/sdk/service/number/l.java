package cn.com.xy.sms.sdk.service.number;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.util.SdkCallBack;
import org.json.JSONObject;

/* compiled from: Unknown */
final class l implements SdkCallBack {
    private final /* synthetic */ XyCallBack a;

    l(XyCallBack xyCallBack) {
        this.a = xyCallBack;
    }

    public final void execute(Object... objArr) {
        try {
            int intValue = ((Integer) objArr[0]).intValue();
            Object obj = objArr[1];
            if (intValue == 3) {
                if (new JSONObject((String) obj).length() <= 0) {
                    XyUtil.doXycallBackResult(this.a, Integer.valueOf(-8), "no data");
                    return;
                }
                XyUtil.doXycallBackResult(this.a, Integer.valueOf(2), r1);
                return;
            }
            XyUtil.doXycallBackResult(this.a, Integer.valueOf(intValue), null, obj);
        } catch (Throwable th) {
            XyUtil.doXycallBackResult(this.a, Integer.valueOf(-10), th.getMessage());
        }
    }
}
