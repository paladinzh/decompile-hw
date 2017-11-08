package cn.com.xy.sms.sdk.db;

import cn.com.xy.sms.sdk.constant.Constant;

/* compiled from: Unknown */
final class o extends Thread {
    o() {
    }

    public final void run() {
        try {
            TrainManager.importTrainData(Constant.getContext());
        } catch (Throwable th) {
            th.getMessage();
        }
    }
}
