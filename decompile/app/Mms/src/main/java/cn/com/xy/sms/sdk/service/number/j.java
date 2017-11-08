package cn.com.xy.sms.sdk.service.number;

import cn.com.xy.sms.sdk.db.entity.r;
import cn.com.xy.sms.util.SdkCallBack;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
final class j implements SdkCallBack {
    private final /* synthetic */ List a;

    j(List list) {
        this.a = list;
    }

    public final void execute(Object... objArr) {
        if (objArr != null) {
            try {
                if (objArr.length > 1 && ((Integer) objArr[0]).intValue() == 3) {
                    JSONArray jSONArray = new JSONArray((String) objArr[1]);
                    if (jSONArray.length() > 0) {
                        for (int i = 0; i < jSONArray.length(); i++) {
                            JSONObject jSONObject = (JSONObject) jSONArray.get(i);
                            PhoneNumServeService.a.put(String.format("%s:%s", new Object[]{jSONObject.getString("phone"), jSONObject.getString("area")}), jSONObject);
                            r.a(r3, r4, jSONObject, System.currentTimeMillis());
                        }
                    }
                    r.a(this.a, System.currentTimeMillis());
                }
            } catch (Throwable th) {
            }
        }
    }
}
