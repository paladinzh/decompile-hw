package com.android.server.location;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.location.gnsschrlog.CSegEVENT_HIGEO_STATISTIC_EVENT;
import com.android.server.location.gnsschrlog.CSubHiGeo_PDR_STEP_LENGTH;
import com.android.server.location.gnsschrlog.CSubHiGeo_RM_DOWNLOAD_STATUS;
import com.android.server.location.gnsschrlog.ChrLogBaseModel;
import com.android.server.location.gnsschrlog.GnssConnectivityLogManager;
import huawei.android.debug.HwDBGSwitchController;
import java.util.Date;

public class HwHiGeoStatisticEvent {
    private static final int DAILY_REPORT = 2;
    private static final boolean DEBUG = HwDBGSwitchController.getDBGSwitch();
    private static final int EXTEND_HIGEO_PDR_STEP_LENGTH = 28;
    private static final int EXTEND_HIGEO_RM_DOWNLOAD_STATUS = 27;
    public static final int HIGEO_PDR_STEP_LENGTH = 39;
    public static final int HIGEO_RM_DOWNLOAD_STATUS = 38;
    private static final int HIGEO_STATISTIC_EVENT = 76;
    private static final String TAG = "HwGpsLog_HiGeoStatisticEvent";
    private static final int TRIGGER_NOW = 1;
    private static final boolean VERBOSE = HwDBGSwitchController.getDBGSwitch();
    private static final String VERTICAL_ESC_SEPARATE = "\\|";
    private GpsSessionEvent mGpsSessionEvent;
    CSegEVENT_HIGEO_STATISTIC_EVENT mHigeoStatisticEvt = new CSegEVENT_HIGEO_STATISTIC_EVENT();

    static class PdrStepLength extends CSubHiGeo_PDR_STEP_LENGTH {
        public PdrStepLength(int ar, long distance, long stepCount) {
            this.iARState.setValue(ar);
            this.lGPS_distance.setValue(distance);
            this.lPDR_step_count.setValue(stepCount);
        }
    }

    static class RmDownloadStatus extends CSubHiGeo_RM_DOWNLOAD_STATUS {
        public RmDownloadStatus(String cityName, int action, int bsize, int asize, String successful, String failureCause, long downloadTime) {
            this.strCityName.setValue(cityName);
            this.iAction.setValue(action);
            this.iBeforeSize.setValue(bsize);
            this.iAfterSize.setValue(asize);
            this.strSuccessful.setValue(successful);
            this.strFailureCause.setValue(failureCause);
            this.lDownloadTime.setValue(downloadTime);
        }
    }

    HwHiGeoStatisticEvent(Context context) {
    }

    public void createNewHiGeoStatisticEvent() {
        Log.d(TAG, "createNewHiGeoStatisticEvent.");
        this.mHigeoStatisticEvt = new CSegEVENT_HIGEO_STATISTIC_EVENT();
    }

    public void setGpsSessionEvent(GpsSessionEvent event) {
        this.mGpsSessionEvent = event;
    }

    public void setStartTime(long time) {
        Log.d(TAG, "setStartTime : " + time);
        this.mHigeoStatisticEvt.lStartTime.setValue(time);
    }

    public void setProvider(String providermode) {
        Log.d(TAG, "setProvider : " + providermode);
        this.mHigeoStatisticEvt.strProviderMode.setValue(providermode);
    }

    public void setPosMode(int mode) {
        Log.d(TAG, "setPosMode : " + mode);
        this.mHigeoStatisticEvt.ucPosMode.setValue(mode);
    }

    public void writeSubevent(int event, String parameter) {
        Log.d(TAG, "writeSubevent event: " + event);
        switch (event) {
            case 27:
                setHiGeoRmDownloadStatus(parameter);
                return;
            case 28:
                setHiGeoPdrStepLength(parameter);
                return;
            default:
                Log.d(TAG, "other event: " + event);
                return;
        }
    }

    public void writeHiGeoStatisticErrorInfo(int errorcode) {
        Date date = new Date();
        if (this.mGpsSessionEvent != null) {
            this.mHigeoStatisticEvt.strLBSversion.setValue(this.mGpsSessionEvent.getLBSVersion());
        }
        this.mHigeoStatisticEvt.ucErrorCode.setValue(errorcode);
        ChrLogBaseModel cChrLogBaseModel = this.mHigeoStatisticEvt;
        Log.d(TAG, "writePosErrInfo: 76 ,ErrorCode:" + errorcode);
        GnssConnectivityLogManager.getInstance().reportAbnormalEventEx(cChrLogBaseModel, 14, 1, HIGEO_STATISTIC_EVENT, date, 1, errorcode);
    }

    public void setHiGeoRmDownloadStatus(String data) {
        Log.d(TAG, "[HiGeoCHRLog] setHiGeoRmDownloadStatus:" + data);
        if (!TextUtils.isEmpty(data)) {
            String[] values = data.split(VERTICAL_ESC_SEPARATE);
            if (values.length >= 7) {
                try {
                    this.mHigeoStatisticEvt.setCSubHiGeo_RM_DOWNLOAD_STATUS(new RmDownloadStatus(values[0], Integer.parseInt(values[1]), Integer.parseInt(values[2]), Integer.parseInt(values[3]), values[4], values[5], Long.parseLong(values[6])));
                } catch (NumberFormatException e) {
                    Log.e(TAG, "RM download error.");
                } catch (Exception e2) {
                    Log.e(TAG, "RM download error.");
                }
                writeHiGeoStatisticErrorInfo(38);
            }
        }
    }

    public void setHiGeoPdrStepLength(String data) {
        Log.d(TAG, "[HiGeoCHRLog] setHiGeoPdrStepLength:" + data);
        if (!TextUtils.isEmpty(data)) {
            String[] values = data.split(VERTICAL_ESC_SEPARATE);
            if (values.length >= 3) {
                try {
                    this.mHigeoStatisticEvt.setCSubHiGeo_PDR_STEP_LENGTH(new PdrStepLength(Integer.parseInt(values[0]), Long.parseLong(values[1]), Long.parseLong(values[2])));
                } catch (NumberFormatException e) {
                    Log.e(TAG, "pdr step length error.");
                } catch (Exception e2) {
                    Log.e(TAG, "pdr step length error.");
                }
                writeHiGeoStatisticErrorInfo(39);
            }
        }
    }
}
