package cn.com.xy.sms.sdk.service.e;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.db.entity.a.f;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.net.util.j;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.XyUtil;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
final class e implements XyCallBack {
    private final /* synthetic */ String a;
    private final /* synthetic */ String b;
    private final /* synthetic */ XyCallBack c;
    private final /* synthetic */ int d;
    private final /* synthetic */ String e;
    private final /* synthetic */ String f;

    e(String str, String str2, XyCallBack xyCallBack, int i, String str3, String str4) {
        this.a = str;
        this.b = str2;
        this.c = xyCallBack;
        this.d = i;
        this.e = str3;
        this.f = str4;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void execute(Object... objArr) {
        int i;
        b.a(this.a, this.b, objArr);
        if (objArr != null && objArr[0].toString().equals("0") && objArr.length == 2) {
            Map b = j.b(objArr[1].toString());
            if (b != null) {
                JSONObject jSONObject = (JSONObject) b.get(b.keySet().iterator().next());
                String optString = jSONObject.optString("id");
                if ("0".equals(optString)) {
                    f.a(jSONObject);
                    try {
                        if (this.c != null) {
                            String pubInfoToJson;
                            if (this.d == 0) {
                                pubInfoToJson = JsonUtil.pubInfoToJson(jSONObject, this.a, this.b);
                                XyUtil.doXycallBackResult(this.c, Integer.valueOf(0), pubInfoToJson, this.e);
                            } else if (this.d == 1) {
                                JSONArray optJSONArray = jSONObject.optJSONArray("pubMenuInfolist");
                                if (optJSONArray != null) {
                                    if (optJSONArray.length() > 0) {
                                        pubInfoToJson = optJSONArray.toString();
                                        XyUtil.doXycallBackResult(this.c, Integer.valueOf(0), pubInfoToJson, this.e);
                                    }
                                }
                                pubInfoToJson = null;
                                XyUtil.doXycallBackResult(this.c, Integer.valueOf(0), pubInfoToJson, this.e);
                            }
                            i = 1;
                            f.a(this.a);
                            if (i == 0 && this.c != null) {
                                XyUtil.doXycallBackResult(this.c, Integer.valueOf(-1), null, this.e);
                            }
                        }
                        i = 0;
                        f.a(this.a);
                    } catch (Throwable th) {
                        f.a(this.a);
                    }
                    XyUtil.doXycallBackResult(this.c, Integer.valueOf(-1), null, this.e);
                } else if ("1".equals(optString)) {
                    NetUtil.QueryTokenRequest(this.f);
                }
            } else {
                return;
            }
        }
        i = 0;
        XyUtil.doXycallBackResult(this.c, Integer.valueOf(-1), null, this.e);
    }
}
