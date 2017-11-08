package com.huawei.keyguard.data;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.huawei.hihealth.motion.HealthOpenSDK;
import com.huawei.hihealth.motion.ICommonReport;
import com.huawei.hihealth.motion.IExecuteResult;
import com.huawei.keyguard.events.AppHandler;
import com.huawei.keyguard.events.EventCenter;
import com.huawei.keyguard.events.EventCenter.IEventListener;
import com.huawei.keyguard.util.HwLog;
import java.text.NumberFormat;

public class SportInfo implements IEventListener {
    static IExecuteResult mExecuteRemoteCommandResultCallback = new IExecuteResult() {
        public void onSuccess(Object o) {
            HwLog.d("SportInfo", "mExecuteRemoteCommandResultCallback : onSuccess");
        }

        public void onFailed(Object o) {
            HwLog.w("SportInfo", "mExecuteRemoteCommandResultCallback : onFailed");
        }

        public void onServiceException(Object o) {
            HwLog.w("SportInfo", "mExecuteRemoteCommandResultCallback : onServiceException");
        }
    };
    static ICommonReport mTrackingDataCallback = new ICommonReport() {
        public void report(Bundle bundle) {
            TrackingStatus.getInst().setTrackingStatus(bundle);
            AppHandler.sendMessage(115);
        }
    };
    private static SportInfo sInst = new SportInfo();
    private boolean isAlreadyRegister = false;
    private boolean isShowingTopMusic = false;
    private boolean mHRValueDisplay = false;
    private HealthOpenSDK mHealthOpenSDK;
    IExecuteResult mInitHealthSDKResultCallback = new IExecuteResult() {
        public void onSuccess(Object o) {
            HwLog.d("SportInfo", "InitHealthSDK : onSuccess");
        }

        public void onFailed(Object o) {
            HwLog.w("SportInfo", "InitHealthSDK : onFailed");
        }

        public void onServiceException(Object o) {
            HwLog.w("SportInfo", "InitHealthSDK : onServiceException");
            SportInfo.this.unregisterTrackingReport();
            SportInfo.this.isAlreadyRegister = false;
            SportInfo.this.mHRValueDisplay = false;
            SportInfo.this.mRegisterRetryNum = 0;
            AppHandler.sendMessage(115, 1, 0, null);
        }
    };
    private int mRegisterRetryNum = 0;
    IExecuteResult mRegisterTrackingResultCallback = new IExecuteResult() {
        public void onSuccess(Object o) {
            HwLog.d("SportInfo", "TrackingDataCallback : onSuccess");
        }

        public void onFailed(Object o) {
            HwLog.w("SportInfo", "TrackingDataCallback : onFailed");
            if (SportInfo.this.mRegisterRetryNum > 2) {
                SportInfo.this.mRegisterRetryNum = 0;
                AppHandler.sendMessage(115, 1, 0, null);
                HwLog.w("SportInfo", "after retry still onFailed so don't show sport view");
                return;
            }
            SportInfo.this.registerTrackingReport();
        }

        public void onServiceException(Object o) {
            SportInfo.this.unregisterTrackingReport();
            SportInfo.this.isAlreadyRegister = false;
            SportInfo.this.mRegisterRetryNum = 0;
            AppHandler.sendMessage(115, 1, 0, null);
            HwLog.w("SportInfo", "TrackingDataCallback : onServiceException");
        }
    };

    public static class TrackingStatus {
        private static TrackingStatus sInst = new TrackingStatus();
        private String calorie;
        private String distance;
        private String duration;
        private int gps;
        private int heartRate;
        private String pace;
        private int sportState;

        public TrackingStatus() {
            clearTrackingData();
        }

        public void setTrackingStatus(Bundle bundle) {
            HwLog.w("SportInfo", "setTrackingStatus:" + bundle.getInt("duration", 0));
            this.gps = bundle.getInt("gps", 0);
            this.distance = SportInfo.distanceFormat(bundle.getInt("distance", 0), 2);
            this.duration = SportInfo.durationFormat(bundle.getInt("duration", 0));
            this.pace = SportInfo.paceFormat(bundle.getInt("pace", 0));
            this.heartRate = bundle.getInt("heartRate", 0);
            this.calorie = SportInfo.distanceFormat(bundle.getInt("calorie", 0), 0);
            this.sportState = bundle.getInt("sportState", 0);
        }

        public void clearTrackingData() {
            this.gps = 0;
            this.distance = SportInfo.distanceFormat(0, 2);
            this.duration = SportInfo.durationFormat(0);
            this.pace = SportInfo.paceFormat(0);
            this.heartRate = 0;
            this.calorie = SportInfo.distanceFormat(0, 1);
            this.sportState = 0;
        }

        public static TrackingStatus getInst() {
            return sInst;
        }

        public int getGps() {
            return this.gps;
        }

        public String getDistance() {
            return this.distance;
        }

        public String getDuration() {
            return this.duration;
        }

        public String getPace() {
            return this.pace;
        }

        public int getHeartRate() {
            return this.heartRate;
        }

        public String getCalorie() {
            return this.calorie;
        }

        public int getSportState() {
            return this.sportState;
        }
    }

    public boolean isHRValueDisplay() {
        return this.mHRValueDisplay;
    }

    public void setHRValueDisplay(boolean displayHRValue) {
        this.mHRValueDisplay = displayHRValue;
    }

    public static SportInfo getInst() {
        if (sInst == null) {
            sInst = new SportInfo();
        }
        return sInst;
    }

    public void initHealthSDK(Context context) {
        if (context == null) {
            HwLog.w("SportInfo", "context in null");
        }
        if (this.mHealthOpenSDK != null) {
            this.mHealthOpenSDK.destorySDK();
        }
        this.mHealthOpenSDK = new HealthOpenSDK();
        this.mHealthOpenSDK.initSDK(context, this.mInitHealthSDKResultCallback, "Keyguard");
        EventCenter.getInst().listen(32, this);
        HwLog.i("SportInfo", "initHealthSDK");
    }

    public boolean onReceive(Context context, Intent intent) {
        if (!"com.huawei.healthcloud.plugintrack.NOTIFY_SPORT_DATA".equals(intent.getAction())) {
            return false;
        }
        if (intent.getIntExtra("state", 0) != 1 || this.isAlreadyRegister) {
            this.mHRValueDisplay = false;
            TrackingStatus.getInst().clearTrackingData();
        } else {
            registerTrackingReport();
        }
        return true;
    }

    public void registerTrackingReport() {
        if (this.mHealthOpenSDK == null) {
            HwLog.e("SportInfo", "registerTrackingReport error");
            return;
        }
        try {
            boolean result = this.mHealthOpenSDK.registerTrackingReport(mTrackingDataCallback, this.mRegisterTrackingResultCallback);
            this.mRegisterRetryNum++;
            HwLog.i("SportInfo", "SportInfo registerTrackingReport");
            if (result) {
                this.isAlreadyRegister = true;
            } else {
                HwLog.i("SportInfo", "registerTrackingReport is failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unregisterTrackingReport() {
        if (this.mHealthOpenSDK == null) {
            HwLog.e("SportInfo", "unregisterTrackingReport error");
            return;
        }
        try {
            HwLog.i("SportInfo", "SportInfo unregisterTrackingReport");
            this.mHealthOpenSDK.unRegisterTrackingReport(mTrackingDataCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getTrackingState() {
        if (1 != getCurrentMode() || this.mHealthOpenSDK == null) {
            return -1;
        }
        return this.mHealthOpenSDK.getTrackingState();
    }

    public int getCurrentMode() {
        if (this.mHealthOpenSDK == null || !this.isAlreadyRegister) {
            return 0;
        }
        int mode = this.mHealthOpenSDK.getCurrentMode();
        HwLog.i("SportInfo", "getCurrentMode is " + mode);
        return mode;
    }

    public void executeRemoteCommand(int executeCode) {
        if (this.mHealthOpenSDK == null) {
            HwLog.i("SportInfo", "mHealthOpenSDK is null for execute Command");
            return;
        }
        HwLog.d("SportInfo", "executeRemoteCommand executeCode is " + executeCode);
        if (this.mHealthOpenSDK.executeRemoteCommand(executeCode, mExecuteRemoteCommandResultCallback) == -1) {
            HwLog.e("SportInfo", "executeRemoteCommand is failed");
        }
    }

    public void setTopMusicShowing(boolean isShowingTopMusic) {
        this.isShowingTopMusic = isShowingTopMusic;
    }

    private static String durationFormat(int time) {
        StringBuilder duration = new StringBuilder();
        int hour = time / 3600;
        int minute = (time % 3600) / 60;
        int second = (time % 3600) % 60;
        if (hour < 10) {
            duration.append("0");
        }
        duration.append(hour).append(":");
        if (minute < 10) {
            duration.append("0");
        }
        duration.append(minute).append(":");
        if (second < 10) {
            duration.append("0");
        }
        duration.append(second);
        return duration.toString();
    }

    private static String distanceFormat(int str, int num) {
        if (num == 0) {
            return String.valueOf(str / 1000);
        }
        float km = ((float) str) / 1000.0f;
        NumberFormat nf = NumberFormat.getInstance();
        if (num > 0) {
            nf.setMaximumFractionDigits(num);
            nf.setMinimumFractionDigits(num);
        }
        return nf.format((double) km);
    }

    private static String paceFormat(int pace) {
        if (pace == 0) {
            return "--";
        }
        return String.valueOf(pace / 60) + "'" + (pace % 60) + "\"";
    }
}
