package com.huawei.watermark.manager.parse.util;

import android.content.Context;
import com.huawei.watermark.wmutil.WMCollectionUtil;
import java.util.ArrayList;
import java.util.List;

public class WMHealthyReportService {
    private HealthDataQueryCallback mCallBack = new HealthDataQueryCallback() {
        public void onResult(int stepNum, int calories) {
            WMHealthyReportService.this.mCurrentStepValue = String.valueOf(stepNum);
            WMHealthyReportService.this.mCurrentCalories = String.valueOf(calories);
            WMHealthyReportService.this.reportCallbacks();
        }
    };
    private boolean mCanStart;
    private Context mContext;
    private String mCurrentCalories;
    private String mCurrentStepValue;
    private List<HealthUpdateCallback> mHealthUpdateCallbacks = new ArrayList();
    private Object mSyncObj = new Object();

    public interface HealthUpdateCallback {
        void onHealthReport(String str, String str2);
    }

    public WMHealthyReportService(Context mContext) {
        this.mContext = mContext;
    }

    public void start() {
        this.mCanStart = true;
        WMHealthyService.getInstance(this.mContext).init(this.mCallBack);
    }

    public void release() {
        this.mCanStart = false;
        this.mCurrentStepValue = "";
        this.mCurrentCalories = "";
        WMHealthyService.getInstance(this.mContext).release();
        synchronized (this.mSyncObj) {
            if (!WMCollectionUtil.isEmptyCollection(this.mHealthUpdateCallbacks)) {
                this.mHealthUpdateCallbacks.clear();
            }
        }
    }

    public void addHealthUpdateCallback(HealthUpdateCallback healthUpdateCallback) {
        synchronized (this.mSyncObj) {
            if (WMCollectionUtil.isEmptyCollection(this.mHealthUpdateCallbacks)) {
                this.mHealthUpdateCallbacks = new ArrayList();
            }
            this.mHealthUpdateCallbacks.add(healthUpdateCallback);
        }
        reportCallback(healthUpdateCallback);
    }

    private void reportCallback(HealthUpdateCallback healthUpdateCallback) {
        if (this.mCanStart) {
            healthUpdateCallback.onHealthReport(this.mCurrentStepValue, this.mCurrentCalories);
        }
    }

    private void reportCallbacks() {
        if (this.mCanStart) {
            synchronized (this.mSyncObj) {
                for (HealthUpdateCallback healthUpdateCallback : this.mHealthUpdateCallbacks) {
                    healthUpdateCallback.onHealthReport(this.mCurrentStepValue, this.mCurrentCalories);
                }
            }
        }
    }
}
