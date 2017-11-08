package cn.com.xy.sms.sdk.service.d;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.n;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.net.NewXyHttpRunnable;
import cn.com.xy.sms.sdk.service.msgurlservice.MsgUrlService;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import org.json.JSONArray;

/* compiled from: Unknown */
final class b implements XyCallBack {
    private final /* synthetic */ String a;
    private final /* synthetic */ String b;

    b(String str, String str2) {
        this.a = str;
        this.b = str2;
    }

    public final void execute(Object... objArr) {
        if (objArr != null && objArr.length > 0) {
            String obj = objArr[0].toString();
            if (obj.equals("1")) {
                NetUtil.requestNewTokenAndPostRequestAgain(NetUtil.URL_PUB_NUMBER, this.a, Constant.FIVE_MINUTES, false, false, true, null, this.b, this);
            } else if (!obj.equals("2") && !obj.equals(NewXyHttpRunnable.ERROR_CODE_SERVICE_ERR) && obj.equals("0") && objArr.length == 2) {
                obj = objArr[1].toString();
                if (StringUtils.isNull(obj)) {
                    XyUtil.doXycallBackResult(null, null, Integer.valueOf(MsgUrlService.RESULT_SERVER_ERROR));
                    return;
                }
                try {
                    n.a(new JSONArray(obj));
                    n.a(true);
                } catch (Exception e) {
                    XyUtil.doXycallBackResult(null, null, Integer.valueOf(MsgUrlService.RESULT_SERVER_ERROR));
                }
            }
        }
    }
}
