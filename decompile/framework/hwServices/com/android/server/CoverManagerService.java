package com.android.server;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.cover.ICoverManager.Stub;
import android.cover.ICoverViewDelegate;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.telephony.MSimTelephonyManager;
import android.telephony.TelephonyManager;
import android.util.Flog;
import android.util.Jlog;
import android.util.Log;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.wm.WindowManagerService;
import com.huawei.android.hwutil.CommandLineUtil;
import com.huawei.android.util.NoExtAPIException;
import java.io.File;
import java.util.List;

public class CoverManagerService extends Stub {
    private static final long ACTION_TIME = 500;
    private static final String CONSTANTS_USER = "root";
    static final String COVER_NOTIFY_ACTION = "android.settings.SMART_COVER_SETTINGS";
    private static final int COVER_TYPE_NORMAL = 0;
    private static final int COVER_TYPE_SIMPLE = 1;
    private static boolean DEBUG = false;
    private static final int DELAY_TIME = 1000;
    private static final long DISMISS_KG_TIME = 200;
    static final String GSETTINGS_COVER_ENABLED = "cover_enabled";
    static final String GSETTINGS_COVER_NOTIFY = "cover_notify";
    private static final int HALL_STATE_CLOSE = 0;
    private static final int HALL_STATE_OPEN = 1;
    private static final String HALL_UEVENT_CLOSE = "CLOSE";
    private static final String HALL_UEVENT_OPEN = "OPEN";
    private static final String KEYGUARD_PERMISSION = "android.permission.CONTROL_KEYGUARD";
    private static final String LOCK = "1";
    private static final String LOCK_SENSITIVE_PATH = "/sys/touchscreen/touch_sensitivity";
    private static final String LOCK_TOUCH_PATH = "/sys/touchscreen/touch_window";
    private static final long MIN_ACTION_TIME = 20;
    private static final String MIN_TOUCH_AREA = " 0 0 1 1";
    private static final String MMITest_IS_RUNNING = "runtime.mmitest.isrunning";
    private static final int MSG_ADD_COVER_SCREEN = 1;
    private static final int MSG_ADD_SENSITIVE = 6;
    private static final int MSG_COVER_DISABLE = 9;
    private static final int MSG_COVER_ENABLE = 8;
    private static final int MSG_DEVICE_FAR = 0;
    private static final int MSG_DEVICE_NEAR = 1;
    private static final int MSG_DISMISS_KG = 5;
    private static final int MSG_REMOVE_BLOCK = 7;
    private static final int MSG_REMOVE_COVER_SCREEN = 2;
    private static final String NOTIFY_STATE = "settings:ui_cover";
    private static final String RELEASE = "0";
    private static final String RELEASE_BLOCK = "0 0 0 0 0";
    private static final int SCREEN_TIEM_OUT = 10000;
    static final String SETTINGS_COVER_TYPE = "cover_type";
    private static final String SPACE = " ";
    static final String TAG = "coverxcms";
    private static final int TYPE_HALL = 10002;
    private static final long WAKE_UP_TIME = 3000;
    private static final String qctLOCK_TOUCH_PATH = "/sys/touchscreen/holster_touch_window";
    private static String sRealSensitiveDevPath = LOCK_SENSITIVE_PATH;
    private static String sRealTouchDevPath = LOCK_TOUCH_PATH;
    final Context mContext;
    private ContentObserver mCoverEnableObserver;
    boolean mCoverPrevOpen = true;
    private IBinder mCoverViewBinder;
    private ICoverViewDelegate mCoverViewService;
    private final CoverManagerHandler mHandler;
    private HisiHall mHisiHall;
    boolean mIsCoverOpen = true;
    private long mLastChange = 0;
    private String mOder = AppHibernateCst.INVALID_PKG;
    PowerManager mPM;
    WindowManagerService mWindowManager;
    private WakeLock wlock;

    private final class CoverManagerHandler extends Handler {
        public CoverManagerHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    CoverManagerService.this.handleAddCoverScreen();
                    break;
                case 2:
                    CoverManagerService.this.handleRemoveCoverScreen();
                    break;
                case 5:
                    CoverManagerService.this.mWindowManager.dismissKeyguard();
                    break;
                case 6:
                    CommandLineUtil.echo(CoverManagerService.CONSTANTS_USER, "1" + CoverManagerService.this.mOder, CoverManagerService.sRealTouchDevPath);
                    CommandLineUtil.echo(CoverManagerService.CONSTANTS_USER, "1", CoverManagerService.sRealSensitiveDevPath);
                    break;
                case 7:
                    CommandLineUtil.echo(CoverManagerService.CONSTANTS_USER, CoverManagerService.RELEASE_BLOCK, CoverManagerService.sRealTouchDevPath);
                    break;
                case 8:
                    CoverManagerService.this.mHisiHall.registerForHallSensor();
                    break;
                case 9:
                    if (CoverManagerService.this.mIsCoverOpen) {
                        CoverManagerService.this.mHisiHall.unregisterForHallSensor();
                        break;
                    }
                    break;
                default:
                    return;
            }
        }
    }

    class HisiHall implements SensorEventListener {
        private int COVERED = 1;
        private SensorManager mSensorManager;

        HisiHall() {
            if (CoverManagerService.this.isCoverEnabledInSettingsDb()) {
                registerForHallSensor();
            }
        }

        private SensorManager getSensorManager() {
            if (this.mSensorManager == null) {
                this.mSensorManager = (SensorManager) CoverManagerService.this.mContext.getSystemService("sensor");
            }
            return this.mSensorManager;
        }

        private boolean shouldAvoidCoverForMMI() {
            ActivityManager am = (ActivityManager) CoverManagerService.this.mContext.getSystemService("activity");
            boolean shouldNoAction = false;
            if (am != null) {
                try {
                    if (!(am.getRunningTasks(1) == null || am.getRunningTasks(1).size() <= 0 || am.getRunningTasks(1).get(0) == null)) {
                        ComponentName cn = ((RunningTaskInfo) am.getRunningTasks(1).get(0)).topActivity;
                        if (cn != null && ("com.huawei.mmitest".equals(cn.getPackageName()) || "com.huawei.mmitest2".equals(cn.getPackageName()) || SystemProperties.get("ro.runmode", "normal").equalsIgnoreCase("factory"))) {
                            if (!CoverManagerService.this.mIsCoverOpen) {
                                Flog.w(MemoryConstant.MSG_PROTECTLRU_SWITCH, "shouldAvoidCoverForMMI top is mmitest");
                            }
                            shouldNoAction = true;
                        } else if (SystemProperties.getBoolean(CoverManagerService.MMITest_IS_RUNNING, false)) {
                            shouldNoAction = true;
                            if (!CoverManagerService.this.mIsCoverOpen) {
                                Flog.w(MemoryConstant.MSG_PROTECTLRU_SWITCH, "shouldAvoidCoverForMMI MMITest_IS_RUNNING on");
                            }
                        }
                    }
                } catch (Exception e) {
                    Flog.w(MemoryConstant.MSG_PROTECTLRU_SWITCH, "shouldAvoidCoverForMMI got Exception:", e);
                }
            }
            if (!CoverManagerService.this.mIsCoverOpen) {
                Flog.i(MemoryConstant.MSG_PROTECTLRU_SWITCH, "shouldAvoidCoverForMMI ret shouldNoAction:" + shouldNoAction);
            }
            return shouldNoAction;
        }

        private void adjustKeyguardShowState(boolean isCoverOpen) {
            CoverManagerService.this.mWindowManager.setCoverManagerState(isCoverOpen);
        }

        public boolean registerForHallSensor() {
            List<Sensor> sensors = getSensorManager().getSensorList(CoverManagerService.TYPE_HALL);
            if (sensors.isEmpty()) {
                Flog.w(MemoryConstant.MSG_PROTECTLRU_SWITCH, "registerHallSensor no target sensor!");
                return false;
            }
            CommandLineUtil.echo(CoverManagerService.CONSTANTS_USER, "0", CoverManagerService.sRealSensitiveDevPath);
            CoverManagerService.this.removeBlockArea(CoverManagerService.MIN_ACTION_TIME);
            return getSensorManager().registerListener(this, (Sensor) sensors.get(0), 0);
        }

        public void unregisterForHallSensor() {
            List<Sensor> sensors = getSensorManager().getSensorList(CoverManagerService.TYPE_HALL);
            if (sensors.isEmpty()) {
                Flog.w(MemoryConstant.MSG_PROTECTLRU_SWITCH, "unregisterHallSensor no target sensor!");
                return;
            }
            CommandLineUtil.echo(CoverManagerService.CONSTANTS_USER, "0", CoverManagerService.sRealSensitiveDevPath);
            CoverManagerService.this.removeBlockArea(CoverManagerService.MIN_ACTION_TIME);
            getSensorManager().unregisterListener(this, (Sensor) sensors.get(0));
        }

        public void onSensorChanged(SensorEvent event) {
            boolean z = false;
            if (event.sensor.getType() == CoverManagerService.TYPE_HALL) {
                long timefromlast = SystemClock.elapsedRealtime() - CoverManagerService.this.mLastChange;
                if (timefromlast < CoverManagerService.MIN_ACTION_TIME) {
                    try {
                        Thread.sleep(CoverManagerService.MIN_ACTION_TIME - timefromlast);
                    } catch (Exception e) {
                        Flog.w(MemoryConstant.MSG_PROTECTLRU_SWITCH, "onSensorChanged->thread sleep intrupt exception happened");
                    }
                }
                CoverManagerService.this.mLastChange = SystemClock.elapsedRealtime();
                CoverManagerService.this.wlock.acquire(CoverManagerService.WAKE_UP_TIME);
                int mHallValue = (int) event.values[0];
                Flog.i(MemoryConstant.MSG_PROTECTLRU_SWITCH, "handle HALL sensor event mHallValue:" + mHallValue + " prev:" + CoverManagerService.this.mCoverPrevOpen);
                if ((this.COVERED & mHallValue) != 0) {
                    CoverManagerService.this.mIsCoverOpen = false;
                } else {
                    Jlog.d(83, "Cover Open");
                    CoverManagerService.this.mIsCoverOpen = true;
                }
                if (!CoverManagerService.this.mIsCoverOpen && shouldAvoidCoverForMMI()) {
                    Flog.w(MemoryConstant.MSG_PROTECTLRU_SWITCH, "avoid cover on for mmitest return early");
                } else if (CoverManagerService.this.mCoverPrevOpen != CoverManagerService.this.mIsCoverOpen) {
                    CoverManagerService.this.mCoverPrevOpen = CoverManagerService.this.mIsCoverOpen;
                    if (!CoverManagerService.this.mIsCoverOpen) {
                        z = true;
                    }
                    HwServiceFactory.setIfCoverClosed(z);
                    adjustKeyguardShowState(CoverManagerService.this.mIsCoverOpen);
                    if (!CoverManagerService.this.mCoverPrevOpen) {
                        CommandLineUtil.echo(CoverManagerService.CONSTANTS_USER, "1 0 0 1 1", CoverManagerService.sRealTouchDevPath);
                    } else if (CoverManagerService.this.mHandler.hasMessages(5)) {
                        CoverManagerService.this.mHandler.removeMessages(5);
                        CoverManagerService.this.mWindowManager.dismissKeyguard();
                    }
                    if (!CoverManagerService.this.mIsCoverOpen) {
                        Flog.i(MemoryConstant.MSG_PROTECTLRU_SWITCH, "onSensorChanged cover closed");
                        Global.putString(CoverManagerService.this.mContext.getContentResolver(), "single_hand_mode", AppHibernateCst.INVALID_PKG);
                        CoverManagerService.this.sendCoverStateChangedBroadcast();
                        if (CoverManagerService.this.getCoverType() == 1) {
                            CoverManagerService.this.dismissKeyguardAsyncly(200);
                            CoverManagerService.this.mPM.goToSleep(SystemClock.uptimeMillis());
                        } else {
                            CoverManagerService.this.addCoverScreenWindow();
                        }
                    } else if (CoverManagerService.this.getCoverType() == 1) {
                        CoverManagerService.this.mPM.wakeUp(SystemClock.uptimeMillis());
                        CommandLineUtil.echo(CoverManagerService.CONSTANTS_USER, CoverManagerService.RELEASE_BLOCK, CoverManagerService.sRealTouchDevPath);
                        CoverManagerService.this.sendCoverStateChangedBroadcast();
                    } else {
                        CoverManagerService.this.removeCoverScreenWindow();
                    }
                }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    public CoverManagerService(Context context, WindowManagerService wm, Handler handler) {
        this.mContext = context;
        this.mWindowManager = wm;
        this.mPM = (PowerManager) context.getSystemService("power");
        this.mHandler = new CoverManagerHandler(handler.getLooper());
        this.wlock = this.mPM.newWakeLock(1, "COVER_WAKE_LOCK");
    }

    public boolean isCoverOpen() {
        return this.mIsCoverOpen;
    }

    public void setCoverViewBinder(IBinder binder) {
        if (this.mContext.checkCallingOrSelfPermission(KEYGUARD_PERMISSION) != 0) {
            Log.w(TAG, "Caller needs permission 'android.permission.CONTROL_KEYGUARD");
            return;
        }
        this.mCoverViewBinder = binder;
        Flog.i(MemoryConstant.MSG_PROTECTLRU_SWITCH, "setCoverBinder, binder = " + binder);
        this.mCoverViewService = ICoverViewDelegate.Stub.asInterface(binder);
    }

    private void sendCoverStateChangedBroadcast() {
        try {
            Intent intent = new Intent("com.huawei.android.cover.STATE");
            intent.putExtra("coverOpen", this.mIsCoverOpen);
            Flog.i(MemoryConstant.MSG_PROTECTLRU_SWITCH, "sendCoverStateChangedBroadcast mIsCoverOpen:" + this.mIsCoverOpen);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
            if (this.mIsCoverOpen) {
                Jlog.d(86, "Broadcast_CoverOpen");
            }
        } catch (Exception e) {
            Flog.w(MemoryConstant.MSG_PROTECTLRU_SWITCH, "sendbroadcast Exception:", e);
        }
    }

    public void addCoverScreenWindow() {
        Message m = this.mHandler.obtainMessage(1);
        if (isPhoneInCall()) {
            if (this.mHandler.hasMessages(1)) {
                this.mHandler.removeMessages(1);
            }
            this.mHandler.sendMessageDelayed(m, 1000);
            addSensitiveMode();
            return;
        }
        this.mHandler.sendMessage(m);
    }

    private void removeCoverScreenWindow() {
        Message m = this.mHandler.obtainMessage(2);
        if (this.mHandler.hasMessages(1)) {
            this.mHandler.removeMessages(1);
        }
        this.mHandler.sendMessage(m);
    }

    private void addSensitiveMode() {
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(6), 500);
    }

    private void removeBlockArea() {
        removeBlockArea(500);
    }

    private void removeBlockArea(long timeout) {
        if (this.mHandler.hasMessages(7)) {
            this.mHandler.removeMessages(7);
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(7), timeout);
    }

    private void dismissKeyguardAsyncly(long timeout) {
        if (this.mHandler.hasMessages(5)) {
            this.mHandler.removeMessages(5);
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(5), timeout);
    }

    private void handleAddCoverScreen() {
        try {
            if (this.mCoverViewService != null && this.mCoverViewBinder.isBinderAlive()) {
                this.mCoverViewService.addCoverScreenWindow();
            }
            if (!isPhoneInCall()) {
                addSensitiveMode();
            }
        } catch (Exception e) {
            Flog.w(MemoryConstant.MSG_PROTECTLRU_SWITCH, "handleAddCoverScreen Exception:", e);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleRemoveCoverScreen() {
        try {
            if (this.mHandler.hasMessages(6)) {
                this.mHandler.removeMessages(6);
            }
            CommandLineUtil.echo(CONSTANTS_USER, "0", sRealSensitiveDevPath);
            Jlog.d(84, "Remove Screen begin");
            if (this.mCoverViewService != null && this.mCoverViewBinder.isBinderAlive()) {
                this.mCoverViewService.removeCoverScreenWindow();
                Jlog.d(85, "Remove Screen end");
            }
            removeBlockArea();
            Flog.i(MemoryConstant.MSG_PROTECTLRU_SWITCH, "CoverScreenRemoved called");
        } catch (Exception e) {
            Flog.w(MemoryConstant.MSG_PROTECTLRU_SWITCH, "handleRemoveCoverScreen Exception:", e);
        } catch (Throwable th) {
            removeBlockArea();
            Flog.i(MemoryConstant.MSG_PROTECTLRU_SWITCH, "CoverScreenRemoved called");
        }
    }

    private void adjustDevPathWhenReady() {
        if (new File(sRealTouchDevPath).exists()) {
            Flog.i(MemoryConstant.MSG_PROTECTLRU_SWITCH, "adjustDevPathWhenReady LOCK_TOUCH_PATH ready");
        } else {
            sRealTouchDevPath = qctLOCK_TOUCH_PATH;
            Flog.i(MemoryConstant.MSG_PROTECTLRU_SWITCH, "adjustDevPathWhenReady qctLOCK_TOUCH_PATH ready:" + new File(sRealTouchDevPath).exists());
        }
        if (new File(sRealSensitiveDevPath).exists()) {
            Flog.i(MemoryConstant.MSG_PROTECTLRU_SWITCH, "adjustDevPathWhenReady LOCK_SENSITIVE_PATH ready");
            return;
        }
        Flog.i(MemoryConstant.MSG_PROTECTLRU_SWITCH, "adjustDevPathWhenReady qctLOCK_SENSITIVE_PATH no need");
        sRealSensitiveDevPath = null;
    }

    private void initTouchArea(String[] coverloc) {
        String[] location = SystemProperties.get("ro.config.cover_toucharea").split(",");
        if (location == null || location.length != 4) {
            location = coverloc;
        } else if (DEBUG) {
            Log.v(TAG, "initTouchArea cover_toucharea ready");
        }
        if (location == null || location.length != 4) {
            Flog.w(MemoryConstant.MSG_PROTECTLRU_SWITCH, "initTouchArea location not set!");
            return;
        }
        int xStart = Integer.parseInt(location[0]);
        int xEnd = Integer.parseInt(location[2]);
        int yStart = Integer.parseInt(location[1]);
        this.mOder = SPACE + xStart + SPACE + yStart + SPACE + xEnd + SPACE + Integer.parseInt(location[3]);
    }

    public void systemReady() {
        adjustDevPathWhenReady();
        try {
            String[] location = SystemProperties.get("ro.config.huawei_smallwindow").split(",");
            if (location.length == 4) {
                initTouchArea(location);
                this.mCoverEnableObserver = new ContentObserver(this.mHandler) {
                    public void onChange(boolean selfChange) {
                        super.onChange(selfChange);
                        boolean mCoverEnabled = CoverManagerService.this.isCoverEnabledInSettingsDb();
                        if (mCoverEnabled) {
                            CoverManagerService.this.mHandler.sendMessage(CoverManagerService.this.mHandler.obtainMessage(8));
                        } else {
                            CoverManagerService.this.mHandler.sendMessage(CoverManagerService.this.mHandler.obtainMessage(9));
                        }
                        if (CoverManagerService.DEBUG) {
                            Log.d(CoverManagerService.TAG, "COVER_ENABLED state = " + mCoverEnabled);
                        }
                    }
                };
                this.mContext.getContentResolver().registerContentObserver(Global.getUriFor(GSETTINGS_COVER_ENABLED), false, this.mCoverEnableObserver);
                this.mHisiHall = new HisiHall();
                return;
            }
            Flog.w(MemoryConstant.MSG_PROTECTLRU_SWITCH, "initTouchArea cover win not set!");
        } catch (Exception e) {
            Flog.w(MemoryConstant.MSG_PROTECTLRU_SWITCH, "initTouchArea Exception:", e);
        }
    }

    private boolean isCoverEnabledInSettingsDb() {
        return Global.getInt(this.mContext.getContentResolver(), GSETTINGS_COVER_ENABLED, 1) != 0;
    }

    private int getCoverType() {
        return Global.getInt(this.mContext.getContentResolver(), SETTINGS_COVER_TYPE, 0);
    }

    private static boolean isMultiSimEnabled() {
        boolean flag = false;
        try {
            flag = MSimTelephonyManager.getDefault().isMultiSimEnabled();
        } catch (NoExtAPIException e) {
            Flog.w(MemoryConstant.MSG_PROTECTLRU_SWITCH, "isMultiSimEnabled NoExtAPIException:", e);
        }
        return flag;
    }

    private boolean isPhoneInCall() {
        boolean phoneInCall = false;
        if (isMultiSimEnabled()) {
            for (int i = 0; i < MSimTelephonyManager.getDefault().getPhoneCount(); i++) {
                if (MSimTelephonyManager.getDefault().getCallState(i) != 0) {
                    phoneInCall = true;
                    break;
                }
            }
        } else if (TelephonyManager.getDefault().getCallState() != 0) {
            phoneInCall = true;
        }
        if (!this.mIsCoverOpen) {
            Flog.i(MemoryConstant.MSG_PROTECTLRU_SWITCH, "check isPhoneInCall:" + phoneInCall);
        }
        return phoneInCall;
    }
}
