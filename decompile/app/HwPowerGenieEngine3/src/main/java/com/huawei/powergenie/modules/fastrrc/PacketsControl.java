package com.huawei.powergenie.modules.fastrrc;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Downloads.Impl;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.powergenie.api.BaseModule;
import com.huawei.powergenie.api.IAppManager;
import com.huawei.powergenie.api.IDeviceState;
import com.huawei.powergenie.api.IPolicy;
import com.huawei.powergenie.core.PowerAction;
import com.huawei.powergenie.debugtest.LogUtils;
import com.huawei.powergenie.integration.adapter.NativeAdapter;
import java.io.PrintWriter;
import java.util.HashMap;

public final class PacketsControl extends BaseModule {
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.pg_fd_debug", false);
    private boolean mAdjustFDByOpt = false;
    private Query mBaseQuery;
    private Context mContext;
    private boolean mDataTransStarted = false;
    private DownloadManager mDownloadManager;
    private DownloadObserver mDownloadObserver = new DownloadObserver();
    private boolean mDownloadRuning = false;
    private int mFDRRCNum = 0;
    private int mFrontAppType = -1;
    private int mFrontUid = -1;
    private boolean mInputMethodFront = false;
    private boolean mIsCDMA3GNetType = false;
    private boolean mIsCallingBusy = false;
    private boolean mIsListenState = false;
    private boolean mIsPacketsFD = false;
    private boolean mIsPacketsRRC = false;
    private boolean mIsTD3GNetType = false;
    private boolean mIsWCDMANetType = false;
    private long mMobileRxPkts = 0;
    private long mMobileTxPkts = 0;
    private PacketsFD mPacketsFD = null;
    private PacketsRRC mPacketsRRC = null;
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        public void onDataActivity(int direction) {
            if (direction == 0 || direction == 4) {
                if (PacketsControl.this.mDataTransStarted) {
                    PacketsControl.this.mDataTransStarted = false;
                    if (PacketsControl.DEBUG) {
                        Log.d("PacketsControl", "direction" + direction + ",mDataTransStarted:  " + PacketsControl.this.mDataTransStarted);
                    }
                    if (4 == direction) {
                        if (PacketsControl.this.mIsPacketsRRC && PacketsControl.this.mPacketsRRC != null) {
                            PacketsControl.this.mPacketsRRC.setListenerData(false);
                            PacketsControl.this.mPacketsRRC.mListenerCount = 0;
                            PacketsControl.this.mPacketsRRC.removePendingMessage(100);
                        }
                        return;
                    }
                    if (PacketsControl.this.mIsPacketsFD && PacketsControl.this.mPacketsFD != null) {
                        PacketsControl.this.mPacketsFD.startFastDormancy();
                    }
                    if (PacketsControl.this.mIsPacketsRRC && PacketsControl.this.mFrontUid != -1 && PacketsControl.this.mPacketsRRC != null && PacketsControl.this.isPermit(true)) {
                        long preUidPkts = 0;
                        if (PacketsControl.this.mUidPkts.containsKey(Integer.valueOf(PacketsControl.this.mFrontUid))) {
                            Long data = (Long) PacketsControl.this.mUidPkts.get(Integer.valueOf(PacketsControl.this.mFrontUid));
                            if (data != null) {
                                preUidPkts = data.longValue();
                            }
                        }
                        long pkts = PacketsControl.this.updateDataTransmitting(PacketsControl.this.mFrontUid);
                        if (pkts - preUidPkts > 0) {
                            if (PacketsControl.this.mFrontAppType == 1) {
                                if (PacketsControl.this.mInputMethodFront) {
                                    PacketsControl.this.mPacketsRRC.postDisableRrc(3000);
                                } else {
                                    PacketsControl.this.mPacketsRRC.postDisableRrc(2000);
                                }
                            } else if (PacketsControl.this.mFrontAppType == 2) {
                                PacketsControl.this.mPacketsRRC.postDisableRrc(4000);
                            }
                        }
                        if (PacketsControl.DEBUG) {
                            Log.d("PacketsControl", "UID:" + PacketsControl.this.mFrontUid + "  Pre-PKTS:" + preUidPkts + " ----->  " + "Now-PKTS:" + pkts);
                        }
                    }
                    if (PacketsControl.this.mIsPacketsRRC && PacketsControl.this.mPacketsRRC != null) {
                        PacketsControl.this.mPacketsRRC.listenerData();
                    }
                }
            } else if (!PacketsControl.this.mDataTransStarted) {
                PacketsControl.this.mDataTransStarted = true;
                if (PacketsControl.DEBUG) {
                    Log.d("PacketsControl", "direction" + direction + ",mDataTransStarted:  " + PacketsControl.this.mDataTransStarted);
                }
                if (PacketsControl.this.mIsPacketsFD && PacketsControl.this.mPacketsFD != null) {
                    PacketsControl.this.mPacketsFD.cancelFastDormancy();
                }
            }
        }

        public void onCallStateChanged(int state, String incomingNumber) {
            if (PacketsControl.DEBUG) {
                Log.i("PacketsControl", "onCallStateChanged state=" + state);
            }
            switch (state) {
                case NativeAdapter.PLATFORM_QCOM /*0*/:
                    PacketsControl.this.mIsCallingBusy = false;
                    return;
                case NativeAdapter.PLATFORM_MTK /*1*/:
                case NativeAdapter.PLATFORM_HI /*2*/:
                    PacketsControl.this.mIsCallingBusy = true;
                    PacketsControl.this.cancelAllPendingMsg();
                    if (PacketsControl.this.mIsPacketsRRC && PacketsControl.this.mPacketsRRC != null) {
                        PacketsControl.this.mPacketsRRC.setListenerData(false);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }

        public void onDataConnectionStateChanged(int state, int networkType) {
            if (PacketsControl.DEBUG) {
                Log.v("PacketsControl", "onDataConnectionStateChanged: state=" + state + " type=" + networkType);
            }
            if (2 == state) {
                PacketsControl.this.mIsWCDMANetType = false;
                PacketsControl.this.mIsCDMA3GNetType = false;
                PacketsControl.this.mIsTD3GNetType = false;
                switch (networkType) {
                    case NativeAdapter.PLATFORM_K3V3 /*3*/:
                    case 8:
                    case 9:
                    case 10:
                    case 15:
                        PacketsControl.this.mIsWCDMANetType = true;
                        return;
                    case 5:
                    case 6:
                    case 12:
                    case 14:
                        PacketsControl.this.mIsCDMA3GNetType = true;
                        return;
                    case 17:
                        PacketsControl.this.mIsTD3GNetType = true;
                        return;
                    case 18:
                    case 19:
                        if (PacketsControl.this.getCoreContext().isHisiPlatform()) {
                            PacketsControl.this.mIsTD3GNetType = true;
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        }
    };
    private IPolicy mPolicy;
    private boolean mRRCEnableNormaMode = SystemProperties.getBoolean("ro.config.rrc_normal_enable", false);
    private long mRrcLastTime = 0;
    private int mSceneRRCNum = 0;
    private TelephonyManager mTelephonyManager;
    private HashMap<Integer, Long> mUidPkts = new HashMap();

    private class DownloadObserver extends ContentObserver {
        public DownloadObserver() {
            super(new Handler());
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onChange(boolean selfChange) {
            if (PacketsControl.this.mBaseQuery == null) {
                Log.e("PacketsControl", "mBaseQuery is null");
                return;
            }
            Cursor cursor = PacketsControl.this.mDownloadManager.query(PacketsControl.this.mBaseQuery);
            if (cursor == null) {
                Log.w("PacketsControl", "cursor is null.");
                return;
            }
            Log.d("PacketsControl", "mDlCursor num:" + cursor.getCount() + " ,runing=" + PacketsControl.this.mDownloadRuning);
            try {
                if (cursor.getCount() <= 0) {
                    if (PacketsControl.this.mDownloadRuning) {
                        PacketsControl.this.mDownloadRuning = false;
                        if (PacketsControl.this.mIsPacketsRRC && PacketsControl.this.mPacketsRRC != null) {
                            PacketsControl.this.mPacketsRRC.setListenerData(false);
                            if (PacketsControl.this.isPermit(true)) {
                                PacketsControl.this.mPacketsRRC.postRrcScreenOffOrOn(500, 2000);
                            }
                        }
                        if (PacketsControl.DEBUG) {
                            Log.d("PacketsControl", "downloading end");
                        }
                        LogUtils.c("DOWNLOAD_FINISH", "TYPE_SYSTEM_DOWNLOAD");
                    }
                } else if (!PacketsControl.this.mDownloadRuning) {
                    PacketsControl.this.mDownloadRuning = true;
                    if (PacketsControl.this.mIsPacketsRRC && PacketsControl.this.mPacketsRRC != null) {
                        PacketsControl.this.mPacketsRRC.setListenerData(false);
                        PacketsControl.this.mPacketsRRC.removePendingMessage(100);
                    }
                    if (PacketsControl.DEBUG) {
                        Log.d("PacketsControl", "downloading start");
                    }
                }
                cursor.close();
            } catch (RuntimeException ex) {
                Log.e("PacketsControl", "RuntimeException:", ex);
            } catch (Throwable th) {
                cursor.close();
            }
        }
    }

    public void onCreate() {
        super.onCreate();
        this.mContext = getCoreContext().getContext();
        this.mPolicy = (IPolicy) getCoreContext().getService("policy");
        initPacketsRRC();
        initPacketsFD();
        Log.i("PacketsControl", "mIsPacketsRRC:" + this.mIsPacketsRRC + " mIsPacketsFD:" + this.mIsPacketsFD + " mAdjustFDByOpt:" + this.mAdjustFDByOpt);
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
    }

    public void onStart() {
        super.onStart();
        if (this.mIsPacketsRRC || this.mIsPacketsFD || this.mAdjustFDByOpt) {
            this.mDownloadRuning = false;
            this.mIsCallingBusy = false;
            this.mIsWCDMANetType = false;
            this.mIsCDMA3GNetType = false;
            this.mFrontUid = -1;
            this.mFrontAppType = -1;
            this.mInputMethodFront = false;
            if (this.mAdjustFDByOpt) {
                addAction(330);
            }
            addAction(350);
            addAction(302);
            if (this.mIsPacketsRRC && (this.mRRCEnableNormaMode || !this.mPolicy.isOffPowerMode())) {
                addRRCAction();
            }
            this.mDownloadManager = (DownloadManager) this.mContext.getSystemService("download");
            this.mDownloadManager.setAccessAllDownloads(true);
            this.mBaseQuery = new Query().setFilterByStatus(2);
            if ((this.mIsPacketsRRC && (this.mRRCEnableNormaMode || !this.mPolicy.isOffPowerMode())) || this.mIsPacketsFD || this.mAdjustFDByOpt) {
                listenState();
                return;
            }
            return;
        }
        Log.i("PacketsControl", "onStart, quick off rrc and FD close.");
    }

    public boolean handleAction(PowerAction action) {
        if (!super.handleAction(action)) {
            return true;
        }
        if (DEBUG) {
            Log.d("PacketsControl", "handleAction:" + action);
        }
        switch (action.getActionId()) {
            case 201:
            case 202:
            case 216:
                if (this.mIsPacketsRRC && this.mPacketsRRC != null) {
                    this.mPacketsRRC.handleActionInner(action);
                    break;
                }
            case 208:
                this.mFrontUid = -1;
                this.mFrontAppType = -1;
                break;
            case 210:
                this.mInputMethodFront = true;
                break;
            case 211:
                this.mInputMethodFront = false;
                break;
            case 224:
                if (getCoreContext().isScreenOff() && this.mIsPacketsRRC && this.mPacketsRRC != null && !this.mIsPacketsFD) {
                    this.mPacketsRRC.handleActionInner(action);
                    break;
                }
            case 248:
                this.mFrontUid = getUidByPkgName(action.getPkgName());
                this.mFrontAppType = 1;
                if (this.mFrontUid != -1) {
                    updateDataTransmitting(this.mFrontUid);
                    break;
                }
                break;
            case 249:
                if (this.mIsPacketsRRC && this.mPacketsRRC != null) {
                    if (!getCoreContext().isScreenOff() && (this.mFrontAppType == 1 || this.mFrontAppType == 2)) {
                        if (DEBUG) {
                            Log.d("PacketsControl", "IM app is running at front, and the IM app's notification comming ,do nothing.");
                            break;
                        }
                    }
                    this.mPacketsRRC.handleActionInner(action);
                    break;
                }
                break;
            case 250:
                this.mFrontUid = getUidByPkgName(action.getPkgName());
                this.mFrontAppType = 2;
                if (this.mFrontUid != -1) {
                    updateDataTransmitting(this.mFrontUid);
                    break;
                }
                break;
            case 300:
            case 301:
                if (this.mIsPacketsFD && this.mPacketsFD != null) {
                    this.mPacketsFD.handleActionInner(action);
                    break;
                }
            case 302:
                if (this.mIsPacketsFD || this.mAdjustFDByOpt) {
                    getCoreContext().releaseRRC();
                    break;
                }
            case 329:
                if (this.mIsPacketsFD && this.mIsPacketsRRC) {
                    this.mIsPacketsRRC = false;
                    stopRRCListen();
                    removeAction(329);
                    Log.i("PacketsControl", "not china operator, Fastdormancy start, close RRC feature");
                    break;
                }
            case 330:
                if (this.mPacketsFD != null) {
                    boolean fdEnable = this.mPacketsFD.handleSimStateChg();
                    if (this.mIsPacketsFD != fdEnable) {
                        this.mIsPacketsFD = fdEnable;
                        Log.i("PacketsControl", "sim state change, mIsPacketsFD:" + this.mIsPacketsFD);
                        if (this.mIsPacketsFD) {
                            registerFDFun();
                        } else {
                            unregisterFDFun();
                        }
                    }
                    if (this.mIsPacketsFD) {
                        this.mPacketsFD.handleActionInner(action);
                        break;
                    }
                }
                break;
            case 350:
                if (!this.mRRCEnableNormaMode) {
                    handlePowerMode(this.mPolicy.getOldPowerMode(), action.getExtraInt());
                    break;
                }
                break;
            default:
                Log.w("PacketsControl", "action unknown!!!");
                break;
        }
        return true;
    }

    private void initPacketsRRC() {
        if (SystemProperties.getBoolean("ro.config.pg_rrc_enable", true)) {
            this.mIsPacketsRRC = this.mPolicy.supportScenarioRRC();
            registerRRCFun();
            return;
        }
        Log.i("PacketsControl", "rrc feature is closed.");
    }

    private void initPacketsFD() {
        if (getCoreContext().isQcommPlatform()) {
            this.mPacketsFD = new PacketsFD(this, getCoreContext());
            this.mIsPacketsFD = this.mPacketsFD.initializeFDFeature();
            this.mAdjustFDByOpt = this.mPacketsFD.isAdjustFDFeatureByOpt();
            if ((this.mIsPacketsFD || this.mAdjustFDByOpt) && this.mIsPacketsRRC) {
                Log.i("PacketsControl", "listern operator chg");
                addAction(329);
            }
            if (this.mIsPacketsFD) {
                registerFDFun();
                return;
            }
            return;
        }
        Log.i("PacketsControl", "fd is not valid for the platform.");
    }

    private void handlePowerMode(int oldMode, int newMode) {
        if (newMode == 3) {
            stopRRCListen();
        } else if (oldMode == 3) {
            this.mFrontUid = -1;
            this.mFrontAppType = -1;
            this.mInputMethodFront = false;
            if (this.mIsPacketsRRC) {
                listenState();
            }
            if (this.mIsPacketsRRC) {
                registerRRCFun();
                addRRCAction();
            }
        }
    }

    private void stopRRCListen() {
        unregisterRRCFun();
        removeRRCAction();
        if (!this.mIsPacketsFD && !this.mAdjustFDByOpt) {
            unlistenState();
        }
    }

    private void listenState() {
        if (!this.mIsListenState) {
            this.mContext.getContentResolver().registerContentObserver(Impl.ALL_DOWNLOADS_CONTENT_URI, true, this.mDownloadObserver, -1);
            this.mTelephonyManager.listen(this.mPhoneStateListener, 225);
            this.mIsListenState = true;
        }
    }

    private void unlistenState() {
        if (this.mIsListenState) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mDownloadObserver);
            this.mTelephonyManager.listen(this.mPhoneStateListener, 0);
            this.mIsListenState = false;
        }
    }

    private void registerRRCFun() {
        if (this.mIsPacketsRRC && this.mPacketsRRC == null) {
            this.mPacketsRRC = new PacketsRRC(this, getCoreContext());
        }
    }

    private void unregisterRRCFun() {
        if (this.mIsPacketsRRC && this.mPacketsRRC != null) {
            this.mPacketsRRC = null;
        }
    }

    private void registerFDFun() {
        addFDAction();
    }

    private void unregisterFDFun() {
        if (this.mPacketsFD != null) {
            this.mPacketsFD.cancelFastDormancy();
            this.mPacketsFD.stopCheckDataActivity();
        }
        removeFDAction();
    }

    private void addFDAction() {
        addAction(300);
        addAction(301);
        addAction(224);
    }

    private void removeFDAction() {
        removeAction(300);
        removeAction(301);
    }

    private void addRRCAction() {
        addAction(201);
        addAction(202);
        addAction(224);
        addAction(216);
        addAction(249);
        addAction(248);
        addAction(208);
        addAction(210);
        addAction(211);
        addAction(250);
    }

    private void removeRRCAction() {
        removeAction(201);
        removeAction(202);
        removeAction(216);
        removeAction(249);
        removeAction(248);
        removeAction(208);
        removeAction(210);
        removeAction(211);
        removeAction(250);
    }

    private int getUidByPkgName(String pkg) {
        int uid = -1;
        if (!TextUtils.isEmpty(pkg)) {
            uid = ((IAppManager) getCoreContext().getService("appmamager")).getUidByPkg(pkg);
        }
        if (DEBUG) {
            Log.d("PacketsControl", "Pkg:" + pkg + "  Uid:" + uid);
        }
        return uid;
    }

    protected boolean isSupportFDFeature() {
        return this.mIsPacketsFD;
    }

    protected boolean isPermit(boolean enableLog) {
        IDeviceState deviceState = (IDeviceState) getCoreContext().getService("device");
        if (deviceState.isWiFiConnected()) {
            if (enableLog) {
                Log.d("PacketsControl", "wifi connected. do nothing.");
            }
            return false;
        } else if (!deviceState.isMobileConnected()) {
            if (enableLog) {
                Log.d("PacketsControl", "mobile network is unavailabe. do nothing.");
            }
            return false;
        } else if (!isWCDMANetType() && !isTD3GNetType() && !isCDMA3GNetType()) {
            if (enableLog) {
                Log.i("PacketsControl", "not 3G net type. do nothing.");
            }
            return false;
        } else if (deviceState.isTetheredMode()) {
            if (enableLog) {
                Log.d("PacketsControl", "is tethered mode. do nothing.");
            }
            return false;
        } else if (this.mIsCallingBusy) {
            if (enableLog) {
                Log.d("PacketsControl", "is calling busy. do nothing.");
            }
            return false;
        } else if (!this.mDownloadRuning) {
            return true;
        } else {
            if (enableLog) {
                Log.d("PacketsControl", "downloadling now. do nothing.");
            }
            return false;
        }
    }

    protected long updateDataTransmitting() {
        long preTxPkts = this.mMobileTxPkts;
        long preRxPkts = this.mMobileRxPkts;
        this.mMobileTxPkts = TrafficStats.getMobileTxPackets();
        this.mMobileRxPkts = TrafficStats.getMobileRxPackets();
        if (DEBUG) {
            Log.d("PacketsControl", "[DATA]tx: " + preTxPkts + " ==> " + this.mMobileTxPkts);
            Log.d("PacketsControl", "[DATA]rx:  " + preRxPkts + " ==> " + this.mMobileRxPkts);
        }
        long deltaTx = this.mMobileTxPkts - preTxPkts;
        long deltaRx = this.mMobileRxPkts - preRxPkts;
        if (DEBUG) {
            Log.d("PacketsControl", "[DATA]delta rx " + deltaRx + " tx " + deltaTx);
        }
        return deltaTx + deltaRx;
    }

    protected long updateDataTransmitting(int uid) {
        long tx = TrafficStats.getUidTxBytes(uid);
        long rx = TrafficStats.getUidRxBytes(uid);
        long data = tx + rx;
        if (DEBUG) {
            Log.d("PacketsControl", "[DATA] ALL: " + data + " TX:" + tx + " RX:" + rx);
        }
        this.mUidPkts.put(Integer.valueOf(uid), Long.valueOf(data));
        return data;
    }

    protected boolean isWCDMANetType() {
        return this.mIsWCDMANetType;
    }

    protected boolean isCDMA3GNetType() {
        return this.mIsCDMA3GNetType;
    }

    protected boolean isTD3GNetType() {
        return this.mIsTD3GNetType;
    }

    private void cancelAllPendingMsg() {
        if (this.mIsPacketsRRC && this.mPacketsRRC != null) {
            this.mPacketsRRC.cancelPendingMsg();
        }
        if (this.mIsPacketsFD && this.mPacketsFD != null) {
            this.mPacketsFD.cancelFastDormancy();
        }
    }

    protected boolean isDataTrans() {
        return this.mDataTransStarted;
    }

    protected boolean offRRC(int type) {
        if (!isPermit(true)) {
            return false;
        }
        boolean result = false;
        long now = SystemClock.elapsedRealtime();
        long interval = now - this.mRrcLastTime;
        long minInterval = 10000;
        if (type == 2) {
            if (getCoreContext().isScreenOff()) {
                minInterval = (long) this.mPacketsFD.mDelaytimeScreenOff;
            } else {
                minInterval = (long) this.mPacketsFD.mDelaytimeScreenOn;
            }
        } else if (type == 1) {
            if (this.mIsWCDMANetType || this.mIsTD3GNetType) {
                minInterval = 10000;
            } else {
                minInterval = 20000;
            }
        }
        if (interval >= minInterval) {
            result = getCoreContext().releaseRRC();
            if (result) {
                if (type == 2) {
                    LogUtils.c("RELEASE_RRC", "RRC_FD");
                    Log.i("PacketsControl", "Off RRC, type: RRC_FD");
                    this.mFDRRCNum++;
                } else {
                    LogUtils.c("RELEASE_RRC", "RRC_SCENE");
                    Log.i("PacketsControl", "Off RRC, type: RRC_SCENE");
                    this.mSceneRRCNum++;
                }
                this.mRrcLastTime = now;
            }
        } else if (DEBUG) {
            Log.d("PacketsControl", (type == 2 ? "RRC_FD" : "RRC_SCENE") + " need wait for: " + (minInterval - interval) + "ms");
        }
        return result;
    }

    public boolean dump(PrintWriter pw, String[] args) {
        pw.println("\nPGMODULE FD RRC");
        pw.println("    FD rrc num: " + this.mFDRRCNum);
        pw.println("    SC rrc num: " + this.mSceneRRCNum);
        return true;
    }
}
