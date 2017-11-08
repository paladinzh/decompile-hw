package cn.com.xy.sms.sdk.service.number;

import cn.com.xy.sms.sdk.db.entity.r;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.util.SdkCallBack;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import org.json.JSONObject;

/* compiled from: Unknown */
final class g implements Runnable {
    private final /* synthetic */ List a;
    private final /* synthetic */ String b;
    private final /* synthetic */ SdkCallBack c;

    g(List list, String str, SdkCallBack sdkCallBack) {
        this.a = list;
        this.b = str;
        this.c = sdkCallBack;
    }

    public final void run() {
        try {
            List arrayList = new ArrayList();
            arrayList.addAll(this.a);
            List arrayList2 = new ArrayList();
            for (Entry value : r.a(this.a, this.b, PhoneNumServeService.a).entrySet()) {
                JSONObject jSONObject = (JSONObject) value.getValue();
                arrayList2.add(jSONObject);
                arrayList.remove(jSONObject.optString("phone"));
            }
            if (arrayList2.size() > 0) {
                PhoneNumServeService.b(arrayList2, this.c);
                new StringBuilder("netQueryNums:").append(arrayList.toString());
            }
            if (arrayList.size() > 0) {
                PhoneNumServeService.a(arrayList, this.b, this.c);
            }
        } catch (Exception e) {
            XyUtil.doXycallBackResult(this.c, Integer.valueOf(100), "exception error");
        }
    }
}
