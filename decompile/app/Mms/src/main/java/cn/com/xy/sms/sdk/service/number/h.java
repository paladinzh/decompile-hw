package cn.com.xy.sms.sdk.service.number;

import cn.com.xy.sms.sdk.db.entity.r;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.util.SdkCallBack;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
final class h implements SdkCallBack {
    private final /* synthetic */ String a;
    private final /* synthetic */ SdkCallBack b;

    h(String str, SdkCallBack sdkCallBack) {
        this.a = str;
        this.b = sdkCallBack;
    }

    public final void execute(Object... objArr) {
        JSONArray jSONArray;
        if (objArr != null && objArr.length > 1 && ((Integer) objArr[0]).intValue() == 3) {
            JSONArray jSONArray2 = new JSONArray((String) objArr[1]);
            if (jSONArray2.length() > 0) {
                for (int i = 0; i < jSONArray2.length(); i++) {
                    JSONObject jSONObject = (JSONObject) jSONArray2.get(i);
                    PhoneNumServeService.a.put(String.format("%s:%s", new Object[]{jSONObject.getString("phone"), this.a}), jSONObject);
                    r.a(r4, this.a, jSONObject, System.currentTimeMillis());
                }
            }
            jSONArray = jSONArray2;
        } else {
            jSONArray = null;
        }
        if (jSONArray != null && jSONArray.length() > 0) {
            XyUtil.doXycallBackResult(this.b, Integer.valueOf(102), jSONArray);
            return;
        }
        try {
            XyUtil.doXycallBackResult(this.b, Integer.valueOf(100), "null error");
        } catch (Throwable th) {
            XyUtil.doXycallBackResult(this.b, Integer.valueOf(100), "exception error");
        }
    }
}
