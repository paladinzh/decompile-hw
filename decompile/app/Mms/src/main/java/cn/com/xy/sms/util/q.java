package cn.com.xy.sms.util;

import cn.com.xy.sms.sdk.util.XyUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: Unknown */
final class q implements SdkCallBack {
    private /* synthetic */ p a;
    private final /* synthetic */ Object[] b;
    private final /* synthetic */ SdkCallBack c;
    private final /* synthetic */ String d;

    q(p pVar, Object[] objArr, SdkCallBack sdkCallBack, String str) {
        this.b = objArr;
        this.c = sdkCallBack;
        this.d = str;
    }

    public final void execute(Object... objArr) {
        Object obj = null;
        try {
            int intValue = ((Integer) objArr[0]).intValue();
            switch (intValue) {
                case 0:
                case 1:
                    if (objArr[1] instanceof JSONArray) {
                        JSONArray jSONArray = (JSONArray) objArr[1];
                        break;
                    }
                    break;
            }
            int intValue2 = ((Integer) this.b[0]).intValue();
            switch (intValue2) {
                case 0:
                case 1:
                    JSONObject jSONObject = (JSONObject) this.b[1];
                    if (obj != null) {
                        jSONObject.put("NEW_ADACTION", obj);
                    }
                    if (intValue == 0 && intValue2 == 0) {
                        XyUtil.doXycallBackResult(this.c, this.b);
                        return;
                    }
                    XyUtil.doXycallBackResult(this.c, Integer.valueOf(1), jSONObject, this.b[2], this.b[3]);
                    return;
                default:
                    if (obj == null) {
                        XyUtil.doXycallBackResult(this.c, this.b);
                        return;
                    }
                    try {
                        new JSONObject().put("NEW_ADACTION", obj);
                    } catch (JSONException e) {
                        e.getMessage();
                    }
                    XyUtil.doXycallBackResult(this.c, Integer.valueOf(intValue), r3, this.d, Integer.valueOf(8));
                    return;
            }
        } catch (JSONException e2) {
            e2.getMessage();
        } catch (Throwable th) {
        }
    }
}
