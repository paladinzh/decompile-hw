package cn.com.xy.sms.sdk.number;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.util.b;
import org.json.JSONObject;

/* compiled from: Unknown */
final class u implements XyCallBack {
    private final /* synthetic */ XyCallBack a;

    u(XyCallBack xyCallBack) {
        this.a = xyCallBack;
    }

    public final void execute(Object... objArr) {
        if (objArr != null && objArr.length >= 2) {
            int intValue = ((Integer) objArr[0]).intValue();
            Object obj = objArr[1];
            if (intValue == 2) {
                JSONObject jSONObject = (JSONObject) obj;
                switch (jSONObject.optInt("status")) {
                    case SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE /*200*/:
                        r.a(this.a, jSONObject);
                        return;
                    default:
                        r.a(this.a, -8, b.a());
                        return;
                }
            }
            r.a(this.a, intValue, "query fail");
            return;
        }
        try {
            r.a(this.a, -10, "obj invalid");
        } catch (Throwable th) {
            r.a(this.a, -10, "query error");
        }
    }
}
