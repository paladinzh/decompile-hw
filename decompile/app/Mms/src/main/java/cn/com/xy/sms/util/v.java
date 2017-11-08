package cn.com.xy.sms.util;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import org.json.JSONObject;

/* compiled from: Unknown */
final class v implements SdkCallBack {
    private final /* synthetic */ int a;
    private final /* synthetic */ SdkCallBack b;

    v(int i, SdkCallBack sdkCallBack) {
        this.a = i;
        this.b = sdkCallBack;
    }

    public final void execute(Object... objArr) {
        if (objArr != null) {
            try {
                String str = (String) objArr[0];
                if (StringUtils.isNull(str)) {
                    XyUtil.doXycallBack(this.b, null);
                    return;
                }
                JSONObject jSONObject = new JSONObject(str);
                XyCallBack xyCallBack;
                Object[] objArr2;
                if (this.a != 1) {
                    xyCallBack = this.b;
                    objArr2 = new Object[1];
                    objArr2[0] = PublicInfoParseManager.getJSONObject("name", jSONObject.optString("name"), NumberInfo.LOGO_NAME_KEY, jSONObject.optString("logoc"));
                    XyUtil.doXycallBackResult(xyCallBack, objArr2);
                    return;
                }
                xyCallBack = this.b;
                objArr2 = new Object[1];
                objArr2[0] = PublicInfoParseManager.getJSONObject("name", jSONObject.optString("name"), NumberInfo.LOGO_NAME_KEY, jSONObject.optString(NumberInfo.LOGO_KEY));
                XyUtil.doXycallBackResult(xyCallBack, objArr2);
            } catch (Throwable th) {
                XyUtil.doXycallBack(this.b, null);
            }
        }
    }
}
