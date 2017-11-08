package com.huawei.watermark.manager.parse.util;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import com.huawei.hihealth.motion.HealthOpenSDK;
import com.huawei.hihealth.motion.IExecuteResult;
import com.huawei.watermark.ui.WMComponent;
import com.huawei.watermark.wmutil.WMResourceUtil;

public class WMHealthyService {
    private static int APP_ID = 4;
    public static final int FAILED = 1;
    public static final int SUCCESS = 0;
    protected static final String TAG = ("WM_" + WMHealthyService.class.getSimpleName());
    private static WMHealthyService sInstance;
    private WMComponent mComponent;
    private Context mContext;
    HealthDataQueryCallback mHealthDataQueryCallback = null;
    HealthOpenSDK mHealthOpenSDK = null;

    private class ExecuteCallback implements IExecuteResult {
        private ExecuteCallback() {
        }

        public void onSuccess(Object o) {
            Log.d(WMHealthyService.TAG, "ExecuteCallback : onSuccess ");
            if (o != null) {
                Log.d(WMHealthyService.TAG, "onSucess not null ");
                if (o instanceof Bundle) {
                    int todayStep = ((Bundle) o).getInt("step", 0);
                    int todayCalories = ((Bundle) o).getInt("carior", 0);
                    Log.d(WMHealthyService.TAG, String.format("Healthy onSuccess: (step:%d, calories:%d)", new Object[]{Integer.valueOf(todayStep), Integer.valueOf(todayCalories)}));
                    WMHealthyService.this.mHealthDataQueryCallback.onResult(todayStep, todayCalories);
                }
            }
        }

        public void onFailed(Object o) {
            Log.d(WMHealthyService.TAG, "ExecuteCallback : onFailed ");
        }

        public void onServiceException(Object o) {
            Log.d(WMHealthyService.TAG, "ExecuteCallback : onServiceException ");
        }
    }

    private class SimpleDataCallback implements IExecuteResult {
        private SimpleDataCallback() {
        }

        public void onSuccess(Object o) {
            if (WMHealthyService.this.mHealthOpenSDK != null) {
                WMHealthyService.this.mHealthOpenSDK.getTodaySportData(new ExecuteCallback());
            }
        }

        public void onFailed(Object o) {
        }

        public void onServiceException(Object o) {
        }
    }

    public static synchronized WMHealthyService getInstance(Context context) {
        WMHealthyService wMHealthyService;
        synchronized (WMHealthyService.class) {
            if (sInstance == null) {
                sInstance = new WMHealthyService(context);
            }
            wMHealthyService = sInstance;
        }
        return wMHealthyService;
    }

    private WMHealthyService(Context context) {
        this.mComponent = (WMComponent) ((Activity) context).findViewById(WMResourceUtil.getId(context, "wm_component"));
        APP_ID = this.mComponent.getWatermarkDelegate().getAppIDInHealthPlatform();
        this.mContext = context;
    }

    public void release() {
        if (this.mHealthOpenSDK != null) {
            this.mHealthOpenSDK.destorySDK();
            this.mHealthOpenSDK = null;
        }
    }

    public void init(HealthDataQueryCallback callback) {
        Log.d(TAG, "init HealthDataStore");
        this.mHealthDataQueryCallback = callback;
        this.mHealthOpenSDK = new HealthOpenSDK();
        this.mHealthOpenSDK.initSDK(this.mContext, new SimpleDataCallback(), this.mContext.getPackageName());
    }
}
