package com.android.server.location;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import com.android.server.location.gnsschrlog.CSegEVENT_GPS_DAILY_CNT_REPORT;
import com.android.server.location.gnsschrlog.ChrLogBaseModel;
import com.android.server.location.gnsschrlog.GnssChrCommonInfo;
import com.android.server.location.gnsschrlog.GnssConnectivityLogManager;
import com.android.server.location.gnsschrlog.GnssLogManager;
import huawei.android.debug.HwDBGSwitchController;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;

public class GpsDailyReportEvent {
    private static final long COMM_UPLOAD_MIN_SPAN = 86400000;
    private static final int DAILY_REPORT = 2;
    private static final boolean DEBUG = HwDBGSwitchController.getDBGSwitch();
    private static final int GPS_DAILY_CNT_REPORT = 71;
    public static final int GPS_DAILY_CNT_REPORT_FAILD = 25;
    private static final String GPS_STATE_CNT_FILE = "/data/misc/gps/HwGpsStateCnt.txt";
    private static final String KEY_GPS_ERR_UPLOAD_CNT = "key_gps_err_upload_cnt:";
    private static final String KEY_GPS_REQ_CNT = "key_gps_req_cnt:";
    private static final String KEY_GPS_RESTART_CNT = "key_gps_restart_cnt:";
    private static final String KEY_GPS_RF_GOOD_STATUS = "key_gps_rf_good_status:";
    private static final String KEY_GPS_RF_VALIED_STATUS = "key_gps_rf_valied_status:";
    private static final String KEY_NETWORK_REQ_CNT = "key_network_req_cnt:";
    private static final String KEY_NETWORK_TIMEOUT_CNT = "key_network_timeout_cnt:";
    private static final String KEY_NTP_FLASH_SUCC_CNT = "key_ntp_flash_succ_cnt:";
    private static final String KEY_NTP_MOBILE_FAIL_CNT = "key_ntp_mobile_fail_cnt:";
    private static final String KEY_NTP_REQ_CNT = "key_ntp_req_cnt:";
    private static final String KEY_NTP_WIFI_FAIL_CNT = "key_ntp_wifi_fail_cnt:";
    private static final String KEY_TIMESTAMP = "key_timestamp:";
    private static final String KEY_XTRA_DLOAD_CNT = "key_xtra_dload_cnt:";
    private static final String KEY_XTRA_REQ_CNT = "key_xtra_req_cnt:";
    private static final int MAX_NUM_TRIGGER_BETA = 5;
    private static final int MAX_NUM_TRIGGER_COMM = 1;
    private static final long MIN_PERIOD_TRIGGER_BETA = 86400000;
    private static final long MIN_PERIOD_TRIGGER_COMM = 604800000;
    private static int MIN_WRITE_STAT_SPAN = 1000;
    private static final String SEPARATOR_KEY = "\n";
    private static final String TAG = "HwGpsLog_DailyRptEvent";
    private static final int TRIGGER_NOW = 1;
    private static final boolean VERBOSE = HwDBGSwitchController.getDBGSwitch();
    private static final Object mLock = new Object();
    File logFile = new File(GPS_STATE_CNT_FILE);
    private int mAgpsConnCnt = 0;
    private int mAgpsConnFailedCnt = 0;
    protected GnssChrCommonInfo mChrComInfo = new GnssChrCommonInfo();
    private Context mContext;
    private int mGpsErrorUploadCnt = 0;
    private int mGpsReqCnt = 0;
    private boolean mIsCn0Good;
    private boolean mIsCn0Valied;
    private int mNetworkReqCnt = 0;
    private int mNetworkTimeOutCnt = 0;
    private int mNtpFlashSuccCnt = 0;
    private int mNtpMobileFailCnt = 0;
    private int mNtpReqCnt = 0;
    private int mNtpWifiFailCnt = 0;
    private long mTimestamp = 0;
    private long mWriteStatTimestamp = 0;
    private int mXtraDloadCnt = 0;
    private int mXtraReqCnt = 0;

    GpsDailyReportEvent(Context context) {
        this.mContext = context;
    }

    public void updateCn0Status(boolean isCn0Valied, boolean IsCn0Good) {
        this.mIsCn0Valied = isCn0Valied;
        this.mIsCn0Good = IsCn0Good;
        saveGpsDailyRptInfo(false, true);
    }

    public void updateGpsPosReqCnt(boolean success) {
        if (success) {
            this.mGpsReqCnt++;
        } else if (!success) {
            this.mGpsErrorUploadCnt++;
        }
        saveGpsDailyRptInfo(false, true);
    }

    public void updateXtraDownLoadCnt(boolean reqXtraCnt, boolean xtraSuccCnt) {
        if (reqXtraCnt) {
            this.mXtraDloadCnt++;
        }
        if (xtraSuccCnt) {
            this.mXtraReqCnt++;
        }
        saveGpsDailyRptInfo(false, true);
    }

    public void updateNtpDownLoadCnt(boolean reqNtpCnt, boolean NtpSuccCnt, boolean wifi, boolean datacall) {
        if (reqNtpCnt) {
            this.mNtpReqCnt++;
        }
        if (NtpSuccCnt) {
            this.mNtpFlashSuccCnt++;
        }
        if (wifi) {
            this.mNtpWifiFailCnt++;
        }
        if (datacall) {
            this.mNtpMobileFailCnt++;
        }
        saveGpsDailyRptInfo(false, true);
    }

    public void updateNetworkReqCnt(boolean networkFailState, boolean networkReqState) {
        if (networkFailState) {
            this.mNetworkTimeOutCnt++;
        }
        if (networkReqState) {
            this.mNetworkReqCnt++;
        }
        saveGpsDailyRptInfo(false, true);
    }

    public void updateAgpsReqCnt(boolean agpsConnFailCnt, boolean agpsReqCnt) {
        if (agpsConnFailCnt) {
            this.mAgpsConnFailedCnt++;
        }
        if (agpsReqCnt) {
            this.mAgpsConnCnt++;
        }
        saveGpsDailyRptInfo(false, true);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void saveGpsDailyRptInfo(boolean flushNow, boolean triggerNow) {
        if (DEBUG) {
            Log.d(TAG, "saveGpsDailyRptInfo , flushNow is : " + flushNow + " ,triggerNow is : " + triggerNow);
        }
        if (!createLogFile()) {
            Log.e(TAG, "create file HwGpsStateCnt.txt filed");
        }
        synchronized (mLock) {
            long now = SystemClock.elapsedRealtime();
            if (flushNow || now - this.mWriteStatTimestamp >= ((long) MIN_WRITE_STAT_SPAN)) {
                this.mWriteStatTimestamp = now;
                if (0 == this.mTimestamp) {
                    this.mTimestamp = System.currentTimeMillis();
                }
            } else {
                return;
            }
        }
        DataOutputStream dataOutputStream = out;
        if (triggerNow) {
            triggerUploadIfNeed();
        }
    }

    private boolean createLogFile() {
        try {
            File directory = new File("/data/misc/gps");
            if (!directory.exists()) {
                Log.e(TAG, "create dir sdcard/gps ,status: " + directory.mkdirs());
            }
            if (!this.logFile.exists()) {
                Log.d(TAG, "create /data/gps/HwGpsStateCnt.txt,status : " + this.logFile.createNewFile());
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "create /data/gps/HwGpsStateCnt.txt failed");
            return false;
        }
    }

    public void reloadGpsDailyRptInfo() {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:com.android.server.location.GpsDailyReportEvent.reloadGpsDailyRptInfo():void. bs: [B:14:0x0057, B:26:0x0086]
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:86)
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:63)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:58)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r10 = this;
        r6 = "HwGpsLog_DailyRptEvent";
        r7 = "reloadGpsDailyRptInfo";
        android.util.Log.d(r6, r7);
        r2 = 0;
        r6 = r10.createLogFile();	 Catch:{ EOFException -> 0x02bb, Exception -> 0x02be }
        if (r6 != 0) goto L_0x0019;	 Catch:{ EOFException -> 0x02bb, Exception -> 0x02be }
    L_0x0010:
        r6 = "HwGpsLog_DailyRptEvent";	 Catch:{ EOFException -> 0x02bb, Exception -> 0x02be }
        r7 = "create file HwGpsStateCnt.txt filed";	 Catch:{ EOFException -> 0x02bb, Exception -> 0x02be }
        android.util.Log.e(r6, r7);	 Catch:{ EOFException -> 0x02bb, Exception -> 0x02be }
    L_0x0019:
        r3 = new java.io.DataInputStream;	 Catch:{ EOFException -> 0x02bb, Exception -> 0x02be }
        r6 = new java.io.BufferedInputStream;	 Catch:{ EOFException -> 0x02bb, Exception -> 0x02be }
        r7 = new java.io.FileInputStream;	 Catch:{ EOFException -> 0x02bb, Exception -> 0x02be }
        r8 = "/data/misc/gps/HwGpsStateCnt.txt";	 Catch:{ EOFException -> 0x02bb, Exception -> 0x02be }
        r7.<init>(r8);	 Catch:{ EOFException -> 0x02bb, Exception -> 0x02be }
        r6.<init>(r7);	 Catch:{ EOFException -> 0x02bb, Exception -> 0x02be }
        r3.<init>(r6);	 Catch:{ EOFException -> 0x02bb, Exception -> 0x02be }
    L_0x002b:
        r4 = r3.readUTF();	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = "key_timestamp:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = r4.startsWith(r6);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        if (r6 == 0) goto L_0x0060;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x0038:
        r6 = "key_timestamp:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r4.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = "\n";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r5.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = java.lang.Long.parseLong(r5);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r10.mTimestamp = r6;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        goto L_0x002b;
    L_0x0053:
        r1 = move-exception;
        r2 = r3;
    L_0x0055:
        if (r2 == 0) goto L_0x005a;
    L_0x0057:
        r2.close();	 Catch:{ Exception -> 0x0261 }
    L_0x005a:
        if (r2 == 0) goto L_0x005f;
    L_0x005c:
        r2.close();	 Catch:{ Exception -> 0x0281 }
    L_0x005f:
        return;
    L_0x0060:
        r6 = "key_xtra_req_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = r4.startsWith(r6);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        if (r6 == 0) goto L_0x00c2;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x0069:
        r6 = "key_xtra_req_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r4.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = "\n";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r5.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = java.lang.Integer.parseInt(r5);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r10.mXtraReqCnt = r6;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        goto L_0x002b;
    L_0x0084:
        r0 = move-exception;
        r2 = r3;
    L_0x0086:
        r6 = "HwGpsLog_DailyRptEvent";	 Catch:{ all -> 0x027e }
        r7 = new java.lang.StringBuilder;	 Catch:{ all -> 0x027e }
        r7.<init>();	 Catch:{ all -> 0x027e }
        r8 = "readGPSCHRStat: No config file, revert to default";	 Catch:{ all -> 0x027e }
        r7 = r7.append(r8);	 Catch:{ all -> 0x027e }
        r7 = r7.append(r0);	 Catch:{ all -> 0x027e }
        r7 = r7.toString();	 Catch:{ all -> 0x027e }
        android.util.Log.e(r6, r7);	 Catch:{ all -> 0x027e }
        if (r2 == 0) goto L_0x005f;
    L_0x00a2:
        r2.close();	 Catch:{ Exception -> 0x00a6 }
        goto L_0x005f;
    L_0x00a6:
        r0 = move-exception;
        r6 = "HwGpsLog_DailyRptEvent";
        r7 = new java.lang.StringBuilder;
        r7.<init>();
        r8 = "readGPSCHRStat: Error closing file";
        r7 = r7.append(r8);
        r7 = r7.append(r0);
        r7 = r7.toString();
        android.util.Log.e(r6, r7);
        goto L_0x005f;
    L_0x00c2:
        r6 = "key_xtra_dload_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = r4.startsWith(r6);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        if (r6 == 0) goto L_0x00ef;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x00cb:
        r6 = "key_xtra_dload_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r4.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = "\n";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r5.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = java.lang.Integer.parseInt(r5);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r10.mXtraDloadCnt = r6;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        goto L_0x002b;
    L_0x00e7:
        r6 = move-exception;
        r2 = r3;
    L_0x00e9:
        if (r2 == 0) goto L_0x00ee;
    L_0x00eb:
        r2.close();	 Catch:{ Exception -> 0x029e }
    L_0x00ee:
        throw r6;
    L_0x00ef:
        r6 = "key_ntp_req_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = r4.startsWith(r6);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        if (r6 == 0) goto L_0x0114;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x00f8:
        r6 = "key_ntp_req_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r4.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = "\n";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r5.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = java.lang.Integer.parseInt(r5);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r10.mNtpReqCnt = r6;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        goto L_0x002b;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x0114:
        r6 = "key_ntp_flash_succ_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = r4.startsWith(r6);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        if (r6 == 0) goto L_0x0139;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x011d:
        r6 = "key_ntp_flash_succ_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r4.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = "\n";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r5.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = java.lang.Integer.parseInt(r5);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r10.mNtpFlashSuccCnt = r6;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        goto L_0x002b;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x0139:
        r6 = "key_ntp_wifi_fail_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = r4.startsWith(r6);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        if (r6 == 0) goto L_0x015e;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x0142:
        r6 = "key_ntp_wifi_fail_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r4.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = "\n";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r5.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = java.lang.Integer.parseInt(r5);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r10.mNtpWifiFailCnt = r6;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        goto L_0x002b;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x015e:
        r6 = "key_ntp_mobile_fail_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = r4.startsWith(r6);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        if (r6 == 0) goto L_0x0183;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x0167:
        r6 = "key_ntp_mobile_fail_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r4.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = "\n";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r5.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = java.lang.Integer.parseInt(r5);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r10.mNtpMobileFailCnt = r6;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        goto L_0x002b;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x0183:
        r6 = "key_network_timeout_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = r4.startsWith(r6);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        if (r6 == 0) goto L_0x01a8;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x018c:
        r6 = "key_network_timeout_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r4.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = "\n";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r5.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = java.lang.Integer.parseInt(r5);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r10.mNetworkTimeOutCnt = r6;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        goto L_0x002b;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x01a8:
        r6 = "key_network_req_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = r4.startsWith(r6);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        if (r6 == 0) goto L_0x01cd;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x01b1:
        r6 = "key_network_req_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r4.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = "\n";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r5.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = java.lang.Integer.parseInt(r5);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r10.mNetworkReqCnt = r6;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        goto L_0x002b;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x01cd:
        r6 = "key_gps_err_upload_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = r4.startsWith(r6);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        if (r6 == 0) goto L_0x01f2;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x01d6:
        r6 = "key_gps_err_upload_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r4.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = "\n";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r5.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = java.lang.Integer.parseInt(r5);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r10.mGpsErrorUploadCnt = r6;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        goto L_0x002b;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x01f2:
        r6 = "key_gps_req_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = r4.startsWith(r6);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        if (r6 == 0) goto L_0x0217;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x01fb:
        r6 = "key_gps_req_cnt:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r4.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = "\n";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r5.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = java.lang.Integer.parseInt(r5);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r10.mGpsReqCnt = r6;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        goto L_0x002b;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x0217:
        r6 = "key_gps_rf_good_status:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = r4.startsWith(r6);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        if (r6 == 0) goto L_0x023c;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x0220:
        r6 = "key_gps_rf_good_status:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r4.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = "\n";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r5.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = java.lang.Boolean.parseBoolean(r5);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r10.mIsCn0Good = r6;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        goto L_0x002b;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x023c:
        r6 = "key_gps_rf_valied_status:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = r4.startsWith(r6);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        if (r6 == 0) goto L_0x002b;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
    L_0x0245:
        r6 = "key_gps_rf_valied_status:";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r4.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = "\n";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r7 = "";	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r5 = r5.replace(r6, r7);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r6 = java.lang.Boolean.parseBoolean(r5);	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        r10.mIsCn0Valied = r6;	 Catch:{ EOFException -> 0x0053, Exception -> 0x0084, all -> 0x00e7 }
        goto L_0x002b;
    L_0x0261:
        r0 = move-exception;
        r6 = "HwGpsLog_DailyRptEvent";	 Catch:{ all -> 0x027e }
        r7 = new java.lang.StringBuilder;	 Catch:{ all -> 0x027e }
        r7.<init>();	 Catch:{ all -> 0x027e }
        r8 = "readGPSCHRStat: Error reading file:";	 Catch:{ all -> 0x027e }
        r7 = r7.append(r8);	 Catch:{ all -> 0x027e }
        r7 = r7.append(r0);	 Catch:{ all -> 0x027e }
        r7 = r7.toString();	 Catch:{ all -> 0x027e }
        android.util.Log.e(r6, r7);	 Catch:{ all -> 0x027e }
        goto L_0x005a;
    L_0x027e:
        r6 = move-exception;
        goto L_0x00e9;
    L_0x0281:
        r0 = move-exception;
        r6 = "HwGpsLog_DailyRptEvent";
        r7 = new java.lang.StringBuilder;
        r7.<init>();
        r8 = "readGPSCHRStat: Error closing file";
        r7 = r7.append(r8);
        r7 = r7.append(r0);
        r7 = r7.toString();
        android.util.Log.e(r6, r7);
        goto L_0x005f;
    L_0x029e:
        r0 = move-exception;
        r7 = "HwGpsLog_DailyRptEvent";
        r8 = new java.lang.StringBuilder;
        r8.<init>();
        r9 = "readGPSCHRStat: Error closing file";
        r8 = r8.append(r9);
        r8 = r8.append(r0);
        r8 = r8.toString();
        android.util.Log.e(r7, r8);
        goto L_0x00ee;
    L_0x02bb:
        r1 = move-exception;
        goto L_0x0055;
    L_0x02be:
        r0 = move-exception;
        goto L_0x0086;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.GpsDailyReportEvent.reloadGpsDailyRptInfo():void");
    }

    private void triggerUploadIfNeed() {
        if (DEBUG) {
            Log.d(TAG, "triggerUploadIfNeed ");
        }
        long now = System.currentTimeMillis();
        long minPeriod = GnssLogManager.getInstance().isCommercialUser() ? MIN_PERIOD_TRIGGER_COMM : 86400000;
        if (DEBUG) {
            Log.d(TAG, "minPeriod : " + minPeriod + " ,now : " + now + " ,mTimestamp : " + this.mTimestamp);
        }
        if (now - this.mTimestamp >= minPeriod && hasDataToTrigger()) {
            writeDailyRptToImonitor();
            writeDailyRptInfo(71);
            this.mTimestamp = now;
            clearDailyRptInfo();
        }
    }

    private boolean hasDataToTrigger() {
        if (((long) ((((this.mXtraReqCnt + this.mNtpReqCnt) + this.mNetworkReqCnt) + this.mGpsReqCnt) + this.mAgpsConnCnt)) > 0) {
            return true;
        }
        return false;
    }

    private void clearDailyRptInfo() {
        this.mXtraDloadCnt = 0;
        this.mXtraReqCnt = 0;
        this.mNtpReqCnt = 0;
        this.mNtpWifiFailCnt = 0;
        this.mNtpMobileFailCnt = 0;
        this.mNtpFlashSuccCnt = 0;
        this.mNetworkTimeOutCnt = 0;
        this.mNetworkReqCnt = 0;
        this.mGpsErrorUploadCnt = 0;
        this.mGpsReqCnt = 0;
        this.mIsCn0Good = false;
        this.mIsCn0Valied = false;
        saveGpsDailyRptInfo(true, false);
    }

    private void writeDailyRptInfo(int type) {
        Date date = new Date();
        ChrLogBaseModel cCSegEVENT_GPS_DAILY_CNT_REPORT = new CSegEVENT_GPS_DAILY_CNT_REPORT();
        cCSegEVENT_GPS_DAILY_CNT_REPORT.ucErrorCode.setValue(25);
        cCSegEVENT_GPS_DAILY_CNT_REPORT.ucCardIndex.setValue(this.mChrComInfo.getCardIndex());
        cCSegEVENT_GPS_DAILY_CNT_REPORT.tmTimeStamp.setValue(date);
        cCSegEVENT_GPS_DAILY_CNT_REPORT.inetworktimeoutCnt.setValue(this.mNetworkTimeOutCnt);
        cCSegEVENT_GPS_DAILY_CNT_REPORT.inetworkReqCnt.setValue(this.mNetworkReqCnt);
        cCSegEVENT_GPS_DAILY_CNT_REPORT.igpserroruploadCnt.setValue(this.mGpsErrorUploadCnt);
        cCSegEVENT_GPS_DAILY_CNT_REPORT.igpsreqCnt.setValue(this.mGpsReqCnt);
        cCSegEVENT_GPS_DAILY_CNT_REPORT.iXtraDloadCnt.setValue(this.mXtraDloadCnt);
        cCSegEVENT_GPS_DAILY_CNT_REPORT.iXtraReqCnt.setValue(this.mXtraReqCnt);
        cCSegEVENT_GPS_DAILY_CNT_REPORT.iNtpFlashSuccCnt.setValue(this.mNtpFlashSuccCnt);
        cCSegEVENT_GPS_DAILY_CNT_REPORT.iNtpWifiFailCnt.setValue(this.mNtpWifiFailCnt);
        cCSegEVENT_GPS_DAILY_CNT_REPORT.iNtpMobileFailCnt.setValue(this.mNtpMobileFailCnt);
        cCSegEVENT_GPS_DAILY_CNT_REPORT.iNtpReqCnt.setValue(this.mNtpReqCnt);
        cCSegEVENT_GPS_DAILY_CNT_REPORT.strIsCn0Valied.setValue(Boolean.toString(this.mIsCn0Valied));
        cCSegEVENT_GPS_DAILY_CNT_REPORT.strIsCn0Good.setValue(Boolean.toString(this.mIsCn0Good));
        ChrLogBaseModel cChrLogBaseModel = cCSegEVENT_GPS_DAILY_CNT_REPORT;
        Log.d(TAG, "writeDailyRptInfo: " + type + "  ,ErrorCode:" + 25);
        GnssConnectivityLogManager.getInstance().reportAbnormalEventEx(cCSegEVENT_GPS_DAILY_CNT_REPORT, 14, 1, type, date, 1);
    }

    private void writeDailyRptToImonitor() {
        HwGnssDftManager hwGnssDftManager = new HwGnssDftManager(this.mContext);
        HwGnssDftGnssDailyParam mHwGnssDftGnssDailyParam = new HwGnssDftGnssDailyParam();
        mHwGnssDftGnssDailyParam.mDftGpsErrorUploadCnt = this.mGpsErrorUploadCnt;
        mHwGnssDftGnssDailyParam.mDftGpsRqCnt = this.mGpsReqCnt;
        mHwGnssDftGnssDailyParam.mDftNetworkTimeoutCnt = this.mNetworkTimeOutCnt;
        mHwGnssDftGnssDailyParam.mDftNetworkReqCnt = this.mNetworkReqCnt;
        hwGnssDftManager.sendDailyDataToImonitor(71, mHwGnssDftGnssDailyParam);
    }
}
