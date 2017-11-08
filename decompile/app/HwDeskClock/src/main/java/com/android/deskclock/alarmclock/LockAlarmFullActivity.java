package com.android.deskclock.alarmclock;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.EGLConfigChooser;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.IWindowManager.Stub;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.alarmclock.WorldAnalogClock;
import com.android.connection.AlarmState;
import com.android.connection.WearUtils;
import com.android.deskclock.AlarmAlertWakeLock;
import com.android.deskclock.AlarmReceiver;
import com.android.deskclock.DeskClockApplication;
import com.android.deskclock.DigitalClock;
import com.android.deskclock.R;
import com.android.deskclock.RingCache;
import com.android.deskclock.alarmclock.CoverView.KeyEventListener;
import com.android.deskclock.alarmclock.ParticleRenderer.DrawStates;
import com.android.deskclock.smartcover.HwCustCoverAdapter;
import com.android.deskclock.smartcover.HwCustSmartCoverManager;
import com.android.util.ClockReporter;
import com.android.util.Config;
import com.android.util.HwLog;
import com.android.util.Log;
import com.android.util.Utils;
import com.huawei.cust.HwCustUtils;
import huawei.android.os.HwGeneralManager;
import java.util.Locale;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import ucd.RythmSurfaceView;

public class LockAlarmFullActivity extends Activity {
    public static final String BBQ_SIZE = "_1440x2560";
    private static final int BIGMODEL = 280;
    private static final String COVER_SIZE = SystemProperties.get("ro.config.small_cover_size", "");
    public static final String COVER_STATE = "coverOpen";
    public static final String COVER_STATE_CHANGED_ACTION = "com.huawei.android.cover.STATE";
    private static final String HANDLE_ALARM = "handle";
    private static final int KILL_PRIORITY = 999;
    public static final int MESSAGE_SNOOZE = 801;
    protected static final String SCREEN_OFF = "screen_off";
    public static final String SETTINGS_COVER_TYPE = "cover_type";
    private static final int STATE_FALSE = 0;
    private static final int STATE_INVALIDE = -1;
    private static final int STATE_TRUE = 1;
    public static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
    public static final String SYSTEM_DIALOG_REASON_KEY = "reason";
    public static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
    private static final String TAG = "LockAlarmFullActivity";
    public static final String VKY_SIZE = "_540x2560";
    public static final String VTR_SIZE = "_401x1920";
    public static final String WAS_SIZE = "_747x1920";
    private static boolean mIsCurverScreen = false;
    private static boolean mIsServiceOn = false;
    private static int sIsCurveScreen = STATE_INVALIDE;
    private GLSurfaceView gView;
    boolean isVisibleCoverWin;
    private int mAlamCount = 1;
    protected Alarm mAlarm;
    private AudioManager mAudioManager;
    private boolean mBeCancel;
    private ControlAlarm mControlAlarm;
    private CoverItemController mController;
    private HwCustCoverAdapter mCover = ((HwCustCoverAdapter) HwCustUtils.createObj(HwCustCoverAdapter.class, new Object[0]));
    private boolean mCoverClockAdded;
    private BallFrameView mCoverCloseSlider;
    private CoverFrameView mCoverCloseSliderWithoutBall;
    private TextView mCoverLabel;
    private CoverView mCoverScreen;
    private RelativeLayout mCoverSnoozeLayout;
    private TextView mCoverSnoozeMin;
    private TextView mCoverSnoozePause;
    private TextView mCoverSnoozeTip;
    private RelativeLayout mCoverView;
    private boolean mFinishRegister;
    private BroadcastReceiver mFisnishReceiver;
    private BallFrameView mFrameView;
    private Handler mHandler;
    private boolean mHasRegisteredReceiver;
    private TextView mLabel;
    private int mLightPointY;
    private LocalBroadcastManager mLocalFinishManager;
    private BroadcastReceiver mNormalReceiver;
    private final HwCustDavPixelCoverAlarm mPixelCoverAlarm = ((HwCustDavPixelCoverAlarm) HwCustUtils.createObj(HwCustDavPixelCoverAlarm.class, new Object[0]));
    private SharedPreferences mPreferences;
    private BroadcastReceiver mReceiver;
    private RythmSurfaceView mRythmView;
    private int mSettingsVol;
    private final HwCustSmartCoverManager mSmartCoverManager = ((HwCustSmartCoverManager) HwCustUtils.createObj(HwCustSmartCoverManager.class, new Object[]{this}));
    private RelativeLayout mSnooze;
    private OnClickListener mSnoozeListener;
    private TextView mSnoozeMin;
    private int mSnoozeMinutes;
    private TextView mSnoozePause;
    private TextView mSnoozeTip;
    private BroadcastReceiver mSystemReceiver;
    private int mToastCount = 1;
    private boolean mUserHandler;
    private View mView;
    private long mWaitingTime;
    private ParticleRenderer particleRenderer;
    StatusBarManager statusBarManager = null;

    private class AlarmHandler extends Handler {
        private AlarmHandler() {
        }

        public void handleMessage(Message msg) {
            Log.iRelease(LockAlarmFullActivity.TAG, "mHandler->handleMessage : " + msg.what);
            switch (msg.what) {
                case 2:
                    if (AlarmState.getInstance().getAlramID() == LockAlarmFullActivity.this.mAlarm.id) {
                        LockAlarmFullActivity.this.clockRepotSmallWindow(1);
                        ClockReporter.reportEventContainMessage(LockAlarmFullActivity.this, 33, "CLOSE", (int) ((System.currentTimeMillis() - LockAlarmFullActivity.this.mWaitingTime) / 1000));
                        HwLog.i("connection", "slide to stop alarm");
                        WearUtils.talkWithWatch(LockAlarmFullActivity.this, 2, LockAlarmFullActivity.this.mAlarm);
                    } else {
                        HwLog.i("connection", "LockAlarmFullActivity alarm is old");
                    }
                    LockAlarmFullActivity.this.mControlAlarm.dismiss(false, LockAlarmFullActivity.this.mAlarm);
                    LockAlarmFullActivity.this.mUserHandler = true;
                    LockAlarmFullActivity.this.finish();
                    break;
                case LockAlarmFullActivity.MESSAGE_SNOOZE /*801*/:
                    HwLog.d("connection", "LockAlarmFullActivity receive MESSAGE_SNOOZE");
                    if (AlarmState.getInstance().getAlramID() == LockAlarmFullActivity.this.mAlarm.id) {
                        HwLog.i("connection", "key press to stop alarm");
                        WearUtils.talkWithWatch(LockAlarmFullActivity.this, 2, LockAlarmFullActivity.this.mAlarm);
                    } else {
                        HwLog.i("connection", "LockAlarmFullActivity alarm is old");
                    }
                    LockAlarmFullActivity.this.mControlAlarm.snooze(LockAlarmFullActivity.this.mAlarm);
                    LockAlarmFullActivity.this.mUserHandler = true;
                    LockAlarmFullActivity.this.finish();
                    break;
            }
            super.handleMessage(msg);
        }
    }

    public class ControlAlarm {
        Context mContext;
        View mView;

        public ControlAlarm(Context context, View view) {
            this.mContext = context;
            this.mView = view;
        }

        public void snooze(Alarm mAlarm) {
            LockAlarmFullActivity.this.statusBarManager.disable(0);
            Log.iRelease(LockAlarmFullActivity.TAG, "snooze : Alarm snooze");
            Alarms.setSnoozeOffAlarm(Alarms.getMIsPowerOffAlarm());
            if (this.mView == null || this.mView.findViewById(R.id.snooze_layout).isEnabled()) {
                int snoozeMinutes = Utils.getDefaultSharedPreferences(this.mContext).getInt("snooze_duration", 10);
                long snoozeTime = System.currentTimeMillis() + (((long) snoozeMinutes) * 60000);
                Log.iRelease(LockAlarmFullActivity.TAG, "ControlAlarm->snooze : snoozeMinutes = " + snoozeMinutes + " snoozeTime = " + snoozeTime);
                Alarms.saveSnoozeAlert(this.mContext, mAlarm.id, snoozeTime);
                Alarms.clearAutoSilent(this.mContext, mAlarm.id);
                if (Alarms.getMIsPowerOffAlarm()) {
                    Log.iRelease(LockAlarmFullActivity.TAG, "ControlAlarm->snooze : is power off alarm, will shut down.");
                }
                mAlarm.showSnoozeNotification(this.mContext, snoozeTime, false);
                LocalBroadcastManager.getInstance(this.mContext).sendBroadcast(new Intent("com.android.deskclock.updatealarmlist"));
                Intent intent = new Intent("com.android.deskclock.ALARM_ALERT");
                intent.setClass(this.mContext, AlarmKlaxon.class);
                this.mContext.stopService(intent);
                LockAlarmFullActivity.setmIsServiceOn(false);
                return;
            }
            dismiss(false, mAlarm);
            Log.iRelease(LockAlarmFullActivity.TAG, "ControlAlarm->snooze : Do not snooze if the snooze button is disabled.");
        }

        private NotificationManager getNotificationManager() {
            return (NotificationManager) this.mContext.getSystemService("notification");
        }

        public void dismiss(boolean killed, Alarm mAlarm) {
            if (!killed && mAlarm.queryDaysOfWeekType() == 0) {
                RingCache.getInstance().deleteRingCache(this.mContext, mAlarm.alert, false);
            }
            LockAlarmFullActivity.this.statusBarManager.disable(0);
            Log.iRelease(LockAlarmFullActivity.TAG, "dismiss : " + (killed ? "Alarm killed" : "Alarm dismissed by user"));
            if (!killed) {
                Alarms.clearAutoSilent(this.mContext, mAlarm.id);
                getNotificationManager().cancel(mAlarm.id);
                if (AlarmState.getInstance().getAlramID() == mAlarm.id) {
                    Intent intent = new Intent("com.android.deskclock.ALARM_ALERT");
                    intent.setClass(this.mContext, AlarmKlaxon.class);
                    this.mContext.stopService(intent);
                    LockAlarmFullActivity.setmIsServiceOn(false);
                }
                if (Alarms.isPowerOffAlarm(LockAlarmFullActivity.this.getApplicationContext(), mAlarm.id)) {
                    Alarms.setSnoozeOffAlarm(false);
                    if (Alarms.isAirplaneMode(this.mContext) == 0) {
                        Alarms.closeAirplaneMode(this.mContext);
                    }
                    return;
                }
            }
            if (Alarms.getMIsPowerOffAlarm() || Alarms.isSnoozeOffAlarm()) {
                Alarms.setSnoozeOffAlarm(false);
                getNotificationManager().cancel(mAlarm.id);
                if (AlarmState.getInstance().getAlramID() == mAlarm.id) {
                    intent = new Intent("com.android.deskclock.ALARM_ALERT");
                    intent.setClass(this.mContext, AlarmKlaxon.class);
                    this.mContext.stopService(intent);
                    LockAlarmFullActivity.setmIsServiceOn(false);
                }
            }
            if (mAlarm.label != null && "Start Alarm Test".equals(mAlarm.label) && mAlarm.vibrate && mAlarm.alert != null && "silent".equals(mAlarm.alert.toString())) {
                Log.iRelease(LockAlarmFullActivity.TAG, "dismiss : it is cts test alarm, we will delete it after dismiss");
                Alarms.deleteAlarm(this.mContext, mAlarm.queryAlarmId());
            }
        }
    }

    public static class HWEGLConfigChooser implements EGLConfigChooser {
        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
            EGLConfig[] configOut = new EGLConfig[1];
            egl.eglChooseConfig(display, new int[]{12339, 4, 12324, 8, 12323, 8, 12322, 8, 12321, 8, 12325, 16, 12338, 1, 12337, 2, 12344}, configOut, 1, new int[1]);
            return configOut[0];
        }
    }

    public LockAlarmFullActivity() {
        this.isVisibleCoverWin = Global.getInt(DeskClockApplication.getDeskClockApplication().getContentResolver(), SETTINGS_COVER_TYPE, 0) == 0;
        this.mHasRegisteredReceiver = false;
        this.mRythmView = null;
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null) {
                    String action = intent.getAction();
                    Log.iRelease(LockAlarmFullActivity.TAG, "onReceive : action = " + action);
                    if ("com.android.deskclock.ALARM_SNOOZE".equals(action) || "android.intent.action.timer_alert".equals(action)) {
                        Log.dRelease(LockAlarmFullActivity.TAG, "onReceive : mToastCount = " + LockAlarmFullActivity.this.mToastCount);
                        if (!LockAlarmFullActivity.this.mAudioManager.isStreamMute(4)) {
                            LockAlarmFullActivity.this.mAudioManager.setStreamVolume(4, LockAlarmFullActivity.this.mSettingsVol, 0);
                        }
                        LockAlarmFullActivity.this.finish();
                    } else if ("com.android.deskclock.ALARM_DELETE".equals(action)) {
                        int alarmId = intent.getIntExtra("delete_alarm_id", LockAlarmFullActivity.STATE_INVALIDE);
                        Log.d(LockAlarmFullActivity.TAG, "onReceive : ALARM_DELETE_ACTION alarmId = " + alarmId + "mAlarm.id = " + LockAlarmFullActivity.this.mAlarm.id);
                        HwLog.d("connection", " LockAlarmFullActivity receiver delete msg to stop alarm");
                        if (alarmId == LockAlarmFullActivity.this.mAlarm.id) {
                            LockAlarmFullActivity.this.mControlAlarm.dismiss(false, LockAlarmFullActivity.this.mAlarm);
                            HwLog.d("connection", "LockAlarmFullActivity handle  delete msg to stop alarm");
                            WearUtils.talkWithWatch(LockAlarmFullActivity.this, 2, LockAlarmFullActivity.this.mAlarm);
                            LockAlarmFullActivity.this.finish();
                        } else {
                            HwLog.d("connection", "LockAlarmFullActivity alarm is old, the newer come");
                        }
                    } else {
                        LockAlarmFullActivity.this.receiveSeparated(context, intent, action);
                    }
                }
            }
        };
        this.mFinishRegister = false;
        this.mFisnishReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    HwLog.i(LockAlarmFullActivity.TAG, "receiver finish message from AlarmReceiver");
                    Alarm alarm = (Alarm) intent.getParcelableExtra("intent.extra.alarm");
                    if ("action_notify_finish_alert".equals(intent.getAction()) && alarm != null && LockAlarmFullActivity.this.mAlarm != null && alarm.queryAlarmId() == LockAlarmFullActivity.this.mAlarm.queryAlarmId()) {
                        HwLog.i(LockAlarmFullActivity.TAG, "finish activity");
                        LockAlarmFullActivity.this.finish();
                    }
                }
            }
        };
        this.mBeCancel = true;
        this.mSystemReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent == null || intent.getAction() == null) {
                    Log.w(LockAlarmFullActivity.TAG, "the intent is null or the action is null.");
                    return;
                }
                String action = intent.getAction();
                Log.iRelease(LockAlarmFullActivity.TAG, "onReceive --> action:" + action);
                if (LockAlarmFullActivity.COVER_STATE_CHANGED_ACTION.equals(action)) {
                    LockAlarmFullActivity.this.handleCoverStateChanged(intent);
                }
            }
        };
        this.mNormalReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null) {
                    HwLog.i(LockAlarmFullActivity.TAG, "onReceive " + intent.getAction());
                    LockAlarmFullActivity.this.handleHardKeyConflict(intent);
                }
            }
        };
        this.mSnoozeListener = new OnClickListener() {
            public void onClick(View v) {
                Log.d(LockAlarmFullActivity.TAG, "v.getId():" + v.getId());
                switch (v.getId()) {
                    case R.id.close_layout:
                        if (AlarmState.getInstance().getAlramID() == LockAlarmFullActivity.this.mAlarm.id) {
                            HwLog.d("connection", "LockAlarmFullActivity click close layout to close alarm");
                            LockAlarmFullActivity.this.mControlAlarm.dismiss(false, LockAlarmFullActivity.this.mAlarm);
                            WearUtils.talkWithWatch(LockAlarmFullActivity.this, 0, LockAlarmFullActivity.this.mAlarm);
                            LockAlarmFullActivity.this.finish();
                            return;
                        }
                        HwLog.d("connection", "LockAlarmFullActivity alarm is old");
                        return;
                    case R.id.snooze_layout:
                    case R.id.cover_snooze_layout:
                        LockAlarmFullActivity.this.clockRepotSmallWindow(0);
                        ClockReporter.reportEventContainMessage(LockAlarmFullActivity.this, 32, "SNOOZE", (int) ((System.currentTimeMillis() - LockAlarmFullActivity.this.mWaitingTime) / 1000));
                        if (AlarmState.getInstance().getAlramID() == LockAlarmFullActivity.this.mAlarm.id) {
                            HwLog.d("connection", "LockAlarmFullActivity click snooze layout to snooze alarm");
                            WearUtils.talkWithWatch(LockAlarmFullActivity.this, 3, LockAlarmFullActivity.this.mAlarm);
                        } else {
                            HwLog.d("connection", "LockAlarmFullActivity alarm is old");
                        }
                        LockAlarmFullActivity.this.mControlAlarm.snooze(LockAlarmFullActivity.this.mAlarm);
                        LockAlarmFullActivity.this.finish();
                        return;
                    default:
                        return;
                }
            }
        };
        this.mHandler = new AlarmHandler();
    }

    public void receiveSeparated(Context context, Intent intent, String action) {
        if ("com.android.deskclock.ALARM_DISMISS".equals(action) || "com.android.deskclock.ALARM_CLOSE_NO_SNOOZE_ACTION".equals(action)) {
            AlarmState alarmState = AlarmState.getInstance();
            int curState = alarmState.getState();
            int curAlarmID = alarmState.getAlramID();
            if ("com.android.deskclock.ALARM_CLOSE_NO_SNOOZE_ACTION".equals(action)) {
                Alarm intentAlarm = (Alarm) intent.getParcelableExtra("intent.extra.alarm");
                if (intentAlarm == null || curAlarmID == intentAlarm.id) {
                    HwLog.d("connection", "LockAlarmFullyActivity close current Alarm");
                    this.mControlAlarm.dismiss(false, this.mAlarm);
                    finish();
                } else {
                    HwLog.d("connection", "LockAlarmFullActivity, this is old alarm, will be handled in AlarmReceiver");
                }
            } else if ("com.android.deskclock.ALARM_DISMISS".equals(action) && curState == 1) {
                alarmState.setState(3);
                this.mControlAlarm.dismiss(false, this.mAlarm);
                HwLog.d("connection", "outter stop alarm");
                WearUtils.talkWithWatch(this, 2, this.mAlarm);
                finish();
            }
        } else if ("com.android.deskclock.ALARM_ALERT".equals(action)) {
            this.mAlamCount++;
        } else if ("alarm_killed".equals(action)) {
            handleAlarmKillIntent(intent);
        } else if (("com.android.deskclock.watch_snooze_action".equals(action) || "com.android.deskclock.watch_close_action".equals(action)) && !isFinishing()) {
            Alarm alarm = (Alarm) intent.getParcelableExtra("intent.extra.alarm");
            if (alarm != null && alarm.id == this.mAlarm.id) {
                finish();
            }
        }
    }

    public static boolean isCurveScreen() {
        if (sIsCurveScreen == STATE_INVALIDE) {
            try {
                sIsCurveScreen = HwGeneralManager.getInstance().isCurveScreen() ? 1 : 0;
            } catch (Exception e) {
                Log.e(TAG, "distinguish curve screen fail!");
            }
        }
        boolean z = sIsCurveScreen != 1 ? SystemProperties.getBoolean("keyguard.debug.curve", false) : true;
        Log.i(TAG, "isCurveScreen sIsCurveScreen = " + z);
        return z;
    }

    private void handleAlarmKillIntent(Intent intent) {
        Log.iRelease(TAG, "handleAlarmKillIntent mAlamCount = " + this.mAlamCount);
        if (this.mAlamCount > 0) {
            this.mAlamCount += STATE_INVALIDE;
        }
        Alarm alarm = (Alarm) intent.getParcelableExtra("intent.extra.alarm");
        AlarmState alarmState = AlarmState.getInstance();
        if (alarm == null || this.mAlamCount >= 1) {
            if (!alarmState.getFireType()) {
                return;
            }
        }
        this.mControlAlarm.dismiss(true, this.mAlarm);
        Log.iRelease(TAG, "LockAlarmFullActivity finish");
        finish();
    }

    private void registerFinishReceiver() {
        if (!this.mFinishRegister) {
            this.mLocalFinishManager = LocalBroadcastManager.getInstance(this);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("action_notify_finish_alert");
            this.mLocalFinishManager.registerReceiver(this.mFisnishReceiver, intentFilter);
            this.mFinishRegister = true;
        }
    }

    private void unregisterFinishReceiver() {
        if (this.mFinishRegister) {
            this.mLocalFinishManager.unregisterReceiver(this.mReceiver);
        }
    }

    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Log.iRelease(TAG, "onCreate");
        requestWindowFeature(1);
        Config.doSetExit_count(1);
        if (!Config.istablet()) {
            setRequestedOrientation(1);
        }
        this.mView = LayoutInflater.from(this).inflate(R.layout.alarm_alert, (ViewGroup) null);
        Intent intent = getIntent();
        if (intent == null || intent.getExtras() == null) {
            finish();
            Log.w(TAG, "onCreate : Can't object serialization alarm clock, so finish self.");
        } else if (AlarmKlaxon.getsKlaxonState() == 1) {
            Log.w(TAG, "onCreate : Because service calls have stopped, can't display, so finish self.");
            finish();
        } else {
            initAlarm(intent);
            ((NotificationManager) getSystemService("notification")).cancel(this.mAlarm.id);
            AlarmReceiver.sendInnerNotification(this, intent, this.mAlarm);
            this.mAudioManager = (AudioManager) getSystemService("audio");
            this.mSettingsVol = this.mAudioManager.getStreamVolume(4);
            int systemAlarmVolume = Utils.getDefaultSharedPreferences(this).getInt("systemAlarmVolume", 0);
            if (systemAlarmVolume > 0) {
                this.mSettingsVol = systemAlarmVolume;
            }
            if (this.mAlarm.id == Utils.getDefaultSharedPreferences(this).getInt("is_power_off_alarm_id", STATE_INVALIDE)) {
                Alarms.setMIsPowerOffAlarm(true);
                Log.d(TAG, "this alarm is powerOff alarm...");
            } else {
                Log.d(TAG, "this alarm not know is powerOff alarm...");
                Alarms.setMIsPowerOffAlarm(intent.getBooleanExtra("FLAG_IS_FIRST_POWER_OFF_ALARM", false));
            }
            this.mPreferences = Utils.getSharedPreferences(this, "setting_activity", 0);
            this.mControlAlarm = new ControlAlarm(this, this.mView);
            this.mController = CoverItemController.getInstance(this);
            Window win = getWindow();
            win.addFlags(524288);
            LayoutParams winParams = win.getAttributes();
            winParams.buttonBrightness = 0.0f;
            win.setAttributes(winParams);
            if (!intent.getBooleanExtra(SCREEN_OFF, false) && (this.mController.isCoverOpen() || (!this.mController.isCoverOpen() && this.isVisibleCoverWin))) {
                win.addFlags(2098304);
            }
            win.addFlags(134217728);
            win.addFlags(67108864);
            setIsCurverScreen(isCurveScreen());
            if (mIsCurverScreen) {
                this.gView = new GLSurfaceView(this);
                this.gView.setEGLConfigChooser(new HWEGLConfigChooser());
                this.gView.setZOrderOnTop(true);
                this.gView.getHolder().setFormat(-3);
                this.particleRenderer = new ParticleRenderer(this);
                this.gView.setRenderer(this.particleRenderer);
                this.particleRenderer.setLCDRatio(0.5960265f);
            }
            setContentView(this.mView);
            this.mRythmView = new RythmSurfaceView(this);
            RelativeLayout relateMusic = (RelativeLayout) findViewById(R.id.relate);
            this.mRythmView.attachView(relateMusic);
            this.statusBarManager = (StatusBarManager) getSystemService("statusbar");
            this.mView.setSystemUiVisibility(((this.mView.getSystemUiVisibility() | 16777216) | 2097152) | 65536);
            updateLayout();
            initAction();
            setTitle();
            this.mWaitingTime = System.currentTimeMillis();
            if (mIsCurverScreen) {
                relateMusic.addView(this.gView);
                final FrameLayout touchView = (FrameLayout) findViewById(R.id.close_layout);
                touchView.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        LockAlarmFullActivity.this.onTouchAction(event, touchView);
                        return false;
                    }
                });
            }
            registerFinishReceiver();
        }
    }

    public void onTouchAction(MotionEvent event, FrameLayout touchView) {
        switch (event.getAction()) {
            case 0:
                this.mBeCancel = false;
                this.particleRenderer.setTranslaterF(event.getRawX(), (float) getBallViewCenterY(touchView));
                this.particleRenderer.setDrawState(DrawStates.STARTDRAW);
                touchView.setVisibility(4);
                return;
            case 1:
                if (!this.mBeCancel) {
                    this.particleRenderer.setTranslaterF(event.getRawX(), (float) getBallViewCenterY(touchView));
                    this.particleRenderer.setDrawState(DrawStates.DISDRAW);
                    touchView.setVisibility(0);
                    this.mBeCancel = true;
                    return;
                }
                return;
            case 2:
                if (isEventInBallView(event, touchView)) {
                    if (!this.mBeCancel) {
                        this.particleRenderer.setTranslaterF(event.getRawX(), (float) getBallViewCenterY(touchView));
                        return;
                    }
                    return;
                } else if (!this.mBeCancel) {
                    this.particleRenderer.setTranslaterF(event.getRawX(), (float) getBallViewCenterY(touchView));
                    this.particleRenderer.setDrawState(DrawStates.DISDRAW);
                    touchView.setVisibility(0);
                    this.mBeCancel = true;
                    return;
                } else {
                    return;
                }
            default:
                return;
        }
    }

    private static void setIsCurverScreen(boolean isCurverScreen) {
        mIsCurverScreen = isCurverScreen;
    }

    public boolean isEventInBallView(MotionEvent event, View closeLayout) {
        int[] xy = new int[2];
        float x = event.getRawX();
        float y = event.getRawY();
        closeLayout.getLocationOnScreen(xy);
        if (x <= ((float) xy[0]) || x >= ((float) (xy[0] + closeLayout.getWidth())) || y <= ((float) xy[1]) || y >= ((float) (xy[1] + closeLayout.getHeight()))) {
            return false;
        }
        return true;
    }

    public int getBallViewCenterY(View closeLayout) {
        if (this.mLightPointY == 0) {
            int[] xy = new int[2];
            closeLayout.getLocationOnScreen(xy);
            this.mLightPointY = xy[1] + (closeLayout.getHeight() / 2);
        }
        return this.mLightPointY;
    }

    protected void onStart() {
        super.onStart();
    }

    private void registerSysReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(COVER_STATE_CHANGED_ACTION);
        filter.setPriority(KILL_PRIORITY);
        registerReceiver(this.mSystemReceiver, filter);
    }

    private void registerNormalReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        filter.setPriority(KILL_PRIORITY);
        registerReceiver(this.mNormalReceiver, filter, "android.permission.INJECT_EVENTS", null);
    }

    private void handleCoverStateChanged(Intent intent) {
        boolean coverOpen = intent.getBooleanExtra(COVER_STATE, true);
        Log.dRelease(TAG, "onReceive : action = com.huawei.android.cover.STATE coverOpen = " + coverOpen);
        if (coverOpen) {
            removeCoverScreen();
            if (!this.isVisibleCoverWin) {
                AlarmAlertWakeLock.acquireFullWakeLock();
            }
        } else if (this.mSmartCoverManager == null || !this.mSmartCoverManager.isSmartCoverEnable()) {
            ClockReporter.reportEventMessage(this, 80, "");
            if (AlarmState.getInstance().getAlramID() == this.mAlarm.id) {
                this.mControlAlarm.snooze(this.mAlarm);
                HwLog.d("connection", "close cover to snooze alarm");
                WearUtils.talkWithWatch(this, 3, this.mAlarm);
                finish();
                return;
            }
            HwLog.d("connection", "LockAlarmFullActivity alarm is old");
        } else {
            Log.i(TAG, "COVER_STATE_CHANGED_ACTION and SmartCoverMode");
            this.mSmartCoverManager.addSnoozeCoverView(this.mHandler);
        }
    }

    private void handleHardKeyConflict(Intent intent) {
        String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
        HwLog.i(TAG, "reason:" + reason);
        if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
            this.mHandler.sendEmptyMessage(MESSAGE_SNOOZE);
        } else if (SYSTEM_DIALOG_REASON_RECENT_APPS.equals(reason)) {
            this.mHandler.sendEmptyMessage(MESSAGE_SNOOZE);
        }
    }

    private void initAction() {
        IntentFilter filter = new IntentFilter("alarm_killed");
        filter.addAction("com.android.deskclock.ALARM_SNOOZE");
        filter.addAction("com.android.deskclock.ALARM_DISMISS");
        filter.addAction("com.android.deskclock.ALARM_CLOSE_NO_SNOOZE_ACTION");
        filter.addAction("com.android.deskclock.ALARM_ALERT");
        filter.addAction("android.intent.action.timer_alert");
        filter.addAction("com.android.deskclock.ALARM_DELETE");
        filter.addAction("com.android.deskclock.watch_snooze_action");
        filter.addAction("com.android.deskclock.watch_close_action");
        registerReceiver(this.mReceiver, filter, "com.huawei.deskclock.broadcast.permission", null);
        registerSysReceiver();
        registerNormalReceiver();
        this.mHasRegisteredReceiver = true;
    }

    private void addCoverScreen() {
        if (this.isVisibleCoverWin && !this.mCoverClockAdded && !this.mController.isCoverOpen() && hasSmallWindowData()) {
            AlarmAlertWakeLock.acquireBrightScreenWakeLock(this);
            LayoutInflater inflater = LayoutInflater.from(this);
            Rect rect = getCoverRect();
            inflateCoverScreen(rect, inflater);
            this.mCoverScreen.setKeyEventListener(new KeyEventListener() {
                public boolean dispatchKeyEvent(KeyEvent event) {
                    return LockAlarmFullActivity.this.handleKeyEvent(event);
                }
            });
            this.mCoverView = (RelativeLayout) this.mCoverScreen.findViewById(R.id.cover_view);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mCoverView.getLayoutParams();
            layoutParams.topMargin = rect.top;
            layoutParams.width = rect.width();
            layoutParams.height = rect.height();
            layoutParams.leftMargin = rect.left;
            this.mCoverView.setLayoutParams(layoutParams);
            this.mCoverView.setSystemUiVisibility(8388608);
            if (!isCoverModeOfDavince()) {
                if (this.mCover == null || !this.mCover.isLONPortCover()) {
                    updateCoverLayout();
                } else {
                    this.mCover.initLONCover(this, this.mCoverScreen, this.mHandler, this.mSnoozeListener);
                }
            }
            this.mCoverSnoozeLayout = (RelativeLayout) this.mCoverScreen.findViewById(R.id.cover_snooze_layout);
            if (isCoverModeOfDavince()) {
                this.mPixelCoverAlarm.slideCloseAlarmClock(this.mCoverSnoozeLayout, this.mControlAlarm, Boolean.valueOf(false), this.mAlarm);
            } else {
                isCoverModeOfDavinceFalseAction(rect);
            }
            this.mCoverLabel = (TextView) this.mCoverScreen.findViewById(R.id.cover_alarm_label);
            this.mCoverLabel.setText(this.mAlarm.getLabelOrDefault(this));
            this.mController.addCoverItem(this.mCoverScreen, true);
            if (this.mFrameView != null) {
                this.mFrameView.stopTextViewAnimal();
            }
            if (this.mCoverCloseSlider != null) {
                this.mCoverCloseSlider.startTextViewAnimal();
            }
            this.mCoverClockAdded = true;
        }
    }

    public void isCoverModeOfDavinceFalseAction(Rect rect) {
        View coverCloseLayout = this.mCoverScreen.findViewById(R.id.cover_close_layout);
        if (coverCloseLayout != null && (coverCloseLayout instanceof BallFrameView)) {
            this.mCoverCloseSlider = (BallFrameView) coverCloseLayout;
            this.mCoverCloseSlider.setMainHandler(this.mHandler);
            this.mCoverCloseSlider.setCoverViewWidth(rect.width());
        }
        if (coverCloseLayout != null && (coverCloseLayout instanceof CoverFrameView)) {
            this.mCoverCloseSliderWithoutBall = (CoverFrameView) coverCloseLayout;
            this.mCoverCloseSliderWithoutBall.setMainHandler(this.mHandler);
            this.mCoverCloseSliderWithoutBall.setCoverViewWidth(rect.width());
        }
        if (coverCloseLayout != null && (coverCloseLayout instanceof PortBallFrameView)) {
            PortBallFrameView portBallFrameView = (PortBallFrameView) coverCloseLayout;
            portBallFrameView.setMainHandler(this.mHandler);
            portBallFrameView.setCoverViewWidth(rect.width());
        }
    }

    private void updateCoverLayout() {
        int snoozeMinutes = Utils.getDefaultSharedPreferences(this).getInt("snooze_duration", 10);
        this.mCoverSnoozePause = (TextView) this.mCoverScreen.findViewById(R.id.cover_snooze_pause_time);
        this.mCoverSnoozePause.setText(String.format(Locale.getDefault(), "%d", new Object[]{Integer.valueOf(snoozeMinutes)}));
        String min = getString(R.string.tips_clock_snoozealarm_min);
        this.mCoverSnoozeMin = (TextView) this.mCoverScreen.findViewById(R.id.cover_snooze_pause_min);
        this.mCoverSnoozeMin.setText(min);
        this.mCoverSnoozeTip = (TextView) this.mCoverScreen.findViewById(R.id.cover_snooze_pause_tip);
        this.mCoverSnoozeTip.setText(getString(R.string.tips_clock_snoozealarm_tip));
        RelativeLayout coverSnooze = (RelativeLayout) this.mCoverScreen.findViewById(R.id.cover_snooze_layout);
        coverSnooze.requestFocus();
        coverSnooze.setOnClickListener(this.mSnoozeListener);
    }

    private void inflateCoverScreen(Rect rect, LayoutInflater inflater) {
        Log.dRelease(TAG, "addCoverScreen : width = " + rect.width() + " height = " + rect.height());
        int layoutID = R.layout.cover_alarm_full;
        int portLayoutID = R.layout.cover_alarm_full_port;
        if (this.mCover != null && this.mCover.isAdapterCoverEnable()) {
            layoutID = this.mCover.getResIdentifier(this, "cover_alarm_full", HwCustCoverAdapter.TYPE_LAYOUT, HwCustCoverAdapter.APP_PACKEGE, R.layout.cover_alarm_full);
            portLayoutID = this.mCover.getResIdentifier(this, "cover_alarm_full_port", HwCustCoverAdapter.TYPE_LAYOUT, HwCustCoverAdapter.APP_PACKEGE, R.layout.cover_alarm_full_port);
        }
        String str = COVER_SIZE;
        if (str.equals(BBQ_SIZE)) {
            this.mCoverScreen = (CoverView) inflater.inflate(R.layout.cover_alarm_full_1440x2560, null);
        } else if (str.equals(WAS_SIZE)) {
            this.mCoverScreen = (CoverView) inflater.inflate(R.layout.cover_alarm_full_port_747x1920, null);
        } else if (str.equals(VKY_SIZE)) {
            this.mCoverScreen = (CoverView) inflater.inflate(R.layout.cover_alarm_full_port_v_plus, null);
        } else if (str.equals(VTR_SIZE)) {
            this.mCoverScreen = (CoverView) inflater.inflate(R.layout.cover_alarm_full_port_v, null);
        } else {
            if (rect.width() < rect.height()) {
                WindowManager wm = getWindowManager();
                int screenWidth = wm.getDefaultDisplay().getWidth();
                int screenHeight = wm.getDefaultDisplay().getHeight();
                if (isCoverModeOfDavince()) {
                    this.mCoverScreen = this.mPixelCoverAlarm.dynamicDisplayCoverAlarmLayout(this);
                } else if (rect.width() > (screenWidth * 2) / 3) {
                    this.mCoverScreen = (CoverView) inflater.inflate(layoutID, null);
                    HwLog.i(TAG, "addCoverScreen : is land (large screen). screenWidth = " + screenWidth);
                } else {
                    HwLog.i(TAG, "addCoverScreen : width = " + screenWidth + " ,height = " + screenHeight);
                    this.mCoverScreen = (CoverView) inflater.inflate(portLayoutID, null);
                }
            } else {
                this.mCoverScreen = (CoverView) inflater.inflate(layoutID, null);
                Log.iRelease(TAG, "addCoverScreen : is land.");
            }
        }
    }

    private boolean isCoverModeOfDavince() {
        return this.mPixelCoverAlarm != null ? this.mPixelCoverAlarm.isPixelCoverEnable() : false;
    }

    private Rect getCoverRect() {
        Rect rect;
        String[] location = null;
        if (SystemProperties.get("ro.config.huawei_smallwindow") != null) {
            location = SystemProperties.get("ro.config.huawei_smallwindow").split(",");
        }
        Display display = ((WindowManager) getSystemService("window")).getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        if (location == null || location.length != 4) {
            rect = new Rect((int) (displayMetrics.density * WorldAnalogClock.DEGREE_ONE_HOUR), (int) (displayMetrics.density * 184.0f), (int) (displayMetrics.density * 330.0f), (int) (displayMetrics.density * 338.0f));
        } else {
            rect = new Rect(Integer.parseInt(location[0]), Integer.parseInt(location[1]), Integer.parseInt(location[2]), Integer.parseInt(location[3]));
        }
        return checkRectSize(this, rect);
    }

    public static Rect checkRectSize(Context context, Rect r) {
        int width = SystemProperties.getInt("persist.sys.rog.width", 0);
        if (width == 0) {
            return r;
        }
        int height = SystemProperties.getInt("persist.sys.rog.height", 0);
        if (height == 0) {
            return r;
        }
        Point tempPoint = getOriginDisplayPoint(context);
        if (tempPoint == null) {
            return r;
        }
        Point oPoint = resetPointWidthHeight(tempPoint);
        if (oPoint.x == width && oPoint.y == height) {
            return r;
        }
        return new Rect((r.left * width) / oPoint.x, (r.top * height) / oPoint.y, (r.right * width) / oPoint.x, (r.bottom * height) / oPoint.y);
    }

    private static Point resetPointWidthHeight(Point point) {
        if (point.x <= point.y) {
            return point;
        }
        int cacheX = point.x;
        point.x = point.y;
        point.y = cacheX;
        return point;
    }

    public static Point getPoint(Context context) {
        if (context == null) {
            Log.w(TAG, "getPoint context is null");
            return null;
        }
        Display display = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        return size;
    }

    public static Point getOriginDisplayPoint(Context context) {
        Point initialSize = new Point();
        try {
            Stub.asInterface(ServiceManager.getService("window")).getInitialDisplaySize(0, initialSize);
            return initialSize;
        } catch (RemoteException e) {
            Log.e(TAG, "getOriginDisplayPoint Fail");
            return getPoint(context);
        } catch (Exception e2) {
            Log.e(TAG, "getOriginDisplayPoint Fail");
            return getPoint(context);
        }
    }

    private void removeCoverScreen() {
        if (this.mCoverClockAdded) {
            this.mController.removeCoverItem();
            if (this.mFrameView != null) {
                this.mFrameView.startTextViewAnimal();
            }
            if (this.mCoverCloseSlider != null) {
                this.mCoverCloseSlider.stopTextViewAnimal();
            }
            if (this.mCover != null && this.mCover.isLONPortCover()) {
                this.mCover.stopLONCoverAnim();
            }
            this.mCoverClockAdded = false;
        }
    }

    private boolean hasSmallWindowData() {
        if (SystemProperties.get("ro.config.huawei_smallwindow") == null || SystemProperties.get("ro.config.huawei_smallwindow").split(",").length != 4) {
            return false;
        }
        return true;
    }

    private void initAlarm(Intent intent) {
        this.mAlarm = (Alarm) intent.getParcelableExtra("intent.extra.alarm");
        if (this.mAlarm == null) {
            finish();
            return;
        }
        Alarm alarm = Alarms.getAlarm(getContentResolver(), this.mAlarm.id);
        if (alarm != null) {
            this.mAlarm = alarm;
            startAlarmKlaxonService(alarm);
        }
    }

    private void startAlarmKlaxonService(Alarm alarm) {
        Log.iRelease(TAG, "ismIsServiceOn:" + ismIsServiceOn());
        Log.d(TAG, "handleIntent : Play the alarm alert");
        Intent playAlarm = new Intent("com.android.deskclock.ALARM_ALERT");
        playAlarm.putExtra("intent.extra.alarm", alarm);
        playAlarm.setClass(this, AlarmKlaxon.class);
        startService(playAlarm);
        setmIsServiceOn(true);
    }

    private void updateLayout() {
        int snoozeMinutes = Utils.getDefaultSharedPreferences(this).getInt("snooze_duration", 10);
        this.mSnoozePause = (TextView) findViewById(R.id.snooze_pause_time);
        this.mSnoozePause.setText(String.format(Locale.getDefault(), "%d", new Object[]{Integer.valueOf(snoozeMinutes)}));
        String min = getString(R.string.tips_clock_snoozealarm_min);
        this.mSnoozeMin = (TextView) findViewById(R.id.snooze_pause_min);
        this.mSnoozeMin.setText(min);
        this.mSnoozeTip = (TextView) findViewById(R.id.snooze_pause_tip);
        this.mSnoozeTip.setText(getString(R.string.tips_clock_snoozealarm_tip));
        this.mFrameView = (BallFrameView) this.mView.findViewById(R.id.close_layout);
        this.mFrameView.setMainHandler(this.mHandler);
        this.mFrameView.setCoverViewWidth(getResources().getDisplayMetrics().widthPixels);
        this.mSnooze = (RelativeLayout) findViewById(R.id.snooze_layout);
        this.mSnoozeMinutes = Utils.getDefaultSharedPreferences(this).getInt("snooze_duration", 10);
        this.mSnooze.requestFocus();
        this.mSnooze.setOnClickListener(this.mSnoozeListener);
        updateView();
    }

    private void updateView() {
        Context context = this;
        if (Utils.isLandScreen(this)) {
            DigitalClock digitalclock = (DigitalClock) this.mView.findViewById(R.id.digitalclock);
            int singlewidth = getResources().getDisplayMetrics().widthPixels / 12;
            int size = (singlewidth * 4) + (singlewidth - Utils.dip2px(this, 67));
            int heightPixels = getResources().getDisplayMetrics().heightPixels;
            int marginleft = Utils.dip2px(this, 54);
            RelativeLayout.LayoutParams snoozeRlp = new RelativeLayout.LayoutParams(singlewidth * 5, -2);
            snoozeRlp.setMargins(marginleft, 0, 0, 0);
            snoozeRlp.addRule(15);
            this.mSnooze.setLayoutParams(snoozeRlp);
            RelativeLayout.LayoutParams digitalclockRlp = new RelativeLayout.LayoutParams(STATE_INVALIDE, -2);
            digitalclockRlp.setMargins(0, (heightPixels - size) / 2, singlewidth, 0);
            digitalclockRlp.addRule(10, R.id.close_layout);
            digitalclock.setLayoutParams(digitalclockRlp);
            RelativeLayout.LayoutParams mFrameViewRlp = new RelativeLayout.LayoutParams(STATE_INVALIDE, getResources().getDimensionPixelSize(R.dimen.alarm_ball_height));
            mFrameViewRlp.setMargins(0, 0, singlewidth, (heightPixels - size) / 2);
            mFrameViewRlp.addRule(12);
            this.mFrameView.setLayoutParams(mFrameViewRlp);
            TextView mLabel = (TextView) this.mView.findViewById(R.id.tip_alarm);
            if (SystemProperties.getInt("persist.sys.dpi", SystemProperties.getInt("ro.sf.lcd_density", 0)) == BIGMODEL) {
                mLabel.setMaxLines(1);
            }
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mView = LayoutInflater.from(this).inflate(R.layout.alarm_alert, (ViewGroup) null);
        setContentView(this.mView);
        this.mView.setSystemUiVisibility(((this.mView.getSystemUiVisibility() | 16777216) | 2097152) | 65536);
        this.mRythmView = new RythmSurfaceView(this);
        this.mRythmView.attachView((RelativeLayout) findViewById(R.id.relate));
        updateLayout();
        setTitle();
        this.mFrameView.restoreAnimal();
    }

    private void addAccessibilityEventText(AccessibilityEvent event, TextView view) {
        if (view.getVisibility() == 0) {
            CharSequence text = view.getText();
            if (!TextUtils.isEmpty(text)) {
                event.getText().add(text);
            }
        }
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        addAccessibilityEventText(event, (TextView) this.mView.findViewById(R.id.digital_full_time));
        addAccessibilityEventText(event, (TextView) this.mView.findViewById(R.id.digital_left_ampm));
        addAccessibilityEventText(event, (TextView) this.mView.findViewById(R.id.digital_right_ampm));
        return true;
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i(TAG, "onNewIntent");
        if (intent != null) {
            this.mAlarm = (Alarm) intent.getParcelableExtra("intent.extra.alarm");
            if (this.mAlarm != null) {
                Alarm alarm = Alarms.getAlarm(getContentResolver(), this.mAlarm.id);
                if (alarm != null) {
                    this.mAlarm = alarm;
                    startAlarmKlaxonService(alarm);
                }
                setTitle();
            }
        }
    }

    private void clockRepotSmallWindow(int way) {
        if (this.mController != null && !this.mController.isCoverOpen() && this.isVisibleCoverWin) {
            ClockReporter.reportEventContainMessage(this, 79, "HANDLEWAY", way);
        }
    }

    protected void onResume() {
        super.onResume();
        HwLog.i(TAG, "onResume");
        Utils.getDefaultSharedPreferences(this).edit().putBoolean("isAlerting", true).commit();
        if (this.mSmartCoverManager == null || this.mController == null || !this.mSmartCoverManager.isSmartCoverEnable() || this.mCoverClockAdded || this.mController.isCoverOpen()) {
            addCoverScreen();
        } else {
            Log.i(TAG, "SmartCoverEnable!");
            this.mCoverScreen = this.mSmartCoverManager.addCoverScreen(this.mHandler);
            if (this.mCoverScreen != null) {
                Log.d(TAG, "null != mCoverScreen");
                this.mCoverScreen.setKeyEventListener(new KeyEventListener() {
                    public boolean dispatchKeyEvent(KeyEvent event) {
                        return LockAlarmFullActivity.this.handleKeyEvent(event);
                    }
                });
                this.mController.addCoverItem(this.mCoverScreen, false);
                if (this.mFrameView != null) {
                    this.mFrameView.stopTextViewAnimal();
                }
                this.mSmartCoverManager.startAnimal();
                this.mCoverClockAdded = true;
            }
        }
        if (Alarms.getAlarm(getContentResolver(), this.mAlarm.id) == null) {
            ((RelativeLayout) findViewById(R.id.snooze_layout)).setEnabled(false);
            if (this.mCoverSnoozeLayout != null) {
                this.mCoverSnoozeLayout.setEnabled(false);
            }
            Log.dRelease(TAG, "the alarm was deleted at some point, will disable snooze.");
        }
    }

    protected void onPause() {
        super.onPause();
        Log.dRelease(TAG, "onPause");
    }

    protected void onDestroy() {
        super.onDestroy();
        Log.iRelease(TAG, "onDestroy");
        if (ismIsServiceOn()) {
            stopAlarmService(this.mAlarm);
        }
        AlarmKlaxon.setsKlaxonState(0);
        Utils.getDefaultSharedPreferences(this).edit().remove("isAlerting").commit();
        this.mHandler.removeCallbacksAndMessages(null);
        if (this.mHasRegisteredReceiver) {
            unregisterReceiver(this.mReceiver);
            unregisterReceiver(this.mSystemReceiver);
            unregisterReceiver(this.mNormalReceiver);
        }
        AlarmAlertWakeLock.releaseBrightLock();
        AlarmAlertWakeLock.releaseFullLock();
        try {
            if (this.mRythmView != null) {
                this.mRythmView.onDestroy();
            }
        } catch (Exception e) {
            HwLog.i(TAG, "animation destory exception");
        }
        unregisterFinishReceiver();
    }

    public static boolean ismIsServiceOn() {
        return mIsServiceOn;
    }

    public static void setmIsServiceOn(boolean isServiceOn) {
        mIsServiceOn = isServiceOn;
    }

    public void finish() {
        removeCoverScreen();
        super.finish();
        overridePendingTransition(0, R.anim.zoom_exit);
    }

    public void onBackPressed() {
        Log.dRelease(TAG, "onBackPressed : sendEmptyMessage MESSAGE_SNOOZE");
        this.mHandler.sendEmptyMessage(MESSAGE_SNOOZE);
    }

    public void stopAlarmService(Alarm mAlarm) {
        AlarmState alarmState = AlarmState.getInstance();
        HwLog.i(TAG, "mUserHandler = " + this.mUserHandler);
        if (mAlarm == null || alarmState.getAlramID() != mAlarm.id) {
            if (!this.mUserHandler) {
                return;
            }
        }
        Intent intent = new Intent("com.android.deskclock.ALARM_ALERT");
        intent.setClass(this, AlarmKlaxon.class);
        stopService(intent);
        setmIsServiceOn(false);
        HwLog.i(TAG, "stop alarm service.");
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        return handleKeyEvent(event);
    }

    private boolean handleKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {
            case 24:
            case PortCallPanelView.DEFAUT_RADIUS /*25*/:
            case 27:
            case 80:
            case 164:
                if (event.getAction() == 1) {
                    switch (this.mPreferences.getInt("choice", 1)) {
                        case 1:
                            ClockReporter.reportEventMessage(this, 83, "");
                            Intent intent = new Intent("ACTION_TYPE_TURNOVER_SILENT");
                            intent.setPackage(getPackageName());
                            sendBroadcast(intent);
                            break;
                        case 2:
                            if (AlarmState.getInstance().getAlramID() != this.mAlarm.id) {
                                HwLog.d("connection", "LockAlarmFullActivity alarm is old");
                                break;
                            }
                            ClockReporter.reportEventMessage(this, 84, "");
                            this.mControlAlarm.snooze(this.mAlarm);
                            HwLog.d("connection", "Volume key event to snooze alarm");
                            WearUtils.talkWithWatch(this, 3, this.mAlarm);
                            finish();
                            break;
                        case 3:
                            if (AlarmState.getInstance().getAlramID() != this.mAlarm.id) {
                                HwLog.d("connection", "LockAlarmFullActivity alarm is old");
                                break;
                            }
                            ClockReporter.reportEventMessage(this, 85, "");
                            HwLog.d("connection", "Volume key event to stop alarm");
                            this.mControlAlarm.dismiss(false, this.mAlarm);
                            WearUtils.talkWithWatch(this, 2, this.mAlarm);
                            finish();
                            break;
                    }
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    private void setTitle() {
        String titleText;
        if (this.mAlarm == null) {
            titleText = getString(R.string.default_label);
        } else {
            titleText = this.mAlarm.getLabelOrDefault(this);
        }
        this.mLabel = (TextView) findViewById(R.id.tip_alarm);
        this.mLabel.setText(titleText);
        setTitle(titleText);
        if (this.mCoverLabel != null) {
            this.mCoverLabel.setText(titleText);
        }
    }
}
