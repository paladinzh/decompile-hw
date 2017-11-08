package com.android.server.location;

import android.content.Context;
import android.os.SystemClock;
import android.provider.Settings.Global;
import android.util.IMonitor;
import android.util.IMonitor.EventStream;
import android.util.Log;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import huawei.android.debug.HwDBGSwitchController;
import java.util.Date;
import java.util.HashMap;

public class HwGnssDftManager {
    private static final boolean DEBUG = HwDBGSwitchController.getDBGSwitch();
    private static final int GPS_DAILY_CNT_REPORT = 71;
    private static final String GPS_LOG_ENABLE = "gps_log_enable";
    private static final int GPS_POS_ERROR_EVENT = 72;
    private static final int GPS_SESSION_EVENT = 73;
    private static final int IMONITOR_UPLOAD_MIN_SPAN = 86400000;
    private static final String TAG = "HwGnssLog_Imonitor";
    private static final HashMap<Integer, Long> mapErrorCodeTrigger = new HashMap();
    private Context mContext;

    public HwGnssDftManager(Context context) {
        this.mContext = context;
    }

    public void sendSessionDataToImonitor(int type, HwGnssDftGnssSessionParam mHwGnssDftGnssSessionParam) {
        Log.d(TAG, "IMonitor upload event " + type);
        if (type == GPS_SESSION_EVENT) {
            try {
                EventStream EventGpsSessionRpt = IMonitor.openEventStream(HwGnssDftEvent.DFT_GPS_SESSION_EVENT);
                if (EventGpsSessionRpt == null) {
                    Log.e(TAG, "EventStabilityStat is null.");
                    return;
                }
                int startTime = (int) (mHwGnssDftGnssSessionParam.startTime / 1000);
                int stopTime = (int) (mHwGnssDftGnssSessionParam.stopTime / 1000);
                int catchSvTime = (int) (mHwGnssDftGnssSessionParam.catchSvTime / 1000);
                if (DEBUG) {
                    Log.d(TAG, "START TIME : " + startTime + " , stopTime : " + stopTime + " , catchSvTime : " + catchSvTime);
                }
                EventGpsSessionRpt.setParam((short) 2, startTime);
                EventGpsSessionRpt.setParam((short) 7, mHwGnssDftGnssSessionParam.ttff);
                EventGpsSessionRpt.setParam((short) 20, stopTime);
                EventGpsSessionRpt.setParam((short) 6, catchSvTime);
                EventGpsSessionRpt.setParam((short) 17, mHwGnssDftGnssSessionParam.isGpsdResart ? 1 : 0);
                EventGpsSessionRpt.setParam((short) 21, mHwGnssDftGnssSessionParam.lostPosCnt);
                IMonitor.sendEvent(EventGpsSessionRpt);
                IMonitor.closeEventStream(EventGpsSessionRpt);
            } catch (Exception e) {
                Log.e(TAG, "uploadDFTEvent error.");
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "type is not match,return! ");
        }
    }

    public void sendDailyDataToImonitor(int type, HwGnssDftGnssDailyParam mHwGnssDftGnssDailyParam) {
        Log.d(TAG, "IMonitor upload event " + type);
        if (type == 71) {
            try {
                EventStream EventDailyRpt = IMonitor.openEventStream(HwGnssDftEvent.DFT_GPS_DAILY_CNT_REPORT);
                if (EventDailyRpt == null) {
                    Log.e(TAG, "EventStabilityStat is null.");
                    return;
                }
                EventDailyRpt.setParam((short) 2, mHwGnssDftGnssDailyParam.mDftGpsErrorUploadCnt);
                EventDailyRpt.setParam((short) 3, mHwGnssDftGnssDailyParam.mDftGpsRqCnt);
                EventDailyRpt.setParam((short) 0, mHwGnssDftGnssDailyParam.mDftNetworkTimeoutCnt);
                EventDailyRpt.setParam((short) 1, mHwGnssDftGnssDailyParam.mDftNetworkReqCnt);
                IMonitor.sendEvent(EventDailyRpt);
                IMonitor.closeEventStream(EventDailyRpt);
            } catch (Exception e) {
                Log.e(TAG, "uploadDFTEvent error.");
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "type is not match,return! ");
        }
    }

    public void sendExceptionDataToImonitor(int type, Date time, int errorCode) {
        switch (type) {
            case GPS_POS_ERROR_EVENT /*72*/:
                EventStream eStream;
                if (needTriggerChipsetLog(errorCode)) {
                    eStream = IMonitor.openEventStream(HwGnssDftEvent.DFT_GPS_CHIPSET_LOG_EVENT);
                    eStream.setParam((short) 0, errorCode);
                    eStream.setParam((short) 1, type);
                } else {
                    eStream = IMonitor.openEventStream(HwGnssDftEvent.DFT_GPS_SESSION_ERROR_EVENT);
                    eStream.setParam((short) 2, errorCode);
                }
                eStream.setTime(time.getTime());
                IMonitor.sendEvent(eStream);
                IMonitor.closeEventStream(eStream);
                return;
            default:
                Log.d(TAG, "unkown type");
                return;
        }
    }

    private boolean needTriggerChipsetLog(int errorCode) {
        if (isErrorCodeMatchToTrigger(errorCode) && isBetaClubChipLogSwitchOpen() && isErrorCodeHasTriggered(errorCode)) {
            return true;
        }
        return false;
    }

    private boolean isErrorCodeMatchToTrigger(int errorCode) {
        switch (errorCode) {
            case 1:
            case 2:
            case 5:
            case 9:
            case 10:
            case 13:
            case 14:
            case 15:
            case 23:
            case 24:
            case 27:
            case 28:
            case 29:
            case 30:
                return true;
            default:
                if (!DEBUG) {
                    return false;
                }
                Log.d(TAG, "errorcode is : " + errorCode + " ,no need to ctach chip log");
                return false;
        }
    }

    private boolean isBetaClubChipLogSwitchOpen() {
        try {
            String result = Global.getString(this.mContext.getContentResolver(), GPS_LOG_ENABLE);
            if (result == null || AppHibernateCst.INVALID_PKG.equals(result)) {
            }
            return Boolean.parseBoolean(result);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        } catch (Throwable th) {
            return false;
        }
    }

    private boolean isErrorCodeHasTriggered(int errorCode) {
        long nowTime = SystemClock.elapsedRealtime();
        long lastUploadTime;
        if (!mapErrorCodeTrigger.containsKey(Integer.valueOf(errorCode))) {
            lastUploadTime = nowTime;
            mapErrorCodeTrigger.put(Integer.valueOf(errorCode), Long.valueOf(nowTime));
            return true;
        } else if (nowTime - ((Long) mapErrorCodeTrigger.get(Integer.valueOf(errorCode))).longValue() <= 86400000) {
            return false;
        } else {
            lastUploadTime = nowTime;
            mapErrorCodeTrigger.put(Integer.valueOf(errorCode), Long.valueOf(nowTime));
            return true;
        }
    }
}
