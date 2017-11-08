package cn.com.xy.sms.sdk.number;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.util.JsonUtil;
import org.json.JSONObject;

/* compiled from: Unknown */
final class n implements XyCallBack {
    private final /* synthetic */ JSONObject a;

    n(JSONObject jSONObject) {
        this.a = jSONObject;
    }

    public final void execute(Object... objArr) {
        if (objArr != null) {
            try {
                if (objArr.length >= 2 && ((Integer) objArr[0]).intValue() == 2) {
                    JsonUtil.JSONCombine(this.a, (JSONObject) objArr[2]);
                }
            } catch (Throwable th) {
            }
        }
    }
}
