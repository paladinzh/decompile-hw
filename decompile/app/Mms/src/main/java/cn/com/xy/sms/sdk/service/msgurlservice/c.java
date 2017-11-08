package cn.com.xy.sms.sdk.service.msgurlservice;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.net.NewXyHttpRunnable;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;

/* compiled from: Unknown */
final class c implements XyCallBack {
    private final /* synthetic */ String a;
    private final /* synthetic */ boolean b;
    private final /* synthetic */ Map c;
    private final /* synthetic */ String d;
    private final /* synthetic */ HashMap e;
    private final /* synthetic */ String f;
    private final /* synthetic */ XyCallBack g;

    c(String str, boolean z, Map map, String str2, HashMap hashMap, String str3, XyCallBack xyCallBack) {
        this.a = str;
        this.b = z;
        this.c = map;
        this.d = str2;
        this.e = hashMap;
        this.f = str3;
        this.g = xyCallBack;
    }

    public final void execute(Object... objArr) {
        if (objArr != null) {
            String obj = objArr[0].toString();
            if (obj.equals("1")) {
                NetUtil.requestNewTokenAndPostRequestAgain(NetUtil.URL_VALIDITY, this.a, Constant.FIVE_MINUTES, this.b, false, true, this.c, this.d, this);
            } else if (obj.equals("2") || obj.equals(NewXyHttpRunnable.ERROR_CODE_SERVICE_ERR)) {
                if (this.e != null) {
                    this.e.put(this.f, Integer.valueOf(MsgUrlService.RESULT_SERVER_ERROR));
                    XyUtil.doXycallBackResult(this.g, Integer.valueOf(MsgUrlService.RESULT_SERVER_ERROR));
                }
            } else if (obj.equals("0") && objArr.length == 2) {
                obj = objArr[1].toString();
                if (!StringUtils.isNull(obj)) {
                    try {
                        JSONArray jSONArray = new JSONArray(obj);
                        int a = MsgUrlService.a(jSONArray);
                        if (this.e != null) {
                            this.e.put(this.f, Integer.valueOf(a));
                            XyUtil.doXycallBackResult(this.g, Integer.valueOf(a));
                        }
                        MsgUrlService.saveUrlResult(jSONArray, this.f, a, this.b);
                    } catch (Throwable th) {
                        if (this.e != null) {
                            this.e.put(this.f, Integer.valueOf(MsgUrlService.RESULT_SERVER_ERROR));
                            XyUtil.doXycallBackResult(this.g, Integer.valueOf(MsgUrlService.RESULT_SERVER_ERROR));
                        }
                    }
                } else if (this.e != null) {
                    this.e.put(this.f, Integer.valueOf(MsgUrlService.RESULT_SERVER_ERROR));
                    XyUtil.doXycallBackResult(this.g, Integer.valueOf(MsgUrlService.RESULT_SERVER_ERROR));
                }
            } else if (this.e != null) {
                this.e.put(this.f, Integer.valueOf(MsgUrlService.RESULT_SERVER_ERROR));
                XyUtil.doXycallBackResult(this.g, Integer.valueOf(MsgUrlService.RESULT_SERVER_ERROR));
            }
        } else if (this.e != null) {
            this.e.put(this.f, Integer.valueOf(MsgUrlService.RESULT_SERVER_ERROR));
            XyUtil.doXycallBackResult(this.g, Integer.valueOf(MsgUrlService.RESULT_SERVER_ERROR));
        }
    }
}
