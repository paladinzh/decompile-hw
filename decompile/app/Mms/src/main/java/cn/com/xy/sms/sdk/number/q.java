package cn.com.xy.sms.sdk.number;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import org.json.JSONArray;

/* compiled from: Unknown */
final class q implements XyCallBack {
    q() {
    }

    public final void execute(Object... objArr) {
        try {
            if (((Integer) objArr[0]).intValue() == 2) {
                k.a((JSONArray) objArr[2]);
            }
        } catch (Throwable th) {
        }
    }
}
