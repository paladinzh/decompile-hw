package cn.com.xy.sms.sdk.service.number;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.util.SdkCallBack;
import java.util.Set;
import org.json.JSONArray;

/* compiled from: Unknown */
final class b implements SdkCallBack {
    private final /* synthetic */ XyCallBack a;
    private final /* synthetic */ Set b;

    b(XyCallBack xyCallBack, Set set) {
        this.a = xyCallBack;
        this.b = set;
    }

    public final void execute(Object... objArr) {
        try {
            int intValue = ((Integer) objArr[0]).intValue();
            Object obj = objArr[1];
            if (intValue == 3) {
                if (a.b(new JSONArray((String) obj)).length() <= 0) {
                    XyUtil.doXycallBackResult(this.a, Integer.valueOf(-8), this.b, "no data");
                    return;
                }
                XyUtil.doXycallBackResult(this.a, Integer.valueOf(2), this.b, r0);
                return;
            }
            XyUtil.doXycallBackResult(this.a, Integer.valueOf(intValue), this.b, obj);
        } catch (Throwable th) {
            XyUtil.doXycallBackResult(this.a, Integer.valueOf(-10), this.b, th.getMessage());
        }
    }
}
