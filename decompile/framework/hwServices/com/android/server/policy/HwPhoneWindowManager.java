package com.android.server.policy;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.cover.CoverManager;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.input.InputManager;
import android.hdm.HwDeviceManager;
import android.hwcontrol.HwWidgetFactory;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.media.IAudioService;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.SystemVibrator;
import android.os.Trace;
import android.os.UserHandle;
import android.os.Vibrator;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.provider.SettingsEx.Systemex;
import android.telecom.TelecomManager;
import android.telephony.MSimTelephonyManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Flog;
import android.util.HwSlog;
import android.util.Jlog;
import android.util.Log;
import android.util.Slog;
import android.view.Display;
import android.view.IWindowManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerGlobal;
import android.view.WindowManagerPolicy.KeyguardDismissDoneListener;
import android.view.WindowManagerPolicy.PointerEventListener;
import android.view.WindowManagerPolicy.ScreenOnListener;
import android.view.WindowManagerPolicy.WindowManagerFuncs;
import android.view.WindowManagerPolicy.WindowState;
import android.view.accessibility.AccessibilityManager.TouchExplorationStateChangeListener;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.ITelephony.Stub;
import com.android.server.LocalServices;
import com.android.server.PPPOEStateMachine;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.HwActivityManagerService;
import com.android.server.input.HwInputManagerService.HwInputManagerServiceInternal;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import com.android.server.rms.iaware.cpu.CPUFeature;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.statusbar.StatusBarManagerInternal;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.android.util.NoExtAPIException;
import com.huawei.forcerotation.HwForceRotationManager;
import huawei.android.app.IHwWindowCallback;
import huawei.android.os.HwGeneralManager;
import huawei.android.provider.FingerSenseSettings;
import huawei.android.provider.FrontFingerPrintSettings;
import huawei.com.android.internal.widget.HwWidgetUtils;
import huawei.com.android.server.policy.HwScreenOnProximityLock;
import huawei.com.android.server.policy.fingersense.SystemWideActionsListener;
import huawei.cust.HwCustUtils;
import java.util.HashSet;
import java.util.List;

public class HwPhoneWindowManager extends PhoneWindowManager implements TouchExplorationStateChangeListener {
    private static final String ACTION_HUAWEI_VASSISTANT_SERVICE = "com.huawei.ziri.model.MODELSERVICE";
    static final boolean DEBUG = false;
    static final boolean DEBUG_IMMERSION = false;
    private static final int DEFAULT_RESULT_VALUE = -2;
    private static final long DISABLE_VOLUMEDOWN_DOUBLE_CLICK_INTERVAl_MAX = 15000;
    private static final long DISABLE_VOLUMEDOWN_DOUBLE_CLICK_INTERVAl_MIN = 1500;
    static final String DROP_SMARTKEY_ACTIVITY = "drop_smartkey_activity";
    static final String FINGERPRINT_ANSWER_CALL = "fp_answer_call";
    static final String FINGERPRINT_CAMERA_SWITCH = "fp_take_photo";
    static final String FINGERPRINT_STOP_ALARM = "fp_stop_alarm";
    private static final int FLOATING_MASK = Integer.MIN_VALUE;
    public static final String FRONT_FINGERPRINT_BUTTON_LIGHT_MODE = "button_light_mode";
    public static final String FRONT_FINGERPRINT_SWAP_KEY_POSITION = "swap_key_position";
    public static final String HAPTIC_FEEDBACK_TRIKEY_SETTINGS = "physic_navi_haptic_feedback_enabled";
    private static final String HUAWEI_RAPIDCAPTURE_START_MODE = "com.huawei.RapidCapture";
    private static final String HUAWEI_SCREENRECORDER_ACTION = "com.huawei.screenrecorder.Start";
    private static final String HUAWEI_SCREENRECORDER_PACKAGE = "com.huawei.screenrecorder";
    private static final String HUAWEI_SCREENRECORDER_START_MODE = "com.huawei.screenrecorder.ScreenRecordService";
    private static final String HUAWEI_SMARTKEY_PACKAGE = "com.huawei.smartkey";
    private static final String HUAWEI_SMARTKEY_SERVICE = "com.android.huawei.smartkey";
    private static final String HUAWEI_VASSISTANT_EXTRA_START_MODE = "com.huawei.vassistant.extra.SERVICE_START_MODE";
    private static final String HUAWEI_VASSISTANT_PACKAGE = "com.huawei.vassistant";
    private static final String HUAWEI_VOICE_DEBUG_BETACLUB = "com.huawei.betaclub";
    private static final String HUAWEI_VOICE_SOUNDTRIGGER_ACTIVITY = "com.mmc.soundtrigger.MainActivity";
    private static final String HUAWEI_VOICE_SOUNDTRIGGER_BROADCAST = "com.mmc.SOUNDTRIGGER";
    private static final String HUAWEI_VOICE_SOUNDTRIGGER_PACKAGE = "com.mmc.soundtrigger";
    private static final boolean IS_LONG_HOME_VASSITANT = SystemProperties.getBoolean("ro.hw.long.home.vassistant", true);
    private static final int JACK_DEVICE_ID = 16777216;
    private static final String KEY_TOUCH_DISABLE_MODE = "touch_disable_mode";
    private static final int MSG_BUTTON_LIGHT_TIMEOUT = 4099;
    private static final int MSG_DISPATCH_INTERNET_AUDIOKEY_WITH_WAKE_LOCK = 11;
    private static final int MSG_FINGERSENSE_DISABLE = 102;
    private static final int MSG_FINGERSENSE_ENABLE = 101;
    private static final int MSG_NAVIBAR_DISABLE = 104;
    private static final int MSG_NAVIBAR_ENABLE = 103;
    private static final int MSG_TRIKEY_BACK_LONG_PRESS = 4097;
    private static final int MSG_TRIKEY_RECENT_LONG_PRESS = 4098;
    private static final long SCREENRECORDER_DEBOUNCE_DELAY_MILLIS = 150;
    private static final int SIM_CARD_1 = 0;
    private static final int SIM_CARD_2 = 1;
    private static final int SINGLE_HAND_STATE = 1989;
    private static final String SMARTKEY_CLICK = "Click";
    private static final String SMARTKEY_DCLICK = "DoubleClick";
    private static final long SMARTKEY_DOUBLE_CLICK_TIMEOUT = 400;
    private static final long SMARTKEY_LONG_PRESS_TIMEOUT = 500;
    private static final String SMARTKEY_LP = "LongPress";
    private static final String SMARTKEY_TAG = "command";
    private static final int START_MODE_QUICK_START_CALL = 2;
    static final int START_MODE_VOICE_WAKEUP_ONE_SHOT = 4;
    private static final long SYSTRACELOG_DEBOUNCE_DELAY_MILLIS = 150;
    private static final long SYSTRACELOG_FINGERPRINT_EFFECT_DELAY = 750;
    static final String TAG = "HwPhoneWindowManager";
    private static final boolean TOUCHPLUS_FORCE_VIBRATION = true;
    private static final int TOUCHPLUS_SETTINGS_DISABLED = 0;
    private static final int TOUCHPLUS_SETTINGS_ENABLED = 1;
    private static final String TOUCHPLUS_SETTINGS_VIBRATION = "hw_membrane_touch_vibrate_enabled";
    private static final long TOUCH_DISABLE_DEBOUNCE_DELAY_MILLIS = 150;
    private static final int TOUCH_EXPLR_NAVIGATION_BAR_COLOR = -16777216;
    private static final int TOUCH_EXPLR_STATUS_BAR_COLOR = -16777216;
    private static final long TOUCH_SPINNING_DELAY_MILLIS = 2000;
    private static final float TYPICAL_PROXIMITY_THRESHOLD = 5.0f;
    private static final String VIBRATE_ON_TOUCH = "vibrate_on_touch";
    private static final int VIBRATOR_LONG_PRESS_FOR_FRONT_FP = SystemProperties.getInt("ro.config.trikey_vibrate_press", 16);
    private static final int VIBRATOR_SHORT_PRESS_FOR_FRONT_FP = SystemProperties.getInt("ro.config.trikey_vibrate_touch", 8);
    private static String VOICE_ASSISTANT_ACTION = "com.huawei.action.VOICE_ASSISTANT";
    private static final long VOLUMEDOWN_DOUBLE_CLICK_TIMEOUT = 400;
    private static final long VOLUMEDOWN_LONG_PRESS_TIMEOUT = 500;
    private static boolean mCustBeInit = false;
    private static boolean mCustUsed = false;
    static final boolean mIsHwNaviBar = SystemProperties.getBoolean("ro.config.hw_navigationbar", false);
    private static boolean mTplusEnabled = SystemProperties.getBoolean("ro.config.hw_touchplus_enabled", false);
    private static int[] mUnableWakeKey;
    private static boolean mUsingHwNavibar = SystemProperties.getBoolean("ro.config.hw_navigationbar", false);
    private boolean DEBUG_SMARTKEY = false;
    private int TRIKEY_NAVI_DEFAULT_MODE = -1;
    private AlertDialog alertDialog;
    FingerprintActionsListener fingerprintActionsListener;
    private boolean isFingerAnswerPhoneOn = false;
    private boolean isFingerShotCameraOn = false;
    private boolean isFingerStopAlarmOn = false;
    private boolean isNavibarHide;
    private boolean isTouchDownUpLeftDoubleClick;
    private boolean isTouchDownUpRightDoubleClick;
    private boolean isVibrateImplemented = SystemProperties.getBoolean("ro.config.touch_vibrate", false);
    private boolean isVoiceRecognitionActive;
    private int lastDensityDpi = -1;
    private int mActionBarHeight;
    private boolean mBackKeyPress = false;
    private long mBackKeyPressTime = 0;
    private Light mBackLight = null;
    volatile boolean mBackTrikeyHandled;
    private WakeLock mBroadcastWakeLock;
    private Light mButtonLight = null;
    private int mButtonLightMode = 1;
    private final Runnable mCancleInterceptFingerprintEvent = new Runnable() {
        public void run() {
            HwPhoneWindowManager.this.mNeedDropFingerprintEvent = false;
        }
    };
    private CoverManager mCoverManager = null;
    private boolean mCoverOpen = true;
    private int mCurUser;
    HwCustPhoneWindowManager mCust = ((HwCustPhoneWindowManager) HwCustUtils.createObj(HwCustPhoneWindowManager.class, new Object[0]));
    int mDesiredRotation = -1;
    private boolean mDeviceProvisioned = false;
    boolean mFingerSenseEnabled = true;
    private ContentObserver mFingerprintObserver;
    private final Runnable mHandleVolumeDownKey = new Runnable() {
        public void run() {
            if (HwPhoneWindowManager.this.isMusicActive()) {
                HwPhoneWindowManager.this.handleVolumeKey(3, 25);
            }
        }
    };
    private Handler mHandlerEx;
    private boolean mHapticEnabled = true;
    private boolean mHeadless;
    private boolean mHintShown;
    private HwScreenOnProximityLock mHwScreenOnProximityLock;
    public IHwWindowCallback mIHwWindowCallback;
    private boolean mInputMethodWindowVisible;
    private boolean mIsHasActionBar;
    protected boolean mIsImmersiveMode = false;
    boolean mIsNavibarAlignLeftWhenLand;
    private boolean mIsProximity = false;
    private boolean mIsSmartKeyDoubleClick = false;
    private boolean mIsSmartKeyTripleOrMoreClick = false;
    private boolean mIsTouchExplrEnabled;
    private WindowState mLastColorWin;
    private int mLastIsEmuiLightStyle;
    private int mLastIsEmuiStyle;
    private int mLastNavigationBarColor;
    private long mLastSmartKeyDownTime;
    private long mLastStartVassistantServiceTime;
    private int mLastStatusBarColor;
    private long mLastTouchDownUpLeftKeyDownTime;
    private long mLastTouchDownUpRightKeyDownTime;
    private long mLastVolumeDownKeyDownTime;
    private ProximitySensorListener mListener = null;
    private boolean mMenuClickedOnlyOnce = false;
    private boolean mMenuKeyPress = false;
    private long mMenuKeyPressTime = 0;
    boolean mNavibarEnabled = false;
    int[] mNavigationBarHeightForRotationMax = new int[4];
    int[] mNavigationBarHeightForRotationMin = new int[4];
    protected NavigationBarPolicy mNavigationBarPolicy = null;
    int[] mNavigationBarWidthForRotationMax = new int[4];
    int[] mNavigationBarWidthForRotationMin = new int[4];
    private boolean mNeedDropFingerprintEvent = false;
    OverscanTimeout mOverscanTimeout = new OverscanTimeout();
    private boolean mPowerKeyDisTouch;
    private long mPowerKeyDisTouchTime;
    private PowerManager mPowerManager;
    final Runnable mProximitySensorTimeoutRunnable = new Runnable() {
        public void run() {
            Log.i(HwPhoneWindowManager.TAG, "mProximitySensorTimeout, unRegisterListener");
            HwPhoneWindowManager.this.turnOffSensorListener();
        }
    };
    volatile boolean mRecentTrikeyHandled;
    private ContentResolver mResolver;
    private long mScreenRecorderPowerKeyTime;
    private boolean mScreenRecorderPowerKeyTriggered;
    private final Runnable mScreenRecorderRunnable = new Runnable() {
        public void run() {
            Intent intent = new Intent();
            intent.setAction(HwPhoneWindowManager.HUAWEI_SCREENRECORDER_ACTION);
            intent.setClassName(HwPhoneWindowManager.HUAWEI_SCREENRECORDER_PACKAGE, HwPhoneWindowManager.HUAWEI_SCREENRECORDER_START_MODE);
            HwPhoneWindowManager.this.mContext.startServiceAsUser(intent, UserHandle.CURRENT_OR_SELF);
            Log.d(HwPhoneWindowManager.TAG, "start screen recorder service");
        }
    };
    private boolean mScreenRecorderVolumeDownKeyTriggered;
    private boolean mScreenRecorderVolumeUpKeyConsumed;
    private long mScreenRecorderVolumeUpKeyTime;
    private boolean mScreenRecorderVolumeUpKeyTriggered;
    private SensorManager mSensorManager = null;
    private boolean mSensorRegisted = false;
    final Object mServiceAquireLock = new Object();
    private SettingsObserver mSettingsObserver;
    private final Runnable mSmartKeyClick = new Runnable() {
        public void run() {
            HwPhoneWindowManager.this.cancelSmartKeyClick();
            HwPhoneWindowManager.this.notifySmartKeyEvent(HwPhoneWindowManager.SMARTKEY_CLICK);
        }
    };
    private final Runnable mSmartKeyLongPressed = new Runnable() {
        public void run() {
            HwPhoneWindowManager.this.cancelSmartKeyLongPressed();
            HwPhoneWindowManager.this.notifySmartKeyEvent(HwPhoneWindowManager.SMARTKEY_LP);
        }
    };
    boolean mStatuBarObsecured;
    IStatusBarService mStatusBarService;
    private boolean mSystraceLogCompleted = true;
    private long mSystraceLogFingerPrintTime = 0;
    private boolean mSystraceLogPowerKeyTriggered = false;
    private final Runnable mSystraceLogRunnable = new Runnable() {
        public void run() {
            HwPhoneWindowManager.this.systraceLogDialogThread = new HandlerThread("SystraceLogDialog");
            HwPhoneWindowManager.this.systraceLogDialogThread.start();
            HwPhoneWindowManager.this.systraceLogDialogHandler = new Handler(HwPhoneWindowManager.this.systraceLogDialogThread.getLooper()) {
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if (msg.what == 0) {
                        HwPhoneWindowManager.this.alertDialog = new Builder(HwPhoneWindowManager.this.mContext).setTitle(17039380).setMessage(HwPhoneWindowManager.this.mContext.getResources().getQuantityString(34734087, 10, new Object[]{Integer.valueOf(10)})).setCancelable(false).create();
                        HwPhoneWindowManager.this.alertDialog.getWindow().setType(2003);
                        HwPhoneWindowManager.this.alertDialog.getWindow().setFlags(128, 128);
                        HwPhoneWindowManager.this.alertDialog.show();
                    } else if (msg.what <= 0 || msg.what >= 10) {
                        HwPhoneWindowManager.this.alertDialog.dismiss();
                    } else {
                        HwPhoneWindowManager.this.alertDialog.setMessage(HwPhoneWindowManager.this.mContext.getResources().getQuantityString(34734087, 10 - msg.what, new Object[]{Integer.valueOf(10 - msg.what)}));
                        TelecomManager telecomManager = (TelecomManager) HwPhoneWindowManager.this.mContext.getSystemService("telecom");
                        if (telecomManager != null && telecomManager.isRinging()) {
                            HwPhoneWindowManager.this.alertDialog.dismiss();
                        }
                    }
                }
            };
            HwPhoneWindowManager.this.systraceLogDialogHandler.sendEmptyMessage(0);
            new Thread(new Runnable() {
                public void run() {
                    int i = 1;
                    while (!HwPhoneWindowManager.this.mSystraceLogCompleted && i < 11) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Log.w(HwPhoneWindowManager.TAG, "systrace log not completed,interrupted");
                        }
                        if (!(HwPhoneWindowManager.this.systraceLogDialogHandler == null || HwPhoneWindowManager.this.mSystraceLogCompleted)) {
                            HwPhoneWindowManager.this.systraceLogDialogHandler.sendEmptyMessage(i);
                        }
                        i++;
                    }
                }
            }).start();
            new Thread(new Runnable() {
                public void run() {
                    IBinder sJankService = ServiceManager.getService("jank");
                    if (sJankService != null) {
                        try {
                            Log.d(HwPhoneWindowManager.TAG, "sJankService is not null");
                            Parcel data = Parcel.obtain();
                            Parcel reply = Parcel.obtain();
                            data.writeInterfaceToken("android.os.IJankManager");
                            sJankService.transact(2, data, reply, 0);
                            Log.d(HwPhoneWindowManager.TAG, "sJankService.transact result = " + reply.readInt());
                        } catch (RemoteException e) {
                            Log.e(HwPhoneWindowManager.TAG, "sJankService.transact remote exception:" + e.getMessage());
                        } finally {
                            HwPhoneWindowManager.this.systraceLogDialogHandler.sendEmptyMessage(10);
                        }
                    }
                    HwPhoneWindowManager.this.systraceLogDialogHandler.sendEmptyMessage(10);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e2) {
                        Log.w(HwPhoneWindowManager.TAG, "sJankService transact not completed,interrupted");
                    } finally {
                        HwPhoneWindowManager.this.mSystraceLogCompleted = true;
                        HwPhoneWindowManager.this.systraceLogDialogThread.quitSafely();
                        Log.d(HwPhoneWindowManager.TAG, "has quit the systraceLogDialogThread" + HwPhoneWindowManager.this.systraceLogDialogThread.getId());
                    }
                }
            }).start();
        }
    };
    private boolean mSystraceLogVolumeDownKeyTriggered = false;
    private boolean mSystraceLogVolumeUpKeyConsumed = false;
    private long mSystraceLogVolumeUpKeyTime = 0;
    private boolean mSystraceLogVolumeUpKeyTriggered = false;
    private TouchCountPolicy mTouchCountPolicy = new TouchCountPolicy();
    private int mTouchDownUpLeftConsumeCount;
    private int mTouchDownUpRightConsumeCount;
    private int mTrikeyNaviMode = -1;
    private SystemVibrator mVibrator;
    private boolean mVolumeDownKeyDisTouch;
    private final Runnable mVolumeDownLongPressed = new Runnable() {
        public void run() {
            HwPhoneWindowManager.this.cancelVolumeDownKeyPressed();
            if ((!HwPhoneWindowManager.this.mIsProximity && HwPhoneWindowManager.this.mSensorRegisted) || !HwPhoneWindowManager.this.mSensorRegisted) {
                HwPhoneWindowManager.this.notifyVassistantService("start", 2, null);
            }
            HwPhoneWindowManager.this.turnOffSensorListener();
            HwPhoneWindowManager.this.isVoiceRecognitionActive = true;
            HwPhoneWindowManager.this.mLastStartVassistantServiceTime = SystemClock.uptimeMillis();
        }
    };
    private WakeLock mVolumeDownWakeLock;
    private boolean mVolumeUpKeyConsumedByDisTouch;
    private boolean mVolumeUpKeyDisTouch;
    private long mVolumeUpKeyDisTouchTime;
    private HashSet<String> needDropSmartKeyActivities = new HashSet();
    SystemWideActionsListener systemWideActionsListener;
    private Handler systraceLogDialogHandler;
    private HandlerThread systraceLogDialogThread;

    class OverscanTimeout implements Runnable {
        OverscanTimeout() {
        }

        public void run() {
            Slog.i(HwPhoneWindowManager.TAG, "OverscanTimeout run");
            Global.putString(HwPhoneWindowManager.this.mContext.getContentResolver(), "single_hand_mode", AppHibernateCst.INVALID_PKG);
        }
    }

    private class PolicyHandlerEx extends Handler {
        private PolicyHandlerEx() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 11:
                    HwPhoneWindowManager.this.dispatchInternetAudioKeyWithWakeLock((KeyEvent) msg.obj);
                    return;
                case 101:
                    HwPhoneWindowManager.this.enableSystemWideActions();
                    return;
                case 102:
                    HwPhoneWindowManager.this.disableSystemWideActions();
                    return;
                case 103:
                    HwPhoneWindowManager.this.disableFingerPrintActions();
                    return;
                case 104:
                    HwPhoneWindowManager.this.enableFingerPrintActions();
                    return;
                case HwPhoneWindowManager.MSG_TRIKEY_BACK_LONG_PRESS /*4097*/:
                    HwPhoneWindowManager.this.mBackTrikeyHandled = true;
                    if (HwPhoneWindowManager.this.mTrikeyNaviMode == 1) {
                        HwPhoneWindowManager.this.startHwVibrate(HwPhoneWindowManager.VIBRATOR_LONG_PRESS_FOR_FRONT_FP);
                        Log.i(HwPhoneWindowManager.TAG, "LEFT->RECENT; RIGHT->BACK, handle longpress with recentTrikey and toggleSplitScreen");
                        ((StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class)).toggleSplitScreen();
                        return;
                    } else if (HwPhoneWindowManager.this.mTrikeyNaviMode == 0) {
                        Log.i(HwPhoneWindowManager.TAG, "LEFT->BACK; RIGHT->RECENT, handle longpress with backTrikey and unlockScreenPinningTest");
                        HwPhoneWindowManager.this.unlockScreenPinningTest();
                        return;
                    } else {
                        return;
                    }
                case HwPhoneWindowManager.MSG_TRIKEY_RECENT_LONG_PRESS /*4098*/:
                    HwPhoneWindowManager.this.mRecentTrikeyHandled = true;
                    if (HwPhoneWindowManager.this.mTrikeyNaviMode == 0) {
                        HwPhoneWindowManager.this.startHwVibrate(HwPhoneWindowManager.VIBRATOR_LONG_PRESS_FOR_FRONT_FP);
                        Log.i(HwPhoneWindowManager.TAG, "LEFT->BACK; RIGHT->RECENT, handle longpress with recentTrikey and toggleSplitScreen");
                        ((StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class)).toggleSplitScreen();
                        return;
                    } else if (HwPhoneWindowManager.this.mTrikeyNaviMode == 1) {
                        Log.i(HwPhoneWindowManager.TAG, "LEFT->RECENT; RIGHT->BACK, handle longpress with backTrikey and unlockScreenPinningTest");
                        HwPhoneWindowManager.this.unlockScreenPinningTest();
                        return;
                    } else {
                        return;
                    }
                case HwPhoneWindowManager.MSG_BUTTON_LIGHT_TIMEOUT /*4099*/:
                    if (HwPhoneWindowManager.this.mButtonLight == null) {
                        return;
                    }
                    if (HwPhoneWindowManager.this.mPowerManager == null || !HwPhoneWindowManager.this.mPowerManager.isScreenOn()) {
                        HwPhoneWindowManager.this.setButtonLightTimeout(false);
                        return;
                    }
                    HwPhoneWindowManager.this.mButtonLight.setBrightness(0);
                    HwPhoneWindowManager.this.setButtonLightTimeout(true);
                    return;
                default:
                    return;
            }
        }
    }

    private class ProximitySensorListener implements SensorEventListener {
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent arg0) {
            boolean z = false;
            float[] its = arg0.values;
            if (its != null && arg0.sensor.getType() == 8 && its.length > 0) {
                Log.i(HwPhoneWindowManager.TAG, "sensor value: its[0] = " + its[0]);
                HwPhoneWindowManager hwPhoneWindowManager = HwPhoneWindowManager.this;
                if (its[0] >= 0.0f && its[0] < HwPhoneWindowManager.TYPICAL_PROXIMITY_THRESHOLD) {
                    z = true;
                }
                hwPhoneWindowManager.mIsProximity = z;
            }
        }
    }

    private class ScreenBroadcastReceiver extends BroadcastReceiver {
        private ScreenBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.SCREEN_ON".equals(intent.getAction())) {
                HwPhoneWindowManager.this.sendLightTimeoutMsg();
            }
        }
    }

    class SettingsObserver extends ContentObserver {
        final /* synthetic */ HwPhoneWindowManager this$0;

        SettingsObserver(HwPhoneWindowManager this$0, Handler handler) {
            boolean z;
            boolean z2 = true;
            this.this$0 = this$0;
            super(handler);
            registerContentObserver(UserHandle.myUserId());
            if (Secure.getIntForUser(this$0.mResolver, "device_provisioned", 0, ActivityManager.getCurrentUser()) != 0) {
                z = true;
            } else {
                z = false;
            }
            this$0.mDeviceProvisioned = z;
            this$0.mTrikeyNaviMode = System.getIntForUser(this$0.mResolver, "swap_key_position", this$0.TRIKEY_NAVI_DEFAULT_MODE, ActivityManager.getCurrentUser());
            this$0.mButtonLightMode = System.getIntForUser(this$0.mResolver, "button_light_mode", 1, ActivityManager.getCurrentUser());
            if (System.getIntForUser(this$0.mResolver, "physic_navi_haptic_feedback_enabled", 1, ActivityManager.getCurrentUser()) == 0) {
                z2 = false;
            }
            this$0.mHapticEnabled = z2;
        }

        public void registerContentObserver(int userId) {
            this.this$0.mResolver.registerContentObserver(System.getUriFor("swap_key_position"), false, this, userId);
            this.this$0.mResolver.registerContentObserver(System.getUriFor("device_provisioned"), false, this, userId);
            this.this$0.mResolver.registerContentObserver(System.getUriFor("button_light_mode"), false, this, userId);
            this.this$0.mResolver.registerContentObserver(System.getUriFor("physic_navi_haptic_feedback_enabled"), false, this, userId);
        }

        public void onChange(boolean selfChange) {
            boolean z;
            boolean z2 = true;
            HwPhoneWindowManager hwPhoneWindowManager = this.this$0;
            if (Secure.getIntForUser(this.this$0.mResolver, "device_provisioned", 0, ActivityManager.getCurrentUser()) != 0) {
                z = true;
            } else {
                z = false;
            }
            hwPhoneWindowManager.mDeviceProvisioned = z;
            this.this$0.mTrikeyNaviMode = System.getIntForUser(this.this$0.mResolver, "swap_key_position", this.this$0.TRIKEY_NAVI_DEFAULT_MODE, ActivityManager.getCurrentUser());
            this.this$0.mButtonLightMode = System.getIntForUser(this.this$0.mResolver, "button_light_mode", 1, ActivityManager.getCurrentUser());
            this.this$0.resetButtonLightStatus();
            Slog.i(HwPhoneWindowManager.TAG, "mTrikeyNaviMode is:" + this.this$0.mTrikeyNaviMode + " mButtonLightMode is:" + this.this$0.mButtonLightMode);
            HwPhoneWindowManager hwPhoneWindowManager2 = this.this$0;
            if (System.getIntForUser(this.this$0.mResolver, "physic_navi_haptic_feedback_enabled", 1, ActivityManager.getCurrentUser()) == 0) {
                z2 = false;
            }
            hwPhoneWindowManager2.mHapticEnabled = z2;
        }
    }

    public void systemReady() {
        super.systemReady();
        this.mHandler.post(new Runnable() {
            public void run() {
                HwPhoneWindowManager.this.initQuickcall();
            }
        });
        this.mHwScreenOnProximityLock = new HwScreenOnProximityLock(this.mContext);
        if (mIsHwNaviBar) {
            this.mNavigationBarPolicy = new NavigationBarPolicy(this.mContext, this);
            this.mWindowManagerFuncs.registerPointerEventListener(new PointerEventListener() {
                public void onPointerEvent(MotionEvent motionEvent) {
                    if (HwPhoneWindowManager.this.mNavigationBarPolicy != null) {
                        HwPhoneWindowManager.this.mNavigationBarPolicy.addPointerEvent(motionEvent);
                    }
                }
            });
        }
        if (SystemProperties.getBoolean("ro.config.hw_easywakeup", false) && this.mSystemReady) {
            EasyWakeUpManager mWakeUpManager = EasyWakeUpManager.getInstance(this.mContext, this.mHandler, this.mKeyguardDelegate);
            ServiceManager.addService("easywakeup", mWakeUpManager);
            mWakeUpManager.saveTouchPointNodePath();
        }
        this.mListener = new ProximitySensorListener();
        this.mResolver = this.mContext.getContentResolver();
        this.TRIKEY_NAVI_DEFAULT_MODE = FrontFingerPrintSettings.getDefaultNaviMode();
        this.mSettingsObserver = new SettingsObserver(this, this.mHandler);
        this.mVibrator = (SystemVibrator) ((Vibrator) this.mContext.getSystemService("vibrator"));
        if (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION && FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1) {
            LightsManager lights = (LightsManager) LocalServices.getService(LightsManager.class);
            this.mButtonLight = lights.getLight(2);
            this.mBackLight = lights.getLight(0);
            if (this.mContext != null) {
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.intent.action.SCREEN_ON");
                this.mContext.registerReceiver(new ScreenBroadcastReceiver(), filter);
            }
        }
    }

    public void addPointerEvent(MotionEvent motionEvent) {
        if (this.mNavigationBarPolicy != null) {
            this.mNavigationBarPolicy.addPointerEvent(motionEvent);
        }
    }

    public void init(Context context, IWindowManager windowManager, WindowManagerFuncs windowManagerFuncs) {
        this.mHandlerEx = new PolicyHandlerEx();
        this.fingersense_enable = "fingersense_smartshot_enabled";
        this.fingersense_letters_enable = "fingersense_letters_enabled";
        this.line_gesture_enable = "fingersense_multiwindow_enabled";
        Flog.i(1503, "init fingersense_letters_enable with " + this.fingersense_letters_enable);
        this.navibar_enable = "enable_navbar";
        this.mCurUser = ActivityManager.getCurrentUser();
        this.mResolver = context.getContentResolver();
        this.mFingerprintObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean selfChange) {
                HwPhoneWindowManager.this.updateFingerprintNav();
            }
        };
        registerFingerprintObserver(this.mCurUser);
        updateFingerprintNav();
        initDropSmartKey();
        super.init(context, windowManager, windowManagerFuncs);
    }

    private void updateFingerprintNav() {
        boolean z;
        boolean z2 = true;
        if (Secure.getIntForUser(this.mResolver, FINGERPRINT_CAMERA_SWITCH, 1, this.mCurUser) == 1) {
            z = true;
        } else {
            z = false;
        }
        this.isFingerShotCameraOn = z;
        if (Secure.getIntForUser(this.mResolver, FINGERPRINT_STOP_ALARM, 0, this.mCurUser) == 1) {
            z = true;
        } else {
            z = false;
        }
        this.isFingerStopAlarmOn = z;
        if (Secure.getIntForUser(this.mResolver, FINGERPRINT_ANSWER_CALL, 0, this.mCurUser) != 1) {
            z2 = false;
        }
        this.isFingerAnswerPhoneOn = z2;
    }

    private void registerFingerprintObserver(int userId) {
        this.mResolver.registerContentObserver(Secure.getUriFor(FINGERPRINT_CAMERA_SWITCH), true, this.mFingerprintObserver, userId);
        this.mResolver.registerContentObserver(Secure.getUriFor(FINGERPRINT_STOP_ALARM), true, this.mFingerprintObserver, userId);
        this.mResolver.registerContentObserver(Secure.getUriFor(FINGERPRINT_ANSWER_CALL), true, this.mFingerprintObserver, userId);
    }

    public void setCurrentUser(int userId, int[] currentProfileIds) {
        this.mCurUser = userId;
        registerFingerprintObserver(userId);
        this.mFingerprintObserver.onChange(true);
    }

    public int checkAddPermission(LayoutParams attrs, int[] outAppOp) {
        if (attrs.type == 2101) {
            return 0;
        }
        return super.checkAddPermission(attrs, outAppOp);
    }

    public int windowTypeToLayerLw(int type) {
        switch (type) {
            case 2100:
                return 30;
            case 2101:
                return 31;
            default:
                return super.windowTypeToLayerLw(type);
        }
    }

    public void freezeOrThawRotation(int rotation) {
        this.mDesiredRotation = rotation;
    }

    public boolean rotationHasCompatibleMetricsLw(int orientation, int rotation) {
        if (this.mDesiredRotation != 0) {
            return super.rotationHasCompatibleMetricsLw(orientation, rotation);
        }
        Slog.d(TAG, "desired rotation is rotation 0");
        return true;
    }

    public int rotationForOrientationLw(int orientation, int lastRotation) {
        if (isDefaultOrientationForced()) {
            return super.rotationForOrientationLw(orientation, lastRotation);
        }
        int desiredRotation = this.mDesiredRotation;
        if (desiredRotation < 0) {
            return super.rotationForOrientationLw(orientation, lastRotation);
        }
        Slog.i(TAG, "mDesiredRotation:" + this.mDesiredRotation);
        return desiredRotation;
    }

    public View addStartingWindow(IBinder appToken, String packageName, int theme, CompatibilityInfo compatInfo, CharSequence nonLocalizedLabel, int labelRes, int icon, int logo, int windowFlags, Configuration overrideConfig) {
        Context context = this.mContext;
        try {
            context = this.mContext.createPackageContext(packageName, 0);
            ViewGroup docview = (ViewGroup) super.addStartingWindow(appToken, packageName, theme, compatInfo, nonLocalizedLabel, labelRes, icon, logo, windowFlags, overrideConfig);
            if (docview == null) {
                return null;
            }
            if (((context.getApplicationInfo() == null ? 0 : context.getApplicationInfo().flags) & 1) == 0 || !this.mIsHasActionBar || (theme >>> 24) != 2 || isHwDarkTheme(context, theme)) {
                return docview;
            }
            Slog.d(TAG, "starting window system app and have actionbar");
            this.mIsHasActionBar = false;
            LayoutParams lp = new LayoutParams(-1, -2);
            lp.flags = 24;
            lp.format = 3;
            lp.width = -1;
            lp.height = this.mActionBarHeight + this.mStatusBarHeight;
            View tmp = getActionBarView(context, theme);
            if (tmp == null) {
                Slog.d(TAG, "action bar view is null");
                return docview;
            }
            docview.addView(tmp, lp);
            return docview;
        } catch (NameNotFoundException e) {
            return super.addStartingWindow(appToken, packageName, theme, compatInfo, nonLocalizedLabel, labelRes, icon, logo, windowFlags, overrideConfig);
        }
    }

    public void beginPostLayoutPolicyLw(int displayWidth, int displayHeight) {
        super.beginPostLayoutPolicyLw(displayWidth, displayHeight);
        this.mStatuBarObsecured = false;
    }

    public void applyPostLayoutPolicyLw(WindowState win, LayoutParams attrs, WindowState attached) {
        super.applyPostLayoutPolicyLw(win, attrs, attached);
        if (win.isVisibleLw() && win.getSurfaceLayer() > this.mStatusBarLayer && isStatusBarObsecuredByWin(win)) {
            this.mStatuBarObsecured = true;
        }
    }

    protected void setHasAcitionBar(boolean hasActionBar) {
        this.mIsHasActionBar = hasActionBar;
    }

    private View getActionBarView(Context context, int theme) {
        context.setTheme(theme);
        View tmp = new View(context);
        int color = HwWidgetFactory.getPrimaryColor(context);
        Slog.d(TAG, "Starting window for " + context.getPackageName() + " ActionBarView color=0x" + Integer.toHexString(color));
        if (HwWidgetUtils.isActionbarBackgroundThemed(this.mContext) || Color.alpha(color) == 0) {
            return null;
        }
        tmp.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        tmp.setBackgroundDrawable(new ColorDrawable(color));
        return tmp;
    }

    private static boolean isMultiSimEnabled() {
        boolean flag = false;
        try {
            flag = MSimTelephonyManager.getDefault().isMultiSimEnabled();
        } catch (NoExtAPIException e) {
            Log.w(TAG, "CoverManagerService->isMultiSimEnabled->NoExtAPIException!");
        }
        return flag;
    }

    private boolean isPhoneInCall() {
        if (isMultiSimEnabled()) {
            for (int i = 0; i < MSimTelephonyManager.getDefault().getPhoneCount(); i++) {
                if (MSimTelephonyManager.getDefault().getCallState(i) != 0) {
                    return true;
                }
            }
            return false;
        } else if (TelephonyManager.getDefault().getCallState(SubscriptionManager.getDefaultSubscriptionId()) != 0) {
            return true;
        } else {
            return false;
        }
    }

    static ITelephony getTelephonyService() {
        return Stub.asInterface(ServiceManager.checkService("phone"));
    }

    public boolean needTurnOff(int why) {
        boolean isOffhook = isPhoneInCall();
        boolean isSecure = isKeyguardSecure(this.mCurrentUserId);
        if (!isOffhook) {
            return true;
        }
        if (!isSecure || why == 3) {
            return false;
        }
        if (why != 6) {
            return true;
        }
        return false;
    }

    public boolean needTurnOffWithDismissFlag() {
        if (this.mDismissKeyguard == 0 || isKeyguardSecure(this.mCurrentUserId)) {
            return true;
        }
        return false;
    }

    protected boolean isWakeKeyWhenScreenOff(int keyCode) {
        if (!mCustUsed) {
            return super.isWakeKeyWhenScreenOff(keyCode);
        }
        for (int i : mUnableWakeKey) {
            if (keyCode == i) {
                return false;
            }
        }
        return true;
    }

    public boolean isWakeKeyFun(int keyCode) {
        if (!mCustBeInit) {
            getKeycodeFromCust();
        }
        if (!mCustUsed) {
            return false;
        }
        for (int i : mUnableWakeKey) {
            if (keyCode == i) {
                return false;
            }
        }
        return true;
    }

    void startDockOrHome(boolean fromHomeKey, boolean awakenFromDreams) {
        if (fromHomeKey) {
            HwInputManagerServiceInternal inputManager = (HwInputManagerServiceInternal) LocalServices.getService(HwInputManagerServiceInternal.class);
            if (inputManager != null) {
                inputManager.notifyHomeLaunching();
            }
        }
        super.startDockOrHome(fromHomeKey, awakenFromDreams);
    }

    private void getKeycodeFromCust() {
        String unableCustomizedWakeKey = null;
        try {
            unableCustomizedWakeKey = Systemex.getString(this.mContext.getContentResolver(), "unable_wake_up_key");
        } catch (Exception e) {
            Log.e(TAG, "Exception when got name value", e);
        }
        if (unableCustomizedWakeKey != null) {
            String[] unableWakeKeyArray = unableCustomizedWakeKey.split(";");
            if (!(unableWakeKeyArray == null || unableWakeKeyArray.length == 0)) {
                mUnableWakeKey = new int[unableWakeKeyArray.length];
                int i = 0;
                while (i < mUnableWakeKey.length) {
                    try {
                        mUnableWakeKey[i] = Integer.parseInt(unableWakeKeyArray[i]);
                        i++;
                    } catch (Exception e2) {
                        Log.e(TAG, "Exception when copy the translated value from sting array to int array", e2);
                    }
                }
                mCustUsed = true;
            }
        }
        mCustBeInit = true;
    }

    public int interceptMotionBeforeQueueingNonInteractive(long whenNanos, int policyFlags) {
        if ((FLOATING_MASK & policyFlags) == 0) {
            return super.interceptMotionBeforeQueueingNonInteractive(whenNanos, policyFlags);
        }
        Slog.i(TAG, "interceptMotionBeforeQueueingNonInteractive policyFlags: " + policyFlags);
        Global.putString(this.mContext.getContentResolver(), "single_hand_mode", AppHibernateCst.INVALID_PKG);
        return 0;
    }

    protected int getSingleHandState() {
        int windowManagerService = WindowManagerGlobal.getWindowManagerService();
        IBinder windowManagerBinder = windowManagerService.asBinder();
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        if (windowManagerBinder != null) {
            try {
                data.writeInterfaceToken("android.view.IWindowManager");
                windowManagerBinder.transact(1990, data, reply, 0);
                reply.readException();
                windowManagerService = reply.readInt();
                return windowManagerService;
            } catch (RemoteException e) {
                return 0;
            } finally {
                data.recycle();
                reply.recycle();
            }
        } else {
            data.recycle();
            reply.recycle();
            return 0;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void unlockScreenPinningTest() {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            if (getHWStatusBarService() != null) {
                IBinder statusBarServiceBinder = getHWStatusBarService().asBinder();
                if (statusBarServiceBinder != null) {
                    Log.d(TAG, "Transact unlockScreenPinningTest to status bar service!");
                    data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    statusBarServiceBinder.transact(111, data, reply, 0);
                }
            }
            reply.recycle();
            data.recycle();
        } catch (RemoteException e) {
            Log.e(TAG, "transactToStatusBarService->threw remote exception");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
        }
    }

    public void finishedGoingToSleep(int why) {
        this.mHandler.removeCallbacks(this.mOverscanTimeout);
        this.mHandler.postDelayed(this.mOverscanTimeout, 200);
        super.finishedGoingToSleep(why);
    }

    private void interceptSystraceLog() {
        long now = SystemClock.uptimeMillis();
        Log.d(TAG, "now=" + now + " mSystraceLogVolumeUpKeyTime=" + this.mSystraceLogVolumeUpKeyTime + " mSystraceLogFingerPrintTime=" + this.mSystraceLogFingerPrintTime);
        if (now <= this.mSystraceLogVolumeUpKeyTime + 150 && now <= this.mSystraceLogFingerPrintTime + SYSTRACELOG_FINGERPRINT_EFFECT_DELAY && this.mSystraceLogCompleted) {
            this.mSystraceLogCompleted = false;
            this.mSystraceLogVolumeUpKeyConsumed = true;
            this.mSystraceLogFingerPrintTime = 0;
            this.mSystraceLogVolumeUpKeyTriggered = false;
            this.mScreenRecorderVolumeUpKeyTriggered = false;
            Trace.traceBegin(8, "invoke_systrace_log_dump");
            Jlog.d(313, "HwPhoneWindowManager Systrace triggered");
            Trace.traceEnd(8);
            Log.d(TAG, "Systrace triggered");
            this.mHandler.postDelayed(this.mSystraceLogRunnable, ViewConfiguration.get(this.mContext).getDeviceGlobalActionKeyTimeout());
        }
    }

    private void interceptScreenRecorder() {
        if (this.mScreenRecorderVolumeUpKeyTriggered && this.mScreenRecorderPowerKeyTriggered && !this.mScreenRecorderVolumeDownKeyTriggered && !SystemProperties.getBoolean("sys.super_power_save", false) && !keyguardIsShowingTq() && checkPackageInstalled(HUAWEI_SCREENRECORDER_PACKAGE)) {
            long now = SystemClock.uptimeMillis();
            if (now <= this.mScreenRecorderVolumeUpKeyTime + 150 && now <= this.mScreenRecorderPowerKeyTime + 150) {
                this.mScreenRecorderVolumeUpKeyConsumed = true;
                cancelPendingPowerKeyActionForDistouch();
                this.mHandler.postDelayed(this.mScreenRecorderRunnable, ViewConfiguration.get(this.mContext).getDeviceGlobalActionKeyTimeout());
            }
        }
    }

    private void cancelPendingScreenRecorderAction() {
        this.mHandler.removeCallbacks(this.mScreenRecorderRunnable);
    }

    boolean isVoiceCall() {
        boolean z = true;
        IAudioService audioService = getAudioService();
        if (audioService != null) {
            try {
                int mode = audioService.getMode();
                if (!(mode == 3 || mode == 2)) {
                    z = false;
                }
                return z;
            } catch (RemoteException e) {
                Log.w(TAG, "getMode exception");
            }
        }
        return false;
    }

    private void sendKeyEvent(int keycode) {
        int[] actions = new int[]{0, 1};
        for (int keyEvent : actions) {
            long curTime = SystemClock.uptimeMillis();
            InputManager.getInstance().injectInputEvent(new KeyEvent(curTime, curTime, keyEvent, keycode, 0, 0, -1, 0, 8, 257), 0);
        }
    }

    private boolean isExcluedScene() {
        String pkgName = ((ActivityManagerService) ServiceManager.getService("activity")).topAppName();
        String pkg_alarm = "com.android.deskclock/.alarmclock.LockAlarmFullActivity";
        boolean isSuperPowerMode = SystemProperties.getBoolean("sys.super_power_save", false);
        if (pkgName == null) {
            return false;
        }
        boolean z;
        if (pkgName.equals(pkg_alarm) || isSuperPowerMode || !this.mDeviceProvisioned) {
            z = true;
        } else {
            z = keyguardOn();
        }
        return z;
    }

    private boolean isExcluedBackScene() {
        boolean z = true;
        if (this.mTrikeyNaviMode == 1) {
            return isExcluedScene();
        }
        if (this.mDeviceProvisioned) {
            z = false;
        }
        return z;
    }

    private boolean isExcluedRecentScene() {
        boolean z = true;
        if (this.mTrikeyNaviMode != 1) {
            return isExcluedScene();
        }
        if (this.mDeviceProvisioned) {
            z = false;
        }
        return z;
    }

    public void setCurrentUserLw(int newUserId) {
        super.setCurrentUserLw(newUserId);
        this.mSettingsObserver.registerContentObserver(newUserId);
        this.mSettingsObserver.onChange(true);
        if (this.fingerprintActionsListener != null) {
            this.fingerprintActionsListener.setCurrentUser(newUserId);
        }
        Slog.i(TAG, "setCurrentUserLw :" + newUserId);
    }

    private void resetButtonLightStatus() {
        if (this.mButtonLight != null) {
            if (this.mDeviceProvisioned) {
                Slog.i(TAG, "resetButtonLightStatus");
                this.mHandlerEx.removeMessages(MSG_BUTTON_LIGHT_TIMEOUT);
                if (this.mTrikeyNaviMode < 0) {
                    setButtonLightTimeout(false);
                    this.mButtonLight.setBrightness(0);
                } else if (this.mButtonLightMode != 0) {
                    setButtonLightTimeout(false);
                    this.mButtonLight.setBrightness(this.mBackLight.getCurrentBrightness());
                } else if (this.mButtonLight.getCurrentBrightness() > 0) {
                    setButtonLightTimeout(false);
                    Message msg = this.mHandlerEx.obtainMessage(MSG_BUTTON_LIGHT_TIMEOUT);
                    msg.setAsynchronous(true);
                    this.mHandlerEx.sendMessageDelayed(msg, 5000);
                } else {
                    setButtonLightTimeout(true);
                }
            } else {
                setButtonLightTimeout(false);
                this.mButtonLight.setBrightness(0);
            }
        }
    }

    private void setButtonLightTimeout(boolean timeout) {
        SystemProperties.set("sys.button.light.timeout", String.valueOf(timeout));
    }

    private void sendLightTimeoutMsg() {
        if (this.mButtonLight != null && this.mDeviceProvisioned) {
            this.mHandlerEx.removeMessages(MSG_BUTTON_LIGHT_TIMEOUT);
            if (this.mTrikeyNaviMode >= 0) {
                int curButtonBrightness = this.mButtonLight.getCurrentBrightness();
                int curBackBrightness = this.mBackLight.getCurrentBrightness();
                if (this.mButtonLightMode == 0) {
                    if (SystemProperties.getBoolean("sys.button.light.timeout", false) && curButtonBrightness == 0) {
                        this.mButtonLight.setBrightness(curBackBrightness);
                    }
                    setButtonLightTimeout(false);
                    Message msg = this.mHandlerEx.obtainMessage(MSG_BUTTON_LIGHT_TIMEOUT);
                    msg.setAsynchronous(true);
                    this.mHandlerEx.sendMessageDelayed(msg, 5000);
                } else if (curButtonBrightness == 0) {
                    this.mButtonLight.setBrightness(curBackBrightness);
                }
            } else {
                setButtonLightTimeout(false);
                this.mButtonLight.setBrightness(0);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void startHwVibrate(int vibrateMode) {
        if (!(isKeyguardLocked() || !this.mHapticEnabled || "true".equals(SystemProperties.get("runtime.mmitest.isrunning", "false")) || this.mVibrator == null)) {
            Log.d(TAG, "startVibrateWithConfigProp:" + vibrateMode);
            this.mVibrator.vibrate((long) vibrateMode);
        }
    }

    private boolean isMMITesting() {
        return "true".equals(SystemProperties.get("runtime.mmitest.isrunning", "false"));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int interceptKeyBeforeQueueing(KeyEvent event, int policyFlags) {
        this.mTouchCountPolicy.updateTouchCountInfo();
        boolean down = event.getAction() == 0;
        int keyCode = event.getKeyCode();
        int flags = event.getFlags();
        int deviceID = event.getDeviceId();
        if (this.mSystraceLogCompleted) {
            if (this.mCust != null) {
                this.mCust.processCustInterceptKey(keyCode, down, this.mContext);
            }
            int origKeyCode = event.getOrigKeyCode();
            Flog.i(WifiProCommonUtils.RESP_CODE_INVALID_URL, "HwPhoneWindowManager has intercept Key : " + keyCode + ", isdown : " + down + ", flags : " + flags);
            boolean isScreenOn = (536870912 & policyFlags) != 0;
            if ((keyCode == 26 || keyCode == 6 || keyCode == 187) && this.mFocusedWindow != null && (this.mFocusedWindow.getAttrs().hwFlags & FLOATING_MASK) == FLOATING_MASK) {
                Log.d(TAG, "power and endcall key received and passsing to user.");
                return 1;
            }
            boolean isInjected;
            boolean isWakeKeyFun;
            int i;
            boolean isWakeKey;
            int result;
            boolean handled;
            Message msg;
            boolean keyguardShow;
            boolean isIntercept;
            boolean isVolumeDownDoubleClick;
            boolean isVoiceCall;
            boolean z;
            boolean z2;
            long interval;
            long timediff;
            KeyEvent newEvent;
            if (SystemProperties.getBoolean("ro.config.hw_easywakeup", false) && this.mSystemReady) {
                if (EasyWakeUpManager.getInstance(this.mContext, this.mHandler, this.mKeyguardDelegate).handleWakeUpKey(event, isScreenOn ? -1 : this.mScreenOffReason)) {
                    Log.d(TAG, "EasyWakeUpManager has handled the keycode : " + event.getKeyCode());
                    return 0;
                }
            }
            if (down && event.getRepeatCount() == 0 && SystemProperties.get(VIBRATE_ON_TOUCH, "false").equals("true")) {
                if (!((keyCode == 82 && (268435456 & flags) == 0) || keyCode == 3 || keyCode == 4)) {
                    if ((policyFlags & 2) != 0) {
                    }
                }
                performHapticFeedbackLw(null, 1, false);
            }
            if (!(origKeyCode == 305 || origKeyCode == 306)) {
                if (origKeyCode == 307) {
                }
                isInjected = (16777216 & policyFlags) == 0;
                isWakeKeyFun = isWakeKeyFun(keyCode);
                if ((policyFlags & 1) == 0) {
                    i = 1;
                } else {
                    i = 0;
                }
                isWakeKey = isWakeKeyFun | i;
                if ((isScreenOn || this.mHeadless) && (!isInjected || isWakeKey)) {
                    result = 0;
                    if (down) {
                        if (isWakeKey) {
                        }
                    }
                } else {
                    result = 1;
                }
                if (this.mFocusedWindow == null && (this.mFocusedWindow.getAttrs().hwFlags & 8) == 8 && ((keyCode == 25 || keyCode == 24) && "true".equals(SystemProperties.get("runtime.mmitest.isrunning", "false")))) {
                    if (isJackDeviceEvent(event.getDeviceId(), keyCode)) {
                        Log.i(TAG, "Pass jack volume event to mmi test before queueing.");
                        return 1;
                    }
                    Log.i(TAG, "Prevent hard key volume event to mmi test before queueing.");
                    return result & -2;
                } else if (isJackDeviceEvent(event.getDeviceId(), keyCode)) {
                    switch (keyCode) {
                        case 3:
                        case 4:
                        case 187:
                            if (!down && this.mHwScreenOnProximityLock != null && this.mHwScreenOnProximityLock.isShowing() && isScreenOn && !this.mHintShown && (event.getFlags() & 1024) == 0) {
                                Log.d(TAG, "keycode: " + keyCode + " is comsumed by disable touch mode.");
                                this.mHwScreenOnProximityLock.forceShowHint();
                                this.mHintShown = true;
                                break;
                            }
                            if (deviceID > 0 && FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION && FrontFingerPrintSettings.isSupportTrikey() && !isMMITesting() && keyCode == 4) {
                                if (!isTrikeyNaviKeycodeFromLON(isInjected, isExcluedBackScene())) {
                                    return 0;
                                }
                                sendLightTimeoutMsg();
                                if (down) {
                                    handled = this.mBackTrikeyHandled;
                                    if (!this.mBackTrikeyHandled) {
                                        this.mBackTrikeyHandled = true;
                                        this.mHandlerEx.removeMessages(MSG_TRIKEY_BACK_LONG_PRESS);
                                    }
                                    if (handled) {
                                        return 0;
                                    }
                                    startHwVibrate(VIBRATOR_SHORT_PRESS_FOR_FRONT_FP);
                                    if (this.mTrikeyNaviMode == 1) {
                                        Flog.bdReport(this.mContext, 16);
                                        sendKeyEvent(187);
                                        return 0;
                                    }
                                }
                                this.mBackTrikeyHandled = false;
                                msg = this.mHandlerEx.obtainMessage(MSG_TRIKEY_BACK_LONG_PRESS);
                                msg.setAsynchronous(true);
                                this.mHandlerEx.sendMessageDelayed(msg, ViewConfiguration.get(this.mContext).getDeviceGlobalActionKeyTimeout());
                                if (this.mTrikeyNaviMode == 1) {
                                    return 0;
                                }
                            }
                            if (!this.mHasNavigationBar && keyCode == 4 && down) {
                                if (!isScreenInLockTaskMode()) {
                                    this.mBackKeyPress = true;
                                    this.mBackKeyPressTime = event.getDownTime();
                                    interceptBackandMenuKey();
                                    break;
                                }
                                this.mBackKeyPress = false;
                                this.mBackKeyPressTime = 0;
                                break;
                            }
                            break;
                        case 24:
                        case 25:
                        case 164:
                            if (keyCode == 25) {
                                if (down) {
                                    if (this.mHwScreenOnProximityLock == null && this.mHwScreenOnProximityLock.isShowing() && isScreenOn && !this.mVolumeDownKeyDisTouch && (event.getFlags() & 1024) == 0) {
                                        Log.d(TAG, "keycode: KEYCODE_VOLUME_DOWN is comsumed by disable touch mode.");
                                        this.mVolumeDownKeyDisTouch = true;
                                        if (!this.mHintShown) {
                                            this.mHwScreenOnProximityLock.forceShowHint();
                                            this.mHintShown = true;
                                            break;
                                        }
                                    }
                                    if (isScreenOn && !this.mScreenRecorderVolumeDownKeyTriggered && (event.getFlags() & 1024) == 0) {
                                        cancelPendingPowerKeyActionForDistouch();
                                        this.mScreenRecorderVolumeDownKeyTriggered = true;
                                        cancelPendingScreenRecorderAction();
                                    }
                                    if (isScreenOn && !this.mSystraceLogVolumeDownKeyTriggered && (event.getFlags() & 1024) == 0) {
                                        this.mSystraceLogVolumeDownKeyTriggered = true;
                                        this.mSystraceLogFingerPrintTime = 0;
                                        this.mSystraceLogVolumeUpKeyTriggered = false;
                                    }
                                } else {
                                    this.mVolumeDownKeyDisTouch = false;
                                    this.mScreenRecorderVolumeDownKeyTriggered = false;
                                    cancelPendingScreenRecorderAction();
                                    this.mSystraceLogVolumeDownKeyTriggered = false;
                                }
                                keyguardShow = keyguardIsShowingTq();
                                Log.d(TAG, "interceptVolumeDownKey down=" + down + " keyguardShow=" + keyguardShow + " policyFlags=" + Integer.toHexString(policyFlags));
                                if ((!isScreenOn || keyguardShow) && !isInjected && (event.getFlags() & 1024) == 0) {
                                    if (!isDeviceProvisioned()) {
                                        if (down) {
                                            if (event.getEventTime() - event.getDownTime() < 500) {
                                                cancelPendingQuickCallChordAction();
                                                break;
                                            }
                                            resetVolumeDownKeyLongPressed();
                                            break;
                                        }
                                        isIntercept = false;
                                        isVolumeDownDoubleClick = false;
                                        isVoiceCall = isVoiceCall();
                                        z = isMusicActive() ? isVoiceCall : true;
                                        z2 = (isMusicActive() || !isPhoneIdle()) ? true : isVoiceCall;
                                        if (this.isVoiceRecognitionActive) {
                                            interval = event.getEventTime() - this.mLastStartVassistantServiceTime;
                                            if (interval > DISABLE_VOLUMEDOWN_DOUBLE_CLICK_INTERVAl_MAX) {
                                                this.isVoiceRecognitionActive = false;
                                            } else if (interval > DISABLE_VOLUMEDOWN_DOUBLE_CLICK_INTERVAl_MIN) {
                                                this.isVoiceRecognitionActive = AudioSystem.isSourceActive(6);
                                            }
                                        }
                                        Log.i(TAG, "isMusicOrFMOrVoiceCallActive=" + z + " isVoiceRecognitionActive=" + this.isVoiceRecognitionActive);
                                        if (!(z || this.isVoiceRecognitionActive || SystemProperties.getBoolean("sys.super_power_save", false))) {
                                            timediff = event.getEventTime() - this.mLastVolumeDownKeyDownTime;
                                            this.mLastVolumeDownKeyDownTime = event.getEventTime();
                                            if (timediff < 400) {
                                                isVolumeDownDoubleClick = true;
                                                if (this.mListener == null) {
                                                    this.mListener = new ProximitySensorListener();
                                                }
                                                turnOnSensorListener();
                                                if ((!this.mIsProximity && this.mSensorRegisted) || !this.mSensorRegisted) {
                                                    Log.i(TAG, "mIsProximity " + this.mIsProximity + ", mSensorRegisted " + this.mSensorRegisted);
                                                    notifyRapidCaptureService("start");
                                                }
                                                turnOffSensorListener();
                                                result &= -2;
                                            } else {
                                                notifyRapidCaptureService("wakeup");
                                                if (this.mListener == null) {
                                                    this.mListener = new ProximitySensorListener();
                                                }
                                                turnOnSensorListener();
                                            }
                                            if (!isScreenOn || isVolumeDownDoubleClick) {
                                                isIntercept = true;
                                            }
                                        }
                                        if (!(z2 || isScreenOn || isVolumeDownDoubleClick || !checkPackageInstalled(HUAWEI_VASSISTANT_PACKAGE))) {
                                            notifyVassistantService("wakeup", 2, event);
                                            if (this.mListener == null) {
                                                this.mListener = new ProximitySensorListener();
                                            }
                                            turnOnSensorListener();
                                            interceptQuickCallChord();
                                            isIntercept = true;
                                        }
                                        Log.i(TAG, "intercept volume down key, isIntercept=" + isIntercept + " now=" + SystemClock.uptimeMillis() + " EventTime=" + event.getEventTime());
                                        if (isIntercept) {
                                            if (getTelecommService().isInCall() && (result & 1) == 0 && this.mCust != null && this.mCust.isVolumnkeyWakeup()) {
                                                this.mCust.volumnkeyWakeup(this.mContext, isScreenOn, this.mPowerManager);
                                                break;
                                            }
                                        }
                                        return result;
                                    }
                                    Log.i(TAG, "Device is not Provisioned");
                                    break;
                                }
                            } else if (keyCode == 24) {
                                if (down) {
                                    if (this.mHwScreenOnProximityLock == null && this.mHwScreenOnProximityLock.isShowing() && isScreenOn && !this.mVolumeUpKeyDisTouch && (event.getFlags() & 1024) == 0) {
                                        Log.d(TAG, "keycode: KEYCODE_VOLUME_UP is comsumed by disable touch mode.");
                                        this.mVolumeUpKeyDisTouch = true;
                                        this.mVolumeUpKeyDisTouchTime = event.getDownTime();
                                        this.mVolumeUpKeyConsumedByDisTouch = false;
                                        if (!this.mHintShown) {
                                            this.mHwScreenOnProximityLock.forceShowHint();
                                            this.mHintShown = true;
                                        }
                                        cancelPendingPowerKeyActionForDistouch();
                                        interceptTouchDisableMode();
                                        break;
                                    }
                                    if (isScreenOn && !this.mScreenRecorderVolumeUpKeyTriggered && (event.getFlags() & 1024) == 0) {
                                        cancelPendingPowerKeyActionForDistouch();
                                        this.mScreenRecorderVolumeUpKeyTriggered = true;
                                        this.mScreenRecorderVolumeUpKeyTime = event.getDownTime();
                                        this.mScreenRecorderVolumeUpKeyConsumed = false;
                                        interceptScreenRecorder();
                                    }
                                    Log.d(TAG, "isScreenOn=" + isScreenOn + " mSystraceLogVolumeUpKeyTriggered=" + this.mSystraceLogVolumeUpKeyTriggered + " mScreenRecorderVolumeUpKeyConsumed=" + this.mScreenRecorderVolumeUpKeyConsumed);
                                    if (Jlog.isEnable() && Jlog.isBetaUser() && isScreenOn && !this.mSystraceLogVolumeUpKeyTriggered && !this.mSystraceLogPowerKeyTriggered && !this.mSystraceLogVolumeDownKeyTriggered && !this.mScreenRecorderVolumeUpKeyConsumed && (event.getFlags() & 1024) == 0) {
                                        this.mSystraceLogVolumeUpKeyTriggered = true;
                                        this.mSystraceLogVolumeUpKeyTime = event.getDownTime();
                                        this.mSystraceLogVolumeUpKeyConsumed = false;
                                        interceptSystraceLog();
                                        Log.d(TAG, "volumeup process: fingerprint first, then volumeup");
                                        if (this.mSystraceLogVolumeUpKeyConsumed) {
                                            return result & -2;
                                        }
                                    }
                                    if (getTelecommService().isInCall() && (result & 1) == 0 && this.mCust != null && this.mCust.isVolumnkeyWakeup()) {
                                        this.mCust.volumnkeyWakeup(this.mContext, isScreenOn, this.mPowerManager);
                                    }
                                } else {
                                    this.mVolumeUpKeyDisTouch = false;
                                    this.mScreenRecorderVolumeUpKeyTriggered = false;
                                    cancelPendingScreenRecorderAction();
                                    this.mSystraceLogVolumeUpKeyTriggered = false;
                                }
                                if (this.mCust != null) {
                                    if (this.mCust.interceptVolumeUpKey(event, this.mContext, isScreenOn, keyguardIsShowingTq(), isMusicActive() ? isVoiceCall() : true, isInjected, down)) {
                                        return result;
                                    }
                                }
                            }
                            break;
                        case 26:
                            cancelSmartKeyLongPressed();
                            if (!down) {
                                if (this.mHwScreenOnProximityLock != null && this.mHwScreenOnProximityLock.isShowing() && isScreenOn && !this.mPowerKeyDisTouch && (event.getFlags() & 1024) == 0) {
                                    this.mPowerKeyDisTouch = true;
                                    this.mPowerKeyDisTouchTime = event.getDownTime();
                                    interceptTouchDisableMode();
                                }
                                if (isScreenOn && !this.mScreenRecorderPowerKeyTriggered && (event.getFlags() & 1024) == 0) {
                                    this.mScreenRecorderPowerKeyTriggered = true;
                                    this.mScreenRecorderPowerKeyTime = event.getDownTime();
                                    interceptScreenRecorder();
                                }
                                if (isScreenOn && !this.mSystraceLogPowerKeyTriggered && (event.getFlags() & 1024) == 0) {
                                    this.mSystraceLogPowerKeyTriggered = true;
                                    this.mSystraceLogFingerPrintTime = 0;
                                    this.mSystraceLogVolumeUpKeyTriggered = false;
                                    break;
                                }
                            }
                            this.mPowerKeyDisTouch = false;
                            this.mScreenRecorderPowerKeyTriggered = false;
                            cancelPendingScreenRecorderAction();
                            this.mSystraceLogPowerKeyTriggered = false;
                            break;
                        case 82:
                            if (!this.mHasNavigationBar && down) {
                                if (!isScreenInLockTaskMode()) {
                                    this.mMenuKeyPress = true;
                                    this.mMenuKeyPressTime = event.getDownTime();
                                    interceptBackandMenuKey();
                                    break;
                                }
                                this.mMenuKeyPress = false;
                                this.mMenuKeyPressTime = 0;
                                break;
                            }
                        case MemoryConstant.MSG_DIRECT_SWAPPINESS /*303*/:
                            if (mTplusEnabled && down) {
                                if (event.getEventTime() - this.mLastTouchDownUpLeftKeyDownTime < 400) {
                                    this.isTouchDownUpLeftDoubleClick = true;
                                    this.mTouchDownUpLeftConsumeCount = 2;
                                    notifyTouchplusService(MemoryConstant.MSG_DIRECT_SWAPPINESS, 1);
                                }
                                this.mLastTouchDownUpLeftKeyDownTime = event.getEventTime();
                                break;
                            }
                        case MemoryConstant.MSG_PROTECTLRU_SET_FILENODE /*304*/:
                            if (mTplusEnabled && down) {
                                if (event.getEventTime() - this.mLastTouchDownUpRightKeyDownTime < 400) {
                                    this.isTouchDownUpRightDoubleClick = true;
                                    this.mTouchDownUpRightConsumeCount = 2;
                                    notifyTouchplusService(MemoryConstant.MSG_PROTECTLRU_SET_FILENODE, 1);
                                }
                                this.mLastTouchDownUpRightKeyDownTime = event.getEventTime();
                                break;
                            }
                        case MemoryConstant.MSG_PROTECTLRU_CONFIG_UPDATE /*308*/:
                            Log.i(TAG, "KeyEvent.KEYCODE_SMARTKEY in");
                            if (!down) {
                                if (this.mHintShown) {
                                    this.mHintShown = false;
                                    return 0;
                                }
                            } else if (this.mHwScreenOnProximityLock != null && this.mHwScreenOnProximityLock.isShowing() && isScreenOn && !this.mHintShown && (event.getFlags() & 1024) == 0) {
                                Log.d(TAG, "keycode: " + keyCode + " is comsumed by disable touch mode.");
                                this.mHwScreenOnProximityLock.forceShowHint();
                                this.mHintShown = true;
                                return 0;
                            }
                            if (!isScreenOn) {
                                handleSmartKey(this.mContext, event, this.mHandler, isScreenOn);
                                return 0;
                            }
                            break;
                        case 401:
                        case 402:
                        case 403:
                        case 404:
                        case 405:
                            processing_KEYCODE_SOUNDTRIGGER_EVENT(keyCode, this.mContext, isMusicActive(), down, keyguardIsShowingTq());
                            break;
                        case 501:
                        case 502:
                        case 511:
                        case 512:
                        case 513:
                        case 514:
                        case WifiProCommonUtils.RESP_CODE_UNSTABLE /*601*/:
                            Log.d(TAG, "event.flags=" + flags + " previous mSystraceLogFingerPrintTime=" + this.mSystraceLogFingerPrintTime);
                            if (flags == 8) {
                                if (Jlog.isEnable() || !Jlog.isBetaUser() || !down || !isScreenOn || this.mSystraceLogPowerKeyTriggered || this.mSystraceLogVolumeDownKeyTriggered) {
                                    return result & -2;
                                }
                                this.mSystraceLogFingerPrintTime = event.getDownTime();
                                return result & -2;
                            }
                            break;
                    }
                    return super.interceptKeyBeforeQueueing(event, policyFlags);
                } else {
                    newEvent = new KeyEvent(event.getDownTime(), event.getEventTime(), event.getAction(), event.getKeyCode(), event.getRepeatCount(), event.getMetaState(), event.getDeviceId() & -16777217, event.getScanCode(), event.getFlags(), event.getSource());
                    Log.d(TAG, "transfer volume event to internet audio :" + event);
                    msg = this.mHandlerEx.obtainMessage(11, newEvent);
                    msg.setAsynchronous(true);
                    msg.sendToTarget();
                    return result & -2;
                }
            }
            if (mTplusEnabled && !isRinging()) {
                ContentResolver resolver = this.mContext.getContentResolver();
                int value = System.getInt(resolver, "hw_membrane_touch_enabled", 0);
                if (value == 0 && down) {
                    notifyTouchplusService(4, 0);
                }
                int navibaron = System.getInt(resolver, "hw_membrane_touch_navbar_enabled", 0);
                if (down && 1 == value && 1 == navibaron) {
                    if (System.getInt(resolver, TOUCHPLUS_SETTINGS_VIBRATION, 1) == 1) {
                        Log.v(TAG, "vibration is not disabled by user");
                        performHapticFeedbackLw(null, 1, true);
                    }
                }
            }
            if ((16777216 & policyFlags) == 0) {
            }
            isWakeKeyFun = isWakeKeyFun(keyCode);
            if ((policyFlags & 1) == 0) {
                i = 0;
            } else {
                i = 1;
            }
            isWakeKey = isWakeKeyFun | i;
            if (isScreenOn) {
            }
            result = 0;
            if (down) {
                if (isWakeKey) {
                }
            }
            if (this.mFocusedWindow == null) {
            }
            if (isJackDeviceEvent(event.getDeviceId(), keyCode)) {
                switch (keyCode) {
                    case 3:
                    case 4:
                    case 187:
                        if (!down) {
                            break;
                        }
                        if (!isTrikeyNaviKeycodeFromLON(isInjected, isExcluedBackScene())) {
                            sendLightTimeoutMsg();
                            if (down) {
                                handled = this.mBackTrikeyHandled;
                                if (this.mBackTrikeyHandled) {
                                    this.mBackTrikeyHandled = true;
                                    this.mHandlerEx.removeMessages(MSG_TRIKEY_BACK_LONG_PRESS);
                                }
                                if (handled) {
                                    return 0;
                                }
                                startHwVibrate(VIBRATOR_SHORT_PRESS_FOR_FRONT_FP);
                                if (this.mTrikeyNaviMode == 1) {
                                    Flog.bdReport(this.mContext, 16);
                                    sendKeyEvent(187);
                                    return 0;
                                }
                            }
                            this.mBackTrikeyHandled = false;
                            msg = this.mHandlerEx.obtainMessage(MSG_TRIKEY_BACK_LONG_PRESS);
                            msg.setAsynchronous(true);
                            this.mHandlerEx.sendMessageDelayed(msg, ViewConfiguration.get(this.mContext).getDeviceGlobalActionKeyTimeout());
                            if (this.mTrikeyNaviMode == 1) {
                                return 0;
                            }
                            if (!isScreenInLockTaskMode()) {
                                this.mBackKeyPress = false;
                                this.mBackKeyPressTime = 0;
                                break;
                            }
                            this.mBackKeyPress = true;
                            this.mBackKeyPressTime = event.getDownTime();
                            interceptBackandMenuKey();
                            break;
                            break;
                        }
                        return 0;
                        break;
                    case 24:
                    case 25:
                    case 164:
                        if (keyCode == 25) {
                            if (down) {
                                if (this.mHwScreenOnProximityLock == null) {
                                    break;
                                }
                                cancelPendingPowerKeyActionForDistouch();
                                this.mScreenRecorderVolumeDownKeyTriggered = true;
                                cancelPendingScreenRecorderAction();
                                this.mSystraceLogVolumeDownKeyTriggered = true;
                                this.mSystraceLogFingerPrintTime = 0;
                                this.mSystraceLogVolumeUpKeyTriggered = false;
                                break;
                            }
                            this.mVolumeDownKeyDisTouch = false;
                            this.mScreenRecorderVolumeDownKeyTriggered = false;
                            cancelPendingScreenRecorderAction();
                            this.mSystraceLogVolumeDownKeyTriggered = false;
                            keyguardShow = keyguardIsShowingTq();
                            Log.d(TAG, "interceptVolumeDownKey down=" + down + " keyguardShow=" + keyguardShow + " policyFlags=" + Integer.toHexString(policyFlags));
                            if (!isDeviceProvisioned()) {
                                if (down) {
                                    if (event.getEventTime() - event.getDownTime() < 500) {
                                        resetVolumeDownKeyLongPressed();
                                        break;
                                    }
                                    cancelPendingQuickCallChordAction();
                                    break;
                                }
                                isIntercept = false;
                                isVolumeDownDoubleClick = false;
                                isVoiceCall = isVoiceCall();
                                if (isMusicActive()) {
                                }
                                if (!isMusicActive()) {
                                    break;
                                }
                                if (this.isVoiceRecognitionActive) {
                                    interval = event.getEventTime() - this.mLastStartVassistantServiceTime;
                                    if (interval > DISABLE_VOLUMEDOWN_DOUBLE_CLICK_INTERVAl_MAX) {
                                        this.isVoiceRecognitionActive = false;
                                    } else if (interval > DISABLE_VOLUMEDOWN_DOUBLE_CLICK_INTERVAl_MIN) {
                                        this.isVoiceRecognitionActive = AudioSystem.isSourceActive(6);
                                    }
                                }
                                Log.i(TAG, "isMusicOrFMOrVoiceCallActive=" + z + " isVoiceRecognitionActive=" + this.isVoiceRecognitionActive);
                                timediff = event.getEventTime() - this.mLastVolumeDownKeyDownTime;
                                this.mLastVolumeDownKeyDownTime = event.getEventTime();
                                if (timediff < 400) {
                                    isVolumeDownDoubleClick = true;
                                    if (this.mListener == null) {
                                        this.mListener = new ProximitySensorListener();
                                    }
                                    turnOnSensorListener();
                                    Log.i(TAG, "mIsProximity " + this.mIsProximity + ", mSensorRegisted " + this.mSensorRegisted);
                                    notifyRapidCaptureService("start");
                                    turnOffSensorListener();
                                    result &= -2;
                                    break;
                                }
                                notifyRapidCaptureService("wakeup");
                                if (this.mListener == null) {
                                    this.mListener = new ProximitySensorListener();
                                }
                                turnOnSensorListener();
                                break;
                                isIntercept = true;
                                notifyVassistantService("wakeup", 2, event);
                                if (this.mListener == null) {
                                    this.mListener = new ProximitySensorListener();
                                }
                                turnOnSensorListener();
                                interceptQuickCallChord();
                                isIntercept = true;
                                Log.i(TAG, "intercept volume down key, isIntercept=" + isIntercept + " now=" + SystemClock.uptimeMillis() + " EventTime=" + event.getEventTime());
                                if (isIntercept) {
                                    this.mCust.volumnkeyWakeup(this.mContext, isScreenOn, this.mPowerManager);
                                    break;
                                }
                                return result;
                                break;
                            }
                            Log.i(TAG, "Device is not Provisioned");
                            break;
                            break;
                        } else if (keyCode == 24) {
                            if (down) {
                                if (this.mHwScreenOnProximityLock == null) {
                                    break;
                                }
                                cancelPendingPowerKeyActionForDistouch();
                                this.mScreenRecorderVolumeUpKeyTriggered = true;
                                this.mScreenRecorderVolumeUpKeyTime = event.getDownTime();
                                this.mScreenRecorderVolumeUpKeyConsumed = false;
                                interceptScreenRecorder();
                                Log.d(TAG, "isScreenOn=" + isScreenOn + " mSystraceLogVolumeUpKeyTriggered=" + this.mSystraceLogVolumeUpKeyTriggered + " mScreenRecorderVolumeUpKeyConsumed=" + this.mScreenRecorderVolumeUpKeyConsumed);
                                this.mSystraceLogVolumeUpKeyTriggered = true;
                                this.mSystraceLogVolumeUpKeyTime = event.getDownTime();
                                this.mSystraceLogVolumeUpKeyConsumed = false;
                                interceptSystraceLog();
                                Log.d(TAG, "volumeup process: fingerprint first, then volumeup");
                                if (this.mSystraceLogVolumeUpKeyConsumed) {
                                    return result & -2;
                                }
                                this.mCust.volumnkeyWakeup(this.mContext, isScreenOn, this.mPowerManager);
                                break;
                            }
                            this.mVolumeUpKeyDisTouch = false;
                            this.mScreenRecorderVolumeUpKeyTriggered = false;
                            cancelPendingScreenRecorderAction();
                            this.mSystraceLogVolumeUpKeyTriggered = false;
                            if (this.mCust != null) {
                                if (isMusicActive()) {
                                }
                                if (this.mCust.interceptVolumeUpKey(event, this.mContext, isScreenOn, keyguardIsShowingTq(), isMusicActive() ? isVoiceCall() : true, isInjected, down)) {
                                    return result;
                                }
                            }
                        }
                        break;
                    case 26:
                        cancelSmartKeyLongPressed();
                        if (!down) {
                            this.mPowerKeyDisTouch = false;
                            this.mScreenRecorderPowerKeyTriggered = false;
                            cancelPendingScreenRecorderAction();
                            this.mSystraceLogPowerKeyTriggered = false;
                            break;
                        }
                        this.mPowerKeyDisTouch = true;
                        this.mPowerKeyDisTouchTime = event.getDownTime();
                        interceptTouchDisableMode();
                        this.mScreenRecorderPowerKeyTriggered = true;
                        this.mScreenRecorderPowerKeyTime = event.getDownTime();
                        interceptScreenRecorder();
                        this.mSystraceLogPowerKeyTriggered = true;
                        this.mSystraceLogFingerPrintTime = 0;
                        this.mSystraceLogVolumeUpKeyTriggered = false;
                        break;
                    case 82:
                        if (!isScreenInLockTaskMode()) {
                            this.mMenuKeyPress = false;
                            this.mMenuKeyPressTime = 0;
                            break;
                        }
                        this.mMenuKeyPress = true;
                        this.mMenuKeyPressTime = event.getDownTime();
                        interceptBackandMenuKey();
                        break;
                        break;
                    case MemoryConstant.MSG_DIRECT_SWAPPINESS /*303*/:
                        if (event.getEventTime() - this.mLastTouchDownUpLeftKeyDownTime < 400) {
                            this.isTouchDownUpLeftDoubleClick = true;
                            this.mTouchDownUpLeftConsumeCount = 2;
                            notifyTouchplusService(MemoryConstant.MSG_DIRECT_SWAPPINESS, 1);
                        }
                        this.mLastTouchDownUpLeftKeyDownTime = event.getEventTime();
                        break;
                    case MemoryConstant.MSG_PROTECTLRU_SET_FILENODE /*304*/:
                        if (event.getEventTime() - this.mLastTouchDownUpRightKeyDownTime < 400) {
                            this.isTouchDownUpRightDoubleClick = true;
                            this.mTouchDownUpRightConsumeCount = 2;
                            notifyTouchplusService(MemoryConstant.MSG_PROTECTLRU_SET_FILENODE, 1);
                        }
                        this.mLastTouchDownUpRightKeyDownTime = event.getEventTime();
                        break;
                    case MemoryConstant.MSG_PROTECTLRU_CONFIG_UPDATE /*308*/:
                        Log.i(TAG, "KeyEvent.KEYCODE_SMARTKEY in");
                        if (!down) {
                            Log.d(TAG, "keycode: " + keyCode + " is comsumed by disable touch mode.");
                            this.mHwScreenOnProximityLock.forceShowHint();
                            this.mHintShown = true;
                            return 0;
                        } else if (this.mHintShown) {
                            this.mHintShown = false;
                            return 0;
                        }
                        if (isScreenOn) {
                            handleSmartKey(this.mContext, event, this.mHandler, isScreenOn);
                            return 0;
                        }
                        break;
                    case 401:
                    case 402:
                    case 403:
                    case 404:
                    case 405:
                        processing_KEYCODE_SOUNDTRIGGER_EVENT(keyCode, this.mContext, isMusicActive(), down, keyguardIsShowingTq());
                        break;
                    case 501:
                    case 502:
                    case 511:
                    case 512:
                    case 513:
                    case 514:
                    case WifiProCommonUtils.RESP_CODE_UNSTABLE /*601*/:
                        Log.d(TAG, "event.flags=" + flags + " previous mSystraceLogFingerPrintTime=" + this.mSystraceLogFingerPrintTime);
                        if (flags == 8) {
                            if (Jlog.isEnable()) {
                                break;
                            }
                            return result & -2;
                        }
                        break;
                }
                return super.interceptKeyBeforeQueueing(event, policyFlags);
            }
            newEvent = new KeyEvent(event.getDownTime(), event.getEventTime(), event.getAction(), event.getKeyCode(), event.getRepeatCount(), event.getMetaState(), event.getDeviceId() & -16777217, event.getScanCode(), event.getFlags(), event.getSource());
            Log.d(TAG, "transfer volume event to internet audio :" + event);
            msg = this.mHandlerEx.obtainMessage(11, newEvent);
            msg.setAsynchronous(true);
            msg.sendToTarget();
            return result & -2;
        }
        Log.d(TAG, " has intercept Key for block : " + keyCode + ", isdown : " + down + ", flags : " + flags);
        return 0;
    }

    boolean isRinging() {
        TelecomManager telecomManager = getTelecommService();
        return (telecomManager == null || !telecomManager.isRinging()) ? false : PPPOEStateMachine.PHASE_INITIALIZE.equals(SystemProperties.get("persist.sys.show_incallscreen", PPPOEStateMachine.PHASE_DEAD));
    }

    public KeyEvent dispatchUnhandledKey(WindowState win, KeyEvent event, int policyFlags) {
        int keyCode = event.getKeyCode();
        boolean isScreenOn = (536870912 & policyFlags) != 0;
        switch (keyCode) {
            case MemoryConstant.MSG_PROTECTLRU_CONFIG_UPDATE /*308*/:
                if (event.getRepeatCount() != 0) {
                    Log.d(TAG, "event.getRepeatCount() != 0 so just break");
                    return null;
                } else if (SystemProperties.getBoolean("ro.config.fingerOnSmartKey", false) && needDropSmartKey()) {
                    return null;
                } else {
                    handleSmartKey(this.mContext, event, this.mHandler, isScreenOn);
                    return null;
                }
            default:
                return super.dispatchUnhandledKey(win, event, policyFlags);
        }
    }

    public long interceptKeyBeforeDispatching(WindowState win, KeyEvent event, int policyFlags) {
        int keyCode = event.getKeyCode();
        int repeatCount = event.getRepeatCount();
        int flags = event.getFlags();
        int origKeyCode = event.getOrigKeyCode();
        boolean down = event.getAction() == 0;
        int deviceID = event.getDeviceId();
        boolean isInjected = (16777216 & policyFlags) != 0;
        try {
            if (this.mIHwWindowCallback != null) {
                this.mIHwWindowCallback.interceptKeyBeforeDispatching(event, policyFlags);
            }
        } catch (Exception ex) {
            Log.w(TAG, "mIHwWindowCallback interceptKeyBeforeDispatching threw RemoteException", ex);
        }
        int result = getDisabledKeyEventResult(keyCode);
        if (-2 != result) {
            return (long) result;
        }
        if ((keyCode == 3 || keyCode == 187) && win != null && (win.getAttrs().hwFlags & FLOATING_MASK) == FLOATING_MASK) {
            return 0;
        }
        if (win != null && (win.getAttrs().hwFlags & 8) == 8 && ((keyCode == 25 || keyCode == 24) && "true".equals(SystemProperties.get("runtime.mmitest.isrunning", "false")))) {
            if (isJackDeviceEvent(event.getDeviceId(), keyCode)) {
                Log.i(TAG, "Pass jack volume event to mmi test before dispatching.");
                return 0;
            }
            Log.i(TAG, "Prevent hard key volume event to mmi test before dispatching.");
            return -1;
        } else if (isJackDeviceEvent(event.getDeviceId(), keyCode)) {
            Log.d(TAG, "skip volume event for jack device");
            return -1;
        } else {
            long now;
            long timeoutTime;
            if ((origKeyCode == 305 || origKeyCode == 306 || origKeyCode == 307) && mTplusEnabled) {
                ContentResolver resolver = this.mContext.getContentResolver();
                int touchPlusOn = System.getInt(resolver, "hw_membrane_touch_enabled", 0);
                int value = System.getInt(resolver, "hw_membrane_touch_navbar_enabled", 0);
                if (!(touchPlusOn == 0 || value == 0)) {
                    if (isRinging() && origKeyCode != 307) {
                    }
                }
                return -1;
            }
            if (keyCode == 303 && mTplusEnabled) {
                if (this.isTouchDownUpLeftDoubleClick) {
                    if (!down) {
                        this.mTouchDownUpLeftConsumeCount--;
                        if (this.mTouchDownUpLeftConsumeCount == 0) {
                            this.isTouchDownUpLeftDoubleClick = false;
                        }
                    }
                    return -1;
                } else if (repeatCount == 0) {
                    now = SystemClock.uptimeMillis();
                    timeoutTime = event.getEventTime() + 400;
                    if (now < timeoutTime) {
                        return timeoutTime - now;
                    }
                }
            }
            if (keyCode == 304 && mTplusEnabled) {
                if (this.isTouchDownUpRightDoubleClick) {
                    if (!down) {
                        this.mTouchDownUpRightConsumeCount--;
                        if (this.mTouchDownUpRightConsumeCount == 0) {
                            this.isTouchDownUpRightDoubleClick = false;
                        }
                    }
                    return -1;
                } else if (repeatCount == 0) {
                    now = SystemClock.uptimeMillis();
                    timeoutTime = event.getEventTime() + 400;
                    if (now < timeoutTime) {
                        return timeoutTime - now;
                    }
                }
            }
            if (keyCode == 82) {
                if (mTplusEnabled && origKeyCode == 307 && 1 == System.getInt(this.mContext.getContentResolver(), "hw_membrane_touch_navbar_enabled", 0)) {
                    if (down) {
                        if (repeatCount == 0) {
                            this.mMenuClickedOnlyOnce = true;
                        } else if (repeatCount == 1) {
                            this.mMenuClickedOnlyOnce = false;
                            transactToStatusBarService(101, "resentapp", "resentapp", 0);
                        }
                    } else if (this.mMenuClickedOnlyOnce) {
                        this.mMenuClickedOnlyOnce = false;
                        if (this.mLastFocusNeedsMenu) {
                            sendHwMenuKeyEvent();
                        } else {
                            toggleRecentApps();
                        }
                    }
                    return -1;
                } else if (!this.mHasNavigationBar && (268435456 & flags) == 0) {
                    if (!down) {
                        if (this.mMenuClickedOnlyOnce) {
                            this.mMenuClickedOnlyOnce = false;
                            sendHwMenuKeyEvent();
                        }
                        cancelPreloadRecentApps();
                    } else if (repeatCount == 0) {
                        this.mMenuClickedOnlyOnce = true;
                        preloadRecentApps();
                    } else if (repeatCount == 1) {
                        this.mMenuClickedOnlyOnce = false;
                        toggleRecentApps();
                    }
                    return -1;
                }
            }
            if (this.mVolumeUpKeyDisTouch && !this.mPowerKeyDisTouch && (flags & 1024) == 0) {
                now = SystemClock.uptimeMillis();
                timeoutTime = this.mVolumeUpKeyDisTouchTime + 150;
                if (now < timeoutTime) {
                    return timeoutTime - now;
                }
                return -1;
            }
            if (keyCode == 24) {
                if (this.mVolumeUpKeyConsumedByDisTouch) {
                    if (!down) {
                        this.mVolumeUpKeyConsumedByDisTouch = false;
                        this.mHintShown = false;
                    }
                    return -1;
                } else if (this.mHintShown) {
                    if (!down) {
                        this.mHintShown = false;
                    }
                    return -1;
                }
            }
            switch (keyCode) {
                case 3:
                case 4:
                case 25:
                case 187:
                    if (this.mHintShown) {
                        if (!down) {
                            this.mHintShown = false;
                        }
                        return -1;
                    } else if (deviceID > 0 && FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION && FrontFingerPrintSettings.isSupportTrikey() && !isMMITesting() && keyCode == 187) {
                        if (isTrikeyNaviKeycodeFromLON(isInjected, isExcluedRecentScene())) {
                            return -1;
                        }
                        sendLightTimeoutMsg();
                        if (!down) {
                            boolean handled = this.mRecentTrikeyHandled;
                            if (!this.mRecentTrikeyHandled) {
                                this.mRecentTrikeyHandled = true;
                                this.mHandlerEx.removeMessages(MSG_TRIKEY_RECENT_LONG_PRESS);
                            }
                            if (!handled) {
                                if (this.mTrikeyNaviMode == 1) {
                                    startHwVibrate(VIBRATOR_SHORT_PRESS_FOR_FRONT_FP);
                                    sendKeyEvent(4);
                                } else if (this.mTrikeyNaviMode == 0) {
                                    Flog.bdReport(this.mContext, 17);
                                    startHwVibrate(VIBRATOR_SHORT_PRESS_FOR_FRONT_FP);
                                    toggleRecentApps();
                                }
                            }
                        } else if (repeatCount == 0) {
                            this.mRecentTrikeyHandled = false;
                            Message msg = this.mHandlerEx.obtainMessage(MSG_TRIKEY_RECENT_LONG_PRESS);
                            msg.setAsynchronous(true);
                            this.mHandlerEx.sendMessageDelayed(msg, ViewConfiguration.get(this.mContext).getDeviceGlobalActionKeyTimeout());
                            if (this.mTrikeyNaviMode == 0) {
                                preloadRecentApps();
                            }
                        }
                        return -1;
                    }
            }
            if ((flags & 1024) == 0) {
                if (this.mScreenRecorderVolumeUpKeyTriggered && !this.mScreenRecorderPowerKeyTriggered) {
                    now = SystemClock.uptimeMillis();
                    timeoutTime = this.mScreenRecorderVolumeUpKeyTime + 150;
                    if (now < timeoutTime) {
                        return timeoutTime - now;
                    }
                }
                if (keyCode == 24 && this.mScreenRecorderVolumeUpKeyConsumed) {
                    if (!down) {
                        this.mScreenRecorderVolumeUpKeyConsumed = false;
                    }
                    return -1;
                }
                if (this.mSystraceLogVolumeUpKeyTriggered) {
                    now = SystemClock.uptimeMillis();
                    timeoutTime = this.mSystraceLogVolumeUpKeyTime + 150;
                    if (now < timeoutTime) {
                        Log.d(TAG, "keyCode=" + keyCode + " down=" + down + " in queue: now=" + now + " timeout=" + timeoutTime);
                        return timeoutTime - now;
                    }
                }
                if (keyCode == 24 && this.mSystraceLogVolumeUpKeyConsumed) {
                    if (!down) {
                        this.mSystraceLogVolumeUpKeyConsumed = false;
                    }
                    Log.d(TAG, "systracelog volumeup down=" + down + " leave queue");
                    return -1;
                }
            }
            return super.interceptKeyBeforeDispatching(win, event, policyFlags);
        }
    }

    private final void sendHwMenuKeyEvent() {
        int[] actions = new int[]{0, 1};
        for (int keyEvent : actions) {
            long curTime = SystemClock.uptimeMillis();
            InputManager.getInstance().injectInputEvent(new KeyEvent(curTime, curTime, keyEvent, 82, 0, 0, -1, 0, 268435464, 257), 0);
        }
    }

    protected void launchAssistAction(String hint, int deviceId) {
        if (checkPackageInstalled("com.google.android.googlequicksearchbox")) {
            super.launchAssistAction(hint, deviceId);
            return;
        }
        sendCloseSystemWindows();
        boolean enableVoiceAssistant = Secure.getInt(this.mContext.getContentResolver(), "hw_long_home_voice_assistant", 0) == 1;
        if (IS_LONG_HOME_VASSITANT && enableVoiceAssistant) {
            performHapticFeedbackLw(null, 0, false);
            try {
                String intent = "android.intent.action.ASSIST";
                if (checkPackageInstalled(HUAWEI_VASSISTANT_PACKAGE)) {
                    intent = VOICE_ASSISTANT_ACTION;
                }
                this.mContext.startActivity(new Intent(intent).setFlags(268435456));
            } catch (ActivityNotFoundException anfe) {
                Slog.w(TAG, "No activity to handle voice assistant action.", anfe);
            }
        }
    }

    private boolean checkPackageInstalled(String packageName) {
        try {
            this.mContext.getPackageManager().getPackageInfo(packageName, 128);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    private boolean isHwDarkTheme(Context context, int themeId) {
        boolean z = false;
        try {
            if (context.getResources().getResourceName(themeId).indexOf("Emui.Dark") >= 0) {
                z = true;
            }
            return z;
        } catch (Exception e) {
            return false;
        }
    }

    boolean isMusicActive() {
        if (((AudioManager) this.mContext.getSystemService("audio")) != null) {
            return AudioSystem.isStreamActive(3, 0);
        }
        Log.w(TAG, "isMusicActive: couldn't get AudioManager reference");
        return false;
    }

    boolean isDeviceProvisioned() {
        if (Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0) {
            return true;
        }
        return false;
    }

    void handleVolumeKey(int stream, int keycode) {
        IAudioService audioService = getAudioService();
        if (audioService != null) {
            try {
                int i;
                if (this.mBroadcastWakeLock != null) {
                    this.mBroadcastWakeLock.acquire();
                }
                if (keycode == 24) {
                    i = 1;
                } else {
                    i = -1;
                }
                audioService.adjustStreamVolume(stream, i, 0, this.mContext.getOpPackageName());
                if (this.mBroadcastWakeLock != null) {
                    this.mBroadcastWakeLock.release();
                }
            } catch (RemoteException e) {
                Log.e(TAG, "IAudioService.adjust*StreamVolume() threw RemoteException");
                if (this.mBroadcastWakeLock != null) {
                    this.mBroadcastWakeLock.release();
                }
            } catch (Throwable th) {
                if (this.mBroadcastWakeLock != null) {
                    this.mBroadcastWakeLock.release();
                }
            }
        }
    }

    static IAudioService getAudioService() {
        IAudioService audioService = IAudioService.Stub.asInterface(ServiceManager.checkService("audio"));
        if (audioService == null) {
            Log.w(TAG, "Unable to find IAudioService interface.");
        }
        return audioService;
    }

    private void sendVolumeDownKeyPressed() {
        this.mHandler.postDelayed(this.mHandleVolumeDownKey, 500);
    }

    private void cancelVolumeDownKeyPressed() {
        this.mHandler.removeCallbacks(this.mHandleVolumeDownKey);
    }

    private void resetVolumeDownKeyPressed() {
        if (this.mHandler.hasCallbacks(this.mHandleVolumeDownKey)) {
            this.mHandler.removeCallbacks(this.mHandleVolumeDownKey);
            this.mHandler.post(this.mHandleVolumeDownKey);
        }
    }

    private void interceptQuickCallChord() {
        this.mHandler.postDelayed(this.mVolumeDownLongPressed, 500);
    }

    private void cancelPendingQuickCallChordAction() {
        this.mHandler.removeCallbacks(this.mVolumeDownLongPressed);
        resetVolumeDownKeyPressed();
    }

    private void resetVolumeDownKeyLongPressed() {
        if (this.mHandler.hasCallbacks(this.mVolumeDownLongPressed)) {
            this.mHandler.removeCallbacks(this.mVolumeDownLongPressed);
            this.mHandler.post(this.mVolumeDownLongPressed);
        }
    }

    private void initQuickcall() {
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        if (this.mPowerManager != null) {
            this.mBroadcastWakeLock = this.mPowerManager.newWakeLock(1, "HwPhoneWindowManager.mBroadcastWakeLock");
            this.mVolumeDownWakeLock = this.mPowerManager.newWakeLock(1, "HwPhoneWindowManager.mVolumeDownWakeLock");
        }
        this.mHeadless = PPPOEStateMachine.PHASE_INITIALIZE.equals(SystemProperties.get("ro.config.headless", PPPOEStateMachine.PHASE_DEAD));
    }

    private void notifyRapidCaptureService(String command) {
        if (this.mSystemReady) {
            Intent intent = new Intent(HUAWEI_RAPIDCAPTURE_START_MODE);
            intent.setPackage("com.huawei.camera");
            intent.putExtra(SMARTKEY_TAG, command);
            this.mContext.startServiceAsUser(intent, UserHandle.CURRENT_OR_SELF);
            if (this.mVolumeDownWakeLock != null) {
                this.mVolumeDownWakeLock.acquire(500);
            }
            Bundle extras = intent.getExtras();
            if (extras != null) {
                Log.d(TAG, "start Rapid Capture Service, command:" + extras.get(SMARTKEY_TAG));
            }
        }
    }

    public void showHwTransientBars() {
        if (this.mStatusBar != null) {
            requestHwTransientBars(this.mStatusBar);
        }
    }

    private void notifyTouchplusService(int kcode, int kval) {
        Intent intent = new Intent("com.huawei.membranetouch.action.MT_MANAGER");
        intent.putExtra("keycode", kcode);
        intent.putExtra("keyvalue", kval);
        intent.setPackage("com.huawei.membranetouch");
        this.mContext.startService(intent);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void transactToStatusBarService(int code, String transactName, String paramName, int paramValue) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            IBinder statusBarServiceBinder = getHWStatusBarService().asBinder();
            if (statusBarServiceBinder != null) {
                data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                if (paramName != null) {
                    data.writeInt(paramValue);
                }
                statusBarServiceBinder.transact(code, data, reply, 0);
            }
            reply.recycle();
            data.recycle();
        } catch (RemoteException e) {
            Log.e(TAG, "transactToStatusBarService four params->threw remote exception");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void transactToStatusBarService(int code, String transactName, int isEmuiStyle, int statusbarColor, int navigationBarColor, int isEmuiLightStyle) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            if (getHWStatusBarService() != null) {
                IBinder statusBarServiceBinder = getHWStatusBarService().asBinder();
                if (statusBarServiceBinder != null) {
                    Log.d(TAG, "Transact:" + transactName + " to status bar service");
                    data.writeInterfaceToken("com.android.internal.statusbar.IStatusBarService");
                    data.writeInt(isEmuiStyle);
                    data.writeInt(statusbarColor);
                    data.writeInt(navigationBarColor);
                    data.writeInt(isEmuiLightStyle);
                    statusBarServiceBinder.transact(code, data, reply, 0);
                }
            }
            reply.recycle();
            data.recycle();
        } catch (RemoteException e) {
            Log.e(TAG, "transactToStatusBarService->threw remote exception");
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
        }
    }

    public void updateSystemUiColorLw(WindowState win) {
        if (win != null) {
            LayoutParams attrs = win.getAttrs();
            if (this.mLastColorWin != win || this.mLastStatusBarColor != attrs.statusBarColor || this.mLastNavigationBarColor != attrs.navigationBarColor) {
                boolean colorChanged;
                boolean isFloating = getFloatingValue(attrs.isEmuiStyle);
                boolean isPopup = (attrs.type == 1000 || attrs.type == 1002 || attrs.type == 2009 || attrs.type == 2010) ? true : attrs.type == 2003;
                if (attrs.type == 3) {
                }
                boolean isTouchExplrEnabled = this.mAccessibilityManager.isTouchExplorationEnabled();
                int isEmuiStyle = getEmuiStyleValue(attrs.isEmuiStyle);
                int statusBarColor = attrs.statusBarColor;
                int navigationBarColor = attrs.navigationBarColor;
                int isEmuiLightStyle = getEmuiLightStyleValue(attrs.hwFlags);
                if (isTouchExplrEnabled) {
                    colorChanged = isTouchExplrEnabled != this.mIsTouchExplrEnabled;
                    isEmuiStyle = -2;
                    isEmuiLightStyle = -1;
                } else if (this.mLastStatusBarColor != statusBarColor) {
                    colorChanged = true;
                } else if (this.mLastNavigationBarColor != navigationBarColor) {
                    colorChanged = true;
                } else {
                    colorChanged = false;
                }
                boolean styleChanged = isEmuiStyleChanged(isEmuiStyle, isEmuiLightStyle);
                boolean isKeyguardHostWindow = (win == this.mStatusBar || attrs.type == 2024) ? true : isKeyguardHostWindow(attrs);
                boolean isInMultiWindowMode = (isKeyguardHostWindow || isFloating || isPopup || (attrs.type == 2034)) ? true : win.getStackId() != 3 ? win.isInMultiWindowMode() : false;
                boolean z = (!styleChanged || isInMultiWindowMode) ? (styleChanged || isInMultiWindowMode) ? false : colorChanged : true;
                if (!isInMultiWindowMode) {
                    win.setCanCarryColors(true);
                }
                if (z) {
                    int i;
                    if (isTouchExplrEnabled) {
                        i = -16777216;
                    } else {
                        i = statusBarColor;
                    }
                    this.mLastStatusBarColor = i;
                    if (isTouchExplrEnabled) {
                        i = -16777216;
                    } else {
                        i = navigationBarColor;
                    }
                    this.mLastNavigationBarColor = i;
                    this.mLastIsEmuiStyle = isEmuiStyle;
                    this.mIsTouchExplrEnabled = isTouchExplrEnabled;
                    this.mLastColorWin = win;
                    this.mLastIsEmuiLightStyle = isEmuiLightStyle;
                    Slog.v(TAG, "updateSystemUiColorLw window=" + win + ",EmuiStyle=" + isEmuiStyle + ",StatusBarColor=0x" + Integer.toHexString(statusBarColor) + ",NavigationBarColor=0x" + Integer.toHexString(navigationBarColor) + ", mLastIsEmuiLightStyle=" + this.mLastIsEmuiLightStyle);
                    this.mHandler.post(new Runnable() {
                        public void run() {
                            HwPhoneWindowManager.this.transactToStatusBarService(CPUFeature.MSG_PROCESS_GROUP_CHANGE, "setSystemUIColor", HwPhoneWindowManager.this.mLastIsEmuiStyle, HwPhoneWindowManager.this.mLastStatusBarColor, HwPhoneWindowManager.this.mLastNavigationBarColor, HwPhoneWindowManager.this.mLastIsEmuiLightStyle);
                        }
                    });
                }
            }
        }
    }

    protected int getEmuiStyleValue(int styleValue) {
        return styleValue == -1 ? -1 : Integer.MAX_VALUE & styleValue;
    }

    protected int getEmuiLightStyleValue(int styleValue) {
        return (styleValue & 16) != 0 ? 1 : -1;
    }

    protected boolean isEmuiStyleChanged(int isEmuiStyle, int isEmuiLightStyle) {
        return (this.mLastIsEmuiStyle == isEmuiStyle && this.mLastIsEmuiLightStyle == isEmuiLightStyle) ? false : true;
    }

    protected boolean getFloatingValue(int styleValue) {
        return styleValue != -1 && (styleValue & FLOATING_MASK) == FLOATING_MASK;
    }

    public void onTouchExplorationStateChanged(boolean enabled) {
        updateSystemUiColorLw(getCurrentWin());
    }

    protected void hwInit() {
        this.mAccessibilityManager.addTouchExplorationStateChangeListener(this);
    }

    IStatusBarService getHWStatusBarService() {
        IStatusBarService iStatusBarService;
        synchronized (this.mServiceAquireLock) {
            if (this.mStatusBarService == null) {
                this.mStatusBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
            }
            iStatusBarService = this.mStatusBarService;
        }
        return iStatusBarService;
    }

    private boolean isJackDeviceEvent(int deviceId, int keyCode) {
        if (deviceId == -1 || (16777216 & deviceId) == 0) {
            return false;
        }
        return 25 == keyCode || 24 == keyCode;
    }

    private void dispatchInternetAudioKeyWithWakeLock(KeyEvent event) {
        IInternetAudioService internetAudioService = getInternetAudioService();
        if (internetAudioService != null) {
            try {
                internetAudioService.dispatchMediaKeyEvent(event);
                return;
            } catch (RemoteException e) {
                Log.e(TAG, "dispatchInternetMediaKeyEvent threw RemoteException");
                return;
            }
        }
        Intent service = new Intent();
        service.setClassName("com.huawei.internetaudioservice", "com.huawei.internetaudioservice.InternetAudioService");
        this.mContext.startService(service);
    }

    private static IInternetAudioService getInternetAudioService() {
        IInternetAudioService audioService = IInternetAudioService.Stub.asInterface(ServiceManager.checkService("internet_audio"));
        if (audioService == null) {
            Log.w(TAG, "Unable to find IInternetAudioService interface, or the service is killed, so restart it!");
        }
        return audioService;
    }

    private void interceptTouchDisableMode() {
        if (this.mVolumeUpKeyDisTouch && this.mPowerKeyDisTouch && !this.mVolumeDownKeyDisTouch) {
            long now = SystemClock.uptimeMillis();
            if (now <= this.mVolumeUpKeyDisTouchTime + 150 && now <= this.mPowerKeyDisTouchTime + 150) {
                this.mVolumeUpKeyConsumedByDisTouch = true;
                cancelPendingPowerKeyActionForDistouch();
                if (this.mHwScreenOnProximityLock != null) {
                    this.mHwScreenOnProximityLock.releaseLock();
                }
            }
        }
    }

    public void screenTurningOn(ScreenOnListener screenOnListener) {
        super.screenTurningOn(screenOnListener);
        if (this.mContext == null) {
            Log.d(TAG, "Context object is null.");
            return;
        }
        boolean isModeEnabled = System.getIntForUser(this.mContext.getContentResolver(), KEY_TOUCH_DISABLE_MODE, 1, ActivityManager.getCurrentUser()) > 0 ? !"factory".equals(SystemProperties.get("ro.runmode", "normal")) : false;
        if (this.mHwScreenOnProximityLock != null && isModeEnabled) {
            this.mHwScreenOnProximityLock.acquireLock(this);
        }
        if (SystemProperties.getBoolean("ro.config.hw_easywakeup", false) && this.mSystemReady) {
            EasyWakeUpManager mWakeUpManager = EasyWakeUpManager.getInstance(this.mContext, this.mHandler, this.mKeyguardDelegate);
            if (mWakeUpManager != null) {
                mWakeUpManager.turnOffSensorListener();
            }
        }
    }

    public void screenTurnedOff() {
        super.screenTurnedOff();
        if (this.mHwScreenOnProximityLock != null) {
            this.mHwScreenOnProximityLock.releaseLock();
        }
        if (SystemProperties.getBoolean("ro.config.hw_easywakeup", false) && this.mSystemReady) {
            EasyWakeUpManager mWakeUpManager = EasyWakeUpManager.getInstance(this.mContext, this.mHandler, this.mKeyguardDelegate);
            if (mWakeUpManager != null) {
                mWakeUpManager.turnOnSensorListener();
            }
        }
        try {
            if (this.mIHwWindowCallback != null) {
                this.mIHwWindowCallback.screenTurnedOff();
            }
        } catch (Exception ex) {
            Log.w(TAG, "mIHwWindowCallback threw RemoteException", ex);
        }
    }

    public int selectAnimationLw(WindowState win, int transit) {
        if (win != this.mNavigationBar || this.mNavigationBarOnBottom || (transit != 1 && transit != 3)) {
            return super.selectAnimationLw(win, transit);
        }
        return mIsHwNaviBar ? 0 : 17432615;
    }

    public void updateNavigationBar(boolean minNaviBar) {
        if (this.mNavigationBarPolicy != null) {
            if (minNaviBar) {
                this.mNavigationBarHeightForRotationDefault = (int[]) this.mNavigationBarHeightForRotationMin.clone();
                this.mNavigationBarWidthForRotationDefault = (int[]) this.mNavigationBarWidthForRotationMin.clone();
            } else {
                this.mNavigationBarHeightForRotationDefault = (int[]) this.mNavigationBarHeightForRotationMax.clone();
                this.mNavigationBarWidthForRotationDefault = (int[]) this.mNavigationBarWidthForRotationMax.clone();
            }
            this.mNavigationBarPolicy.updateNavigationBar(minNaviBar);
        }
    }

    public int getNonDecorDisplayWidth(int fullWidth, int fullHeight, int rotation, int uiMode) {
        if ((uiMode & 15) == 3) {
            return super.getNonDecorDisplayWidth(fullWidth, fullHeight, rotation, uiMode);
        }
        if (!this.mHasNavigationBar || !this.mNavigationBarCanMove || fullWidth <= fullHeight) {
            return fullWidth;
        }
        if (this.mNavigationBarPolicy != null && this.mNavigationBarPolicy.mMinNavigationBar) {
            return fullWidth - this.mNavigationBarWidthForRotationMin[rotation];
        }
        int nonDecorDisplayWidth;
        if (getNavibarAlignLeftWhenLand()) {
            nonDecorDisplayWidth = fullWidth - this.mContext.getResources().getDimensionPixelSize(34472125);
        } else {
            nonDecorDisplayWidth = fullWidth - this.mNavigationBarWidthForRotationMax[rotation];
        }
        return nonDecorDisplayWidth;
    }

    public int getNonDecorDisplayHeight(int fullWidth, int fullHeight, int rotation, int uiMode) {
        if ((uiMode & 15) == 3) {
            return super.getNonDecorDisplayHeight(fullWidth, fullHeight, rotation, uiMode);
        }
        if (!this.mHasNavigationBar || (this.mNavigationBarCanMove && fullWidth >= fullHeight)) {
            return fullHeight;
        }
        if (this.mNavigationBarPolicy == null || !this.mNavigationBarPolicy.mMinNavigationBar) {
            return fullHeight - this.mNavigationBarHeightForRotationMax[rotation];
        }
        return fullHeight - this.mNavigationBarHeightForRotationMin[rotation];
    }

    public void setInputMethodWindowVisible(boolean visible) {
        this.mInputMethodWindowVisible = visible;
    }

    public void setNaviBarFlag(boolean flag) {
        if (flag != this.isNavibarHide) {
            this.isNavibarHide = flag;
            HwSlog.d(TAG, "setNeedHideWindow setFlag isNavibarHide is " + this.isNavibarHide);
        }
    }

    public int getNaviBarHeightForRotationMin(int index) {
        return this.mNavigationBarHeightForRotationMin[index];
    }

    public int getNaviBarWidthForRotationMin(int index) {
        return this.mNavigationBarWidthForRotationMin[index];
    }

    public int getNaviBarHeightForRotationMax(int index) {
        return this.mNavigationBarHeightForRotationMax[index];
    }

    public int getNaviBarWidthForRotationMax(int index) {
        return this.mNavigationBarWidthForRotationMax[index];
    }

    public void setInitialDisplaySize(Display display, int width, int height, int density) {
        super.setInitialDisplaySize(display, width, height, density);
        if (this.mContext != null) {
            Resources res = this.mContext.getResources();
            ContentResolver resolver = this.mContext.getContentResolver();
            int[] iArr = this.mNavigationBarHeightForRotationMax;
            int i = this.mPortraitRotation;
            int dimensionPixelSize = res.getDimensionPixelSize(17104920);
            this.mNavigationBarHeightForRotationMax[this.mUpsideDownRotation] = dimensionPixelSize;
            iArr[i] = dimensionPixelSize;
            iArr = this.mNavigationBarHeightForRotationMax;
            i = this.mLandscapeRotation;
            dimensionPixelSize = res.getDimensionPixelSize(17104921);
            this.mNavigationBarHeightForRotationMax[this.mSeascapeRotation] = dimensionPixelSize;
            iArr[i] = dimensionPixelSize;
            iArr = this.mNavigationBarHeightForRotationMin;
            i = this.mPortraitRotation;
            dimensionPixelSize = System.getInt(resolver, "navigationbar_height_min", 0);
            this.mNavigationBarHeightForRotationMin[this.mSeascapeRotation] = dimensionPixelSize;
            this.mNavigationBarHeightForRotationMin[this.mLandscapeRotation] = dimensionPixelSize;
            this.mNavigationBarHeightForRotationMin[this.mUpsideDownRotation] = dimensionPixelSize;
            iArr[i] = dimensionPixelSize;
            iArr = this.mNavigationBarWidthForRotationMax;
            i = this.mPortraitRotation;
            dimensionPixelSize = res.getDimensionPixelSize(17104922);
            this.mNavigationBarWidthForRotationMax[this.mSeascapeRotation] = dimensionPixelSize;
            this.mNavigationBarWidthForRotationMax[this.mLandscapeRotation] = dimensionPixelSize;
            this.mNavigationBarWidthForRotationMax[this.mUpsideDownRotation] = dimensionPixelSize;
            iArr[i] = dimensionPixelSize;
            iArr = this.mNavigationBarWidthForRotationMin;
            i = this.mPortraitRotation;
            dimensionPixelSize = System.getInt(resolver, "navigationbar_width_min", 0);
            this.mNavigationBarWidthForRotationMin[this.mSeascapeRotation] = dimensionPixelSize;
            this.mNavigationBarWidthForRotationMin[this.mLandscapeRotation] = dimensionPixelSize;
            this.mNavigationBarWidthForRotationMin[this.mUpsideDownRotation] = dimensionPixelSize;
            iArr[i] = dimensionPixelSize;
        }
    }

    protected boolean computeNaviBarFlag() {
        boolean z = false;
        LayoutParams focusAttrs = null;
        if (this.mFocusedWindow != null) {
            focusAttrs = this.mFocusedWindow.getAttrs();
        }
        int type = focusAttrs != null ? focusAttrs.type : 0;
        boolean forceNavibar = focusAttrs != null ? (focusAttrs.hwFlags & 1) == 1 : false;
        boolean keyguardOn = type != 2101 ? type == 2100 : true;
        boolean keyguardOn2 = type == 2009 ? keyguardOn() : false;
        boolean dreamOn = focusAttrs != null && focusAttrs.type == 2023;
        boolean isNeedHideNaviBarWin = (focusAttrs == null || (focusAttrs.privateFlags & FLOATING_MASK) == 0) ? false : true;
        if (this.mStatusBar == this.mFocusedWindow) {
            return false;
        }
        if (keyguardOn2 && !forceNavibar) {
            return true;
        }
        if (dreamOn) {
            return false;
        }
        if (keyguardOn || isNeedHideNaviBarWin) {
            return true;
        }
        if (this.isNavibarHide && !this.mInputMethodWindowVisible) {
            z = true;
        }
        return z;
    }

    public boolean isNaviBarMini() {
        if (this.mNavigationBarPolicy == null || !this.mNavigationBarPolicy.mMinNavigationBar) {
            return false;
        }
        return true;
    }

    public boolean swipeFromTop() {
        if (Secure.getInt(this.mContext.getContentResolver(), "device_provisioned", 1) == 0) {
            return true;
        }
        if (!mIsHwNaviBar) {
            return false;
        }
        if (isLastImmersiveMode()) {
            requestHwTransientBars(this.mStatusBar);
        } else {
            requestTransientStatusBars();
        }
        return true;
    }

    public boolean swipeFromBottom() {
        if (Secure.getInt(this.mContext.getContentResolver(), "device_provisioned", 1) == 0) {
            return true;
        }
        if (!mIsHwNaviBar || !isLastImmersiveMode() || this.mNavigationBar == null || !this.mNavigationBarOnBottom) {
            return false;
        }
        requestHwTransientBars(this.mNavigationBar);
        return true;
    }

    public boolean swipeFromRight() {
        if (!mIsHwNaviBar || !isLastImmersiveMode() || this.mNavigationBar == null || this.mNavigationBarOnBottom) {
            return false;
        }
        requestHwTransientBars(this.mNavigationBar);
        return true;
    }

    public boolean isGestureIsolated() {
        WindowState win = this.mFocusedWindow != null ? this.mFocusedWindow : this.mTopFullscreenOpaqueWindowState;
        if (win == null || (win.getAttrs().hwFlags & 512) != 512) {
            return false;
        }
        return true;
    }

    public void requestTransientStatusBars() {
        synchronized (this.mWindowManagerFuncs.getWindowManagerLock()) {
            BarController barController = getStatusBarController();
            boolean sb = false;
            if (barController != null) {
                sb = barController.checkShowTransientBarLw();
            }
            if (sb && barController != null) {
                barController.showTransient();
            }
            ImmersiveModeConfirmation immer = getImmersiveModeConfirmation();
            if (immer != null) {
                immer.confirmCurrentPrompt();
            }
            updateHwSystemUiVisibilityLw();
        }
    }

    public boolean isTopIsFullscreen() {
        return this.mTopIsFullscreen;
    }

    public boolean okToShowTransientBar() {
        boolean z = false;
        BarController barController = getStatusBarController();
        if (barController == null) {
            return false;
        }
        if (barController.checkShowTransientBarLw()) {
            z = true;
        }
        return z;
    }

    private void turnOnSensorListener() {
        if (this.mSensorManager == null) {
            this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        }
        if (this.mCoverManager == null) {
            this.mCoverManager = new CoverManager();
        }
        if (this.mCoverManager != null) {
            this.mCoverOpen = this.mCoverManager.isCoverOpen();
        }
        boolean touchDisableModeOpen = System.getIntForUser(this.mContext.getContentResolver(), KEY_TOUCH_DISABLE_MODE, 1, -2) == 1;
        if (this.mCoverOpen && !this.mSensorRegisted && this.mListener != null && touchDisableModeOpen) {
            Log.i(TAG, "turnOnSensorListener, registerListener");
            this.mSensorManager.registerListener(this.mListener, this.mSensorManager.getDefaultSensor(8), 0);
            this.mSensorRegisted = true;
            this.mHandler.removeCallbacks(this.mProximitySensorTimeoutRunnable);
            this.mHandler.postDelayed(this.mProximitySensorTimeoutRunnable, 1000);
        }
    }

    public void turnOffSensorListener() {
        if (this.mSensorRegisted && this.mListener != null) {
            Log.i(TAG, "turnOffSensorListener, unregisterListener ");
            this.mSensorManager.unregisterListener(this.mListener);
            this.mHandler.removeCallbacks(this.mProximitySensorTimeoutRunnable);
        }
        this.mSensorRegisted = false;
    }

    public void setHwWindowCallback(IHwWindowCallback hwWindowCallback) {
        Log.i(TAG, "setHwWindowCallback=" + hwWindowCallback);
        this.mIHwWindowCallback = hwWindowCallback;
    }

    public IHwWindowCallback getHwWindowCallback() {
        return this.mIHwWindowCallback;
    }

    public void updateSettings() {
        Flog.i(1503, "updateSettings");
        super.updateSettings();
        updateFingerSenseSettings();
        setFingerSenseState();
        setNaviBarState();
    }

    private void updateFingerSenseSettings() {
        ContentResolver cr = this.mContext.getContentResolver();
        FingerSenseSettings.updateSmartshotEnabled(cr);
        FingerSenseSettings.updateLineGestureEnabled(cr);
        FingerSenseSettings.updateDrawGestureEnabled(cr);
    }

    public void enableScreenAfterBoot() {
        super.enableScreenAfterBoot();
        enableSystemWideAfterBoot(this.mContext);
        enableFingerPrintActionsAfterBoot(this.mContext);
    }

    public WindowState getFocusedWindow() {
        return this.mFocusedWindow;
    }

    public int getRestrictedScreenHeight() {
        return this.mRestrictedScreenHeight;
    }

    public boolean isNavigationBarVisible() {
        return (!this.mHasNavigationBar || this.mNavigationBar == null) ? false : this.mNavigationBar.isVisibleLw();
    }

    protected void enableSystemWideActions() {
        if (SystemProperties.getBoolean("ro.config.finger_joint", false)) {
            Flog.i(1503, "FingerSense enableSystemWideActions");
            if (this.systemWideActionsListener == null) {
                this.systemWideActionsListener = new SystemWideActionsListener(this.mContext, this);
                this.mWindowManagerFuncs.registerPointerEventListener(this.systemWideActionsListener);
                this.systemWideActionsListener.createPointerLocationView();
            }
            SystemProperties.set("persist.sys.fingersense", PPPOEStateMachine.PHASE_INITIALIZE);
            return;
        }
        Flog.i(1503, "Can not enable fingersense, ro.config.finger_joint is set to false");
    }

    protected void disableSystemWideActions() {
        Flog.i(1503, "FingerSense disableSystemWideActions");
        if (this.systemWideActionsListener != null) {
            this.mWindowManagerFuncs.unregisterPointerEventListener(this.systemWideActionsListener);
            this.systemWideActionsListener.destroyPointerLocationView();
            this.systemWideActionsListener = null;
        }
        SystemProperties.set("persist.sys.fingersense", PPPOEStateMachine.PHASE_DEAD);
    }

    protected void enableFingerPrintActions() {
        if (this.fingerprintActionsListener == null) {
            this.fingerprintActionsListener = new FingerprintActionsListener(this.mContext, this);
            this.mWindowManagerFuncs.registerPointerEventListener(this.fingerprintActionsListener);
            this.fingerprintActionsListener.createSearchPanelView();
            this.fingerprintActionsListener.createMultiWinArrowView();
        }
    }

    protected void disableFingerPrintActions() {
        if (this.fingerprintActionsListener != null) {
            this.mWindowManagerFuncs.unregisterPointerEventListener(this.fingerprintActionsListener);
            this.fingerprintActionsListener.destroySearchPanelView();
            this.fingerprintActionsListener.destroyMultiWinArrowView();
            this.fingerprintActionsListener = null;
        }
    }

    protected void enableFingerPrintActionsAfterBoot(Context context) {
        final ContentResolver resolver = context.getContentResolver();
        this.mHandler.post(new Runnable() {
            public void run() {
                if (FrontFingerPrintSettings.isNaviBarEnabled(resolver)) {
                    HwPhoneWindowManager.this.disableFingerPrintActions();
                } else {
                    HwPhoneWindowManager.this.enableFingerPrintActions();
                }
            }
        });
    }

    protected void setNaviBarState() {
        boolean navibarEnable = FrontFingerPrintSettings.isNaviBarEnabled(this.mContext.getContentResolver());
        if (this.mNavibarEnabled != navibarEnable) {
            int i;
            this.mNavibarEnabled = navibarEnable;
            Handler handler = this.mHandlerEx;
            if (navibarEnable) {
                i = 103;
            } else {
                i = 104;
            }
            handler.sendEmptyMessage(i);
        }
    }

    protected void updateSplitScreenView() {
        if (this.fingerprintActionsListener != null) {
            this.fingerprintActionsListener.createMultiWinArrowView();
        }
    }

    protected void enableSystemWideAfterBoot(Context context) {
        final ContentResolver resolver = context.getContentResolver();
        this.mHandler.post(new Runnable() {
            public void run() {
                if (FingerSenseSettings.isFingerSenseEnabled(resolver)) {
                    HwPhoneWindowManager.this.enableSystemWideActions();
                } else {
                    HwPhoneWindowManager.this.disableSystemWideActions();
                }
            }
        });
    }

    protected void setFingerSenseState() {
        boolean fingersense = FingerSenseSettings.isFingerSenseEnabled(this.mContext.getContentResolver());
        if (this.mFingerSenseEnabled != fingersense) {
            int i;
            this.mFingerSenseEnabled = fingersense;
            Flog.i(1503, "setFingerSenseState to " + fingersense);
            Handler handler = this.mHandlerEx;
            if (fingersense) {
                i = 101;
            } else {
                i = 102;
            }
            handler.sendEmptyMessage(i);
        }
    }

    public void processing_KEYCODE_SOUNDTRIGGER_EVENT(int keyCode, Context context, boolean isMusicOrFMActive, boolean down, boolean keyguardShow) {
        Log.d(TAG, "intercept DSP WAKEUP EVENT" + keyCode + " down=" + down + " keyguardShow=" + keyguardShow);
        this.mContext = context;
        ITelephony telephonyService = Stub.asInterface(ServiceManager.checkService("phone"));
        switch (keyCode) {
            case 401:
                if (down) {
                    Log.i(TAG, "soundtrigger wakeup.");
                    if (isTOPActivity(HUAWEI_VOICE_SOUNDTRIGGER_PACKAGE)) {
                        Log.i(TAG, "start SoundTiggerTest");
                        notifySoundTriggerTest();
                        return;
                    } else if (isTOPActivity(HUAWEI_VOICE_DEBUG_BETACLUB)) {
                        Log.i(TAG, "soundtrigger debug during betaclub.");
                        notifySoundTriggerTest();
                        return;
                    } else {
                        Log.i(TAG, "start VA");
                        notifyVassistantService("start", 4, null);
                        return;
                    }
                }
                return;
            case 402:
                if (down) {
                    Log.i(TAG, "command that find my phone.");
                    if (telephonyService != null) {
                    }
                    if (isTOPActivity(HUAWEI_VOICE_SOUNDTRIGGER_PACKAGE)) {
                        Log.i(TAG, "looking for my phone during SoundTiggerTest");
                        return;
                    } else if (isTOPActivity(HUAWEI_VOICE_DEBUG_BETACLUB)) {
                        Log.i(TAG, "looking for my phone during betaclub.");
                        return;
                    } else {
                        Log.i(TAG, "findphone.");
                        notifyVassistantService("findphone", 4, null);
                        return;
                    }
                }
                return;
            default:
                return;
        }
    }

    private boolean isTOPActivity(String appnames) {
        try {
            List<RunningTaskInfo> tasks = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(1);
            if (tasks == null || tasks.isEmpty()) {
                return false;
            }
            for (RunningTaskInfo info : tasks) {
                Log.i(TAG, "info.topActivity.getPackageName() is " + info.topActivity.getPackageName());
                if (info.topActivity.getPackageName().equals(appnames) && info.baseActivity.getPackageName().equals(appnames)) {
                    return true;
                }
            }
            return false;
        } catch (RuntimeException e) {
            Log.e(TAG, "isTOPActivity->RuntimeException happened");
        } catch (Exception e2) {
            Log.e(TAG, "isTOPActivity->other exception happened");
        }
    }

    private void notifyVassistantService(String command, int mode, KeyEvent event) {
        Intent intent = new Intent(ACTION_HUAWEI_VASSISTANT_SERVICE);
        intent.putExtra(HUAWEI_VASSISTANT_EXTRA_START_MODE, mode);
        intent.putExtra(SMARTKEY_TAG, command);
        if (event != null) {
            intent.putExtra("KeyEvent", event);
        }
        intent.setPackage(HUAWEI_VASSISTANT_PACKAGE);
        this.mContext.startService(intent);
        if (this.mVolumeDownWakeLock != null) {
            this.mVolumeDownWakeLock.acquire(500);
        }
        Bundle extras = intent.getExtras();
        if (extras != null) {
            Log.d(TAG, "start VASSISTANT Service, state:" + extras.get(HUAWEI_VASSISTANT_EXTRA_START_MODE) + " command:" + extras.get(SMARTKEY_TAG));
        }
    }

    private void notifySoundTriggerTest() {
        try {
            this.mContext.sendBroadcast(new Intent(HUAWEI_VOICE_SOUNDTRIGGER_BROADCAST));
            Log.i(TAG, "start up HUAWEI_VOICE_SOUNDTRIGGER_BROADCAST");
        } catch (ActivityNotFoundException e) {
            Log.w(TAG, "No receiver to handle HUAWEI_VOICE_SOUNDTRIGGER_BROADCAST intent", e);
        }
    }

    public void handleSmartKey(Context context, KeyEvent event, Handler handler, boolean isScreenOn) {
        boolean down = event.getAction() == 0;
        int keyCode = event.getKeyCode();
        if (ActivityManagerNative.getDefault().testIsSystemReady()) {
            if (this.DEBUG_SMARTKEY) {
                Log.d(TAG, "handleSmartKey keycode = " + keyCode + " down = " + down + " isScreenOn = " + isScreenOn);
            }
            switch (keyCode) {
                case MemoryConstant.MSG_PROTECTLRU_CONFIG_UPDATE /*308*/:
                    if (!down) {
                        if (SystemProperties.getBoolean("ro.config.fingerOnSmartKey", false)) {
                            this.mHandler.postDelayed(this.mCancleInterceptFingerprintEvent, 400);
                        }
                        if (!this.mIsSmartKeyDoubleClick && !this.mIsSmartKeyTripleOrMoreClick && event.getEventTime() - event.getDownTime() < 500) {
                            cancelSmartKeyLongPressed();
                            sendSmartKeyEvent(SMARTKEY_CLICK);
                            break;
                        }
                        cancelSmartKeyLongPressed();
                        break;
                    }
                    if (SystemProperties.getBoolean("ro.config.fingerOnSmartKey", false)) {
                        this.mNeedDropFingerprintEvent = true;
                        this.mHandler.removeCallbacks(this.mCancleInterceptFingerprintEvent);
                    }
                    if (!isScreenOn) {
                        if (this.mListener == null) {
                            this.mListener = new ProximitySensorListener();
                        }
                        turnOnSensorListener();
                    }
                    sendSmartKeyEvent(SMARTKEY_LP);
                    long timediff = event.getEventTime() - this.mLastSmartKeyDownTime;
                    this.mLastSmartKeyDownTime = event.getEventTime();
                    if (timediff < 400) {
                        if (!this.mIsSmartKeyDoubleClick && !this.mIsSmartKeyTripleOrMoreClick) {
                            cancelSmartKeyClick();
                            cancelSmartKeyLongPressed();
                            sendSmartKeyEvent(SMARTKEY_DCLICK);
                            this.mIsSmartKeyDoubleClick = true;
                            break;
                        }
                        this.mIsSmartKeyTripleOrMoreClick = true;
                        this.mIsSmartKeyDoubleClick = false;
                        break;
                    }
                    this.mIsSmartKeyTripleOrMoreClick = false;
                    this.mIsSmartKeyDoubleClick = false;
                    break;
                    break;
            }
            return;
        }
        Log.d(TAG, "System is not ready, just discard it this time.");
    }

    private void sendSmartKeyEvent(String Type) {
        if (SMARTKEY_LP.equals(Type)) {
            this.mHandler.postDelayed(this.mSmartKeyLongPressed, 500);
        } else if (SMARTKEY_DCLICK.equals(Type)) {
            notifySmartKeyEvent(SMARTKEY_DCLICK);
        } else {
            this.mHandler.postDelayed(this.mSmartKeyClick, 400);
        }
    }

    private void cancelSmartKeyClick() {
        this.mHandler.removeCallbacks(this.mSmartKeyClick);
    }

    private void cancelSmartKeyLongPressed() {
        this.mHandler.removeCallbacks(this.mSmartKeyLongPressed);
    }

    private void notifySmartKeyEvent(String strType) {
        Intent intent = new Intent(HUAWEI_SMARTKEY_PACKAGE);
        intent.setFlags(268435456);
        intent.putExtra(SMARTKEY_TAG, strType);
        this.mContext.sendBroadcast(intent);
        Log.i(TAG, "send smart key " + strType);
        if ((!this.mIsProximity && this.mSensorRegisted) || !this.mSensorRegisted || (isPhoneInCall() && SMARTKEY_LP.equals(strType))) {
            intent.setPackage(HUAWEI_SMARTKEY_SERVICE);
            this.mContext.startServiceAsUser(intent, UserHandle.CURRENT);
            Log.i(TAG, "notify smartkey service " + strType);
        }
        turnOffSensorListener();
    }

    public boolean getNeedDropFingerprintEvent() {
        return this.mNeedDropFingerprintEvent;
    }

    private String getTopActivity() {
        return ((ActivityManagerService) ServiceManager.getService("activity")).topAppName();
    }

    private void initDropSmartKey() {
        String dropSmartKeyActivity = Systemex.getString(this.mResolver, DROP_SMARTKEY_ACTIVITY);
        if (TextUtils.isEmpty(dropSmartKeyActivity)) {
            Log.w(TAG, "dropSmartKeyActivity not been configured in hw_defaults.xml!");
            return;
        }
        for (String str : dropSmartKeyActivity.split(";")) {
            this.needDropSmartKeyActivities.add(str);
        }
    }

    private boolean needDropSmartKey() {
        boolean result = false;
        String topActivityName = getTopActivity();
        if (this.needDropSmartKeyActivities != null && this.needDropSmartKeyActivities.contains(topActivityName)) {
            result = true;
            Log.d(TAG, "drop smartkey event because of conflict with fingerprint authentication!");
        }
        if ((!isCamera() || !this.isFingerShotCameraOn) && ((!isInCallUIAndRinging() || !this.isFingerAnswerPhoneOn) && (!isAlarm(this.mCurUser) || !this.isFingerStopAlarmOn))) {
            return result;
        }
        Log.d(TAG, "drop smartkey event because of conflict with fingerprint longpress event!");
        return true;
    }

    private boolean isCamera() {
        String pkgName = getTopActivity();
        return pkgName != null ? pkgName.startsWith("com.huawei.camera") : false;
    }

    private boolean isInCallUIAndRinging() {
        TelecomManager telecomManager = (TelecomManager) this.mContext.getSystemService("telecom");
        return telecomManager != null ? telecomManager.isRinging() : false;
    }

    private boolean isAlarm(int user) {
        return ((HwActivityManagerService) ServiceManager.getService("activity")).serviceIsRunning(ComponentName.unflattenFromString("com.android.deskclock/.alarmclock.AlarmKlaxon"), user);
    }

    public void waitKeyguardDismissDone(KeyguardDismissDoneListener listener) {
        synchronized (this.mLock) {
            this.mKeyguardDismissListener = listener;
        }
        this.mWindowManagerInternal.waitForKeyguardDismissDone(this.mKeyguardDismissDoneCallback, 300);
    }

    public void cancelWaitKeyguardDismissDone() {
        synchronized (this.mLock) {
            this.mKeyguardDismissListener = null;
        }
    }

    protected void finishKeyguardDismissDone() {
        synchronized (this.mLock) {
            KeyguardDismissDoneListener listener = this.mKeyguardDismissListener;
            this.mKeyguardDismissListener = null;
        }
        if (listener != null) {
            listener.onKeyguardDismissDone();
        }
    }

    public void setInterceptInputForWaitBrightness(boolean intercept) {
        this.mInterceptInputForWaitBrightness = intercept;
    }

    public boolean getInterceptInputForWaitBrightness() {
        return this.mInterceptInputForWaitBrightness;
    }

    private void interceptBackandMenuKey() {
        long now = SystemClock.uptimeMillis();
        if (isScreenInLockTaskMode() && this.mBackKeyPress && this.mMenuKeyPress && now <= this.mBackKeyPressTime + 2000 && now <= this.mMenuKeyPressTime + 2000) {
            this.mBackKeyPress = false;
            this.mMenuKeyPress = false;
            this.mBackKeyPressTime = 0;
            this.mMenuKeyPressTime = 0;
        }
    }

    private boolean isScreenInLockTaskMode() {
        boolean isScreenLocked = false;
        try {
            isScreenLocked = ActivityManagerNative.getDefault().isInLockTaskMode();
        } catch (RemoteException e) {
            Log.e(TAG, "isScreenInLockTaskMode  ", e);
        }
        return isScreenLocked;
    }

    public boolean isStatusBarObsecured() {
        return this.mStatuBarObsecured;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean isStatusBarObsecuredByWin(WindowState win) {
        boolean z = false;
        if (win == null || this.mStatusBar == null || (win.getAttrs().flags & 16) != 0 || win.toString().contains("hwSingleMode_window")) {
            return false;
        }
        Rect winFrame = win.getFrameLw();
        Rect statusbarFrame = this.mStatusBar.getFrameLw();
        if (winFrame.top <= statusbarFrame.top && winFrame.bottom >= statusbarFrame.bottom && winFrame.left <= statusbarFrame.left && winFrame.right >= statusbarFrame.right) {
            z = true;
        }
        return z;
    }

    public void setNaviImmersiveMode(boolean mode) {
        if (this.mNavigationBarPolicy != null) {
            this.mNavigationBarPolicy.setImmersiveMode(mode);
        }
        this.mIsImmersiveMode = mode;
    }

    public boolean getImmersiveMode() {
        return this.mIsImmersiveMode;
    }

    public void adjustConfigurationLw(Configuration config, int keyboardPresence, int navigationPresence) {
        super.adjustConfigurationLw(config, keyboardPresence, navigationPresence);
        int tempDpi = config.densityDpi;
        if (tempDpi != this.lastDensityDpi && this.systemWideActionsListener != null) {
            this.systemWideActionsListener.updateConfiguration();
            this.lastDensityDpi = tempDpi;
        }
    }

    public boolean performHapticFeedbackLw(WindowState win, int effectId, boolean always) {
        if (effectId != 1 || !this.isVibrateImplemented || always) {
            return super.performHapticFeedbackLw(win, effectId, always);
        }
        if (1 != System.getInt(this.mContext.getContentResolver(), "touch_vibrate_mode", 1)) {
            return false;
        }
        HwGeneralManager.getInstance().playIvtEffect("VIRTUAL_KEY");
        return true;
    }

    public void setNavibarAlignLeftWhenLand(boolean isLeft) {
        this.mIsNavibarAlignLeftWhenLand = isLeft;
    }

    public boolean getNavibarAlignLeftWhenLand() {
        return this.mIsNavibarAlignLeftWhenLand;
    }

    public boolean isPhoneIdle() {
        ITelephony telephonyService = getTelephonyService();
        if (telephonyService == null) {
            return false;
        }
        try {
            if (!TelephonyManager.getDefault().isMultiSimEnabled()) {
                return telephonyService.isIdle(this.mContext.getPackageName());
            }
            return telephonyService.isIdleForSubscriber(0, this.mContext.getPackageName()) ? telephonyService.isIdleForSubscriber(1, this.mContext.getPackageName()) : false;
        } catch (RemoteException ex) {
            Log.w(TAG, "ITelephony threw RemoteException", ex);
            return false;
        }
    }

    public int getDisabledKeyEventResult(int keyCode) {
        switch (keyCode) {
            case 3:
                if ((this.mCust == null || !this.mCust.disableHomeKey(this.mContext)) && !HwDeviceManager.disallowOp(14)) {
                    return -2;
                }
                Log.i(TAG, "the device's home key has been disabled for the user.");
                return 0;
            case 4:
                if (!HwDeviceManager.disallowOp(16)) {
                    return -2;
                }
                Log.i(TAG, "the device's back key has been disabled for the user.");
                return -1;
            case 187:
                if (!HwDeviceManager.disallowOp(15)) {
                    return -2;
                }
                Log.i(TAG, "the device's task key has been disabled for the user.");
                return 0;
            default:
                return -2;
        }
    }

    public int[] getTouchCountInfo() {
        return this.mTouchCountPolicy.getTouchCountInfo();
    }

    public int[] getDefaultTouchCountInfo() {
        return this.mTouchCountPolicy.getDefaultTouchCountInfo();
    }

    private boolean isTrikeyNaviKeycodeFromLON(boolean isInjected, boolean excluded) {
        return (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 0 || (!isInjected && this.mTrikeyNaviMode < 0)) ? true : excluded;
    }

    public void overrideRectForForceRotation(WindowState win, Rect pf, Rect df, Rect of, Rect cf, Rect vf, Rect dcf) {
        HwForceRotationManager forceRotationManager = HwForceRotationManager.getDefault();
        if (forceRotationManager.isForceRotationSupported() && forceRotationManager.isForceRotationSwitchOpen(this.mContext) && win != null && win.getAppToken() != null && win.getAttrs() != null) {
            String winTitle = String.valueOf(win.getAttrs().getTitle());
            if (!TextUtils.isEmpty(winTitle) && !winTitle.startsWith("SurfaceView") && !winTitle.startsWith("PopupWindow")) {
                if (win.isInMultiWindowMode()) {
                    Slog.d(TAG, "window is in multiwindow mode");
                    return;
                }
                Display defDisplay = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                DisplayMetrics dm = new DisplayMetrics();
                defDisplay.getMetrics(dm);
                if (dm.widthPixels < dm.heightPixels) {
                    Slog.d(TAG, "the current screen is portrait");
                    return;
                }
                Rect tmpRect = new Rect(vf);
                if (forceRotationManager.isAppForceLandRotatable(win.getAttrs().packageName, win.getAppToken().asBinder())) {
                    forceRotationManager.applyForceRotationLayout(win.getAppToken().asBinder(), tmpRect);
                    if (!tmpRect.equals(vf)) {
                        int i = tmpRect.left;
                        vf.left = i;
                        cf.left = i;
                        df.left = i;
                        pf.left = i;
                        dcf.left = i;
                        of.left = i;
                        i = tmpRect.right;
                        vf.right = i;
                        cf.right = i;
                        df.right = i;
                        pf.right = i;
                        dcf.right = i;
                        of.right = i;
                    }
                    LayoutParams attrs = win.getAttrs();
                    attrs.privateFlags |= 64;
                }
            }
        }
    }
}
