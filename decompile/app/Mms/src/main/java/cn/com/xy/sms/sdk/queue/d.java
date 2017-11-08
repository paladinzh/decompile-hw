package cn.com.xy.sms.sdk.queue;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import org.json.JSONObject;

/* compiled from: Unknown */
final class d implements XyCallBack {
    private final /* synthetic */ String a;
    private final /* synthetic */ String b;
    private final /* synthetic */ int c;

    d(String str, String str2, int i) {
        this.a = str;
        this.b = str2;
        this.c = i;
    }

    public final void execute(Object... objArr) {
        if (objArr != null) {
            try {
                if (objArr.length > 0) {
                    String obj = objArr[0].toString();
                    if (obj.equals("1")) {
                        NetUtil.requestNewTokenAndPostRequestAgain(NetUtil.URL_LOG_SERVICE, this.a, Constant.FIVE_MINUTES, false, false, true, null, this.b, this);
                    } else if (obj.equals("0") && objArr.length >= 2) {
                        obj = objArr[1].toString();
                        if (!StringUtils.isNull(obj) && new JSONObject(obj).optString("result").equals("0")) {
                            DexUtil.postCallback(Integer.valueOf(this.c), this.a);
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
    }
}
