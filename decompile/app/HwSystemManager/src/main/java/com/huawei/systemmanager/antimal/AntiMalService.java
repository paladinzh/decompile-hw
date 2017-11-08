package com.huawei.systemmanager.antimal;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import com.huawei.systemmanager.service.MainService.HsmService;
import com.huawei.systemmanager.util.HwLog;

public class AntiMalService implements HsmService {
    public static final String TAG = "AntiMalService";
    private static AntiMalService mInstance = null;
    private AntiMalManager mAntiMalManager = null;
    private int mCfgThermal = 40;
    private final Context mContext;
    private AntiMalConfig malConfig;

    public AntiMalService(Context ctx) {
        this.mContext = ctx;
    }

    public void init() {
        HwLog.i(TAG, "init.");
        this.mAntiMalManager = new AntiMalManager(this.mContext);
        boolean bInit = this.mAntiMalManager.initAntiMalware();
        this.malConfig = this.mAntiMalManager.getAntiMalConfig();
        if (!bInit) {
            this.mAntiMalManager = null;
            HwLog.d(TAG, "AntiMalware feature is closed.");
        }
        if (this.malConfig != null) {
            this.mCfgThermal = this.malConfig.mCfgThermalValue;
        }
        HwLog.i(TAG, "mCfgThermal = " + this.mCfgThermal);
    }

    public void onDestroy() {
    }

    public void onConfigurationChange(Configuration newConfig) {
    }

    public void onStartCommand(Intent intent, int flags, int startId) {
    }

    public AntiMalManager getAntiMalMgr() {
        return this.mAntiMalManager;
    }

    public static synchronized AntiMalService getInstance(Context context) {
        AntiMalService antiMalService;
        synchronized (AntiMalService.class) {
            if (mInstance == null) {
                mInstance = new AntiMalService(context);
            }
            antiMalService = mInstance;
        }
        return antiMalService;
    }

    public int getCfgThermal() {
        return this.mCfgThermal;
    }
}
