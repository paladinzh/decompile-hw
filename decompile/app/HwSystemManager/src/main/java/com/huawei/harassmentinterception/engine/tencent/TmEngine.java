package com.huawei.harassmentinterception.engine.tencent;

import android.content.Context;
import com.huawei.harassmentinterception.common.CommonObject.SmsIntentWrapper;
import com.huawei.harassmentinterception.engine.HwEngine;
import com.huawei.harassmentinterception.engine.HwEngine.EngineMode;
import com.huawei.harassmentinterception.engine.HwEngineManager.EngineId;
import com.huawei.harassmentinterception.update.IHwUpdateListener;
import com.huawei.harassmentinterception.update.UpdateHelper;
import com.huawei.systemmanager.sdk.tmsdk.TMSEngineFeature;
import com.huawei.systemmanager.util.HwLog;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.intelli_sms.IntelliSmsManager;

public class TmEngine extends HwEngine {
    private static final String TAG = "TmEngine";
    private IntelliSmsManager mIntelliSmsManager;
    private TmUpdateManager mTmUpdateManager;

    public TmEngine(Context context) {
        super(context);
    }

    public void initEngine(EngineMode mode, int nFlag) {
        HwLog.i(TAG, "initEngine: mode = " + mode + ", nFlag = " + nFlag);
        this.mMode = mode;
        if (EngineMode.OFF == this.mMode) {
            HwLog.i(TAG, "EngineMode OFF ");
        } else if (TMSEngineFeature.isSupportTMS()) {
            HwLog.i(TAG, "BLOCK_INTELLIGENT ON");
            try {
                this.mIntelliSmsManager = (IntelliSmsManager) ManagerCreatorC.getManager(IntelliSmsManager.class);
                this.mIntelliSmsManager.init();
            } catch (Exception e) {
                HwLog.e(TAG, "initEngine: Exception", e);
                this.mIntelliSmsManager = null;
            }
        } else {
            HwLog.w(TAG, "initEngine: TMS is not supported");
        }
    }

    public EngineId getEngineId() {
        return EngineId.TENCENT_TMS;
    }

    public void destroyEngine(int nFlag) {
        HwLog.i(TAG, "destroyEngine: nFlag = " + nFlag);
        if (this.mIntelliSmsManager != null) {
            this.mIntelliSmsManager.destroy();
            this.mIntelliSmsManager = null;
        }
        UpdateHelper.cancelAutoUpdateSchedule(this.mContext);
    }

    public boolean handleSms(SmsIntentWrapper smsIntentWrapper) {
        IntelliSmsManager intelliSmsManager = getIntellSmsManager();
        if (intelliSmsManager == null) {
            HwLog.w(TAG, "handleSms: Engine is not initialized");
            return false;
        }
        HwLog.i(TAG, "handleSms called, begint to check sms");
        try {
            return TmHelper.parseSmsCheckResult(intelliSmsManager.checkSms(TmHelper.getSmsEntity(smsIntentWrapper), Boolean.valueOf(false)));
        } catch (Exception e) {
            HwLog.e(TAG, "handleSms: Exception", e);
            return false;
        }
    }

    public int doUpdate(IHwUpdateListener hwUpdateListener) {
        if (this.mTmUpdateManager == null) {
            this.mTmUpdateManager = new TmUpdateManager(hwUpdateListener);
        }
        return this.mTmUpdateManager.doUpdate();
    }

    public int cancelUpdate() {
        if (this.mTmUpdateManager == null) {
            return 0;
        }
        return this.mTmUpdateManager.cancelUpdate();
    }

    private IntelliSmsManager getIntellSmsManager() {
        if (this.mIntelliSmsManager != null) {
            return this.mIntelliSmsManager;
        }
        HwLog.i(TAG, "try to get getIntellSmsManager");
        try {
            this.mIntelliSmsManager = (IntelliSmsManager) ManagerCreatorC.getManager(IntelliSmsManager.class);
            this.mIntelliSmsManager.init();
        } catch (Exception e) {
            HwLog.e(TAG, "initEngine: Exception", e);
            this.mIntelliSmsManager = null;
        }
        return this.mIntelliSmsManager;
    }
}
