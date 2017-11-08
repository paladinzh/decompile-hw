package com.android.server.audio;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManagerNative;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.soundtrigger.SoundTrigger.GenericSoundModel;
import android.media.AudioRoutesInfo;
import android.media.AudioSystem;
import android.media.IAudioRoutesObserver;
import android.media.SoundPool;
import android.media.soundtrigger.SoundTriggerDetector;
import android.media.soundtrigger.SoundTriggerDetector.Callback;
import android.media.soundtrigger.SoundTriggerDetector.EventPayload;
import android.media.soundtrigger.SoundTriggerManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.UserManagerInternal;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.provider.SettingsEx.Systemex;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.IMonitor;
import android.util.IMonitor.EventStream;
import android.util.Slog;
import android.view.ContextThemeWrapper;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;
import com.android.internal.app.ISoundTriggerService;
import com.android.internal.app.ISoundTriggerService.Stub;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.HwActivityManagerService;
import com.android.server.audio.AbsAudioService.DeviceVolumeState;
import com.android.server.audio.AudioService.SetModeDeathHandler;
import com.android.server.audio.report.AudioConst;
import com.android.server.audio.report.AudioExceptionMsg;
import com.android.server.pfw.autostartup.comm.XmlConst.PreciseIgnore;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.huawei.pgmng.IPGPlugCallbacks;
import com.huawei.pgmng.PGAction;
import com.huawei.pgmng.PGPlug;
import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;
import java.util.UUID;

public class HwAudioService extends AudioService {
    private static final String ACTION_COMMFORCE = "android.intent.action.COMMFORCE";
    private static final String ACTION_INCALL_EXTRA = "IsForegroundActivity";
    private static final String ACTION_INCALL_SCREEN = "InCallScreenIsForegroundActivity";
    private static final String ACTION_START_SOUNDTRIGGER_DETECTOR = "com.huawei.vassistant.intent.action.START_SOUNDTRIGGER";
    private static final String ACTION_WAKEUP_SERVICE = "com.huawei.wakeup.services.WakeupService";
    private static final String BLUETOOTH_PKG_NAME = "com.android.bluetooth";
    private static final boolean DEBUG = SystemProperties.getBoolean("ro.media.debuggable", false);
    private static final boolean DEBUG_THEME_SOUND = false;
    private static final int DTS_CLOSE = 1;
    private static final String DTS_MODE_OFF_PARA = "srs_cfg:trumedia_enable=0";
    private static final String DTS_MODE_ON_PARA = "srs_cfg:trumedia_enable=1";
    private static final String DTS_MODE_PARA = "srs_cfg:trumedia_enable";
    private static final String DTS_MODE_PRESTATE = "persist.sys.dts.prestate";
    private static final int DTS_OFF = 0;
    private static final int DTS_ON = 3;
    private static final int DTS_OPEN = 2;
    private static final int DUALPA_RCV_DEALY_DURATION = 5000;
    private static final String DUALPA_RCV_DEALY_OFF = "dualpa_security_delay=0";
    private static final String DUALPA_RCV_DEALY_ON = "dualpa_security_delay=1";
    private static final String EXTRA_SOUNDTRIGGER_STATE = "soundtrigger_state";
    private static final String[] FORBIDDEN_DTS_WHITE_LIST = new String[]{"com.jawbone.up", "com.codoon.gps", "com.lakala.android", "com.hoolai.magic", "com.android.bankabc"};
    private static final String HS_NO_CHARGE_OFF = "hs_no_charge=off";
    private static final String HS_NO_CHARGE_ON = "hs_no_charge=on";
    public static final int HW_AUDIO_EXCEPTION_OCCOUR = -1000;
    public static final int HW_AUDIO_MODE_TYPE_EXCEPTION_OCCOUR = -1001;
    public static final int HW_AUDIO_TRACK_OVERFLOW_TYPE_EXCEPTION_OCCOUR = -1002;
    public static final int HW_MIC_MUTE_EXCEPTION_OCCOUR = -2002;
    public static final int HW_RING_MODE_TYPE_EXCEPTION_OCCOUR = -2001;
    public static final int HW_SCO_CONNECT_EXCEPTION_OCCOUR = -2003;
    private static final UUID MODEL_UUID = UUID.fromString("7dc67ab3-eab6-4e34-b62a-f4fa3788092a");
    private static final int MSG_AUDIO_EXCEPTION_OCCUR = 10000;
    private static final int MSG_DUALPA_RCV_DEALY = 71;
    private static final int MSG_RECORD_ACTIVE = 61;
    private static final int MSG_RELOAD_SNDEFFS = 99;
    private static final int MSG_RINGER_MODE_CHANGE = 80;
    private static final String PERMISSION_COMMFORCE = "android.permission.COMM_FORCE";
    public static final int PID_BIT_WIDE = 21;
    private static final int RADIO_UID = 1001;
    private static final String[] RECORD_ACTIVE_APP_LIST = new String[]{"com.realvnc.android.remote"};
    private static final String[] RECORD_REQUEST_APP_LIST = new String[]{"com.google.android.googlequicksearchbox", "com.google.android.googlequicksearchbox:search", "com.google.android.googlequicksearchbox:interactor", "ru.yandex.searchplugin"};
    private static final String SESSION_ID = "session_id";
    private static final String SOUNDTRIGGER_MAD_OFF = "mad=off";
    private static final String SOUNDTRIGGER_MAD_ON = "mad=on";
    private static final String SOUNDTRIGGER_START = "start";
    private static final int SOUNDTRIGGER_STATUS_OFF = 0;
    private static final int SOUNDTRIGGER_STATUS_ON = 1;
    private static final String SOUNDTRIGGER_STOP = "stop";
    private static final String SOUNDTRIGGER_WAKUP_OFF = "wakeup=off";
    private static final String SOUNDTRIGGER_WAKUP_ON = "wakeup=on";
    private static final int SPK_RCV_STEREO_OFF = 0;
    private static final String SPK_RCV_STEREO_OFF_PARA = "stereo_landscape_portrait_enable=0";
    private static final int SPK_RCV_STEREO_ON = 1;
    private static final String SPK_RCV_STEREO_ON_PARA = "stereo_landscape_portrait_enable=1";
    private static final String SWS_MODE_OFF_PARA = "HIFIPARA=STEREOWIDEN_Enable=false";
    private static final String SWS_MODE_ON_PARA = "HIFIPARA=STEREOWIDEN_Enable=true";
    private static final int SWS_OFF = 0;
    private static final int SWS_ON = 3;
    private static final int SWS_SCENE_AUDIO = 0;
    private static final int SWS_SCENE_VIDEO = 1;
    private static final String SYSKEY_SOUND_HWT = "syskey_sound_hwt";
    private static final int SYSTEM_UID = 1000;
    private static final String TAG = "HwAudioService";
    private static final long THREE_SEC_LIMITED = 3000;
    private static final String VASSISTANT_PACKAGE_NAME = "com.huawei.vassistant";
    private static boolean isCallForeground = false;
    private static final boolean mIsHuaweiSWS3Config = "sws3".equalsIgnoreCase(SystemProperties.get("ro.config.sws_version", "sws1"));
    private static final boolean mIsHuaweiSafeMediaConfig = SystemProperties.getBoolean("ro.config.huawei_safe_media", true);
    private static Handler mMyHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    if (AudioSystem.getDeviceConnectionState(8, AppHibernateCst.INVALID_PKG) == 1 || AudioSystem.getDeviceConnectionState(4, AppHibernateCst.INVALID_PKG) == 1) {
                        String curDTSState = AudioSystem.getParameters(HwAudioService.DTS_MODE_PARA);
                        if (curDTSState != null && curDTSState.contains(HwAudioService.DTS_MODE_ON_PARA)) {
                            AudioSystem.setParameters(HwAudioService.DTS_MODE_OFF_PARA);
                            SystemProperties.set(HwAudioService.DTS_MODE_PRESTATE, PreciseIgnore.COMP_SCREEN_ON_VALUE_);
                            if (HwAudioService.DEBUG) {
                                Slog.i(HwAudioService.TAG, "Cur DTS mode = " + curDTSState + " force set DTS off");
                                return;
                            }
                            return;
                        }
                        return;
                    }
                    return;
                case 2:
                    String preDTSState = SystemProperties.get(HwAudioService.DTS_MODE_PRESTATE, "unknown");
                    if (preDTSState != null && preDTSState.equals(PreciseIgnore.COMP_SCREEN_ON_VALUE_)) {
                        AudioSystem.setParameters(HwAudioService.DTS_MODE_ON_PARA);
                        SystemProperties.set(HwAudioService.DTS_MODE_PRESTATE, "unknown");
                        if (HwAudioService.DEBUG) {
                            Slog.i(HwAudioService.TAG, "set DTS on");
                            return;
                        }
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private static final int mSecurityVolumeIndex = SystemProperties.getInt("ro.config.hw.security_volume", 0);
    private static int mSwsScene = 0;
    private long dialogShowTime = 0;
    private boolean isShowingDialog = false;
    private AudioExceptionRecord mAudioExceptionRecord = null;
    private IBinder mBinder;
    private Callback mCallBack = new Callback() {
        public void onAvailabilityChanged(int var1) {
        }

        public void onDetected(EventPayload var1) {
            Slog.i(HwAudioService.TAG, "onDetected() called with: eventPayload = [" + var1 + "]");
            if (var1.getCaptureSession() != null) {
                HwAudioService.this.startWakeupService(var1.getCaptureSession().intValue());
                return;
            }
            Slog.e(HwAudioService.TAG, "session invalid!");
        }

        public void onError() {
            Slog.e(HwAudioService.TAG, "onError()");
        }

        public void onRecognitionPaused() {
        }

        public void onRecognitionResumed() {
        }
    };
    private ContentResolver mContentResolver;
    private Context mContext;
    DeathRecipient mDeathRecipient = new DeathRecipient() {
        public void binderDied() {
            Slog.e(HwAudioService.TAG, "binderDied");
            HwAudioService.this.startSoundTriggerV2(true);
        }
    };
    private int mDtsStatus = 0;
    private boolean mEnableAdjustVolume = true;
    private EnableVolumeClient mEnableVolumeClient = null;
    private final Object mEnableVolumeLock = new Object();
    private int mHeadsetSwitchState = 0;
    private HwThemeHandler mHwThemeHandler;
    private boolean mIsFmConnected = false;
    private boolean mIsLinkDeathRecipient = false;
    private Toast mLVMToast = null;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals("android.intent.action.FM")) {
                int state = intent.getIntExtra("state", 0);
                if (HwAudioService.DEBUG) {
                    Slog.i(HwAudioService.TAG, "Broadcast Receiver: Got ACTION_FMRx_PLUG, state =" + state);
                }
                boolean isConnected = HwAudioService.this.mIsFmConnected;
                if (state == 0 && isConnected) {
                    AudioSystem.setDeviceConnectionState(1048576, 0, AppHibernateCst.INVALID_PKG, AppHibernateCst.INVALID_PKG);
                    HwAudioService.this.mIsFmConnected = false;
                } else if (state == 1 && !isConnected) {
                    AudioSystem.setDeviceConnectionState(1048576, 1, AppHibernateCst.INVALID_PKG, AppHibernateCst.INVALID_PKG);
                    HwAudioService.this.mIsFmConnected = true;
                }
            } else if (action != null && action.equals("android.intent.action.RINGTONE_CHANGE")) {
                HwAudioService.this.mSysKeyEffectFile = intent.getStringExtra("KEYTOUCH_AUDIOEFFECT_PATH");
                if (HwAudioService.this.mSysKeyEffectFile != null) {
                    HwAudioService.this.mHwThemeHandler.sendMessage(HwAudioService.this.mHwThemeHandler.obtainMessage(99, 0, 0, null));
                }
            } else if (action != null && HwAudioService.ACTION_INCALL_SCREEN.equals(action)) {
                HwAudioService.isCallForeground = intent.getBooleanExtra(HwAudioService.ACTION_INCALL_EXTRA, true);
                if (HwAudioService.DEBUG) {
                    Slog.v(HwAudioService.TAG, "isCallForeground= " + HwAudioService.isCallForeground);
                }
            } else if (action != null && "android.intent.action.PHONE_STATE".equals(action)) {
                String state2 = intent.getStringExtra("state");
                if (HwAudioService.DEBUG) {
                    Slog.v(HwAudioService.TAG, "phone state=" + state2);
                }
                if (TelephonyManager.EXTRA_STATE_IDLE.equals(state2)) {
                    HwAudioService.this.force_exitLVMMode();
                }
            } else if (action != null && "android.intent.action.USER_UNLOCKED".equals(action)) {
                HwAudioService.this.onUserUnlocked(intent.getIntExtra("android.intent.extra.user_handle", -10000));
            } else if (action != null && "android.intent.action.USER_SWITCHED".equals(action)) {
                HwAudioService.this.onUserSwitched();
            }
        }
    };
    private Toast mRingerModeToast = null;
    private SoundTriggerDetector mSoundTriggerDetector;
    private String mSoundTriggerGram = null;
    private Handler mSoundTriggerHandler;
    private SoundTriggerManager mSoundTriggerManager;
    private String mSoundTriggerRes = null;
    private ISoundTriggerService mSoundTriggerService;
    private int mSoundTriggerStatus = 0;
    private int mSpkRcvStereoStatus = -1;
    private int mSwsStatus = 0;
    private String mSysKeyEffectFile;
    private UserManager mUserManager;
    private final UserManagerInternal mUserManagerInternal;
    private int oldInCallDevice = 0;
    private int oldLVMDevice = 0;

    private class EnableVolumeClient implements DeathRecipient {
        private String mCallerPkg;
        private IBinder mCb;

        public String getCallerPkg() {
            return this.mCallerPkg;
        }

        EnableVolumeClient(IBinder cb) {
            if (cb != null) {
                try {
                    cb.linkToDeath(this, 0);
                } catch (RemoteException e) {
                    Slog.w(HwAudioService.TAG, "EnableVolumeClient() could not link to " + cb + " binder death");
                    cb = null;
                }
            }
            this.mCb = cb;
            this.mCallerPkg = HwAudioService.this.getPackageNameByPid(Binder.getCallingPid());
        }

        public void binderDied() {
            synchronized (HwAudioService.this.mEnableVolumeLock) {
                Slog.i(HwAudioService.TAG, " EnableVolumeClient died.pkgname:" + this.mCallerPkg);
                HwAudioService.this.mEnableAdjustVolume = true;
                release();
            }
        }

        public void release() {
            if (this.mCb != null) {
                this.mCb.unlinkToDeath(this, 0);
                this.mCb = null;
            }
            HwAudioService.this.mEnableVolumeClient = null;
        }
    }

    private class HwThemeHandler extends Handler {
        private HwThemeHandler() {
        }

        public void handleMessage(Message msg) {
            if (99 == msg.what) {
                HwAudioService.this.reloadSoundEffects();
            }
        }
    }

    private static class IPGPClient implements IPGPlugCallbacks {
        private PGPlug mPGPlug = new PGPlug(this, HwAudioService.TAG);

        public IPGPClient() {
            new Thread(this.mPGPlug, HwAudioService.TAG).start();
        }

        public void onDaemonConnected() {
            Slog.i(HwAudioService.TAG, "HwAudioService:IPGPClient connected success!");
        }

        public boolean onEvent(int actionID, String value) {
            if (1 != PGAction.checkActionType(actionID)) {
                if (HwAudioService.DEBUG) {
                    Slog.i(HwAudioService.TAG, "HwAudioService:Filter application event id : " + actionID);
                }
                return true;
            }
            int subFlag = PGAction.checkActionFlag(actionID);
            if (HwAudioService.DEBUG) {
                Slog.i(HwAudioService.TAG, "HwAudioService:IPGP onEvent actionID=" + actionID + ", value=" + value + ",  subFlag=" + subFlag);
            }
            if (subFlag != 3) {
                if (HwAudioService.DEBUG) {
                    Slog.i(HwAudioService.TAG, "Not used non-parent scene , ignore it");
                }
                return true;
            }
            if (actionID == 10009) {
                if (HwAudioService.mSwsScene == 0) {
                    HwAudioService.mSwsScene = 1;
                    Slog.i(HwAudioService.TAG, "HwAudioService:Video_Front");
                    AudioSystem.setParameters("IPGPMode=video");
                }
            } else if (HwAudioService.mSwsScene != 0) {
                HwAudioService.mSwsScene = 0;
                Slog.i(HwAudioService.TAG, "HwAudioService:Video_Not_Front");
                AudioSystem.setParameters("IPGPMode=audio");
            }
            return true;
        }

        public void onConnectedTimeout() {
            Slog.e(HwAudioService.TAG, "HwAudioService:Client connect timeout!");
        }
    }

    protected boolean usingHwSafeMediaConfig() {
        return mIsHuaweiSafeMediaConfig;
    }

    protected int getHwSafeMediaVolumeIndex() {
        return mSecurityVolumeIndex * 10;
    }

    protected boolean isHwSafeMediaVolumeEnabled() {
        return mSecurityVolumeIndex > 0;
    }

    protected boolean checkMMIRunning() {
        return "true".equals(SystemProperties.get("runtime.mmitest.isrunning", "false"));
    }

    public HwAudioService(Context context) {
        super(context);
        Slog.i(TAG, TAG);
        this.mContext = context;
        IntentFilter intentFilter = new IntentFilter("android.intent.action.FM");
        if (intentFilter != null) {
            intentFilter.addAction("android.intent.action.RINGTONE_CHANGE");
            intentFilter.addAction(ACTION_INCALL_SCREEN);
            intentFilter.addAction("android.intent.action.PHONE_STATE");
            intentFilter.addAction("android.intent.action.USER_UNLOCKED");
            intentFilter.addAction("android.intent.action.USER_SWITCHED");
            context.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, intentFilter, null, null);
        }
        this.mContentResolver = this.mContext.getContentResolver();
        this.mSysKeyEffectFile = Systemex.getString(this.mContentResolver, SYSKEY_SOUND_HWT);
        this.mAudioExceptionRecord = new AudioExceptionRecord();
        readPersistedSettingsEx(this.mContentResolver);
        setAudioSystemParameters();
        HwServiceFactory.getHwDrmDialogService().startDrmDialogService(context);
        if (SWS_SOUND_EFFECTS_SUPPORT && mIsHuaweiSWS3Config) {
            Slog.i(TAG, "HwAudioService: Start SWS3.0 IPGPClient.");
            IPGPClient iPGPClient = new IPGPClient();
        }
        this.mUserManagerInternal = (UserManagerInternal) LocalServices.getService(UserManagerInternal.class);
    }

    public void initHwThemeHandler() {
        this.mHwThemeHandler = new HwThemeHandler();
    }

    public void reloadSoundEffects() {
        unloadSoundEffects();
        loadSoundEffects();
    }

    private void onUserUnlocked(int userId) {
        if (userId != -10000) {
            createManagers();
            boolean isPrimary = this.mUserManager.getUserInfo(userId).isPrimary();
            Slog.i(TAG, "user unlocked ,start soundtrigger! isPrimary : " + isPrimary);
            if (isPrimary) {
                createAndStartSoundTrigger(true);
            }
        }
    }

    private void onUserSwitched() {
        int userId = ActivityManager.getCurrentUser();
        if (userId != -10000) {
            createManagers();
            boolean isPrimary = this.mUserManager.getUserInfo(userId).isPrimary();
            Slog.i(TAG, "user switched ,isPrimary : " + isPrimary);
            if (isPrimary) {
                createAndStartSoundTrigger(true);
            } else {
                createAndStartSoundTrigger(false);
            }
        }
    }

    private boolean isSoundTriggerOn() {
        if (Secure.getInt(this.mContentResolver, "hw_soundtrigger_enabled", 0) == 1) {
            return true;
        }
        return false;
    }

    private void createAndStartSoundTrigger(final boolean start) {
        createSoundTriggerHandler();
        this.mSoundTriggerHandler.post(new Runnable() {
            public void run() {
                Slog.i(HwAudioService.TAG, "createAndStartSoundTrigger startsoundtrigger");
                HwAudioService.this.startSoundTriggerV2(start);
            }
        });
    }

    private boolean startSoundTriggerDetector() {
        createManagers();
        createSoundTriggerDetector();
        GenericSoundModel model = getCurrentModel();
        Slog.i(TAG, "startSoundTriggerDetector model : " + model);
        if (model == null || !this.mSoundTriggerDetector.startRecognition(1)) {
            Slog.e(TAG, "start recognition failed!");
            return false;
        }
        Slog.i(TAG, "start recognition successfully!");
        return true;
    }

    private boolean stopSoundTriggerDetector() {
        createManagers();
        createSoundTriggerDetector();
        GenericSoundModel model = getCurrentModel();
        Slog.i(TAG, "stopSoundTriggerDetector model : " + model);
        if (model == null || !this.mSoundTriggerDetector.stopRecognition()) {
            Slog.e(TAG, "stop recognition failed!");
            return false;
        }
        Slog.i(TAG, "stop recognition successfully!");
        return true;
    }

    private void createSoundTriggerHandler() {
        if (this.mSoundTriggerHandler == null) {
            this.mSoundTriggerHandler = new Handler(Looper.getMainLooper());
        }
    }

    private void createManagers() {
        if (this.mSoundTriggerManager == null) {
            this.mSoundTriggerManager = (SoundTriggerManager) this.mContext.getSystemService("soundtrigger");
            this.mUserManager = (UserManager) this.mContext.getSystemService("user");
            this.mSoundTriggerService = Stub.asInterface(ServiceManager.getService("soundtrigger"));
        }
    }

    private void createSoundTriggerDetector() {
        if (this.mSoundTriggerDetector == null) {
            createSoundTriggerHandler();
            this.mSoundTriggerDetector = this.mSoundTriggerManager.createSoundTriggerDetector(MODEL_UUID, this.mCallBack, this.mSoundTriggerHandler);
        }
    }

    private static boolean isSupportWakeUpV2() {
        String wakeupV2 = AudioSystem.getParameters("audio_capability=soundtrigger_version");
        Slog.i(TAG, "wakeupV2 : " + wakeupV2);
        if (AppHibernateCst.INVALID_PKG.equals(wakeupV2)) {
            return false;
        }
        return true;
    }

    private void startSoundTriggerV2(boolean start) {
        if (isSupportWakeUpV2()) {
            createManagers();
            GenericSoundModel model = getCurrentModel();
            boolean isUserUnlocked = this.mUserManager.isUserUnlocked(UserHandle.SYSTEM);
            boolean isSoundTriggerOn = isSoundTriggerOn();
            Slog.i(TAG, "startSoundTriggerV2 model : " + model + " isSoundTriggerOn : " + isSoundTriggerOn + " isUserUnlocked : " + isUserUnlocked + " start : " + start);
            if (isUserUnlocked && model != null && isSoundTriggerOn) {
                if (start) {
                    startSoundTriggerDetector();
                } else {
                    stopSoundTriggerDetector();
                }
            }
            return;
        }
        Slog.i(TAG, "startSoundTriggerV2 not support wakeup v2");
    }

    private GenericSoundModel getCurrentModel() {
        try {
            if (this.mSoundTriggerService != null) {
                return this.mSoundTriggerService.getSoundModel(new ParcelUuid(MODEL_UUID));
            }
            Slog.e(TAG, "getCurrentModel mSoundTriggerService is null!");
            return null;
        } catch (Exception e) {
            Slog.e(TAG, "getCurrentModel e : " + e);
        }
    }

    private void startWakeupService(int session) {
        Intent intent = new Intent(ACTION_WAKEUP_SERVICE);
        intent.setPackage(VASSISTANT_PACKAGE_NAME);
        intent.putExtra(SESSION_ID, session);
        this.mContext.startService(intent);
    }

    public AudioRoutesInfo startWatchingRoutes(IAudioRoutesObserver observer) {
        Slog.d(TAG, "startWatchingRoutes");
        if (linkVAtoDeathRe(observer)) {
            return null;
        }
        return super.startWatchingRoutes(observer);
    }

    private boolean linkVAtoDeathRe(IAudioRoutesObserver observer) {
        Slog.d(TAG, "linkVAtoDeathRe for vassistant");
        int uid = Binder.getCallingUid();
        if (this.mContext == null) {
            return false;
        }
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            return false;
        }
        String[] packages = pm.getPackagesForUid(uid);
        if (packages == null) {
            return false;
        }
        int i = 0;
        while (i < packages.length) {
            String packageName = packages[i];
            Slog.d(TAG, "packageName:" + packageName);
            if (VASSISTANT_PACKAGE_NAME.equals(packageName)) {
                this.mBinder = observer.asBinder();
                try {
                    this.mBinder.linkToDeath(this.mDeathRecipient, 0);
                    this.mIsLinkDeathRecipient = true;
                    return true;
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                i++;
            }
        }
        return false;
    }

    public boolean isFileReady(String filename) {
        return new File(filename).canRead();
    }

    public void unloadHwThemeSoundEffects() {
        this.mHwThemeHandler.removeMessages(99);
    }

    public int getSampleId(SoundPool soundpool, int effect, String defFilePath, int index) {
        int sampleId = 0;
        if (effect == 0) {
            String themeFilePath = this.mSysKeyEffectFile;
            if (themeFilePath == null) {
                themeFilePath = defFilePath;
            }
            if (themeFilePath == null) {
                return -1;
            }
            if (isFileReady(themeFilePath)) {
                sampleId = soundpool.load(themeFilePath, index);
                if (sampleId > 0) {
                    Systemex.putString(this.mContentResolver, SYSKEY_SOUND_HWT, themeFilePath);
                }
            }
        } else {
            sampleId = soundpool.load(defFilePath, index);
        }
        return sampleId;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        int _result;
        switch (code) {
            case 101:
                int event = data.readInt();
                if (DEBUG) {
                    Slog.v(TAG, "HwAudioService.onTransact: got event " + event);
                }
                int mForcedUseForMedia = event == 1 ? 1 : 0;
                sendMsg(this.mAudioHandler, 8, 2, 1, mForcedUseForMedia, null, 0);
                if (DEBUG) {
                    Slog.v(TAG, "setSpeakermediaOn " + mForcedUseForMedia);
                }
                reply.writeNoException();
                return true;
            case 102:
                reply.writeNoException();
                return true;
            case 1002:
                data.enforceInterface("android.media.IAudioService");
                _result = setSoundEffectState(data.readInt() != 0, data.readString(), data.readInt() != 0, data.readString());
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            case 1003:
                data.enforceInterface("android.media.IAudioService");
                sendAudioRecordStateChangedIntent(data.readString(), data.readInt(), data.readInt(), data.readString());
                reply.writeNoException();
                return true;
            case 1004:
                data.enforceInterface("android.media.IAudioService");
                if (!checkRecordActive(Binder.getCallingPid())) {
                    Slog.i(TAG, "AudioException record is not occpied.check mic mute.");
                    String recordPkg = getPackageNameByPid(Binder.getCallingPid());
                    printCtaifsLog(getRecordAppName(this.mContext, recordPkg), recordPkg, "onTransact", "本地录音");
                    checkMicMute();
                }
                reply.writeNoException();
                return true;
            case 1101:
                _result = isAdjustVolumeEnable() ? 1 : 0;
                Slog.i(TAG, "isAdjustVolumeEnable transaction called. result:" + _result);
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            case 1102:
                IBinder callerToken = data.readStrongBinder();
                boolean _arg0 = data.readInt() != 0;
                Slog.i(TAG, "enableVolumeAdjust  transaction called.enable:" + _arg0);
                enableVolumeAdjust(_arg0, Binder.getCallingUid(), callerToken);
                reply.writeNoException();
                return true;
            case 1103:
                boolean _result2;
                data.enforceInterface("android.media.IAudioService");
                if (data.readInt() != 0) {
                    _result2 = startSoundTriggerDetector();
                } else {
                    _result2 = stopSoundTriggerDetector();
                }
                reply.writeNoException();
                reply.writeInt(_result2 ? 1 : 0);
                if (this.mBinder != null && this.mIsLinkDeathRecipient) {
                    this.mBinder.unlinkToDeath(this.mDeathRecipient, 0);
                    this.mIsLinkDeathRecipient = false;
                }
                return true;
            default:
                return super.onTransact(code, data, reply, flags);
        }
    }

    public static void printCtaifsLog(String applicationName, String packageName, String callingMethod, String desciption) {
        Slog.i("ctaifs<" + applicationName + ">[" + applicationName + "][" + packageName + "]", "[" + callingMethod + "] " + desciption);
    }

    public static String getRecordAppName(Context context, String packageName) {
        if (context == null) {
            return null;
        }
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            return null;
        }
        ApplicationInfo appInfo = null;
        long ident = Binder.clearCallingIdentity();
        try {
            appInfo = pm.getApplicationInfoAsUser(packageName, 0, getCurrentUserId());
        } catch (NameNotFoundException e) {
            Slog.e(TAG, "App Name Not Found");
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
        if (appInfo == null) {
            return null;
        }
        CharSequence displayName = pm.getApplicationLabel(appInfo);
        if (TextUtils.isEmpty(displayName)) {
            return null;
        }
        return String.valueOf(displayName);
    }

    protected void appendExtraInfo(Intent intent) {
        if (DEBUG) {
            Slog.d(TAG, "appendExtraInfo mHeadsetSwitchState: " + this.mHeadsetSwitchState);
        }
        if (intent != null) {
            intent.putExtra("switch_state", this.mHeadsetSwitchState);
        }
    }

    protected void sendDeviceConnectionIntentForImcs(int device, int state, String name) {
        Intent intent = new Intent();
        intent.putExtra("state", state);
        intent.putExtra("name", name);
        if (DEBUG) {
            Slog.d(TAG, "sendDeviceConnectionIntentForImcs mHeadsetSwitchState: " + this.mHeadsetSwitchState);
        }
        intent.putExtra("switch_state", this.mHeadsetSwitchState);
        if (device == 4) {
            intent.setAction("imcs.action.HEADSET_PLUG");
            intent.putExtra("microphone", 1);
        } else if (device == 8) {
            intent.setAction("imcs.action.HEADSET_PLUG");
            intent.putExtra("microphone", 0);
        } else {
            return;
        }
        long ident = Binder.clearCallingIdentity();
        try {
            ActivityManagerNative.broadcastStickyIntent(intent, "imcs.permission.HEADSET_PLUG", -1);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private boolean isAdjustVolumeEnable() {
        Slog.i(TAG, "isAdjustVolumeEnable,mEnableAdjustVolume:" + this.mEnableAdjustVolume);
        return this.mEnableAdjustVolume;
    }

    private void enableVolumeAdjust(boolean enable, int callerUid, IBinder cb) {
        if (hasSystemPriv(callerUid)) {
            synchronized (this.mEnableVolumeLock) {
                String callerPkg = getPackageNameByPid(Binder.getCallingPid());
                if (this.mEnableVolumeClient == null) {
                    this.mEnableVolumeClient = new EnableVolumeClient(cb);
                    this.mEnableAdjustVolume = enable;
                } else {
                    String clientPkg = this.mEnableVolumeClient.getCallerPkg();
                    if (callerPkg.equals(clientPkg)) {
                        this.mEnableAdjustVolume = enable;
                    } else {
                        Slog.w(TAG, "Just allowed one caller use the interface until older caller die.older. caller:" + callerPkg + " old callser:" + clientPkg);
                    }
                }
            }
            return;
        }
        Slog.i(TAG, "caller is not system app.Can not set volumeAdjust to enable or disable.");
    }

    protected void readPersistedSettingsEx(ContentResolver cr) {
        if (SOUND_EFFECTS_SUPPORT) {
            getEffectsState(cr);
        }
        if (HW_SOUND_TRIGGER_SUPPORT) {
            getSoundTriggerSettings(cr);
        }
        if (SPK_RCV_STEREO_SUPPORT) {
            getSpkRcvStereoState(cr);
        }
    }

    protected void onErrorCallBackEx(int exceptionId) {
        Slog.i(TAG, "AudioException onErrorCallBackEx exceptionId:" + exceptionId);
        if (exceptionId <= -1000) {
            sendMsg(this.mAudioHandler, 10000, 1, exceptionId, 0, null, 0);
        }
    }

    protected void onScoExceptionOccur(int clientPid) {
        Slog.w(TAG, "AudioException ScoExceptionOccur,clientpid:" + clientPid + " have more than one sco connected!");
        sendMsg(this.mAudioHandler, 10000, 1, HW_SCO_CONNECT_EXCEPTION_OCCOUR, 0, new AudioExceptionMsg(HW_SCO_CONNECT_EXCEPTION_OCCOUR, getPackageNameByPid(clientPid)), 0);
    }

    public void setMicrophoneMute(boolean on, String callingPackage, int userId) {
        boolean firstState = AudioSystem.isMicrophoneMuted();
        super.setMicrophoneMute(on, callingPackage, userId);
        if (!firstState && AudioSystem.isMicrophoneMuted()) {
            this.mAudioExceptionRecord.updateMuteMsg(callingPackage);
        }
    }

    protected void handleMessageEx(Message msg) {
        AudioExceptionMsg audioExceptionMsg = null;
        switch (msg.what) {
            case MSG_RECORD_ACTIVE /*61*/:
                if (!this.isShowingDialog) {
                    showRecordWarnDialog(msg.arg1, msg.arg2);
                    break;
                } else {
                    Slog.i(TAG, "MSG_RECORD_ACTIVE should not show record warn dialog");
                    return;
                }
            case 71:
                AudioSystem.setParameters(DUALPA_RCV_DEALY_OFF);
                break;
            case MSG_RINGER_MODE_CHANGE /*80*/:
                if (msg.obj != null) {
                    AudioExceptionMsg tempMsg = msg.obj;
                    String caller = tempMsg.msgPackagename;
                    if (caller == null) {
                        caller = AppHibernateCst.INVALID_PKG;
                    }
                    int fromRingerMode = msg.arg1;
                    int ringerMode = msg.arg2;
                    if (isFirstTimeShow(caller)) {
                        Slog.i(TAG, "AudioException showRingerModeDialog");
                        showRingerModeDialog(caller, fromRingerMode, ringerMode);
                        savePkgNameToDB(caller);
                    } else {
                        Slog.i(TAG, "AudioException showRingerModeToast");
                        showRingerModeToast(caller, fromRingerMode, ringerMode);
                    }
                    onAudioException(HW_RING_MODE_TYPE_EXCEPTION_OCCOUR, tempMsg);
                    break;
                }
                Slog.e(TAG, "MSG_RINGER_MODE_CHANGE msg obj is null!");
                return;
            case 10000:
                int i = msg.arg1;
                if (msg.obj != null) {
                    audioExceptionMsg = (AudioExceptionMsg) msg.obj;
                }
                onAudioException(i, audioExceptionMsg);
                break;
            case 10001:
                if (LOUD_VOICE_MODE_SUPPORT) {
                    handleLVMModeChangeProcess(msg.arg1, msg.obj);
                    break;
                }
                break;
        }
    }

    public void onAudioException(int exceptionId, AudioExceptionMsg exceptionMsg) {
        if (!this.mSystemReady) {
            Slog.e(TAG, "AudioException,but system is not ready! ");
        }
        switch (exceptionId) {
            case HW_SCO_CONNECT_EXCEPTION_OCCOUR /*-2003*/:
                Slog.w(TAG, "AudioException HW_SCO_CONNECT_EXCEPTION_OCCOUR");
                sendAudioException(exceptionMsg);
                return;
            case HW_MIC_MUTE_EXCEPTION_OCCOUR /*-2002*/:
                if (!this.mUserManagerInternal.getUserRestriction(getCurrentUserId(), "no_unmute_microphone")) {
                    Slog.w(TAG, "AudioException HW_MIC_MUTE_EXCEPTION_OCCOUR");
                    sendAudioException(exceptionMsg);
                    AudioSystem.muteMicrophone(false);
                    return;
                }
                return;
            case HW_RING_MODE_TYPE_EXCEPTION_OCCOUR /*-2001*/:
                Slog.w(TAG, "AudioException HW_RING_MODE_TYPE_EXCEPTION_OCCOUR");
                sendAudioException(exceptionMsg);
                return;
            case HW_AUDIO_MODE_TYPE_EXCEPTION_OCCOUR /*-1001*/:
                Slog.w(TAG, "AudioException HW_AUDIO_MODE_TYPE_EXCEPTION_OCCOUR, setModeInt AudioSystem.MODE_NORMAL");
                int pid = -1;
                if (getMode() == 3) {
                    if (!this.mSetModeDeathHandlers.isEmpty()) {
                        SetModeDeathHandler hdlr = (SetModeDeathHandler) this.mSetModeDeathHandlers.get(0);
                        if (hdlr.getMode() == 3) {
                            pid = hdlr.getPid();
                        }
                    }
                    if (pid > 0) {
                        sendAudioException(new AudioExceptionMsg(HW_AUDIO_MODE_TYPE_EXCEPTION_OCCOUR, getPackageNameByPid(pid)));
                        return;
                    } else {
                        Slog.e(TAG, "AudioException top sedModeDeathHander's mode is not COMMUNICATION");
                        return;
                    }
                }
                Slog.e(TAG, "AudioException catched HW_AUDIO_MODE_TYPE_EXCEPTION_OCCOUR,but state is not MODE_IN_COMMUNICATION.");
                return;
            default:
                int tmp = -exceptionId;
                if ((-(tmp >> 21)) == HW_AUDIO_TRACK_OVERFLOW_TYPE_EXCEPTION_OCCOUR) {
                    int exceptionPid = tmp - 2101346304;
                    if (exceptionPid > 0) {
                        String packageName = getPackageNameByPid(exceptionPid);
                        if (packageName != null) {
                            Slog.e(TAG, "AudioTrack_Overflow packageName = " + packageName + " pid = " + exceptionPid);
                            sendAudioException(new AudioExceptionMsg(HW_AUDIO_TRACK_OVERFLOW_TYPE_EXCEPTION_OCCOUR, packageName));
                            return;
                        }
                        Slog.w(TAG, "AudioTrack_Overflow getPackageNameByPid failed");
                        return;
                    }
                    Slog.w(TAG, "AudioTrack_Overflow pid error");
                    return;
                }
                Slog.w(TAG, "No such AudioException exceptionId:" + exceptionId);
                return;
        }
    }

    private void sendAudioException(AudioExceptionMsg exceptionMsg) {
        if (exceptionMsg == null) {
            try {
                Slog.e(TAG, "exceptionMsg is null.");
            } catch (Exception e) {
                Slog.e(TAG, "sendAudioException error!");
                e.printStackTrace();
            }
        } else {
            EventStream eventAudioException = IMonitor.openEventStream(AudioConst.EID_AUDIOEXCEPTION);
            if (eventAudioException == null) {
                Slog.e(TAG, "eventAudioException is null.");
                return;
            }
            eventAudioException.setParam((short) 0, exceptionMsg.msgType).setParam((short) 1, exceptionMsg.msgPackagename == null ? AppHibernateCst.INVALID_PKG : exceptionMsg.msgPackagename).setParam((short) 2, AppHibernateCst.INVALID_PKG);
            IMonitor.sendEvent(eventAudioException);
            IMonitor.closeEventStream(eventAudioException);
            Slog.d(TAG, "IMonitor send AudioException success in " + exceptionMsg.msgType);
        }
    }

    protected void handleLVMModeChangeProcess(int state, Object object) {
        if (state == 1) {
            if (object != null) {
                DeviceVolumeState myObj = (DeviceVolumeState) object;
                setLVMMode(myObj.mDirection, myObj.mDevice, myObj.mOldIndex, myObj.mstreamType);
            }
        } else if (isInCall()) {
            int device = getInCallDevice();
            setLVMMode(0, device, -1, 0);
            setVoiceALGODeviceChange(device);
        }
    }

    private boolean isInCall() {
        if (getMode() != 2) {
            return false;
        }
        TelecomManager telecomManager = (TelecomManager) this.mContext.getSystemService("telecom");
        if (telecomManager == null) {
            return false;
        }
        return telecomManager.isInCall();
    }

    protected int getOldInCallDevice(int mode) {
        if (mode == 2) {
            this.oldInCallDevice = getInCallDevice();
        } else if (mode == 0) {
            this.oldInCallDevice = 0;
        }
        return this.oldInCallDevice;
    }

    private int getInCallDevice() {
        int activeStreamType;
        if (AudioSystem.getForceUse(0) == 3) {
            activeStreamType = 6;
        } else {
            activeStreamType = 0;
        }
        return hwGetDeviceForStream(activeStreamType);
    }

    private void setVoiceALGODeviceChange(int device) {
        if (this.oldInCallDevice != device) {
            AudioSystem.setParameters("VOICE_ALGO_DeviceChange=true");
            this.oldInCallDevice = device;
        }
    }

    private void force_exitLVMMode() {
        if ("true".equals(AudioSystem.getParameters("VOICE_LVM_Enable"))) {
            AudioSystem.setParameters("VOICE_LVM_Enable=false");
            this.oldLVMDevice = 0;
            if (DEBUG) {
                Slog.i(TAG, "force disable LVM");
            }
        }
    }

    protected void setLVMMode(int direction, int device, int oldIndex, int streamType) {
        if (getMode() == 2 && this.mContext.checkCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE") != 0) {
            Slog.e(TAG, "MODIFY_PHONE_STATE Permission Denial: setLVMMode");
        } else if (LOUD_VOICE_MODE_SUPPORT && isInCall() && streamType == 0) {
            boolean isMaxVolume;
            boolean isLVMMode = "true".equals(AudioSystem.getParameters("VOICE_LVM_Enable"));
            boolean isLVMDevice = 2 == device || 1 == device;
            if (getStreamMaxIndex(0) == oldIndex) {
                isMaxVolume = true;
            } else {
                isMaxVolume = false;
            }
            boolean isChangeVolume = false;
            if (isMaxVolume && isLVMDevice && this.oldLVMDevice == device) {
                isChangeVolume = getStreamIndex(0, device) < oldIndex;
            }
            if (DEBUG) {
                Slog.i(TAG, "direction:" + direction + " device:" + device + " oldIndex:" + oldIndex + " isLVMMode:" + isLVMMode + " isChangeVolume:" + isChangeVolume);
            }
            if (!isLVMMode && isMaxVolume && 1 == direction && isLVMDevice) {
                AudioSystem.setParameters("VOICE_LVM_Enable=true");
                showLVMToast(33685769);
                this.oldLVMDevice = device;
                if (DEBUG) {
                    Slog.i(TAG, "enable LVM after  = " + this.oldLVMDevice);
                }
            } else if (isLVMMode) {
                if (-1 != direction && device == this.oldLVMDevice) {
                    if (isChangeVolume) {
                    }
                }
                AudioSystem.setParameters("VOICE_LVM_Enable=false");
                showLVMToast(33685770);
                this.oldLVMDevice = 0;
                if (DEBUG) {
                    Slog.i(TAG, "disable LVM after oldLVMDevice= " + this.oldLVMDevice);
                }
            }
        } else {
            Slog.e(TAG, "setLVMMode abort");
        }
    }

    private void showLVMToast(int message) {
        if (this.mLVMToast == null) {
            this.mLVMToast = Toast.makeText(new ContextThemeWrapper(this.mContext, this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Toast", null, null)), "Unknown State", 0);
            LayoutParams windowParams = this.mLVMToast.getWindowParams();
            windowParams.privateFlags |= 16;
        }
        try {
            if (DEBUG) {
                Slog.i(TAG, "showLVMToast ");
            }
            this.mLVMToast.setText(message);
            this.mLVMToast.show();
        } catch (Exception e) {
            Slog.e(TAG, "showLVMToast exception: " + e);
        }
    }

    protected void getSoundTriggerSettings(ContentResolver cr) {
        String KEY_SOUNDTRIGGER_TYPE = "hw_soundtrigger_type";
        this.mSoundTriggerStatus = Secure.getInt(cr, "hw_soundtrigger_enabled", 0);
        this.mSoundTriggerRes = Secure.getString(cr, "hw_soundtrigger_resource");
        this.mSoundTriggerGram = Secure.getString(cr, "hw_soundtrigger_grammar");
        if (DEBUG) {
            Slog.i(TAG, "mSoundTriggerStatus = " + this.mSoundTriggerStatus + " mSoundTriggerRes = " + this.mSoundTriggerRes + " mSoundTriggerGram = " + this.mSoundTriggerGram);
        }
    }

    private void setSoundTrigger() {
        if (this.mSoundTriggerStatus == 1) {
            AudioSystem.setParameters(SOUNDTRIGGER_MAD_ON);
            AudioSystem.setParameters(SOUNDTRIGGER_WAKUP_ON);
            Slog.i(TAG, "setSoundTrigger = on");
        } else if (this.mSoundTriggerStatus == 0) {
            AudioSystem.setParameters(SOUNDTRIGGER_MAD_OFF);
            AudioSystem.setParameters(SOUNDTRIGGER_WAKUP_OFF);
            Slog.i(TAG, "setSoundTrigger = off");
            return;
        }
        if (this.mSoundTriggerRes != null && !this.mSoundTriggerRes.isEmpty() && this.mSoundTriggerGram != null && !this.mSoundTriggerGram.isEmpty()) {
            AudioSystem.setParameters("mad=tslice_model=" + this.mSoundTriggerRes);
            AudioSystem.setParameters("mad=tslice_gram=" + this.mSoundTriggerGram);
        }
    }

    private boolean isInCallingState() {
        TelephonyManager telephonyManager = null;
        if (this.mContext != null) {
            telephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        }
        if (telephonyManager == null) {
            return false;
        }
        if (telephonyManager.getCallState() != 0) {
            if (DEBUG) {
                Slog.v(TAG, "phone is in call state");
            }
            return true;
        }
        if (DEBUG) {
            Slog.v(TAG, "phone is NOT in call state");
        }
        return false;
    }

    private boolean hasSystemPriv(int uid) {
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            return false;
        }
        for (String pkg : pm.getPackagesForUid(uid)) {
            try {
                ApplicationInfo info = pm.getApplicationInfo(pkg, 0);
                if (!(info == null || (info.flags & 1) == 0)) {
                    Slog.i(TAG, "system app " + pkg);
                    return true;
                }
            } catch (Exception e) {
                Slog.i(TAG, "not found app " + pkg);
            }
        }
        return false;
    }

    protected boolean checkAudioSettingAllowed(String msg) {
        if (isInCallingState()) {
            int uid = Binder.getCallingUid();
            if (1001 == uid || 1000 == uid || hasSystemPriv(uid)) {
                if (DEBUG) {
                    Slog.v(TAG, "Audio Settings ALLOW from func=" + msg + ", from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                }
                return true;
            }
            if (DEBUG) {
                Slog.v(TAG, "Audio Settings NOT allow from func=" + msg + ", from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            }
            return false;
        }
        Slog.w(TAG, "Audio Settings ALLOW from func=" + msg + ", from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
        return true;
    }

    protected String getPackageNameByPid(int pid) {
        if (pid <= 0) {
            return null;
        }
        ActivityManager activityManager = (ActivityManager) this.mContext.getSystemService("activity");
        if (activityManager == null) {
            return null;
        }
        List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return null;
        }
        String packageName = null;
        for (RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.pid == pid) {
                packageName = appProcess.processName;
                break;
            }
        }
        return packageName;
    }

    public void sendAudioRecordStateChangedIntent(String sender, int state, int pid, String packageName) {
        if (!HW_KARAOKE_EFFECT_ENABLED) {
            return;
        }
        if (this.mContext.checkCallingPermission("android.permission.RECORD_AUDIO") == -1) {
            Slog.e(TAG, "sendAudioRecordStateChangedIntent dennied from " + packageName);
            return;
        }
        if (DEBUG) {
            Slog.i(TAG, "sendAudioRecordStateChangedIntent=" + sender + " " + state + " " + pid);
        }
        Intent intent = new Intent("android.media.AUDIO_RECORD_STATE_CHANGED_ACTION");
        intent.putExtra("sender", sender);
        intent.putExtra("state", state);
        intent.putExtra("packagename", getPackageNameByPid(pid));
        long ident = Binder.clearCallingIdentity();
        try {
            ActivityManagerNative.getDefault().broadcastIntent(null, intent, null, null, -1, null, null, null, -1, null, false, false, -1);
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private String getPackageNameByPidEx(int pid) {
        String str = null;
        try {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("android.app.IActivityManager");
            data.writeInt(pid);
            ActivityManagerNative.getDefault().asBinder().transact(504, data, reply, 0);
            reply.readException();
            str = reply.readString();
            data.recycle();
            reply.recycle();
            return str;
        } catch (Exception e) {
            Slog.e(TAG, "getPackageNameForPid " + pid, e);
            return str;
        }
    }

    private boolean checkRecordActive(int requestRecordPid) {
        if (TextUtils.isEmpty(AudioSystem.getParameters("active_record_pid"))) {
            return false;
        }
        boolean isRecordOccupied;
        int activeRecordPid = Integer.parseInt(AudioSystem.getParameters("active_record_pid"));
        boolean isSourceActive = !AudioSystem.isSourceActive(1999) ? AudioSystem.isSourceActive(8) : true;
        Slog.i(TAG, "AudioException checkRecordActive requestRecordPid = " + requestRecordPid + ", activeRecordPid = " + activeRecordPid);
        if (activeRecordPid == -1 || requestRecordPid == activeRecordPid || isSourceActive) {
            isRecordOccupied = false;
        } else {
            isRecordOccupied = true;
        }
        String requestPkgName = getPackageNameByPid(requestRecordPid);
        if (isRecordOccupied) {
            if (isInRequestAppList(requestPkgName) || isInActiveAppList(getPackageNameByPidEx(activeRecordPid))) {
                return isRecordOccupied;
            }
            sendMsg(this.mAudioHandler, MSG_RECORD_ACTIVE, 2, requestRecordPid, activeRecordPid, null, 0);
        } else if (!(requestPkgName == null || requestPkgName.equals(AppHibernateCst.INVALID_PKG))) {
            AudioSystem.setParameters("RecordCallingAppName=" + requestPkgName);
        }
        return isRecordOccupied;
    }

    private void checkMicMute() {
        if (AudioSystem.isMicrophoneMuted()) {
            Slog.i(TAG, "AudioException mic is muted when record! set it not mute!");
            sendMsg(this.mAudioHandler, 10000, 1, HW_MIC_MUTE_EXCEPTION_OCCOUR, 0, new AudioExceptionMsg(HW_MIC_MUTE_EXCEPTION_OCCOUR, this.mAudioExceptionRecord.mLastMutePackageName), HwActivityManagerService.SERVICE_ADJ);
        }
    }

    private boolean isInRequestAppList(String pkgName) {
        int i = 0;
        while (i < RECORD_REQUEST_APP_LIST.length) {
            if (pkgName != null && pkgName.contains(RECORD_REQUEST_APP_LIST[i])) {
                return true;
            }
            i++;
        }
        return false;
    }

    private boolean isInActiveAppList(String pkgName) {
        for (String equals : RECORD_ACTIVE_APP_LIST) {
            if (equals.equals(pkgName)) {
                Slog.i(TAG, "isInActiveAppList " + pkgName);
                return true;
            }
        }
        return false;
    }

    private String getApplicationLabel(String pkgName) {
        String label = null;
        try {
            PackageManager packageManager = this.mContext.getPackageManager();
            label = packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkgName, 0)).toString();
        } catch (NameNotFoundException e) {
            Slog.e(TAG, "getApplicationLabel exception", e);
        }
        return label;
    }

    private void showRecordWarnDialog(int requestRecordPid, int activeRecordPid) {
        String requestPkgName = getPackageNameByPid(requestRecordPid);
        String requestPkgLabel = getApplicationLabel(requestPkgName);
        String activePkgName = getPackageNameByPid(activeRecordPid);
        String activePkgLabel = getApplicationLabel(activePkgName);
        if (activePkgName == null) {
            activePkgName = this.mContext.getString(17040608);
        }
        Builder builder = new Builder(this.mContext, this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null));
        String string = this.mContext.getString(33685811);
        Object[] objArr = new Object[2];
        if (activePkgLabel == null) {
            activePkgLabel = activePkgName;
        }
        objArr[0] = activePkgLabel;
        if (requestPkgLabel == null) {
            requestPkgLabel = requestPkgName;
        }
        objArr[1] = requestPkgLabel;
        builder.setMessage(String.format(string, objArr));
        builder.setCancelable(false);
        builder.setPositiveButton(33685810, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
                HwAudioService.this.isShowingDialog = false;
            }
        });
        Dialog dialog = builder.create();
        dialog.getWindow().setType(2003);
        if (SystemClock.elapsedRealtime() - this.dialogShowTime > THREE_SEC_LIMITED || SystemClock.elapsedRealtime() - this.dialogShowTime < 0) {
            dialog.show();
            this.dialogShowTime = SystemClock.elapsedRealtime();
            this.isShowingDialog = true;
        }
    }

    protected void getEffectsState(ContentResolver contentResolver) {
        if (DTS_SOUND_EFFECTS_SUPPORT) {
            getDtsstatus(contentResolver);
        }
        if (SWS_SOUND_EFFECTS_SUPPORT) {
            getSwsstatus(contentResolver);
        }
    }

    private void setEffectsState() {
        if (DTS_SOUND_EFFECTS_SUPPORT) {
            setDtsStatus();
        }
        if (SWS_SOUND_EFFECTS_SUPPORT) {
            setSwsStatus();
        }
    }

    private void getDtsstatus(ContentResolver contentResolver) {
        this.mDtsStatus = Secure.getInt(contentResolver, "dts_mode", 0);
    }

    private void getSwsstatus(ContentResolver contentResolver) {
        this.mSwsStatus = System.getInt(contentResolver, "sws_mode", 0);
    }

    private void setDtsStatus() {
        if (this.mDtsStatus == 3) {
            AudioSystem.setParameters(DTS_MODE_ON_PARA);
            Slog.i(TAG, "setDtsStatus = on");
        } else if (this.mDtsStatus == 0) {
            AudioSystem.setParameters(DTS_MODE_OFF_PARA);
            Slog.i(TAG, "setDtsStatus = off");
        }
    }

    private void setSwsStatus() {
        if (this.mSwsStatus == 3) {
            AudioSystem.setParameters(SWS_MODE_ON_PARA);
        } else if (this.mSwsStatus == 0) {
            AudioSystem.setParameters(SWS_MODE_OFF_PARA);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void onSetSoundEffectState(int device, int state) {
        if (checkAudioSettingsPermission("onSetSoundEffectState()") && !isScreenOff() && !isKeyguardLocked() && DTS_SOUND_EFFECTS_SUPPORT) {
            onSetDtsState(device, state);
        }
    }

    protected boolean checkEnbaleVolumeAdjust() {
        return isAdjustVolumeEnable();
    }

    private boolean isTopActivity(String[] appnames) {
        try {
            List<RunningTaskInfo> tasks = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(1);
            if (tasks == null || tasks.isEmpty()) {
                return false;
            }
            ComponentName topActivity = ((RunningTaskInfo) tasks.get(0)).topActivity;
            if (topActivity == null || topActivity.getPackageName() == null) {
                return false;
            }
            if (DEBUG) {
                Slog.i(TAG, "isTOPActivity topActivity.getPackageName()" + topActivity.getPackageName());
            }
            for (String equals : appnames) {
                if (equals.equals(topActivity.getPackageName())) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Slog.e(TAG, " Failure to get topActivity PackageName " + e);
        }
    }

    private void onSetDtsState(int device, int state) {
        if ((device == 4 || device == 8) && isTopActivity(FORBIDDEN_DTS_WHITE_LIST)) {
            String preDTSState = SystemProperties.get(DTS_MODE_PRESTATE, "unknown");
            if (state == 1) {
                String curDTSState = AudioSystem.getParameters(DTS_MODE_PARA);
                if (curDTSState != null && curDTSState.contains(DTS_MODE_ON_PARA)) {
                    AudioSystem.setParameters(DTS_MODE_OFF_PARA);
                    SystemProperties.set(DTS_MODE_PRESTATE, PreciseIgnore.COMP_SCREEN_ON_VALUE_);
                    if (DEBUG) {
                        Slog.i(TAG, "onSetDTSState cur DTS mode = " + curDTSState + " force set DTS off");
                    }
                }
            } else if (state == 0 && preDTSState != null && preDTSState.equals(PreciseIgnore.COMP_SCREEN_ON_VALUE_)) {
                AudioSystem.setParameters(DTS_MODE_ON_PARA);
                SystemProperties.set(DTS_MODE_PRESTATE, "unknown");
                if (DEBUG) {
                    Slog.i(TAG, "onSetDTSState set DTS on");
                }
            }
        }
    }

    public int setSoundEffectState(boolean restore, String packageName, boolean isOnTop, String reserved) {
        if (!DTS_SOUND_EFFECTS_SUPPORT || !checkAudioSettingsPermission("setSoundEffectState()") || isScreenOff() || isKeyguardLocked()) {
            return -1;
        }
        if (DEBUG) {
            Slog.i(TAG, "restore:" + restore + " packageName:" + packageName + " Top:" + isOnTop);
        }
        if (DTS_SOUND_EFFECTS_SUPPORT) {
            if (restore) {
                restoreDTSAndHSState(packageName);
            } else {
                onSetDTSAndHSState(packageName, isOnTop);
            }
        }
        return 0;
    }

    static void onSetDTSAndHSState(String packageName, boolean isOnTOP) {
        for (String equals : FORBIDDEN_DTS_WHITE_LIST) {
            if (equals.equals(packageName)) {
                AudioSystem.setParameters(isOnTOP ? HS_NO_CHARGE_ON : HS_NO_CHARGE_OFF);
                mMyHandler.sendEmptyMessage(isOnTOP ? 1 : 2);
                if (DEBUG) {
                    Slog.i(TAG, "onSetDTSAndHSState message: " + (isOnTOP ? "HS_NO_CHARGE_ON + DTS_CLOSE" : "HS_NO_CHARGE_OFF + DTS_OPEN"));
                }
                return;
            }
        }
    }

    static void restoreDTSAndHSState(String processName) {
        String preDTSState = SystemProperties.get(DTS_MODE_PRESTATE, "unknown");
        for (String equals : FORBIDDEN_DTS_WHITE_LIST) {
            if (equals.equals(processName)) {
                AudioSystem.setParameters(HS_NO_CHARGE_OFF);
                if (preDTSState != null && preDTSState.equals(PreciseIgnore.COMP_SCREEN_ON_VALUE_)) {
                    AudioSystem.setParameters(DTS_MODE_ON_PARA);
                    SystemProperties.set(DTS_MODE_PRESTATE, "unknown");
                    if (DEBUG) {
                        Slog.i(TAG, "restoreDTSAndHSState success!");
                    }
                }
                return;
            }
        }
    }

    protected void sendCommForceBroadcast() {
        if (getMode() == 2) {
            int uid = Binder.getCallingUid();
            if (1000 == uid || "com.android.bluetooth".equals(getPackageNameByPid(Binder.getCallingPid()))) {
                try {
                    this.mContext.sendBroadcastAsUser(new Intent(ACTION_COMMFORCE), UserHandle.getUserHandleForUid(UserHandle.getCallingUserId()), PERMISSION_COMMFORCE);
                } catch (Exception e) {
                    Slog.e(TAG, "sendCommForceBroadcast exception: " + e);
                }
                return;
            }
            if (DEBUG) {
                Slog.v(TAG, "Ignore sendCommForceBroadcast from uid = " + uid);
            }
        }
    }

    protected void checkMuteRcvDelay(int curMode, int mode) {
        if (mode == 0 && (curMode == 2 || curMode == 3)) {
            AudioSystem.setParameters(DUALPA_RCV_DEALY_ON);
            sendMsg(this.mAudioHandler, 71, 0, 0, 0, null, DUALPA_RCV_DEALY_DURATION);
        } else if (curMode != mode && (mode == 1 || mode == 2 || mode == 3)) {
            AudioSystem.setParameters(DUALPA_RCV_DEALY_OFF);
        } else if (DEBUG) {
            Slog.i(TAG, "checkMuteRcvDelay Do Nothing");
        }
    }

    private void setAudioSystemParameters() {
        if (HW_SOUND_TRIGGER_SUPPORT) {
            setSoundTrigger();
        }
        if (SOUND_EFFECTS_SUPPORT) {
            setEffectsState();
        }
        if (SPK_RCV_STEREO_SUPPORT) {
            setSpkRcvStereoStatus();
        }
    }

    protected void processMediaServerRestart() {
        readPersistedSettingsEx(this.mContentResolver);
        setAudioSystemParameters();
        Slog.i(TAG, "mediaserver restart ,start soundtrigger!");
        createAndStartSoundTrigger(true);
    }

    private void getSpkRcvStereoState(ContentResolver contentResolver) {
        this.mSpkRcvStereoStatus = Secure.getInt(contentResolver, "stereo_landscape_portrait", 1);
    }

    private void setSpkRcvStereoStatus() {
        if (this.mSpkRcvStereoStatus == 1) {
            AudioSystem.setParameters(SPK_RCV_STEREO_ON_PARA);
            AudioSystem.setParameters("rotation=0");
        } else if (this.mSpkRcvStereoStatus == 0) {
            AudioSystem.setParameters(SPK_RCV_STEREO_OFF_PARA);
        } else {
            Slog.e(TAG, "setSpkRcvStereoStatus Fail " + this.mSpkRcvStereoStatus);
        }
    }

    public void setRingerModeExternal(int toRingerMode, String caller) {
        boolean shouldControll = !isSystemApp(caller);
        if (shouldControll) {
            Slog.i(TAG, "AudioException setRingerModeExternal shouldReport=" + checkShouldAbortRingerModeChange(caller) + " uid:" + Binder.getCallingUid() + " caller:" + caller);
        }
        int fromRingerMode = getRingerModeExternal();
        super.setRingerModeExternal(toRingerMode, caller);
        if (fromRingerMode == getRingerModeInternal()) {
            Slog.d(TAG, "AudioException setRingerModeExternal ,but not change");
            return;
        }
        if (shouldControll) {
            alertUserRingerModeChange(caller, fromRingerMode, toRingerMode);
        }
    }

    private void alertUserRingerModeChange(String caller, int fromRingerMode, int ringerMode) {
        sendMsg(this.mAudioHandler, MSG_RINGER_MODE_CHANGE, 2, fromRingerMode, ringerMode, new AudioExceptionMsg(HW_RING_MODE_TYPE_EXCEPTION_OCCOUR, caller), 0);
    }

    private boolean isFirstTimeShow(String caller) {
        boolean z = true;
        String pkgs = Secure.getStringForUser(this.mContentResolver, "change_ringer_mode_pkgs", -2);
        if (TextUtils.isEmpty(pkgs)) {
            return true;
        }
        if (pkgs.contains(caller)) {
            z = false;
        }
        return z;
    }

    private void savePkgNameToDB(String caller) {
        if (!TextUtils.isEmpty(caller)) {
            String pkgs = Secure.getStringForUser(this.mContentResolver, "change_ringer_mode_pkgs", -2);
            if (TextUtils.isEmpty(pkgs)) {
                pkgs = caller + "\\";
            } else {
                pkgs = pkgs + caller + "\\";
            }
            Secure.putStringForUser(this.mContentResolver, "change_ringer_mode_pkgs", pkgs, -2);
        }
    }

    private boolean checkShouldAbortRingerModeChange(String caller) {
        return isScreenOff() || isKeyguardLocked() || !isCallerShowing(caller);
    }

    private boolean isKeyguardLocked() {
        KeyguardManager keyguard = (KeyguardManager) this.mContext.getSystemService("keyguard");
        if (keyguard != null) {
            return keyguard.isKeyguardLocked();
        }
        return false;
    }

    private boolean isCallerShowing(String caller) {
        String pkgName = getTopApp();
        if (pkgName != null) {
            return pkgName.startsWith(caller);
        }
        Slog.e(TAG, "AudioException getTopApp ,but pkgname is null.");
        return false;
    }

    private String getTopApp() {
        return ((ActivityManagerService) ServiceManager.getService("activity")).topAppName();
    }

    private boolean isScreenOff() {
        boolean z = false;
        PowerManager power = (PowerManager) this.mContext.getSystemService("power");
        if (power == null) {
            return false;
        }
        if (!power.isScreenOn()) {
            z = true;
        }
        return z;
    }

    private boolean isSystemApp(String caller) {
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            return false;
        }
        try {
            ApplicationInfo info = pm.getApplicationInfo(caller, 0);
            if (!(info == null || (info.flags & 1) == 0)) {
                return true;
            }
        } catch (Exception e) {
            Slog.i(TAG, "AudioException not found app:" + caller);
        }
        return false;
    }

    private void showRingerModeToast(String caller, int fromRingerMode, int toRingerMode) {
        int fromResId = getRingerModeStrResId(fromRingerMode);
        int toResId = getRingerModeStrResId(toRingerMode);
        if (fromResId <= 0 || toResId <= 0) {
            Slog.e(TAG, "AudioException showRingerModeToast resid not found ");
            return;
        }
        String callerAppName = getApplicationLabel(caller);
        if (this.mRingerModeToast == null) {
            this.mRingerModeToast = Toast.makeText(new ContextThemeWrapper(this.mContext, this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Toast", null, null)), "Unknown State", 0);
        }
        try {
            String messageStr = this.mContext.getString(33685812, new Object[]{callerAppName, this.mContext.getString(fromResId), this.mContext.getString(toResId)});
            Slog.i(TAG, "AudioException showRingerModeToast ");
            this.mRingerModeToast.setText(messageStr);
            this.mRingerModeToast.show();
        } catch (Exception e) {
            Slog.e(TAG, "AudioException showRingerModeToast exception: " + e);
        }
    }

    private void showRingerModeDialog(String caller, int fromRingerMode, int toRingerMode) {
        int fromResId = getRingerModeStrResId(fromRingerMode);
        int toResId = getRingerModeStrResId(toRingerMode);
        if (fromResId <= 0 || toResId <= 0) {
            Slog.e(TAG, "AudioException showRingerModeDialog resid not found ");
            return;
        }
        String callerAppName = getApplicationLabel(caller);
        String messageStr = this.mContext.getString(33685812, new Object[]{callerAppName, this.mContext.getString(fromResId), this.mContext.getString(toResId)});
        Builder builder = new Builder(this.mContext, this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null));
        builder.setMessage(messageStr);
        builder.setCancelable(false);
        builder.setPositiveButton(33685814, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        Dialog dialog = builder.create();
        dialog.getWindow().setType(2003);
        dialog.show();
        Slog.i(TAG, "AudioException showRingerModeDialog show ");
        this.isShowingDialog = true;
    }

    private int getRingerModeStrResId(int RingerMode) {
        switch (RingerMode) {
            case 0:
                return 33685816;
            case 1:
                return 33685813;
            case 2:
                return 33685815;
            default:
                Slog.e(TAG, "getRingerModeStrResId RingerMode is error.RingerMode =" + RingerMode);
                return 0;
        }
    }

    private static int getCurrentUserId() {
        int i;
        long ident = Binder.clearCallingIdentity();
        try {
            i = ActivityManagerNative.getDefault().getCurrentUser().id;
            return i;
        } catch (RemoteException e) {
            i = TAG;
            Slog.w(i, "Activity manager not running, nothing we can do assume user 0.");
            return 0;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        super.dump(fd, pw, args);
        this.mContext.enforceCallingOrSelfPermission("android.permission.DUMP", TAG);
        pw.print("HwAudioService:");
        pw.print("DEBUG =");
        pw.println(DEBUG);
        pw.print("mIsHuaweiSafeMediaConfig =");
        pw.println(mIsHuaweiSafeMediaConfig);
        pw.print("mSecurityVolumeIndex =");
        pw.println(mSecurityVolumeIndex);
        pw.print("LOUD_VOICE_MODE_SUPPORT=");
        pw.println(LOUD_VOICE_MODE_SUPPORT);
        pw.print("Lound Voice State:");
        pw.println(AudioSystem.getParameters("VOICE_LVM_Enable"));
        pw.print("HW_SOUND_TRIGGER_SUPPORT=");
        pw.println(HW_SOUND_TRIGGER_SUPPORT);
        pw.print("mSoundTriggerStatus=");
        pw.println(this.mSoundTriggerStatus);
        pw.print("mad=on:");
        pw.println(AudioSystem.getParameters(SOUNDTRIGGER_MAD_ON));
        pw.print("wakeup=on:");
        pw.println(AudioSystem.getParameters(SOUNDTRIGGER_WAKUP_ON));
        pw.print("HW_KARAOKE_EFFECT_ENABLED=");
        pw.println(HW_KARAOKE_EFFECT_ENABLED);
        pw.print("DTS_SOUND_EFFECTS_SUPPORT=");
        pw.println(DTS_SOUND_EFFECTS_SUPPORT);
        pw.print("mDtsStatus=");
        pw.println(this.mDtsStatus);
        pw.print("Dts State:");
        pw.println(AudioSystem.getParameters(DTS_MODE_PARA));
        pw.print("DTS_MODE_PRESTATE:");
        pw.println(SystemProperties.get(DTS_MODE_PRESTATE, "unknown"));
        pw.print("HS_NO_CHARGE_ON:");
        pw.println(AudioSystem.getParameters(HS_NO_CHARGE_ON));
        pw.print("isCallForeground=");
        pw.println(isCallForeground);
        pw.print("DUAL_SMARTPA_SUPPORT=");
        pw.println(DUAL_SMARTPA_SUPPORT);
        pw.print("SPK_RCV_STEREO_SUPPORT=");
        pw.println(SPK_RCV_STEREO_SUPPORT);
        pw.print("mSpkRcvStereoStatus=");
        pw.println(this.mSpkRcvStereoStatus);
    }
}
