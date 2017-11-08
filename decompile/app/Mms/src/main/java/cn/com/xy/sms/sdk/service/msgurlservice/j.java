package cn.com.xy.sms.sdk.service.msgurlservice;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.util.XyUtil;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
final class j implements XyCallBack {
    private final /* synthetic */ String a;
    private final /* synthetic */ String b;
    private final /* synthetic */ int c;
    private final /* synthetic */ JSONObject d;
    private final /* synthetic */ XyCallBack e;

    j(String str, String str2, int i, JSONObject jSONObject, XyCallBack xyCallBack) {
        this.a = str;
        this.b = str2;
        this.c = i;
        this.d = jSONObject;
        this.e = xyCallBack;
    }

    public final void execute(Object... objArr) {
        JSONArray jSONArray = null;
        int i = MsgUrlService.RESULT_NOT_FIND;
        if (objArr != null && objArr.length > 1) {
            String obj = objArr[0].toString();
            if (obj.equals("1")) {
                NetUtil.requestNewTokenAndPostRequestAgain(NetUtil.URL_VALIDITY, this.a, Constant.FIVE_MINUTES, false, false, true, null, NetUtil.getToken(), this);
            } else if (obj.equals("0")) {
                JSONArray jSONArray2;
                int a;
                try {
                    jSONArray2 = new JSONArray(objArr[1].toString());
                    try {
                        a = MsgUrlService.a(jSONArray2);
                        try {
                            MsgUrlService.saveUrlResult(jSONArray2, this.b, a);
                        } catch (Throwable th) {
                        }
                    } catch (Throwable th2) {
                        a = MsgUrlService.RESULT_NOT_FIND;
                    }
                } catch (Throwable th3) {
                    jSONArray2 = null;
                    a = MsgUrlService.RESULT_NOT_FIND;
                }
                jSONArray = jSONArray2;
                i = a;
            }
        }
        try {
            JSONObject a2 = MsgUrlService.b(this.b, this.c, i, this.d, jSONArray);
            XyUtil.doXycallBackResult(this.e, a2);
        } catch (Throwable th4) {
        }
    }
}
