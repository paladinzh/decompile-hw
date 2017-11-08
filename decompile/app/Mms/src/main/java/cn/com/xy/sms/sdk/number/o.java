package cn.com.xy.sms.sdk.number;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.util.XyUtil;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
final class o implements XyCallBack {
    private final /* synthetic */ String a;
    private final /* synthetic */ XyCallBack b;

    o(String str, XyCallBack xyCallBack) {
        this.a = str;
        this.b = xyCallBack;
    }

    public final void execute(Object... objArr) {
        try {
            int intValue = ((Integer) objArr[0]).intValue();
            Object obj = objArr[2];
            if (intValue == 2) {
                JSONObject jSONObject = ((JSONArray) obj).getJSONObject(0);
                XyUtil.doXycallBackResult(this.b, Integer.valueOf(2), this.a, jSONObject);
                return;
            }
            k.c(this.a);
            XyUtil.doXycallBackResult(this.b, Integer.valueOf(intValue), this.a, obj);
            l.j(this.a);
        } catch (Throwable th) {
            XyUtil.doXycallBackResult(this.b, Integer.valueOf(-10), this.a, th.getMessage());
        } finally {
            l.j(this.a);
        }
    }
}
