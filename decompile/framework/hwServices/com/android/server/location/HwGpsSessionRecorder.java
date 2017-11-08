package com.android.server.location;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.GpsStatus;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.util.Log;
import com.android.internal.location.ProviderRequest;
import com.android.server.HwNetworkPropertyChecker;
import com.android.server.location.gnsschrlog.GnssLogManager;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import huawei.android.debug.HwDBGSwitchController;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class HwGpsSessionRecorder {
    private static final long AGPS_TIMEOUT_SECOND = 20;
    private static final int ALMANAC_MASK = 1;
    private static final int ANY_VALUE = 4;
    private static final int C2K_VALUE = 2;
    private static final String CHIP_HI1102 = "hi1102";
    private static final String CHIP_HISI = "hisi";
    private static final long COMM_UPLOAD_MIN_SPAN = 86400000;
    private static final int CONNECTION_TYPE_ANY = 0;
    private static final int CONNECTION_TYPE_C2K = 2;
    private static final int CONNECTION_TYPE_SUPL = 1;
    private static final int CONNECTION_TYPE_WIFI = 4;
    private static final int CONNECTION_TYPE_WWAN_ANY = 3;
    private static final boolean DEBUG = HwDBGSwitchController.getDBGSwitch();
    private static final int EPHEMERIS_MASK = 0;
    private static final int GOODSIGNAL = 32;
    private static final int GPS_AGPS_DATA_CONNECTED = 3;
    private static final int GPS_AGPS_DATA_CONN_DONE = 4;
    private static final int GPS_AGPS_DATA_CONN_FAILED = 5;
    private static final int GPS_DAILY_CNT_REPORT = 71;
    private static final int GPS_DAILY_UPLOAD = 70;
    private static final int GPS_POS_ERROR_EVENT = 72;
    private static final int GPS_POS_FLOW_ERROR_EVENT = 65;
    private static final int GPS_POS_FLOW_ERROR_EVENT_EX = 68;
    private static final int GPS_POS_TIMEOUT_EVENT = 66;
    private static final int GPS_POS_TIMEOUT_EVENT_EX = 69;
    private static final int GPS_RELEASE_AGPS_DATA_CONN = 2;
    private static final int GPS_REQUEST_AGPS_DATA_CONN = 1;
    private static final int GPS_SESSION_EVENT = 73;
    private static final int GPS_SESSION_EVT_BETA_TRIGGER_NUM = 50;
    private static final int GPS_SESSION_EVT_COMM_TRIGGER_NUM = 20;
    private static final String GPS_SESSION_RPT = "gps_pos_session_report";
    private static final int GPS_STATUS_ENGINE_OFF = 4;
    private static final int GPS_STATUS_ENGINE_ON = 3;
    private static final int GPS_STATUS_SESSION_BEGIN = 1;
    private static final int GPS_STATUS_SESSION_END = 2;
    private static final int HIGEO_STATISTIC_EVENT = 76;
    private static final String INJECT_EXTRA_DATA = "extra_data";
    private static final String INJECT_NTP_TIME = "ntp_time";
    private static final String INJECT_REF_LOCATION = "ref_location";
    private static int LOCATION_LOST_TIMEOUT = 4000;
    private static final int MAXSIGNAL = 35;
    private static final int MAX_NUM_TRIGGER_BETA = 100;
    private static final int MAX_NUM_TRIGGER_COMM = 50;
    private static final int MAX_UPLOAD_LOST_CNT = 20;
    private static final int NETWK_POS_TIMEOUT_EVENT = 64;
    private static final int NETWK_POS_TIMEOUT_EVENT_EX = 67;
    private static final long NETWORK_POS_TIMEOUT_SECOND = 10000;
    private static final int NETWORK_SESSION_EVT_BETA_TRIGGER_NUM = 50;
    private static final int NETWORK_SESSION_EVT_COMM_TRIGGER_NUM = 20;
    private static final String NETWORK_SESSION_RPT = "network_pos_session_report";
    private static final int NORMALSIGNAL = 28;
    private static final String PROPERTY_CONNECTIVITY_CHIPSET = "ro.connectivity.sub_chiptype";
    private static final int REPORT_SNR_THRESHOLD = 20;
    private static final long STANDALONE_TIMEOUT_SECOND = 60;
    private static final int SUPL_VALUE = 0;
    private static final String TAG = "HwGnssLog_GpsRecord";
    private static final int USED_FOR_FIX_MASK = 2;
    private static final int VALIDSIGNAL = 25;
    private static final boolean VERBOSE = HwDBGSwitchController.getDBGSwitch();
    private static final int WIFI_VALUE = 1;
    private static final int WWAN_ANY_VALUE = 3;
    protected static final HashMap<String, TriggerLimit> mapEventTriggerFreq = new HashMap();
    private boolean isGlobalVersion;
    private int lostPosCnt_OneSession;
    private int mAccuracy;
    private int mAvgPositionAcc;
    private Context mContext;
    private int[] mCurSvCn0 = new int[32];
    GpsDailyReportEvent mDailyRptEvent;
    private long mFirstFixTimeOutVal;
    private int mFirstTimoutErrCode;
    private boolean mFixPosRpt;
    private boolean mFixed;
    GpsPosErrorEvent mGpsPosErrEvent;
    GpsSessionEvent mGpsSessionEvent;
    private Listener mGpsStatusListener;
    private boolean mGpsStopped;
    private Timer mGpsTimer = null;
    private TimerTask mGpsTimerTask = null;
    private boolean mGpsdResart;
    private GpsSessionRecorderHandler mHandler;
    private HwBcmGnssManager mHwBcmGnssManager;
    HwHiGeoStatisticEvent mHwHiGeoStatisticEvent;
    private HwHisiGnssManager mHwHisiGnssManager;
    private boolean mInjectNtpTimePending;
    private byte mInjectParam;
    private boolean mIsCheckedSpeed;
    private boolean mIsGpsRfGood;
    private boolean mIsGpsRfvalied;
    private boolean mIsPosLost;
    private boolean mIsResume;
    private boolean mIsSetFirstCatchSvTime;
    private boolean mIsWifiType;
    private boolean mIssueFlag = false;
    private boolean mJudgeFirstFix;
    private LocationManager mLocationManager;
    private boolean mMobileDataConnect;
    private boolean mNetAvailable;
    private Timer mNetTimer = null;
    private TimerTask mNetTimerTask = null;
    private boolean mNetWorkFixPending;
    private boolean mNetworkFixed;
    NetworkPosErrorEvent mNetworkPosErrEvent;
    private String mNtpIpAddr = "Unknown";
    private boolean mNtpstatus;
    private String mProvider = AppHibernateCst.INVALID_PKG;
    private int mReStartCnt_OneSession;
    private int mReportLocCnt;
    private int mSpeed;
    private int mSubNetworkType;
    private int mSvBestSvSignalInfo;
    private int mSvCount;
    private String mSvInfoString = null;
    private int mSvNormalSvSignalInfo;
    private HandlerThread mThread;
    private int mUsedSvCount;
    private int ucAGPSConnReq;
    private byte ucGpsEngineCap;
    private byte ucGpsRunStatus;

    class GpsSessionRecorderHandler extends Handler {
        private ArrayList list;

        GpsSessionRecorderHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            this.list = (ArrayList) msg.obj;
            switch (msg.what) {
                case 0:
                    String provider = (String) this.list.get(1);
                    HwGpsSessionRecorder.this.handlerNetWorkLocation(provider, (ProviderRequest) this.list.get(0));
                    return;
                case 1:
                    HwGpsSessionRecorder.this.handlerInitGps(((Boolean) this.list.get(0)).booleanValue(), ((Byte) this.list.get(1)).byteValue());
                    return;
                case 2:
                    Boolean Enable = (Boolean) this.list.get(0);
                    HwGpsSessionRecorder.this.handlerStartGps(Enable.booleanValue(), ((Integer) this.list.get(1)).intValue());
                    return;
                case 3:
                    HwGpsSessionRecorder.this.handlerUpdateNetworkState((NetworkInfo) this.list.get(0));
                    return;
                case 4:
                    HwGpsSessionRecorder.this.handlerUpdateAgpsState(((Integer) this.list.get(0)).intValue(), ((Integer) this.list.get(1)).intValue());
                    return;
                case 5:
                    HwGpsSessionRecorder.this.handlerStopGps(((Boolean) this.list.get(0)).booleanValue());
                    return;
                case 6:
                    HwGpsSessionRecorder.this.handlerUpdateGpsRunState(((Integer) this.list.get(0)).intValue());
                    return;
                case 8:
                    HwGpsSessionRecorder.this.handlerUpdateLocation((Location) this.list.get(0), ((Long) this.list.get(1)).longValue());
                    return;
                case 9:
                    int[] svs = (int[]) this.list.get(1);
                    float[] snrs = (float[]) this.list.get(2);
                    float[] svElevations = (float[]) this.list.get(3);
                    float[] svAzimuths = (float[]) this.list.get(4);
                    HwGpsSessionRecorder.this.handlerUpdateSvStatus(((Integer) this.list.get(0)).intValue(), svs, snrs, svElevations, svAzimuths);
                    return;
                case 10:
                    HwGpsSessionRecorder.this.handlerUpdateXtraDloadStatus(((Boolean) this.list.get(0)).booleanValue());
                    return;
                case 11:
                    HwGpsSessionRecorder.this.mNtpstatus = ((Boolean) this.list.get(0)).booleanValue();
                    if (!HwGpsSessionRecorder.this.mInjectNtpTimePending) {
                        HwGpsSessionRecorder.this.mInjectNtpTimePending = true;
                        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                            public void run() {
                                HwGpsSessionRecorder.this.handlerUpdateNtpDloadStatus(HwGpsSessionRecorder.this.mNtpstatus);
                                HwGpsSessionRecorder.this.mInjectNtpTimePending = false;
                            }
                        });
                        return;
                    }
                    return;
                case 12:
                    HwGpsSessionRecorder.this.handlerUpdateSetPosMode(((Boolean) this.list.get(0)).booleanValue());
                    return;
                case 13:
                    HwGpsSessionRecorder.this.handlerPermissionErr();
                    return;
                case 14:
                    HwGpsSessionRecorder.this.handlerOpenGpsSwitchFail(((Integer) this.list.get(0)).intValue());
                    return;
                case 15:
                    HwGpsSessionRecorder.this.handlerAddGeofenceFail();
                    return;
                case 16:
                    HwGpsSessionRecorder.this.handlerAddBatchingFail();
                    return;
                case 17:
                    HwGpsSessionRecorder.this.handlerLostLocation();
                    return;
                case 18:
                    HwGpsSessionRecorder.this.handlerUpdateApkName((LocationRequest) this.list.get(0), (String) this.list.get(1));
                    return;
                case 19:
                    HwGpsSessionRecorder.this.handlerUpdateNtpErrorStatus(((Long) this.list.get(0)).longValue(), ((Long) this.list.get(1)).longValue());
                    return;
                case 20:
                    HwGpsSessionRecorder.this.handlerUpdateLocationProviderBindErrorStatus();
                    return;
                case 21:
                    new Thread(new Runnable() {
                        public void run() {
                            if (HwGpsSessionRecorder.this.mHwBcmGnssManager != null) {
                                HwGpsSessionRecorder.this.mHwBcmGnssManager.bcmGnssSocketinit();
                            }
                        }
                    }).start();
                    return;
                case 22:
                    HwGpsSessionRecorder.this.handlerBrcmGnssMsg(((Integer) this.list.get(0)).intValue(), ((Boolean) this.list.get(1)).booleanValue());
                    return;
                case 23:
                    HwGpsSessionRecorder.this.mNtpIpAddr = (String) this.list.get(0);
                    Log.i(HwGpsSessionRecorder.TAG, "NTP server ip address is : " + HwGpsSessionRecorder.this.mNtpIpAddr);
                    return;
                case 24:
                    HwGpsSessionRecorder.this.handlerInjectExtraParam((String) this.list.get(0));
                    return;
                case 25:
                    String parameter = (String) this.list.get(2);
                    HwGpsSessionRecorder.this.handlerLogEvent(((Integer) this.list.get(0)).intValue(), ((Integer) this.list.get(1)).intValue(), parameter);
                    return;
                default:
                    Log.d(HwGpsSessionRecorder.TAG, "====handleMessage: msg.what = " + msg.what + "====");
                    return;
            }
        }
    }

    private static class TriggerLimit {
        long lastUploadTime;
        int triggerNum;

        private TriggerLimit() {
        }

        public void reset() {
            this.lastUploadTime = 0;
            this.triggerNum = 0;
        }
    }

    HwGpsSessionRecorder(HandlerThread thread, Context context) {
        boolean z = false;
        if (!"CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region"))) {
            z = true;
        }
        this.isGlobalVersion = z;
        this.mGpsStatusListener = new Listener() {
            public void onGpsStatusChanged(int event) {
                GpsStatus gpsstatus = HwGpsSessionRecorder.this.mLocationManager.getGpsStatus(null);
                switch (event) {
                    case 3:
                        int ttff = (gpsstatus.getTimeToFirstFix() / 1000) + 1;
                        HwGpsSessionRecorder.this.mGpsSessionEvent.setTTFF(ttff);
                        Log.d(HwGpsSessionRecorder.TAG, "ttff is : " + ttff);
                        return;
                    default:
                        return;
                }
            }
        };
        this.mThread = thread;
        this.mContext = context;
        this.mHandler = new GpsSessionRecorderHandler(this.mThread.getLooper());
        this.mGpsPosErrEvent = new GpsPosErrorEvent(this.mContext);
        this.mDailyRptEvent = new GpsDailyReportEvent(this.mContext);
        this.mGpsSessionEvent = new GpsSessionEvent(this.mContext);
        this.mGpsPosErrEvent.setGpsSessionEvent(this.mGpsSessionEvent);
        this.mHwBcmGnssManager = new HwBcmGnssManager(this.mHandler, this.mGpsSessionEvent, this.mGpsPosErrEvent);
        this.mHandler.sendEmptyMessage(21);
        this.mHwHisiGnssManager = new HwHisiGnssManager();
        this.mHwHiGeoStatisticEvent = new HwHiGeoStatisticEvent(this.mContext);
        this.mHwHiGeoStatisticEvent.setGpsSessionEvent(this.mGpsSessionEvent);
    }

    public void netWorkLocation(String provider, ProviderRequest providerRequest) {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        list.add(providerRequest);
        list.add(provider);
        msg.what = 0;
        msg.obj = list;
        this.mHandler.sendMessage(msg);
    }

    public void initGps(boolean isEnable, byte EngineCapabilities) {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        list.add(Boolean.valueOf(isEnable));
        list.add(Byte.valueOf(EngineCapabilities));
        msg.what = 1;
        msg.obj = list;
        this.mHandler.sendMessage(msg);
    }

    public void updateXtraDloadStatus(boolean status) {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        list.add(Boolean.valueOf(status));
        msg.what = 10;
        msg.obj = list;
        this.mHandler.sendMessage(msg);
    }

    public int updatelogEvent(int type, int event, String parameter) {
        try {
            ArrayList list = new ArrayList();
            Message msg = new Message();
            list.clear();
            list.add(Integer.valueOf(type));
            list.add(Integer.valueOf(event));
            list.add(parameter);
            msg.what = 25;
            msg.obj = list;
            this.mHandler.sendMessage(msg);
            return 0;
        } catch (NullPointerException e) {
            Log.e(TAG, "mHandler is null.");
            return -1;
        } catch (Exception e2) {
            Log.e(TAG, "send logevent error.");
            return -1;
        }
    }

    private void handlerLogEvent(int type, int event, String parameter) {
        switch (type) {
            case GPS_POS_ERROR_EVENT /*72*/:
                this.mGpsPosErrEvent.writeSubevent(event, parameter);
                return;
            case GPS_SESSION_EVENT /*73*/:
                this.mGpsSessionEvent.writeSubevent(event, parameter);
                return;
            case HIGEO_STATISTIC_EVENT /*76*/:
                this.mHwHiGeoStatisticEvent.writeSubevent(event, parameter);
                return;
            default:
                Log.d(TAG, "other type: " + type);
                return;
        }
    }

    public void updateNtpDloadStatus(boolean status) {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        list.add(Boolean.valueOf(status));
        msg.what = 11;
        msg.obj = list;
        this.mHandler.sendMessage(msg);
    }

    public void updateSetPosMode(boolean status) {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        list.add(Boolean.valueOf(status));
        msg.what = 12;
        msg.obj = list;
        this.mHandler.sendMessage(msg);
    }

    public void updateApkName(LocationRequest request, String name) {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        list.add(request);
        list.add(name);
        msg.what = 18;
        msg.obj = list;
        this.mHandler.sendMessage(msg);
    }

    public void startGps(boolean isEnable, int PositionMode) {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        list.add(Boolean.valueOf(isEnable));
        list.add(Integer.valueOf(PositionMode));
        msg.what = 2;
        msg.obj = list;
        this.mHandler.sendMessage(msg);
    }

    public void updateNetworkState(NetworkInfo info) {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        list.add(info);
        msg.what = 3;
        msg.obj = list;
        this.mHandler.sendMessage(msg);
    }

    public void updateAgpsState(int type, int state) {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        list.add(Integer.valueOf(type));
        list.add(Integer.valueOf(state));
        msg.what = 4;
        msg.obj = list;
        this.mHandler.sendMessage(msg);
    }

    public void stopGps(boolean status) {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        list.add(Boolean.valueOf(status));
        msg.what = 5;
        msg.obj = list;
        this.mHandler.sendMessage(msg);
    }

    public void permissionErr() {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        msg.what = 13;
        msg.obj = list;
        this.mHandler.sendMessage(msg);
    }

    public void openGpsSwitchFail(int open) {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        list.add(Integer.valueOf(open));
        msg.what = 14;
        msg.obj = list;
        this.mHandler.sendMessage(msg);
    }

    public void addGeofenceStatus() {
        Message msg = new Message();
        msg.what = 15;
        this.mHandler.sendMessage(msg);
    }

    public void addBatchingStatus() {
        Message msg = new Message();
        msg.what = 16;
        this.mHandler.sendMessage(msg);
    }

    public void updateGpsRunState(int status) {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        list.add(Integer.valueOf(status));
        msg.what = 6;
        msg.obj = list;
        this.mHandler.sendMessage(msg);
    }

    public void updateLocation(Location location, long time, String provider) {
        if (provider.equalsIgnoreCase("network")) {
            if (!this.mNetworkFixed) {
                this.mNetworkFixed = true;
            }
        } else if (provider.equalsIgnoreCase("gps")) {
            if (!this.mFixed) {
                this.mFixed = true;
            }
            ArrayList list = new ArrayList();
            Message msg = new Message();
            list.clear();
            list.add(location);
            list.add(Long.valueOf(time));
            msg.what = 8;
            msg.obj = list;
            this.mHandler.sendMessage(msg);
            this.mHandler.removeMessages(17);
            this.mHandler.sendEmptyMessageDelayed(17, (long) LOCATION_LOST_TIMEOUT);
        }
    }

    public void updateSvStatus(int svCount, int[] svs, float[] snrs, float[] svElevations, float[] svAzimuths) {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        list.add(Integer.valueOf(svCount));
        list.add(svs);
        list.add(snrs);
        list.add(svElevations);
        list.add(svAzimuths);
        msg.what = 9;
        msg.obj = list;
        this.mHandler.sendMessage(msg);
    }

    public void reportErrorNtpTime(long currentNtpTime, long realTime) {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        list.add(Long.valueOf(currentNtpTime));
        list.add(Long.valueOf(realTime));
        msg.what = 19;
        msg.obj = list;
        this.mHandler.sendMessage(msg);
    }

    public void reportBinderError() {
        Message msg = new Message();
        msg.what = 20;
        this.mHandler.sendMessage(msg);
    }

    public void updateNtpServerInfo(String ntpAddr) {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        list.add(ntpAddr);
        msg.what = 23;
        msg.obj = list;
        this.mHandler.sendMessage(msg);
    }

    public void injectExtraParam(String extraParam) {
        ArrayList list = new ArrayList();
        Message msg = new Message();
        list.clear();
        list.add(extraParam);
        msg.what = 24;
        msg.obj = list;
        this.mHandler.sendMessage(msg);
    }

    private void handlerBrcmGnssMsg(int bcmErr, boolean trigger) {
        if (DEBUG) {
            Log.d(TAG, "Brcm chipset msg is : " + bcmErr + " ,trigger value is " + trigger);
        }
        switch (bcmErr) {
            case 30:
                this.mReStartCnt_OneSession++;
                if (trigger) {
                    this.mGpsdResart = true;
                    this.mGpsSessionEvent.setBrcmRestartFlag(this.mGpsdResart);
                    break;
                }
                break;
        }
        reportPosErrEvt(bcmErr);
    }

    private void handlerInjectExtraParam(String extraParam) {
        if (extraParam.equalsIgnoreCase(INJECT_NTP_TIME)) {
            this.mInjectParam = (byte) (this.mInjectParam | 1);
        } else if (extraParam.equalsIgnoreCase(INJECT_REF_LOCATION)) {
            this.mInjectParam = (byte) (this.mInjectParam | 2);
        } else if (extraParam.equalsIgnoreCase(INJECT_EXTRA_DATA)) {
            this.mInjectParam = (byte) (this.mInjectParam | 4);
        } else {
            Log.d(TAG, "handlerInjectExtraParam, unkonwn extra param : " + extraParam);
        }
        this.mGpsSessionEvent.setInjectParam(this.mInjectParam);
    }

    private void handlerInitGps(boolean isEnable, byte EngineCapabilities) {
        this.ucGpsEngineCap = EngineCapabilities;
        if (DEBUG) {
            Log.d(TAG, "handlerInitGps ,isEnable = " + isEnable + " ,EngineCapabilities = " + this.ucGpsEngineCap);
        }
        if (!isEnable) {
            reportPosErrEvt(24);
        }
        this.mDailyRptEvent.reloadGpsDailyRptInfo();
        this.mLocationManager = (LocationManager) this.mContext.getSystemService("location");
        this.mLocationManager.addGpsStatusListener(this.mGpsStatusListener);
    }

    private void handlerStartGps(boolean isEnable, int PositionMode) {
        if (DEBUG) {
            Log.d(TAG, "handlerStartGps: isEnable : " + isEnable + " , PositionMode : " + PositionMode);
        }
        long time = System.currentTimeMillis();
        this.mGpsSessionEvent.setProvider("gps");
        this.mGpsPosErrEvent.setProvider("gps");
        this.mHwHiGeoStatisticEvent.setProvider("gps");
        this.mGpsSessionEvent.setStartTime(time);
        this.mGpsPosErrEvent.setStartTime(time);
        this.mHwHiGeoStatisticEvent.setStartTime(time);
        this.mGpsSessionEvent.setPosMode(PositionMode);
        this.mGpsPosErrEvent.setPosMode(PositionMode);
        this.mHwHiGeoStatisticEvent.setPosMode(PositionMode);
        this.mFixed = false;
        this.mFixPosRpt = false;
        this.mIsResume = false;
        this.mGpsdResart = false;
        this.mGpsStopped = false;
        this.mJudgeFirstFix = false;
        this.mIsSetFirstCatchSvTime = false;
        this.mIsCheckedSpeed = false;
        this.mSpeed = 0;
        this.mAccuracy = 0;
        this.mReportLocCnt = 0;
        this.ucAGPSConnReq = 0;
        this.mAvgPositionAcc = 0;
        this.lostPosCnt_OneSession = 0;
        this.mReStartCnt_OneSession = 0;
        this.mInjectParam = (byte) 0;
        if (!isEnable) {
            Log.e(TAG, "start gps failed,pos mode is " + PositionMode);
            reportPosErrEvt(1);
        }
        this.mDailyRptEvent.updateGpsPosReqCnt(true);
    }

    private void handlerStopGps(boolean status) {
        if (DEBUG) {
            Log.d(TAG, "handlerStopGps");
        }
        if (!status) {
            Log.e(TAG, "Stop GPS failed !");
            reportPosErrEvt(2);
        }
        stopGpsTimer();
        this.mHandler.removeMessages(17);
        if (matchEventTriggerFreq(GnssLogManager.getInstance().isCommercialUser(), GPS_SESSION_RPT)) {
            this.mDailyRptEvent.updateCn0Status(this.mIsGpsRfvalied, this.mIsGpsRfGood);
            this.mGpsSessionEvent.setCommFlag(this.mIssueFlag);
            this.mGpsSessionEvent.setNetworkAvailable(this.mNetAvailable);
            this.mGpsSessionEvent.setNetworkStatus(this.mSubNetworkType);
            this.mGpsSessionEvent.setStopTime(System.currentTimeMillis());
            this.mGpsSessionEvent.setLostPosCnt(this.lostPosCnt_OneSession);
            this.mGpsSessionEvent.setReStartCnt(this.mReStartCnt_OneSession);
            this.mGpsSessionEvent.setGpsdRestartFlag(this.mGpsdResart);
            setFirstFixPosInfo();
            this.mGpsSessionEvent.writeGpsSessionInfo();
        }
        this.mGpsSessionEvent.createNewGpsSessionEvent();
        this.mGpsPosErrEvent.createNewGpsPosErrorEvent();
        this.mHwHiGeoStatisticEvent.createNewHiGeoStatisticEvent();
        this.mIsGpsRfGood = false;
        this.mIsGpsRfvalied = false;
        this.mIsPosLost = false;
        this.mIsResume = false;
        this.mIssueFlag = false;
        this.mFixed = false;
        this.mFixPosRpt = false;
        this.mGpsStopped = true;
    }

    private void handlerPermissionErr() {
        Log.e(TAG, "GPS permission denied!");
    }

    private void handlerOpenGpsSwitchFail(int open) {
        int errorcode = 0;
        if (open == 1) {
            Log.e(TAG, "Open gps switch fail");
            errorcode = 7;
        } else if (open == 2) {
            Log.e(TAG, "close gps switch fail");
            errorcode = 8;
        }
        reportPosErrEvt(errorcode);
    }

    private void handlerAddGeofenceFail() {
        Log.e(TAG, "add geofence fail");
        reportPosErrEvt(9);
    }

    private void handlerAddBatchingFail() {
        Log.e(TAG, "add batching fail");
        reportPosErrEvt(10);
    }

    private void handlerUpdateXtraDloadStatus(boolean status) {
        if (this.mNetAvailable) {
            boolean xtraDownloadStatus = false;
            if (DEBUG) {
                Log.d(TAG, "handlerUpdateXtraDloadStatus:" + status);
            }
            if (status) {
                xtraDownloadStatus = true;
            } else {
                Log.e(TAG, "Download xtra data failed");
                reportPosErrEvt(3);
            }
            this.mDailyRptEvent.updateXtraDownLoadCnt(true, xtraDownloadStatus);
            return;
        }
        if (DEBUG) {
            Log.e(TAG, "Network not avaiable ! Stop xtra data download");
        }
    }

    private void handlerUpdateNtpDloadStatus(boolean status) {
        if (DEBUG) {
            Log.d(TAG, "handlerUpdateNtpDloadStatus:" + status);
        }
        if (this.mNetAvailable) {
            boolean wifiFail = false;
            boolean dataCallFail = false;
            boolean ntpDownloadStatus = false;
            if (status) {
                ntpDownloadStatus = true;
            } else if (1 == System.getInt(this.mContext.getContentResolver(), "CtrlSocketSaving", 0)) {
                Log.i(TAG, "power saving mode has started, ignore ntp download fail issue!");
                return;
            } else {
                if (!isRealConnNetwork()) {
                    if (this.mIsWifiType) {
                        wifiFail = true;
                        Log.d(TAG, "NTP,wifi network can not reachedable!");
                    } else {
                        dataCallFail = true;
                        Log.d(TAG, "NTP,mobile network can not reachedable!");
                    }
                }
                Log.e(TAG, "Download ntp data failed");
                reportPosErrEvt(4);
            }
            this.mDailyRptEvent.updateNtpDownLoadCnt(true, ntpDownloadStatus, wifiFail, dataCallFail);
        }
    }

    private static boolean isHisiConnectivityChip() {
        String chipType = SystemProperties.get(PROPERTY_CONNECTIVITY_CHIPSET, AppHibernateCst.INVALID_PKG);
        Log.d(TAG, " GPS chipType: " + chipType);
        return !CHIP_HISI.equals(chipType) ? CHIP_HI1102.equals(chipType) : true;
    }

    private void handlerNetWorkLocation(String provider, ProviderRequest providerRequest) {
        this.mProvider = provider;
        boolean enable = providerRequest.reportLocation;
        long requestInterval = providerRequest.interval;
        if (DEBUG) {
            Log.d(TAG, "enable : " + enable + " , mProvider : " + this.mProvider + " , requestInterval" + requestInterval);
        }
        if (provider.equalsIgnoreCase("gps") && isHisiConnectivityChip()) {
            long mIntervalTimeout;
            if (requestInterval < 1000 || requestInterval > 30000) {
                mIntervalTimeout = 4000;
            } else {
                mIntervalTimeout = 4 * requestInterval;
            }
            Log.d(TAG, " Interval  : " + requestInterval + " Provider : " + provider + "  PreInterval : " + LOCATION_LOST_TIMEOUT + " , CurInterval : " + mIntervalTimeout);
            LOCATION_LOST_TIMEOUT = (int) mIntervalTimeout;
        }
        if (!provider.equalsIgnoreCase("network")) {
            return;
        }
        if (this.mNetWorkFixPending || this.mNetworkPosErrEvent == null) {
            Log.d(TAG, "return !, mNetWorkFixPending : " + this.mNetWorkFixPending + " , mNetworkPosErrEvent : " + this.mNetworkPosErrEvent);
        } else if (requestInterval >= 86400000) {
            Log.d(TAG, "network location interval is : " + requestInterval + " ,larger than 24 hours,ignore !");
        } else {
            this.mNetworkPosErrEvent.setProvider(this.mProvider);
            this.mNetworkPosErrEvent.setStartTime(System.currentTimeMillis());
            if (this.mNetAvailable) {
                if (!enable) {
                    stopNetTimer();
                    this.mNetWorkFixPending = false;
                } else if (this.mNetWorkFixPending) {
                    Log.e(TAG, "Network pos is already runing!");
                    return;
                } else {
                    this.mNetWorkFixPending = true;
                    this.mNetworkFixed = false;
                    this.mDailyRptEvent.updateNetworkReqCnt(false, true);
                    if (this.mNetTimer == null) {
                        this.mNetTimer = new Timer();
                    }
                    if (this.mNetTimerTask != null) {
                        this.mNetTimerTask.cancel();
                        this.mNetTimerTask = null;
                    }
                    this.mNetTimerTask = new TimerTask() {
                        public void run() {
                            if (HwGpsSessionRecorder.this.mNetworkFixed) {
                                HwGpsSessionRecorder.this.mNetWorkFixPending = false;
                                return;
                            }
                            Log.e(HwGpsSessionRecorder.TAG, "network position over 10s");
                            HwGpsSessionRecorder.this.mDailyRptEvent.updateNetworkReqCnt(true, false);
                            if (HwGpsSessionRecorder.this.matchEventTriggerFreq(GnssLogManager.getInstance().isCommercialUser(), HwGpsSessionRecorder.NETWORK_SESSION_RPT)) {
                                HwGpsSessionRecorder.this.mNetworkPosErrEvent.setNetworkPosTimeOUTInfo(0, HwGpsSessionRecorder.this.mNetAvailable);
                                HwGpsSessionRecorder.this.mNetworkPosErrEvent.setNetworkAvailable(HwGpsSessionRecorder.this.mNetAvailable);
                                HwGpsSessionRecorder.this.mNetworkPosErrEvent.setNetworkInfo(HwGpsSessionRecorder.this.mSubNetworkType);
                                HwGpsSessionRecorder.this.mNetworkPosErrEvent.writeNetworkPosErrInfo();
                            }
                        }
                    };
                    try {
                        this.mNetTimer.schedule(this.mNetTimerTask, 10000);
                        this.mNetWorkFixPending = false;
                    } catch (IllegalStateException e) {
                        Log.e(TAG, "TimerTask is scheduled already !");
                    }
                }
                return;
            }
            Log.e(TAG, "Network not available !");
        }
    }

    private void handlerUpdateSetPosMode(boolean status) {
        if (!status) {
            Log.e(TAG, "set pos mode failed");
            reportPosErrEvt(5);
        }
    }

    private void handlerUpdateApkName(LocationRequest request, String name) {
        if (DEBUG) {
            Log.d(TAG, "handlerUpdateApkName: " + name + "request value is : " + request);
        }
        String providerName = request.getProvider();
        String version = getPackageApkVersion(name);
        if (version == null) {
            version = "unknown";
        }
        if (providerName.equalsIgnoreCase("gps")) {
            this.mGpsSessionEvent.setGpsApkName(name, version);
            this.mGpsPosErrEvent.setGpsApkName(name, version);
        } else if (providerName.equalsIgnoreCase("network")) {
            this.mNetworkPosErrEvent = new NetworkPosErrorEvent(this.mContext);
            this.mNetworkPosErrEvent.setGpsApkName(name, version);
        }
    }

    private String getPackageApkVersion(String apkName) {
        String pkgVersion = "unknown";
        try {
            return this.mContext.getPackageManager().getPackageInfo(apkName, 0).versionName;
        } catch (NameNotFoundException e) {
            Log.e(TAG, "getVersionName " + e.toString());
            return pkgVersion;
        }
    }

    private void handlerUpdateNtpErrorStatus(long ntpTime, long realTime) {
        this.mGpsPosErrEvent.setNtpErrTime(ntpTime, realTime, this.mNtpIpAddr);
        reportPosErrEvt(26);
    }

    private void handlerUpdateLocationProviderBindErrorStatus() {
        Log.d(TAG, "handlerUpdateLocationProviderBindErrorStatus");
        reportPosErrEvt(31);
    }

    private void handlerUpdateNetworkState(NetworkInfo info) {
        this.mNetAvailable = false;
        this.mSubNetworkType = 0;
        if (info == null) {
            Log.d(TAG, "network info is null , return!");
            return;
        }
        this.mNetAvailable = info.isAvailable();
        if (1 == info.getType()) {
            this.mIsWifiType = true;
            this.mSubNetworkType = 100;
        } else {
            this.mIsWifiType = false;
            this.mSubNetworkType = info.getSubtype();
        }
        if (DEBUG) {
            Log.d(TAG, "Networktype is :" + info.getType() + " ,SubNetworkType : " + this.mSubNetworkType + " ,networkStatus is : " + this.mNetAvailable);
        }
    }

    private void handlerUpdateAgpsState(int type, int state) {
        if (DEBUG) {
            Log.d(TAG, "handlerUpdateAgpsState:" + type + " ,state is: " + state);
        }
        switch (type) {
            case 0:
                this.ucAGPSConnReq = 4;
                break;
            case 1:
                this.ucAGPSConnReq = 0;
                break;
            case 2:
                this.ucAGPSConnReq = 2;
                break;
            case 3:
                this.ucAGPSConnReq = 3;
                break;
            case 4:
                this.ucAGPSConnReq = 1;
                break;
        }
        if (DEBUG) {
            Log.d(TAG, "handlerUpdateAgpsState, ucAGPSConnReq is : " + this.ucAGPSConnReq);
        }
        switch (state) {
            case 1:
                this.mDailyRptEvent.updateAgpsReqCnt(false, true);
                return;
            case 2:
            case 3:
            case 4:
                return;
            case 5:
                Log.e(TAG, "agps conn failed");
                this.mDailyRptEvent.updateAgpsReqCnt(true, false);
                reportPosErrEvt(18);
                return;
            default:
                if (DEBUG) {
                    Log.d(TAG, "handlerUpdateAgpsState, no  state case matched!");
                    return;
                }
                return;
        }
    }

    private void handlerUpdateGpsRunState(int status) {
        if (DEBUG) {
            Log.d(TAG, "handlerUpdateGpsRunState:" + status);
        }
        switch (status) {
            case 1:
                this.ucGpsRunStatus = (byte) 0;
                this.ucGpsRunStatus = (byte) (this.ucGpsRunStatus | 1);
                this.ucGpsRunStatus = (byte) (this.ucGpsRunStatus | 4);
                this.ucGpsRunStatus = (byte) (this.ucGpsRunStatus | 16);
                this.ucGpsRunStatus = (byte) (this.ucGpsRunStatus | 32);
                return;
            case 2:
                this.ucGpsRunStatus = (byte) (this.ucGpsRunStatus | 2);
                this.ucGpsRunStatus = (byte) (this.ucGpsRunStatus & -17);
                return;
            case 3:
                this.ucGpsRunStatus = (byte) (this.ucGpsRunStatus | 4);
                this.ucGpsRunStatus = (byte) (this.ucGpsRunStatus | 32);
                return;
            case 4:
                this.ucGpsRunStatus = (byte) (this.ucGpsRunStatus | 2);
                this.ucGpsRunStatus = (byte) (this.ucGpsRunStatus | 8);
                this.ucGpsRunStatus = (byte) (this.ucGpsRunStatus & -17);
                this.ucGpsRunStatus = (byte) (this.ucGpsRunStatus & -33);
                return;
            default:
                if (DEBUG) {
                    Log.d(TAG, "handlerUpdateGpsRunState, no  status case matched!");
                    return;
                }
                return;
        }
    }

    private void handlerUpdateLocation(Location location, long time) {
        int diffTime = (int) TimeUnit.NANOSECONDS.toSeconds(time - location.getElapsedRealtimeNanos());
        getLocationParam(location);
        if (DEBUG) {
            Log.d(TAG, "mAvgPositionAcc is : " + this.mAvgPositionAcc + ",difftime in secs is : " + diffTime);
        }
        if (this.mIsPosLost) {
            this.mIsPosLost = false;
            if (this.lostPosCnt_OneSession < 20) {
                this.mIsResume = true;
            }
        }
        if (diffTime > 1) {
            Log.e(TAG, "delivering  postion data is delayed in framework layer ,difftime is : " + diffTime);
            this.mGpsPosErrEvent.setDataDeliveryDelay(diffTime);
            reportPosErrEvt(17);
        }
    }

    private void getLocationParam(Location location) {
        this.mAccuracy = (int) location.getAccuracy();
        this.mSpeed = (int) location.getSpeed();
        if (this.mReportLocCnt == 0) {
            this.mAvgPositionAcc = this.mAccuracy;
            this.mGpsSessionEvent.setFixLocation(location);
        }
        if (this.mReportLocCnt < 10) {
            this.mReportLocCnt++;
            this.mAvgPositionAcc = (this.mAccuracy + this.mAvgPositionAcc) / 2;
            this.mGpsSessionEvent.setAvgAcc(this.mAvgPositionAcc);
        }
        if (!this.mIsCheckedSpeed && this.mSpeed > 40) {
            this.mIsCheckedSpeed = true;
            setDrivingModeCn0();
        }
    }

    private void setDrivingModeCn0() {
        int i;
        int Cn0Val = 0;
        for (i = 0; i < this.mCurSvCn0.length - 1; i++) {
            for (int j = i + 1; j < this.mCurSvCn0.length; j++) {
                if (this.mCurSvCn0[i] < this.mCurSvCn0[j]) {
                    int temp = this.mCurSvCn0[i];
                    this.mCurSvCn0[i] = this.mCurSvCn0[j];
                    this.mCurSvCn0[j] = temp;
                }
            }
        }
        for (i = 0; i < 4; i++) {
            Cn0Val += this.mCurSvCn0[i];
        }
        if (Cn0Val != 0) {
            Cn0Val /= 4;
        }
        this.mGpsSessionEvent.setDrivingAvgCn0(Cn0Val);
    }

    private void stopGpsTimer() {
        if (DEBUG) {
            Log.d(TAG, "stopGpsTimer ");
        }
        if (this.mGpsTimer != null) {
            this.mGpsTimer.cancel();
            this.mGpsTimer.purge();
            this.mGpsTimer = null;
        }
        if (this.mGpsTimerTask != null) {
            this.mGpsTimerTask.cancel();
            this.mGpsTimerTask = null;
        }
    }

    private void stopNetTimer() {
        if (DEBUG) {
            Log.d(TAG, "stopNetTimer ");
        }
        if (this.mNetTimer != null) {
            this.mNetTimer.cancel();
            this.mNetTimer.purge();
            this.mNetTimer = null;
        }
        if (this.mNetTimerTask != null) {
            this.mNetTimerTask.cancel();
            this.mNetTimerTask = null;
        }
    }

    private void handlerCommercialFirstFixTimeOUT() {
        if (DEBUG) {
            Log.d(TAG, "Enter handlerCommercialFirstFixTimeOUT ");
        }
        this.mJudgeFirstFix = true;
        this.mMobileDataConnect = false;
        this.mGpsSessionEvent.setCatchSvTime(System.currentTimeMillis());
        NetworkInfo networkinfo = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getNetworkInfo(0);
        if (networkinfo != null) {
            this.mMobileDataConnect = networkinfo.isConnected();
        }
        if (this.mMobileDataConnect) {
            this.mFirstFixTimeOutVal = AGPS_TIMEOUT_SECOND;
        } else {
            this.mFirstFixTimeOutVal = STANDALONE_TIMEOUT_SECOND;
        }
        handlerCommercialTimeoutFirstTimeOutImp(this.mFirstFixTimeOutVal);
    }

    private void handlerCommercialTimeoutFirstTimeOutImp(long timeout) {
        if (DEBUG) {
            Log.d(TAG, "Enter handlerCommercialTimeoutFirstTimeOutImp,delay time is " + timeout);
        }
        if (this.mGpsTimer == null) {
            this.mGpsTimer = new Timer();
        }
        if (this.mGpsTimerTask != null) {
            this.mGpsTimerTask.cancel();
            this.mGpsTimerTask = null;
        }
        this.mGpsTimerTask = new TimerTask() {
            public void run() {
                if (!HwGpsSessionRecorder.this.mFixed && !HwGpsSessionRecorder.this.mGpsStopped) {
                    if (HwGpsSessionRecorder.this.mMobileDataConnect) {
                        HwGpsSessionRecorder.this.mFirstTimoutErrCode = 14;
                    } else {
                        HwGpsSessionRecorder.this.mFirstTimoutErrCode = 13;
                    }
                    HwGpsSessionRecorder.this.mGpsPosErrEvent.setFirstFixTimeOutStatus(HwGpsSessionRecorder.this.mSvCount, HwGpsSessionRecorder.this.mUsedSvCount, HwGpsSessionRecorder.this.mSvInfoString, HwGpsSessionRecorder.this.mInjectParam);
                    HwGpsSessionRecorder.this.reportPosErrEvt(HwGpsSessionRecorder.this.mFirstTimoutErrCode);
                }
            }
        };
        try {
            this.mGpsTimer.schedule(this.mGpsTimerTask, 1000 * timeout);
        } catch (IllegalStateException e) {
            Log.e(TAG, "TimerTask is scheduled already !");
        }
    }

    private void handlerLostLocation() {
        Log.e(TAG, "no position report in 4s");
        this.mIsPosLost = true;
        this.lostPosCnt_OneSession++;
        if (this.lostPosCnt_OneSession > 20 || this.mGpsStopped) {
            Log.e(TAG, "lost pos again,lost num is in this session : " + this.lostPosCnt_OneSession + ",or gps navigation status is : " + this.mGpsStopped);
            return;
        }
        int errorcode;
        this.mGpsSessionEvent.setLostPos_SvStatus(System.currentTimeMillis() - ((long) LOCATION_LOST_TIMEOUT), this.mAccuracy, this.mSpeed, this.mSvCount, this.mUsedSvCount, this.mSvInfoString);
        this.mGpsPosErrEvent.setLostPos_SvStatus(System.currentTimeMillis() - ((long) LOCATION_LOST_TIMEOUT), this.mAccuracy, this.mSpeed, this.mSvCount, this.mUsedSvCount, this.mSvInfoString);
        if (this.mSvCount > 0) {
            if (this.mSvNormalSvSignalInfo < 4) {
                errorcode = 19;
                Log.d(TAG, "handlerLostLocation , GPS_LOW_SIGNAl");
            } else if (this.mSvBestSvSignalInfo > 1) {
                errorcode = 11;
                Log.d(TAG, "handlerLostLocation , GPS_LOST_POSITION_FAILED");
            } else {
                errorcode = 23;
                Log.d(TAG, "catch num of normal cn0(28db) SVs > 4,but num of best cno(32) is less than 1! ");
            }
            Log.d(TAG, "mSvCount is : " + this.mSvCount + "NormalSv num is : " + this.mSvNormalSvSignalInfo + "BestSv num is : " + this.mSvBestSvSignalInfo);
        } else if (this.mGpsdResart) {
            errorcode = 21;
            Log.d(TAG, "handlerLostLocation , GPSD_NOT_RECOVERY_FAILED");
        } else {
            errorcode = 20;
            Log.d(TAG, "handlerLostLocation , GPS_IN_DOOR_FAILED");
        }
        reportPosErrEvt(errorcode);
    }

    private void handlerUpdateSvStatus(int svCount, int[] svidWithFlags, float[] snrs, float[] svElevations, float[] svAzimuths) {
        if (this.mGpsStopped) {
            Log.d(TAG, "gps engine has stopped, not report sv info, just return!");
            return;
        }
        int svid = 0;
        Arrays.fill(this.mCurSvCn0, 0);
        this.mUsedSvCount = 0;
        this.mSvCount = svCount;
        this.mSvNormalSvSignalInfo = 0;
        this.mSvBestSvSignalInfo = 0;
        StringBuilder svInfoString = new StringBuilder();
        if (!this.mIsSetFirstCatchSvTime && this.mSvCount > 0) {
            this.mIsSetFirstCatchSvTime = true;
            this.mGpsSessionEvent.setFirstCatchSvTime(System.currentTimeMillis());
        }
        for (int i = 0; i < svCount; i++) {
            int snrVal = (int) snrs[i];
            if (i < 32) {
                svid = svidWithFlags[i] >> 7;
                String svInfo = svid + "," + snrVal;
                this.mCurSvCn0[i] = snrVal;
                svInfoString.append(svInfo);
                svInfoString.append(";");
                if ((svidWithFlags[i] & 4) != 0) {
                    this.mUsedSvCount++;
                }
            }
            if (DEBUG) {
                Log.d(TAG, "handlerUpdateSvStatus ,SV NUM : " + svid + " ,CNO : " + snrVal);
            }
            if (snrVal > 25) {
                this.mIsGpsRfvalied = true;
                if (snrVal > 28) {
                    this.mSvNormalSvSignalInfo++;
                    if (snrVal > 32) {
                        this.mSvBestSvSignalInfo++;
                        if (!this.mIsGpsRfGood && snrVal > 35) {
                            this.mIsGpsRfGood = true;
                        }
                    }
                }
            }
        }
        this.mSvInfoString = svInfoString.toString();
        if (DEBUG) {
            Log.d(TAG, "handlerUpdateSvStatus ,SV counts is :" + svCount + " ,mUsedSvCount is : " + this.mUsedSvCount + " ,mSvInfoString value is " + this.mSvInfoString);
        }
        setFirstFixPosInfo();
        if (this.mIsResume) {
            this.mGpsSessionEvent.setResumePos_SvStatus(System.currentTimeMillis(), this.mAccuracy, this.mSpeed, this.mSvCount, this.mUsedSvCount, this.mSvInfoString);
            this.mIsResume = false;
        }
        if (!(this.mSvNormalSvSignalInfo <= 4 || this.mFixed || this.mJudgeFirstFix || this.mGpsStopped)) {
            handlerCommercialFirstFixTimeOUT();
        }
    }

    private void setFirstFixPosInfo() {
        if (this.mFixed && !this.mFixPosRpt) {
            this.mGpsSessionEvent.setFixPos_SvStatus(System.currentTimeMillis(), this.mSvCount, this.mUsedSvCount, this.mSvInfoString);
            this.mFixPosRpt = true;
        }
    }

    private void reportPosErrEvt(int errorcode) {
        this.mIssueFlag = true;
        String code = "UNKNOWN_ISSUE";
        switch (errorcode) {
            case 1:
            case 2:
            case 11:
            case 13:
            case 14:
            case 17:
            case 21:
            case 30:
                this.mDailyRptEvent.updateGpsPosReqCnt(false);
                break;
        }
        switch (errorcode) {
            case 0:
                code = "UNKNOWN_ISSUE";
                break;
            case 1:
                code = "GPS_POS_START_FAILED";
                break;
            case 2:
                code = "GPS_POS_STOP_FAILED";
                break;
            case 3:
                code = "GPS_XTRA_DLOAD_FAILED";
                break;
            case 4:
                code = "GPS_NTP_DLOAD_FAILED";
                break;
            case 5:
                code = "GPS_SET_POS_MODE_FAILED";
                break;
            case 6:
                code = "GPS_PERMISSION_DENIED";
                break;
            case 7:
                code = "GPS_OPEN_GPS_SWITCH_FAILED";
                break;
            case 8:
                code = "GPS_CLOSE_GPS_SWITCH_FAILED";
                break;
            case 9:
                code = "GPS_ADD_GEOFENCE_FAILED";
                break;
            case 10:
                code = "GPS_ADD_BATCHING_FAILED";
                break;
            case 11:
                code = "GPS_LOST_POSITION_FAILED";
                break;
            case 12:
                code = "GPS_WAKE_LOCK_NOT_RELEASE_FAILED";
                break;
            case 13:
                code = "STANDALONE_TIMEOUT";
                break;
            case 14:
                code = "AGPS_TIMEOUT";
                break;
            case 15:
                code = "HOTSTART_TIMEOUT";
                break;
            case 16:
                code = "NAVIGATION_ABORT";
                break;
            case 17:
                code = "DATA_DELIVERY_DELAY";
                break;
            case 18:
                code = "AGPS_CONN_FAILED";
                break;
            case 19:
                code = "GPS_LOW_SIGNAL_FAILED";
                break;
            case 20:
                code = "GPS_IN_DOOR_FAILED";
                break;
            case 21:
                code = "GPSD_NOT_RECOVERY_FAILED";
                break;
            case 22:
                code = "NETWORK_POSITION_TIMEOUT";
                break;
            case 23:
                code = "GPS_LOST_POSITION_UNSURE_FAILED";
                break;
            case 24:
                code = "GPS_INIT_FAILED";
                break;
            case 25:
                code = "GPS_DAILY_CNT_REPORT_FAILD";
                break;
            case 26:
                code = "GPS_NTP_WRONG";
                break;
            case 27:
                code = "GPS_XTRA_DATA_ERR";
                break;
            case 28:
                code = "GPS_SUPL_DATA_ERR";
                break;
            case 29:
                code = "GPS_LOCAL_DATA_ERR";
                break;
            case 30:
                code = "GPS_BRCM_ASSERT";
                break;
            case 31:
                code = "LOCATIONPROVIDER_BIND_FAIL";
                break;
            default:
                code = "UNKNOWN_ISSUE";
                break;
        }
        if (matchEventTriggerFreq(GnssLogManager.getInstance().isCommercialUser(), code)) {
            this.mGpsPosErrEvent.setNetworkAvailable(this.mNetAvailable);
            this.mGpsPosErrEvent.setNetworkInfo(this.mSubNetworkType);
            this.mGpsPosErrEvent.writePosErrInfo(errorcode);
            return;
        }
        Log.d(TAG, " errorcode :< " + code + " > is not match trigger conditions,just return !");
    }

    private boolean isRealConnNetwork() {
        boolean commercialUser = GnssLogManager.getInstance().isCommercialUser();
        if (commercialUser || this.isGlobalVersion) {
            Log.d(TAG, "commercialUser value is : " + commercialUser + " ,isGlobalVersion value is : " + this.isGlobalVersion + ",not do ping server test,just return !");
            return true;
        }
        boolean isConn = false;
        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = (HttpURLConnection) new URL(HwNetworkPropertyChecker.CHINA_MAINLAND_MAIN_SERVER).openConnection();
            httpURLConnection.setConnectTimeout(5000);
            if (httpURLConnection.getResponseCode() == 200) {
                isConn = true;
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        } catch (IOException e2) {
            e2.printStackTrace();
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        } catch (Throwable th) {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        Log.d(TAG, "isConnByHttp ,network status is : " + isConn);
        return isConn;
    }

    boolean matchEventTriggerFreq(boolean commercialUser, String suberror) {
        if (suberror == null) {
            return false;
        }
        boolean isMatch = true;
        long nowTime = SystemClock.elapsedRealtime();
        TriggerLimit triggerLimit;
        if (mapEventTriggerFreq.containsKey(suberror)) {
            triggerLimit = (TriggerLimit) mapEventTriggerFreq.get(suberror);
            if (triggerLimit != null) {
                if (nowTime - triggerLimit.lastUploadTime > 86400000) {
                    triggerLimit.triggerNum = 0;
                } else {
                    int triggerFreq = suberror.equalsIgnoreCase(GPS_SESSION_RPT) ? commercialUser ? 20 : 50 : suberror.equalsIgnoreCase(NETWORK_SESSION_RPT) ? commercialUser ? 20 : 50 : commercialUser ? 50 : 100;
                    if (triggerLimit.triggerNum > triggerFreq) {
                        isMatch = false;
                    }
                }
                if (isMatch) {
                    triggerLimit.triggerNum++;
                    triggerLimit.lastUploadTime = nowTime;
                }
            }
        } else {
            triggerLimit = new TriggerLimit();
            triggerLimit.triggerNum = 1;
            triggerLimit.lastUploadTime = nowTime;
            mapEventTriggerFreq.put(suberror, triggerLimit);
        }
        if (DEBUG) {
            Log.d(TAG, "GPS matchEventTriggerFreq , isMatch = " + isMatch);
        }
        return isMatch;
    }

    public void processGnssHalDriverEvent(String strJsonExceptionBody) {
        this.mHwHisiGnssManager.handleGnssHalDriverEvent(strJsonExceptionBody);
    }
}
