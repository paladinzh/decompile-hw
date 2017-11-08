package cn.com.xy.sms.sdk.number;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import org.json.JSONArray;

/* compiled from: Unknown */
final class y implements XyCallBack {
    y() {
    }

    public final void execute(Object... objArr) {
        try {
            if (((Integer) objArr[0]).intValue() == 4) {
                k.b((JSONArray) objArr[2]);
            }
        } catch (Throwable th) {
        }
    }
}
