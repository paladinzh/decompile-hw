package cn.com.xy.sms.util;

import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import org.json.JSONObject;

/* compiled from: Unknown */
final class u implements SdkCallBack {
    private final /* synthetic */ int a;
    private final /* synthetic */ SdkCallBack b;

    u(int i, SdkCallBack sdkCallBack) {
        this.a = i;
        this.b = sdkCallBack;
    }

    public final void execute(Object... objArr) {
        if (objArr != null) {
            try {
                String str = (String) objArr[0];
                if (StringUtils.isNull(str)) {
                    XyUtil.doXycallBack(this.b, "");
                    return;
                }
                JSONObject jSONObject = new JSONObject(str);
                if (this.a != 1) {
                    XyUtil.doXycallBack(this.b, jSONObject.optString("logoc"));
                } else {
                    XyUtil.doXycallBack(this.b, jSONObject.optString(NumberInfo.LOGO_KEY));
                }
            } catch (Throwable th) {
                XyUtil.doXycallBack(this.b, "");
            }
        }
    }
}
