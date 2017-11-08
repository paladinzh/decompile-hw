package cn.com.xy.sms.sdk.net;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.net.util.b;
import cn.com.xy.sms.sdk.net.util.h;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.util.XyUtil;
import org.json.JSONObject;

/* compiled from: Unknown */
final class g implements XyCallBack {
    private final /* synthetic */ XyCallBack a;

    g(XyCallBack xyCallBack) {
        this.a = xyCallBack;
    }

    public final void execute(Object... objArr) {
        int i;
        if (objArr != null) {
            try {
                if (objArr[0].toString().equals("0") && objArr.length == 2) {
                    JSONObject jSONObject = new JSONObject(objArr[1].toString());
                    String optString = jSONObject.optString(NetUtil.REQ_QUERY_TOEKN);
                    if (optString != null) {
                        SysParamEntityManager.setParam(Constant.NEWHTTPTOKEN, optString);
                        SysParamEntityManager.cacheMap.put(Constant.NEWHTTPTOKEN, optString);
                        byte[] a = b.a(jSONObject.optString("aesKey"));
                        SysParamEntityManager.setParam(Constant.AESKEY, h.a(a));
                        SysParamEntityManager.cacheMap.put(Constant.AESKEY, h.a(a));
                        if (this.a != null) {
                            try {
                                XyUtil.doXycallBackResult(this.a, "0", optString);
                                i = 0;
                            } catch (Exception e) {
                                i = 0;
                            }
                            if (i != 0 && this.a != null) {
                                XyUtil.doXycallBackResult(this.a, ThemeUtil.SET_NULL_STR);
                            }
                            return;
                        }
                    }
                }
            } catch (Exception e2) {
                i = 1;
            }
        }
        i = 1;
        if (i != 0) {
            XyUtil.doXycallBackResult(this.a, ThemeUtil.SET_NULL_STR);
        }
    }
}
