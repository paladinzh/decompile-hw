package com.android.deskclock.alarmclock;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.service.notification.StatusBarNotification;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.vrsystem.IVRListener.Stub;
import com.android.connection.AlarmState;
import com.android.connection.ConnectionConstants;
import com.android.connection.WearUtils;
import com.android.deskclock.AlarmAlertWakeLock;
import com.android.deskclock.AlarmReceiver;
import com.android.deskclock.DeskClockApplication;
import com.android.deskclock.MotionManager;
import com.android.deskclock.MotionManager.MotionListener;
import com.android.deskclock.R;
import com.android.deskclock.RingCache;
import com.android.deskclock.RingtoneHelper;
import com.android.util.ClockReporter;
import com.android.util.CompatUtils;
import com.android.util.DrmUtils;
import com.android.util.HwLog;
import com.android.util.Log;
import com.android.util.ReflexUtil;
import com.android.util.Utils;
import com.android.util.WeakenVolume;
import com.huawei.cust.HwCustUtils;
import java.io.File;
import java.io.IOException;
import java.util.TimerTask;

public class AlarmKlaxon extends Service {
    private static int RING_OFFHOOK_VB = 2000;
    private static Alarm mCurrentAlarm;
    private static int sKlaxonState = 0;
    private static final long[] sVibratePattern = new long[]{500, 500};
    private int DEFAULT_ALARM_TIMEOUT = 300;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                Log.i("AlarmKlaxon", "broadcastReceiver->OnReceive : maybe intent is null or action is null.");
                return;
            }
            Log.iRelease("AlarmKlaxon", "onReceive:" + intent.getAction());
            if ("ACTION_TYPE_TURNOVER_SILENT".equals(intent.getAction())) {
                if (AlarmKlaxon.this.mPlayer != null && AlarmKlaxon.this.mPlayer.isPlaying()) {
                    AlarmKlaxon.this.mPlayer.pause();
                    HwLog.d("connection", "flip to mute alarm");
                    WearUtils.startActionHandleMute(context, Long.valueOf((long) AlarmKlaxon.mCurrentAlarm.queryAlarmId()), true);
                }
                if (AlarmKlaxon.this.mVibrator != null) {
                    AlarmKlaxon.this.mVibrator.cancel();
                }
            } else if ("ACTION_TYPE_TYPE_PROXIMITY_WEAKEN".equals(intent.getAction())) {
                if (AlarmKlaxon.this.mHandler != null) {
                    AlarmKlaxon.this.mHandler.removeMessages(1001);
                }
            } else if ("com.android.server.input.fpn.stopalarm".equals(intent.getAction())) {
                Log.iRelease("AlarmKlaxon", "fingerprint dismiss alarm");
                ClockReporter.reportEventMessage(context, 66, "");
                HwLog.d("connection", "AlarmKlaxon receive fpn message");
                if (AlarmState.getInstance().getState() == 1) {
                    HwLog.d("connection", "AlarmKlaxon handle fpn message");
                    AlarmKlaxon.this.stopAlarmInService(context);
                    HwLog.d("connection", "finger stop alarm");
                    WearUtils.talkWithWatch(AlarmKlaxon.this, 2, AlarmKlaxon.mCurrentAlarm);
                }
            }
            AlarmKlaxon.this.onOtherAction(context, intent);
        }
    };
    private AudioManager mAudioManager;
    private HwCustCoopSensorManager mCoopSensor = ((HwCustCoopSensorManager) HwCustUtils.createObj(HwCustCoopSensorManager.class, new Object[0]));
    private float mCurVolume;
    private Handler mHandler = new ServiceHandler();
    private HwCustAlarmKlaxon mHwCustAlarmKlaxon = ((HwCustAlarmKlaxon) HwCustUtils.createObj(HwCustAlarmKlaxon.class, new Object[0]));
    private int mInitialCallState;
    private MotionListener mMotionListener = new MotionListener() {
        public void flipMute() {
            Intent intent = new Intent("ACTION_TYPE_TURNOVER_SILENT");
            intent.setPackage(AlarmKlaxon.this.getPackageName());
            AlarmKlaxon.this.sendBroadcast(intent);
            if (AlarmKlaxon.this.mCoopSensor != null && AlarmKlaxon.this.mCoopSensor.isRegister()) {
                AlarmKlaxon.this.mCoopSensor.clear();
            }
            if (AlarmKlaxon.this.mMotionManager != null) {
                AlarmKlaxon.this.mMotionManager.stopAlarmFlipMuteGestureListener();
            }
            ClockReporter.reportEventContainMessage(AlarmKlaxon.this.getApplicationContext(), 71, "flip 0", 0);
        }

        public void pickupReduce() {
            AlarmKlaxon.this.stopWeakenVL();
            Intent intent = new Intent("ACTION_TYPE_TYPE_PROXIMITY_WEAKEN");
            intent.setPackage(AlarmKlaxon.this.getPackageName());
            AlarmKlaxon.this.sendBroadcast(intent);
            AlarmKlaxon.this.startWeakenVL(AlarmKlaxon.this.getApplicationContext(), AlarmKlaxon.this.mSettingsVol);
            ClockReporter.reportEventContainMessage(AlarmKlaxon.this.getApplicationContext(), 72, "pick 0", 0);
        }
    };
    private MotionManager mMotionManager = null;
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String ignored) {
            if (state != 0 && state != AlarmKlaxon.this.mInitialCallState) {
                Log.iRelease("AlarmKlaxon", "mPhoneStateListener->onCallStateChanged : stop alarm by callstate change");
                HwLog.d("connection", "phone call comming, need to kill alarm");
                if (AlarmState.getInstance().getState() == 1) {
                    HwLog.d("connection", " AlarmKaxon phone call comming, kill alarm");
                    if (AlarmKlaxon.mCurrentAlarm != null) {
                        AlarmKlaxon.this.sendKillBroadcast(AlarmKlaxon.mCurrentAlarm);
                        HwLog.d("connection", "AlarmKaxon phone call comming to  kill alarm");
                        WearUtils.talkWithWatch(AlarmKlaxon.this, 1, AlarmKlaxon.mCurrentAlarm);
                        AlarmKlaxon.setsKlaxonState(1);
                    }
                    AlarmKlaxon.this.stopSelf();
                }
            }
        }
    };
    private MediaPlayer mPlayer;
    private boolean mPlaying = false;
    private boolean mScreenOn = false;
    private int mSettingsVol;
    private int mShutDownTime = (this.DEFAULT_ALARM_TIMEOUT * 1000);
    private long mStartTime;
    private BroadcastReceiver mSystemReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                Log.iRelease("AlarmKlaxon", "onReceive:" + intent.getAction());
                if (LockAlarmFullActivity.COVER_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                    if (!intent.getBooleanExtra(LockAlarmFullActivity.COVER_STATE, true)) {
                        ClockReporter.reportEventMessage(AlarmKlaxon.this.getApplicationContext(), 80, "");
                    } else {
                        return;
                    }
                }
                if ("android.intent.action.SCREEN_OFF".equals(intent.getAction())) {
                    AlarmKlaxon.this.screenOfftime = System.currentTimeMillis();
                    boolean isScreenOn = ((PowerManager) AlarmKlaxon.this.getSystemService("power")).isScreenOn();
                    Log.iRelease("AlarmKlaxon", "onReceive --> isScreenOn:" + isScreenOn);
                    if (!isScreenOn) {
                        AlarmKlaxon.this.screenOfftime = 0;
                        ClockReporter.reportEventMessage(context, 86, "");
                    } else {
                        return;
                    }
                }
                if ("android.intent.action.SCREEN_ON".equals(intent.getAction())) {
                    if (AlarmKlaxon.this.screenOfftime != 0 && System.currentTimeMillis() - AlarmKlaxon.this.screenOfftime < 5000) {
                        HwLog.i("AlarmKlaxon", "AlarmKlaxon receive screen on message");
                        if (AlarmState.getInstance().getState() == 1) {
                            HwLog.i("AlarmKlaxon", "AlarmKlaxon handle screen on message");
                            AlarmKlaxon.this.sendKillBroadcast(AlarmKlaxon.mCurrentAlarm);
                            HwLog.d("connection", "AlarmKaxon screen off and on  kill  alarm");
                            WearUtils.talkWithWatch(AlarmKlaxon.this, 1, AlarmKlaxon.mCurrentAlarm);
                            AlarmKlaxon.this.stopSelf();
                        }
                    }
                    return;
                }
                if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                    ((NotificationManager) context.getSystemService("notification")).cancel(AlarmKlaxon.mCurrentAlarm.id);
                }
                HwLog.i("AlarmKlaxon", "AlarmKlaxon receive user swich message and shutdown message");
                if (AlarmState.getInstance().getState() == 1) {
                    HwLog.i("AlarmKlaxon", "AlarmKlaxon handle user swich message and shutdown message");
                    AlarmKlaxon.this.sendKillBroadcast(AlarmKlaxon.mCurrentAlarm);
                    HwLog.d("connection", "AlarmKaxon receive shutdown message to  kill  alarm");
                    WearUtils.talkWithWatch(AlarmKlaxon.this, 1, AlarmKlaxon.mCurrentAlarm);
                    AlarmKlaxon.this.stopSelf();
                }
            }
        }
    };
    private TelephonyManager mTelephonyManager;
    private int mTmpFpSlideSwitch = 0;
    private AlarmIVRListener mVRListener = null;
    private Vibrator mVibrator;
    private WeakenVolume mWeakenVolume = null;
    private long screenOfftime = 0;

    class AlarmIVRListener extends Stub {
        private Context context;
        final /* synthetic */ AlarmKlaxon this$0;

        public void onModeChanged(boolean isVR) {
            Log.iRelease("AlarmKlaxon", "onModeChanged isVR:" + isVR);
            if (!isVR) {
                if (AlarmKlaxon.mCurrentAlarm != null) {
                    this.this$0.stop();
                    if (AlarmKlaxon.mCurrentAlarm.time == 0) {
                        AlarmKlaxon.mCurrentAlarm.time = Alarms.calculateAlarm(AlarmKlaxon.mCurrentAlarm);
                    }
                    Log.iRelease("AlarmKlaxon", "isvrmode false alarm.time:" + AlarmKlaxon.mCurrentAlarm.time);
                    Alarms.enableAlert(this.context, AlarmKlaxon.mCurrentAlarm, System.currentTimeMillis());
                    AlarmKlaxon.mCurrentAlarm = null;
                }
                Alarms.removeAllVRAlarm(this.context);
            } else if (AlarmKlaxon.mCurrentAlarm != null) {
                HwLog.d("connection", "AlarmKlaxon AlarmIVRListener onModeChanged");
                if (AlarmState.getInstance().getState() == 1) {
                    HwLog.d("connection", "AlarmKlaxon AlarmIVRListener onModeChanged handle");
                    Intent sleepAlarm = new Intent(this.context, AlarmReceiver.class);
                    sleepAlarm.setAction("headup_snoose");
                    sleepAlarm.putExtra("intent.extra.alarm", AlarmKlaxon.mCurrentAlarm);
                    this.this$0.sendBroadcast(sleepAlarm);
                }
            }
        }

        public void onPhoneStateChanged(int subId, int state, String incomingNumber) {
        }

        public void onNewSMS(Intent intent) {
        }

        public void onNewNotification(StatusBarNotification sbn) {
        }

        public void onBatteryChanged(Intent intent) {
        }

        public void onNetworkStateChanged(Intent intent) {
        }

        public void onHelmetChanged(boolean up) {
        }

        public void onHeartBeatChanged(boolean connected) {
        }
    }

    private class ServiceHandler extends Handler {
        private ServiceHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1000:
                    HwLog.i("connection", "AlarmKaxon receive KILLER message");
                    AlarmState alarmState = AlarmState.getInstance();
                    Alarm tempAlarm = msg.obj;
                    if (alarmState.getState() == 1 && tempAlarm.id == alarmState.getAlramID()) {
                        HwLog.d("connection", "AlarmKaxon handle KILLER message");
                        Log.iRelease("AlarmKlaxon", "mHandler->handleMessage : Alarm killer triggered");
                        AlarmKlaxon.this.sendKillBroadcast((Alarm) msg.obj);
                        HwLog.d("connection", " AlarmKaxon time arrive, auto to kill alarm");
                        WearUtils.talkWithWatch(AlarmKlaxon.this, 1, AlarmKlaxon.mCurrentAlarm);
                        AlarmKlaxon.this.stopSelf();
                        return;
                    }
                    return;
                case 1001:
                    AlarmKlaxon alarmKlaxon = AlarmKlaxon.this;
                    alarmKlaxon.mCurVolume = (float) (((double) alarmKlaxon.mCurVolume) + 0.02d);
                    if (AlarmKlaxon.this.mCurVolume < 1.0f) {
                        AlarmKlaxon.this.mHandler.sendEmptyMessageDelayed(1001, 500);
                    } else {
                        AlarmKlaxon.this.mCurVolume = 1.0f;
                    }
                    if (AlarmKlaxon.this.mPlayer != null) {
                        AlarmKlaxon.this.mPlayer.setVolume(AlarmKlaxon.this.mCurVolume, AlarmKlaxon.this.mCurVolume);
                        return;
                    } else {
                        AlarmKlaxon.this.mHandler.removeMessages(1001);
                        return;
                    }
                default:
                    return;
            }
        }
    }

    private void stopAlarmInService(Context context) {
        Intent intent1 = new Intent();
        intent1.setAction("com.android.deskclock.ALARM_CLOSE_NO_SNOOZE_ACTION");
        intent1.putExtra("intent.extra.alarm", mCurrentAlarm);
        intent1.setPackage(context.getPackageName());
        context.sendBroadcast(intent1);
    }

    public void onOtherAction(Context context, Intent intent) {
        if ("android.intent.action.timer_alert".equals(intent.getAction())) {
            HwLog.d("connection", "AlarmKlaxon receive timer alert message");
            if (AlarmState.getInstance().getState() == 1) {
                HwLog.d("connection", "AlarmKlaxon handle timer alert message");
                sendKillBroadcast(mCurrentAlarm);
                HwLog.d("connection", "AlarmKaxon timer kill alarm");
                WearUtils.talkWithWatch(this, 1, mCurrentAlarm);
                stopSelf();
            }
        }
        if ("com.huawei.systemmamanger.action.KILL_ROGAPP_STARTED".equals(intent.getAction())) {
            Log.iRelease("AlarmKlaxon", "KILL_APP_START_ACTION");
            HwLog.d("connection", "AlarmKlaxon receive kill app message");
            if (AlarmState.getInstance().getState() == 1) {
                HwLog.d("connection", "AlarmKlaxon handle kill app message");
                stopAlarmInService(context);
                HwLog.d("connection", "AlarmKaxon phone manager alarm");
                WearUtils.talkWithWatch(this, 2, mCurrentAlarm);
            }
        }
        if ("com.huawei.hwvdrive.action.SNOOZE_CLOCK".equals(intent.getAction()) && AlarmState.getInstance().getState() == 1) {
            sendKillBroadcast(mCurrentAlarm);
            WearUtils.talkWithWatch(this, 1, mCurrentAlarm);
            stopSelf();
        }
    }

    private void registerSystemReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(LockAlarmFullActivity.COVER_STATE_CHANGED_ACTION);
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.ACTION_SHUTDOWN");
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.USER_SWITCHED");
        registerReceiver(this.mSystemReceiver, filter);
    }

    private void startGesture() {
        if (this.mCoopSensor == null || !this.mCoopSensor.isCoop()) {
            beginGestureListener();
        } else {
            this.mCoopSensor.startListener(this, this.mMotionListener);
        }
    }

    private void beginGestureListener() {
        boolean z = true;
        if (this.mMotionManager == null) {
            this.mMotionManager = MotionManager.getInstance(this);
        } else {
            stopWeakenVL();
            this.mMotionManager.stopAlarmGestureListener();
        }
        int settingsVol = this.mAudioManager.getStreamVolume(4);
        if (this.mSettingsVol > 0) {
            settingsVol = this.mSettingsVol;
        }
        MotionManager motionManager = this.mMotionManager;
        MotionListener motionListener = this.mMotionListener;
        if (settingsVol <= 1) {
            z = false;
        }
        motionManager.startAlarmGestureListener(motionListener, z);
    }

    public void onCreate() {
        Log.iRelease("AlarmKlaxon", "onCreate");
        this.mAudioManager = (AudioManager) getSystemService("audio");
        this.mVibrator = (Vibrator) getSystemService("vibrator");
        this.mTelephonyManager = (TelephonyManager) getSystemService("phone");
        try {
            this.mTelephonyManager.listen(this.mPhoneStateListener, 32);
        } catch (Exception e) {
            Log.iRelease("AlarmKlaxon", "onCreate->PhoneStateListener faild");
            e.printStackTrace();
        }
        activeFingerPrintStopAlarm();
        IntentFilter filter = new IntentFilter();
        filter.addAction("ACTION_TYPE_TURNOVER_SILENT");
        filter.addAction("ACTION_TYPE_TYPE_PROXIMITY_WEAKEN");
        filter.addAction("com.android.server.input.fpn.stopalarm");
        filter.addAction("android.intent.action.timer_alert");
        filter.addAction("com.huawei.systemmamanger.action.KILL_ROGAPP_STARTED");
        filter.addAction("com.huawei.hwvdrive.action.SNOOZE_CLOCK");
        registerReceiver(this.broadcastReceiver, filter, "com.huawei.deskclock.broadcast.permission", null);
        registerSystemReceiver();
        if (Utils.VR_SWITCH) {
            ReflexUtil.registerVRListener(this, this.mVRListener);
        }
        AlarmAlertWakeLock.acquireCpuWakeLock(this);
        boolean isVisibleCoverWin = Global.getInt(DeskClockApplication.getDeskClockApplication().getContentResolver(), LockAlarmFullActivity.SETTINGS_COVER_TYPE, 0) == 0;
        CoverItemController controller = CoverItemController.getInstance(this);
        if (isVisibleCoverWin) {
            AlarmAlertWakeLock.acquireBrightScreenWakeLock(this);
            this.mScreenOn = true;
        } else if (controller.isCoverOpen()) {
            AlarmAlertWakeLock.acquireBrightScreenWakeLock(this);
            this.mScreenOn = true;
        }
        this.mInitialCallState = this.mTelephonyManager.getCallState();
        startGesture();
        super.onCreate();
    }

    public static int getCurrentZenMode(Context context) {
        try {
            return Global.getInt(context.getContentResolver(), "zen_mode");
        } catch (SettingNotFoundException e) {
            Log.w("AlarmKlaxon", "Get current zen mode fail.");
            return 0;
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mAudioManager.abandonAudioFocus(null) == 0) {
            Log.iRelease("AlarmKlaxon", "onDestroy : abandonAudioFocus failed");
        }
        this.mHandler.removeCallbacksAndMessages(null);
        Log.iRelease("AlarmKlaxon", "onDestroy");
        stop(false);
        deactiveFingerPrintStopAlarm();
        stopWeakenVL();
        if (this.mMotionManager != null) {
            this.mMotionManager.stopAlarmGestureListener();
        }
        if (this.mCoopSensor != null && this.mCoopSensor.isRegister()) {
            this.mCoopSensor.clear();
        }
        try {
            this.mTelephonyManager.listen(this.mPhoneStateListener, 0);
        } catch (Exception e) {
            Log.iRelease("AlarmKlaxon", "onDestroy->PhoneStateListener faild");
            e.printStackTrace();
        }
        unregisterReceiver(this.broadcastReceiver);
        unregisterReceiver(this.mSystemReceiver);
        if (Utils.VR_SWITCH) {
            Log.iRelease("AlarmKlaxon", "AlarmKlaxon onDestroy unregisterVRListener");
            ReflexUtil.unregisterVRListener(this, this.mVRListener);
        }
        AlarmAlertWakeLock.releaseCpuLock();
        if (this.mScreenOn) {
            AlarmAlertWakeLock.releaseBrightLock();
            this.mScreenOn = false;
        }
        if (mCurrentAlarm == null) {
            HwLog.d("connection", "onDetory mCurrentAlarm is null");
        } else {
            ConnectionConstants.print(mCurrentAlarm);
            AlarmState alarmState = AlarmState.getInstance();
            if (alarmState.getState() == 3 && alarmState.getAlramID() == mCurrentAlarm.id) {
                HwLog.d("connection", "AlarmKlaxon set alram state as idle");
                alarmState.setState(0);
            }
        }
        setCurrentAlarm(null);
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public static Alarm getCurrentAlarm() {
        return mCurrentAlarm;
    }

    public static void setCurrentAlarm(Alarm alarm) {
        mCurrentAlarm = alarm;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.iRelease("AlarmKlaxon", "onStartCommand");
        setsKlaxonState(0);
        if (intent == null || intent.getExtras() == null) {
            stopSelf();
            Log.iRelease("AlarmKlaxon", "onStartCommand : the intent is null, so stop self.");
            return 2;
        }
        Alarm alarm = (Alarm) intent.getParcelableExtra("intent.extra.alarm");
        alarm = Alarms.getAlarm(getContentResolver(), alarm != null ? alarm.id : -1);
        if (alarm == null) {
            Log.iRelease("AlarmKlaxon", "onStartCommand : AlarmKlaxon failed to parse the alarm from the intent");
            stopSelf();
            return 2;
        }
        if (mCurrentAlarm != null) {
            if (alarm.id == mCurrentAlarm.id) {
                Log.iRelease("AlarmKlaxon", "onStartCommand : do not play the same alarm");
                return 1;
            }
            Log.iRelease("AlarmKlaxon", "onStartCommand : kill current Alarm");
            HwLog.d("connection", "there are two alarms now,  want to kill the privous");
            if (AlarmState.getInstance().getState() == 1) {
                HwLog.d("connection", "there are two alarms now,  handle to kill the privous");
                sendKillBroadcast(mCurrentAlarm);
                WearUtils.talkWithWatch(this, 1, mCurrentAlarm);
            }
            startGesture();
        }
        this.mCurVolume = 0.0f;
        play(alarm);
        setCurrentAlarm(alarm);
        return 1;
    }

    private void sendKillBroadcast(Alarm alarm) {
        int minutes = (int) Math.round(((double) (System.currentTimeMillis() - this.mStartTime)) / 60000.0d);
        Intent alarmKilled = new Intent("alarm_killed");
        alarmKilled.putExtra("intent.extra.alarm", alarm);
        alarmKilled.putExtra("alarm_killed_timeout", minutes);
        alarmKilled.setPackage(getPackageName());
        sendOrderedBroadcast(alarmKilled, null);
    }

    private void rePlayAgain(Context context, Uri defaultUri) throws Exception {
        this.mPlayer = null;
        this.mPlayer = new MediaPlayer();
        try {
            this.mPlayer.setDataSource(context, defaultUri);
            startAlarm(this.mPlayer);
            this.mPlaying = true;
        } catch (IllegalArgumentException e) {
            Log.e("AlarmKlaxon", "rePlayAgain : IllegalArgumentException = " + e.getMessage());
            throw e;
        } catch (SecurityException e2) {
            Log.e("AlarmKlaxon", "rePlayAgain : SecurityException = " + e2.getMessage());
            throw e2;
        } catch (IllegalStateException e3) {
            Log.e("AlarmKlaxon", "rePlayAgain : IllegalStateException = " + e3.getMessage());
            throw e3;
        } catch (IOException e4) {
            Log.e("AlarmKlaxon", "rePlayAgain : IOException = " + e4.getMessage());
            throw e4;
        }
    }

    private void play(Alarm alarm) {
        Uri alert;
        stop();
        try {
            alert = RingtoneHelper.getDefaultAlarmRington(this, RingtoneHelper.getDefaultUri(alarm.alert));
            Log.iRelease("AlarmKlaxon", "alarm.alert = " + alarm.alert);
        } catch (NullPointerException e) {
            Log.e("AlarmKlaxon", "play : maybe you phone is not K3 or TI, it is mtk or other. exception = " + e.getMessage());
            alert = null;
        }
        Log.iRelease("AlarmKlaxon", "play : alarm.id = " + alarm.id + " alert Uri = " + alert);
        this.mSettingsVol = this.mAudioManager.getStreamVolume(4);
        int systemAlarmVolume = Utils.getDefaultSharedPreferences(this).getInt("systemAlarmVolume", 0);
        if (systemAlarmVolume > 0) {
            this.mSettingsVol = systemAlarmVolume;
        }
        this.DEFAULT_ALARM_TIMEOUT = Utils.getDefaultSharedPreferences(this).getInt("bell_duration", 5) * 60;
        this.mShutDownTime = (this.DEFAULT_ALARM_TIMEOUT * 1000) + 1000;
        if (!(alert == null || alert.equals(Uri.parse("silent")))) {
            if (this.mAudioManager.requestAudioFocus(null, 4, 2) == 0) {
                Log.iRelease("AlarmKlaxon", "play : requestAudioFocus fail");
            }
            this.mPlayer = new MediaPlayer();
            this.mPlayer.setOnErrorListener(new OnErrorListener() {
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.iRelease("AlarmKlaxon", "play : Error occurred while playing audio. what = " + what + " extra = " + extra);
                    mp.stop();
                    mp.release();
                    try {
                        AlarmKlaxon.this.rePlayAgain(AlarmKlaxon.this, RingtoneHelper.getAvailableRingtone(AlarmKlaxon.this, Uri.parse("content://settings/system/alarm_alert")));
                    } catch (Exception e) {
                        AlarmKlaxon.this.mPlaying = false;
                    }
                    return true;
                }
            });
            try {
                int callState = this.mTelephonyManager.getCallState();
                Log.dRelease("AlarmKlaxon", "play : MediaPlayer has no error mTelephonyManager.getCallState() = " + callState);
                if (callState != 0) {
                    Log.iRelease("AlarmKlaxon", "play : Using the in-call alarm. mCurVolume = " + this.mCurVolume + " mSettingsVol = " + this.mSettingsVol);
                    this.mCurVolume = (float) this.mSettingsVol;
                    setDataSourceFromResource(getResources(), this.mPlayer, R.raw.in_call_alarm);
                    this.mHandler.postDelayed(new TimerTask() {
                        public void run() {
                            if (AlarmKlaxon.this.mPlayer != null && AlarmKlaxon.this.mPlayer.isPlaying()) {
                                AlarmKlaxon.this.mPlayer.stop();
                            }
                        }
                    }, 2000);
                } else {
                    this.mPlayer.setDataSource(this, alert);
                }
                startAlarm(this.mPlayer);
            } catch (Exception ex) {
                Log.e("AlarmKlaxon", "play : Using the fallback ringtone. IOException = " + ex.getMessage());
                if (RingCache.getInstance().isUserUnlocked(this) || !RingCache.getInstance().isSdCardRing(alarm.alert)) {
                    HwLog.i("AlarmKlaxon", "user Unlock mode");
                    usingDefualtRingtone(alarm, alert);
                } else {
                    HwLog.i("AlarmKlaxon", "user locked mode,play cache ring");
                    useRingCache(alarm, alert);
                }
            }
        }
        if (2 != getCurrentZenMode(this)) {
            startVibrator(alarm);
        } else {
            this.mVibrator = null;
        }
        enableKiller(alarm);
        this.mPlaying = true;
        this.mStartTime = System.currentTimeMillis();
    }

    private void usingDefualtRingtone(Alarm alarm, Uri alert) {
        try {
            String alertPath;
            this.mPlayer.reset();
            Uri defaultUri = null;
            if (alarm.alert == null) {
                alertPath = "";
            } else {
                alertPath = alarm.alert.toString();
            }
            if (TextUtils.isEmpty(alertPath) || "content://settings/system/alarm_alert".equals(alertPath)) {
                defaultUri = RingtoneManager.getDefaultUri(4);
            }
            if (defaultUri == null) {
                defaultUri = RingtoneHelper.getAvailableRingtone(this, RingtoneHelper.getUriByPath(this, alarm.alert));
            }
            this.mPlayer.setDataSource(this, defaultUri);
            startAlarm(this.mPlayer);
            this.mPlaying = true;
        } catch (Exception e) {
            fixException(alarm, alert);
        }
    }

    private void useRingCache(Alarm alarm, Uri alert) {
        HwLog.i("AlarmKlaxon", "useRingCache");
        try {
            this.mPlayer.reset();
            this.mPlayer.setDataSource(this, Uri.fromFile(new File(RingCache.getInstance().getCacheFilePath(this, alarm.alert == null ? "" : alarm.alert.toString()))));
            startAlarm(this.mPlayer);
            this.mPlaying = true;
        } catch (Exception e) {
            HwLog.e("AlarmKlaxon", "useRingCache " + e);
            usingDefualtRingtone(alarm, alert);
        }
    }

    private void fixException(Alarm alarm, Uri alert) {
        isDRMRingtone(alarm.id, alert);
        try {
            Log.dRelease("AlarmKlaxon", "play : Must reset the media player to clear the error state");
            if (this.mPlayer != null) {
                this.mPlayer.reset();
                setDataSourceFromResource(getResources(), this.mPlayer, R.raw.fallbackring);
                startAlarm(this.mPlayer);
                this.mShutDownTime = maxTime(this.mPlayer.getDuration(), this.DEFAULT_ALARM_TIMEOUT * 1000);
                return;
            }
            Log.w("AlarmKlaxon", "play : the mediaplayer is null.");
        } catch (Exception ex2) {
            Log.e("AlarmKlaxon", "play : Exception = " + ex2.getMessage());
            if (this.mPlayer != null) {
                this.mPlayer.reset();
                this.mPlayer.release();
                this.mPlayer = null;
                this.mPlaying = false;
            }
        }
    }

    private void startVibrator(Alarm alarm) {
        if (!alarm.vibrate || (Utils.VR_SWITCH && ReflexUtil.isVRMode())) {
            this.mVibrator.cancel();
        } else if (this.mTelephonyManager.getCallState() != 0) {
            this.mVibrator.vibrate((long) RING_OFFHOOK_VB);
        } else if (this.mHwCustAlarmKlaxon == null || !this.mHwCustAlarmKlaxon.vibrate(this, this.mVibrator)) {
            this.mVibrator.vibrate(sVibratePattern, 0);
        }
    }

    private void isDRMRingtone(int alarmId, Uri alert) {
        if (CompatUtils.hasPermission(this, "android.permission.READ_EXTERNAL_STORAGE")) {
            Log.dRelease("AlarmKlaxon", "isDRMRingtone : DrmUtils.DRM_ENABLED = " + DrmUtils.DRM_ENABLED);
            if (DrmUtils.DRM_ENABLED) {
                String[] proj = new String[]{"_data"};
                Cursor cursor = getContentResolver().query(alert, proj, null, null, null);
                String str = null;
                if (cursor != null) {
                    try {
                        int column_index = cursor.getColumnIndexOrThrow(proj[0]);
                        if (cursor.moveToFirst()) {
                            str = cursor.getString(column_index);
                            DrmUtils.initialize(this);
                            boolean isDrm = DrmUtils.isDrmFile(str);
                            Log.dRelease("AlarmKlaxon", "isDRMRingtone : isDrm = " + isDrm);
                            if (isDrm && !DrmUtils.haveRightsForAction(str, 1)) {
                                Alarms.updateAlarmRingtone(this, RingtoneManager.getDefaultUri(4), alarmId);
                            }
                        }
                        cursor.close();
                    } catch (Throwable th) {
                        cursor.close();
                    }
                }
                Log.dRelease("AlarmKlaxon", "isDRMRingtone : filePath = " + str);
            }
            return;
        }
        Log.iRelease("AlarmKlaxon", "isDRMRingtone->has no READ_EXTERNAL_STORAGE permissions");
    }

    private void startAlarm(MediaPlayer player) throws Exception {
        try {
            Log.d("AlarmKlaxon", "startAlarm : play the alarm audio");
            player.setAudioStreamType(4);
            player.setLooping(true);
            player.setVolume(0.0f, 0.0f);
            player.prepare();
            player.start();
            this.mHandler.sendEmptyMessage(1001);
        } catch (IOException e) {
            throw e;
        } catch (IllegalArgumentException e2) {
            throw e2;
        } catch (IllegalStateException e3) {
            throw e3;
        }
    }

    private void setDataSourceFromResource(Resources resources, MediaPlayer player, int res) throws IOException {
        AssetFileDescriptor afd = resources.openRawResourceFd(res);
        if (afd != null) {
            try {
                player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            } finally {
                afd.close();
            }
        }
    }

    public void stop() {
        stop(true);
    }

    public void stop(boolean isReset) {
        Log.dRelease("AlarmKlaxon", "stop : mPlaying = " + this.mPlaying);
        if (this.mPlaying) {
            this.mPlaying = false;
            if (this.mPlayer != null) {
                if (this.mPlayer.isPlaying()) {
                    this.mPlayer.stop();
                    if (isReset) {
                        this.mPlayer.reset();
                    }
                }
                this.mPlayer.release();
                this.mPlayer = null;
            }
            if (!this.mAudioManager.isStreamMute(4)) {
                this.mAudioManager.setStreamVolume(4, this.mSettingsVol, 0);
            }
            Editor editor = Utils.getDefaultSharedPreferences(this).edit();
            editor.remove("systemAlarmVolume");
            editor.commit();
        }
        if (this.mVibrator != null) {
            this.mVibrator.cancel();
        }
        disableKiller();
    }

    protected int maxTime(int time1, int time2) {
        return time1 > time2 ? time1 : time2;
    }

    private void enableKiller(Alarm alarm) {
        int autoSnoozeMseconds = this.mShutDownTime;
        Log.iRelease("AlarmKlaxon", "enableKiller : send message delayed = " + autoSnoozeMseconds);
        if (autoSnoozeMseconds != -1) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1000, alarm), (long) autoSnoozeMseconds);
        }
    }

    private void disableKiller() {
        Log.d("AlarmKlaxon", "disableKiller : remove message.");
        this.mHandler.removeMessages(1000);
    }

    public static int getsKlaxonState() {
        return sKlaxonState;
    }

    public static void setsKlaxonState(int sKlaxonState) {
        sKlaxonState = sKlaxonState;
    }

    private void activeFingerPrintStopAlarm() {
        try {
            this.mTmpFpSlideSwitch = Secure.getInt(getContentResolver(), "fingerprint_slide_switch", 0);
            Secure.putInt(getContentResolver(), "fingerprint_slide_switch", 1);
        } catch (IllegalArgumentException e) {
            Log.e("AlarmKlaxon", "Settings operation exception");
        } catch (Exception e2) {
            Log.e("AlarmKlaxon", "Settings operation exception");
        }
    }

    private void deactiveFingerPrintStopAlarm() {
        try {
            Secure.putInt(getContentResolver(), "fingerprint_slide_switch", this.mTmpFpSlideSwitch);
            this.mTmpFpSlideSwitch = 0;
        } catch (IllegalArgumentException e) {
            Log.e("AlarmKlaxon", "Settings operation exception");
        } catch (Exception e2) {
            Log.e("AlarmKlaxon", "Settings operation exception");
        }
    }

    private void startWeakenVL(Context context, int systemVolume) {
        this.mWeakenVolume = new WeakenVolume(context, systemVolume, 1, this.mMotionManager);
        this.mWeakenVolume.setRun(true);
        this.mWeakenVolume.setName("AlarmWeakenVolume");
        this.mWeakenVolume.start();
    }

    private void stopWeakenVL() {
        if (this.mWeakenVolume != null) {
            this.mWeakenVolume.setRun(false);
            this.mWeakenVolume.stopThread();
            this.mWeakenVolume = null;
        }
    }

    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        NotificationManager nm = (NotificationManager) getSystemService("notification");
        if (mCurrentAlarm != null) {
            nm.cancel(mCurrentAlarm.id);
        }
    }
}
