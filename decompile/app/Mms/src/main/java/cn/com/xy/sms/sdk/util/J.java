package cn.com.xy.sms.sdk.util;

import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.a.a;
import cn.com.xy.sms.sdk.db.entity.h;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.util.SdkCallBack;

/* compiled from: Unknown */
final class j implements Runnable {
    private final /* synthetic */ SdkCallBack a;

    j(SdkCallBack sdkCallBack) {
        this.a = sdkCallBack;
    }

    public final void run() {
        try {
            h.e();
            a.b();
            String tempPARSE_PATH = Constant.getTempPARSE_PATH();
            String parse_path = Constant.getPARSE_PATH();
            f.d(tempPARSE_PATH);
            f.d(parse_path);
            f.a(Constant.getContext().getDir("outdex", 0));
            g.d();
            XyUtil.doXycallBack(this.a, "0");
        } catch (Throwable th) {
            XyUtil.doXycallBack(this.a, ThemeUtil.SET_NULL_STR);
        } finally {
            g.b = false;
        }
    }
}
