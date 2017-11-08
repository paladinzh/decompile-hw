package cn.com.xy.sms.sdk.number;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.XyUtil;

/* compiled from: Unknown */
final class e extends Handler {
    private /* synthetic */ d a;

    e(d dVar, Looper looper) {
        super(looper);
    }

    private static boolean a(Bundle bundle) {
        return (bundle != null && bundle.containsKey("latitude") && bundle.containsKey("longitude")) ? false : true;
    }

    public final void handleMessage(Message message) {
        Object obj = null;
        try {
            double d;
            double d2;
            Bundle data = message.getData();
            if (data != null && data.containsKey("latitude")) {
                if (!data.containsKey("longitude")) {
                }
                if (obj == null) {
                    d = data.getDouble("latitude");
                    d2 = data.getDouble("longitude");
                    if (d == 0.0d || d2 != 0.0d) {
                        XyUtil.setLoactionInfo(d, d2);
                        XyUtil.removeAreaCodeInfo();
                    }
                    if (d.e) {
                        d.e = false;
                        Thread.sleep(5000);
                        DuoquUtils.getSdkDoAction().getLocation(Constant.getContext(), d.d);
                    }
                    return;
                }
            }
            obj = 1;
            if (obj == null) {
                d = data.getDouble("latitude");
                d2 = data.getDouble("longitude");
                if (d == 0.0d) {
                }
                XyUtil.setLoactionInfo(d, d2);
                XyUtil.removeAreaCodeInfo();
            }
        } catch (Throwable th) {
        }
    }
}
