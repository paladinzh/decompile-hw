package com.huawei.harassmentinterception.engine;

import android.content.Context;
import com.huawei.harassmentinterception.common.CommonObject.SmsIntentWrapper;
import com.huawei.harassmentinterception.engine.HwEngine.EngineMode;
import com.huawei.harassmentinterception.update.IHwUpdateListener;
import com.huawei.systemmanager.util.HwLog;

public class HwEngineCaller {
    public static final String TAG = HwEngineCaller.class.getSimpleName();
    private Context mContext = null;

    public HwEngineCaller(Context context) {
        this.mContext = context;
    }

    public boolean handleSms(SmsIntentWrapper smsIntentWrapper) {
        HwEngine engine = HwEngineManager.getEngine(this.mContext);
        if (engine != null) {
            return engine.handleSms(smsIntentWrapper);
        }
        HwLog.w(TAG, "handleSms: Fail to get engine ,skip");
        return false;
    }

    public void onSwitchIn(int nFlag) {
        HwEngine engine = HwEngineManager.getEngine(this.mContext);
        if (engine == null) {
            HwLog.e(TAG, "onSwitchIn: Fail to get engine");
        } else {
            engine.initEngine(EngineMode.INTELLIGENT, 0);
        }
    }

    public void onSwitchOut(int nFlag) {
        HwEngineManager.destroyEngine(nFlag);
    }

    public int doUpdate(IHwUpdateListener hwUpdateListener) {
        HwEngine engine = HwEngineManager.getEngine(this.mContext);
        if (engine != null) {
            return engine.doUpdate(hwUpdateListener);
        }
        HwLog.e(TAG, "doUpdate: Fail to get engine");
        return 4;
    }

    public int cancelUpdate() {
        HwEngine engine = HwEngineManager.getEngine(this.mContext);
        if (engine != null) {
            return engine.cancelUpdate();
        }
        HwLog.e(TAG, "doUpdate: Fail to get engine");
        return 0;
    }
}
