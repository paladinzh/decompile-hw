package cn.com.xy.sms.sdk.service.number;

import cn.com.xy.sms.sdk.db.entity.r;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.util.SdkCallBack;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
final class f implements SdkCallBack {
    private /* synthetic */ e a;
    private final /* synthetic */ String b;
    private final /* synthetic */ String c;
    private final /* synthetic */ String d;
    private final /* synthetic */ SdkCallBack e;

    f(e eVar, String str, String str2, String str3, SdkCallBack sdkCallBack) {
        this.b = str;
        this.c = str2;
        this.d = str3;
        this.e = sdkCallBack;
    }

    public final void execute(Object... objArr) {
        Object obj;
        if (objArr != null && objArr.length > 1 && ((Integer) objArr[0]).intValue() == 3) {
            JSONArray jSONArray = new JSONArray((String) objArr[1]);
            if (jSONArray.length() > 0) {
                Object obj2 = null;
                for (int i = 0; i < jSONArray.length(); i++) {
                    JSONObject jSONObject = (JSONObject) jSONArray.get(i);
                    String string = jSONObject.getString("phone");
                    if (string.equalsIgnoreCase(this.b)) {
                        JSONObject jSONObject2 = jSONObject;
                    }
                    PhoneNumServeService.a.put(this.c, jSONObject);
                    r.a(string, this.d, jSONObject, System.currentTimeMillis());
                }
                obj = obj2;
                if (obj != null) {
                    try {
                        XyUtil.doXycallBackResult(this.e, Integer.valueOf(100), null);
                    } catch (Throwable th) {
                        XyUtil.doXycallBackResult(this.e, Integer.valueOf(101), null);
                        return;
                    }
                }
                XyUtil.doXycallBackResult(this.e, Integer.valueOf(102), obj);
                return;
            }
        }
        obj = null;
        if (obj != null) {
            XyUtil.doXycallBackResult(this.e, Integer.valueOf(102), obj);
            return;
        }
        XyUtil.doXycallBackResult(this.e, Integer.valueOf(100), null);
    }
}
