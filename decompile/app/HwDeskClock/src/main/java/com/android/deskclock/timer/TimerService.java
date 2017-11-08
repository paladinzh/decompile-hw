package com.android.deskclock.timer;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlarmManager;
import android.app.Notification.Builder;
import android.app.Notification.DecoratedCustomViewStyle;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import com.android.deskclock.AlarmAlertWakeLock;
import com.android.deskclock.AlarmsMainActivity;
import com.android.deskclock.R;
import com.android.deskclock.RingtoneHelper;
import com.android.util.ClockReporter;
import com.android.util.CompatUtils;
import com.android.util.DrmUtils;
import com.android.util.HwLog;
import com.android.util.Log;
import com.android.util.Utils;
import java.io.IOException;
import java.util.List;
import java.util.TimerTask;

public class TimerService extends Service {
    private static final String[] PROJECTION = new String[]{"_id"};
    private static final String[] SELECTIONARGS = new String[]{"Timer_Beep.ogg"};
    static boolean serviceRunning = false;
    private static int volume = 0;
    public String defaultRingtone;
    private boolean flipMute = false;
    private Intent intent;
    private boolean isRun = true;
    private AlarmManager mAlarmManager;
    private PendingIntent mAlertIntent;
    private OnAudioFocusChangeListener mAudioFocusChangeListener = new OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case -2:
                case -1:
                    Log.i("TimerService", "onAudioFocusChange : AUDIOFOCUS_LOSS | AUDIOFOCUS_LOSS_TRANSIENT");
                    if (2 == ((AudioManager) TimerService.this.getSystemService("audio")).getRingerMode()) {
                        TimerService.this.stopPlayer(false);
                        return;
                    }
                    return;
                case 1:
                case 2:
                    Log.i("TimerService", "onAudioFocusChange:AUDIOFOCUS_GAIN | AUDIOFOCUS_GAIN_TRANSIENT");
                    TimerService.this.flipMute = false;
                    TimerService.this.startPlay();
                    return;
                default:
                    return;
            }
        }
    };
    private AudioManager mAudioMgr = null;
    private long mBeginTime = 0;
    private boolean mCallState = false;
    private Context mContext;
    private long mCurLeaveTime = 0;
    private Editor mEditor;
    private long mElapsedTime = 0;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 213:
                    Log.i("TimerService", "handleMessage : TIME_UP");
                    TimerService.this.startDeskClockFromForeGround();
                    TimerService.this.mEditor.putInt("state", 3);
                    TimerService.this.mEditor.commit();
                    TimerPage.setIsFromCTS(false);
                    return;
                default:
                    return;
            }
        }
    };
    private long mLeaveTime = 0;
    private MediaPlayer mMediaPlayer;
    private NotificationManager mNotificationManager;
    private int mPoolId;
    private long mPreStartTime = 0;
    private SharedPreferences mPreferences;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                Log.w("TimerService", "the intent is null or the action is null.");
                return;
            }
            String action = intent.getAction();
            Log.i("TimerService", "onReceive : action = " + action);
            if ("action_type_timer_silent".equals(action)) {
                TimerService.this.pauseMedia();
                if (TimerService.this.mAudioMgr != null) {
                    TimerService.this.mAudioMgr.abandonAudioFocus(TimerService.this.mAudioFocusChangeListener);
                }
            } else if ("android.intent.action.timer_resume".equals(action)) {
                TimerService.this.mHandler.removeCallbacks(TimerService.this.runnable);
                TimerService.this.mHandler.post(TimerService.this.runnable);
            } else if ("android.intent.action.timer_pause".equals(action)) {
                long leaveTime = TimerService.this.mLeaveTime - (SystemClock.elapsedRealtime() - TimerService.this.mBeginTime);
                if (leaveTime > 5000) {
                    Log.i("TimerService", "onReceive : removeCallbacks by ACTION_TIMER_PAUSE leaveTime = " + leaveTime);
                    TimerService.this.mHandler.removeCallbacks(TimerService.this.runnable);
                }
            }
        }
    };
    private boolean mServiceStop = false;
    private boolean mShowNotify = false;
    private SoundPool mSoundPool;
    private long mStartTime = 0;
    private int mState;
    private HwPhoneStateListener mStateListener;
    private int mStreamId;
    private BroadcastReceiver mSystemReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                Log.w("TimerService", "the intent is null or the action is null.");
                return;
            }
            String action = intent.getAction();
            if ("android.intent.action.ACTION_SHUTDOWN".equals(action) || "android.intent.action.REBOOT".equals(action)) {
                TimerService.this.clearPreference();
            }
            if ("com.huawei.systemmamanger.action.KILL_ROGAPP_STARTED".equals(action)) {
                TimerService.this.setRogChangeFlag();
            }
        }
    };
    private TelephonyManager mTelephonyManager;
    private long mTotalTime = 0;
    private Uri mUri;
    private Vibrator mVibrator;
    private WakeLock mWakeLock;
    private Runnable runnable = new Runnable() {
        public void run() {
            long leaveTime = TimerService.this.mLeaveTime - (SystemClock.elapsedRealtime() - TimerService.this.mBeginTime);
            TimerService.this.mHandler.postDelayed(TimerService.this.runnable, leaveTime > 0 ? 60 : leaveTime - (((leaveTime / 1000) - 1) * 1000));
            if (leaveTime <= 0) {
                leaveTime = (leaveTime / 1000) * 1000;
                if (TimerService.this.isRun) {
                    TimerService.this.wakeLockForce();
                    AlarmAlertWakeLock.releaseCpuLock();
                    TimerService.this.mHandler.sendEmptyMessageDelayed(213, 0);
                    TimerService.this.isRun = false;
                } else if (TimerService.this.mCurLeaveTime == leaveTime) {
                    return;
                }
                TimerService.this.timeoutStopService((-1 * leaveTime) / 1000);
            }
            TimerService.this.mCurLeaveTime = leaveTime;
            TimerService.this.intent.putExtra("leaveTime", TimerService.this.mCurLeaveTime);
            TimerService.this.sendBroadcast(TimerService.this.intent);
        }
    };
    private Runnable updateNotifyRunnable = new Runnable() {
        public void run() {
            TimerService.this.blinkNotification();
            TimerService.this.mHandler.postDelayed(this, 10000);
        }
    };

    public class HwPhoneStateListener extends PhoneStateListener {
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case 0:
                    TimerService.this.mCallState = false;
                    return;
                case 1:
                case 2:
                    TimerService.this.mCallState = true;
                    return;
                default:
                    return;
            }
        }
    }

    public void onCreate() {
        Log.d("TimerService", "onCreate");
        this.mVibrator = (Vibrator) getSystemService("vibrator");
        this.mAlarmManager = (AlarmManager) getSystemService("alarm");
        this.mPreferences = Utils.getSharedPreferences(this, "timer", 0);
        this.mEditor = this.mPreferences.edit();
        this.mEditor.putBoolean("stopForce", false);
        this.mEditor.commit();
        setServiceRunning(true);
        this.intent = new Intent("com.android.deskclock.timer");
        this.intent.setPackage(getPackageName());
        registerReceiver();
        registerSysReceiver();
        this.mContext = this;
        if (this.mStateListener == null) {
            this.mStateListener = new HwPhoneStateListener();
        }
        if (this.mTelephonyManager == null) {
            this.mTelephonyManager = (TelephonyManager) getSystemService("phone");
        }
        try {
            this.mTelephonyManager.listen(this.mStateListener, 32);
        } catch (Exception e) {
            Log.iRelease("TimerService", "onCreate->PhoneStateListener faild");
            e.printStackTrace();
        }
        super.onCreate();
        this.mNotificationManager = (NotificationManager) getSystemService("notification");
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("action_type_timer_silent");
        filter.addAction("android.intent.action.timer_pause");
        filter.addAction("android.intent.action.timer_resume");
        registerReceiver(this.mReceiver, filter, "com.huawei.deskclock.broadcast.permission", null);
    }

    public static void setServiceRunning(boolean serviceRunning) {
        serviceRunning = serviceRunning;
    }

    private void wakeLockForce() {
        if (this.mWakeLock == null) {
            this.mWakeLock = ((PowerManager) getSystemService("power")).newWakeLock(805306394, "TimerService");
            this.mWakeLock.acquire();
        }
    }

    public void blinkNotification() {
        if (this.mState == 1 && this.mShowNotify) {
            setNotification(updateNotificationTime(this.mTotalTime - (Utils.getTimeNow() - this.mBeginTime)), true);
        }
    }

    public void timeoutStopService(long timeout) {
        if (timeout > 10) {
            HwLog.i("TimerService", "timeoutStopService timeout");
            if (!isTopActivity(".timer.TimerAlertActivity")) {
                HwLog.i("TimerService", "!isTopActivity");
                if (!this.mServiceStop) {
                    this.mServiceStop = true;
                    HwLog.i("TimerService", "timeoutStopService timeout to releaseWakeLock");
                    releaseWakeLock();
                }
            }
        }
    }

    private boolean isTopActivity(String activityName) {
        List<RunningTaskInfo> tasksInfo = ((ActivityManager) getSystemService("activity")).getRunningTasks(1);
        return tasksInfo != null && tasksInfo.size() > 0 && activityName.equals(((RunningTaskInfo) tasksInfo.get(0)).topActivity.getShortClassName());
    }

    public void startDeskClockFromForeGround() {
        if (Utils.isDeskClockForeground()) {
            HwLog.i("TimerService", "startDeskClockFromForeGround startDeskClock");
            startDeskClock();
        }
    }

    public void startDeskClockFromBackGround() {
        if (!Utils.isDeskClockForeground()) {
            HwLog.i("TimerService", "startDeskClockFromBackGround startDeskClock");
            startDeskClock();
        }
    }

    private void startDeskClock() {
        Editor mEditor = Utils.getDefaultSharedPreferences(this).edit();
        mEditor.putInt("currentTab", 3);
        mEditor.commit();
        Intent intent = new Intent(this, TimerAlertActivity.class);
        intent.setFlags(268435456);
        startActivity(intent);
        stopPool();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("TimerService", "onStartCommand");
        if (intent == null || intent.getAction() == null) {
            return 2;
        }
        String action = intent.getAction();
        Log.i("TimerService", "onStartCommand : action = " + action);
        long actionTime = intent.getLongExtra("action_time", Utils.getTimeNow());
        boolean showNotif = intent.getBooleanExtra("show_notify", true);
        getDefaultRingtone();
        int reportId = intent.getIntExtra("report_id", 0);
        if (reportId != 0) {
            ClockReporter.reportEventMessage(this.mContext, reportId, "");
        }
        if ("timer.action.start".equals(action)) {
            HwLog.d("timer_notify", "TimerService  onStartCommand ACTION_START");
            if (reportId == 0) {
                ClockReporter.reportEventMessage(this.mContext, 93, "");
            }
            this.mState = this.mPreferences.getInt("state", 3);
            if (this.mState == 3) {
                HwLog.d("timer_notify", "TimerService  onStartCommand ACTION_START stop service");
                stopSelf();
                return super.onStartCommand(intent, flags, startId);
            } else if (this.mState == 1) {
                return super.onStartCommand(intent, flags, startId);
            } else {
                this.mStartTime = actionTime;
                this.mTotalTime = this.mPreferences.getLong("leaveTime", 0);
                this.mPreStartTime = this.mPreferences.getLong("beginTime", this.mBeginTime);
                if (this.mPreStartTime == -1) {
                    this.mElapsedTime = 0;
                } else {
                    this.mElapsedTime = this.mStartTime - this.mPreStartTime;
                }
                this.mTotalTime -= this.mElapsedTime;
                this.mState = 1;
                HwLog.d("timer_notify", "mTotalTime = " + this.mTotalTime + ", mStartTime = " + this.mStartTime + ", showNotif = " + showNotif);
                if (showNotif) {
                    setNotification(updateNotificationTime(this.mTotalTime), true);
                    this.mHandler.removeCallbacks(this.updateNotifyRunnable);
                    this.mHandler.postDelayed(this.updateNotifyRunnable, 10000);
                }
                saveNotification(this.mStartTime, this.mTotalTime);
                this.mLeaveTime = this.mTotalTime;
                this.mBeginTime = this.mStartTime;
                this.mHandler.removeCallbacks(this.runnable);
                this.mHandler.post(this.runnable);
                this.mAlertIntent = PendingIntent.getBroadcast(this, 0, new Intent("android.intent.action.timer_alert"), 134217728);
                if (Utils.isKitKatOrLater()) {
                    this.mAlarmManager.setExact(2, this.mStartTime + this.mTotalTime, this.mAlertIntent);
                } else {
                    this.mAlarmManager.set(2, this.mStartTime + this.mTotalTime, this.mAlertIntent);
                }
            }
        } else if ("timer.action.pause".equals(action)) {
            HwLog.d("timer_notify", "TimerService  onStartCommand ACTION_PAUSE");
            if (reportId == 0) {
                ClockReporter.reportEventMessage(this.mContext, 94, "");
            }
            if (this.mState == 2) {
                return super.onStartCommand(intent, flags, startId);
            }
            this.mHandler.removeCallbacks(this.updateNotifyRunnable);
            this.mState = 2;
            this.mStartTime = this.mPreferences.getLong("beginTime", 0);
            this.mTotalTime = this.mPreferences.getLong("leaveTime", 0);
            if (this.mStartTime != -1) {
                this.mElapsedTime = Utils.getTimeNow() - this.mStartTime;
            } else {
                this.mElapsedTime = 0;
            }
            this.mTotalTime -= this.mElapsedTime;
            if (showNotif) {
                setNotification(updateNotificationTime(this.mTotalTime), false);
            }
            this.mHandler.removeCallbacks(this.runnable);
            saveNotification(-1, this.mTotalTime);
            disableTimerAlert();
        } else if ("timer.action.reset".equals(action)) {
            HwLog.d("timer_notify", "TimerService  onStartCommand ACTION_RESET");
            this.mHandler.removeCallbacks(this.updateNotifyRunnable);
            clearSavedNotification();
            stopSelf();
        } else if ("kill_nofity".equals(action)) {
            HwLog.d("timer_notify", "TimerService  onStartCommand KILL_NOTIF");
            this.mShowNotify = false;
            this.mHandler.removeCallbacks(this.updateNotifyRunnable);
            this.mNotificationManager.cancel(2147483645);
        } else if ("show_notify".equals(action)) {
            HwLog.d("timer_notify", "TimerService  onStartCommand SHOW_NOTIF");
            if (showSavedNotification()) {
                this.mShowNotify = true;
            } else {
                HwLog.d("timer_notify", "TimerService  onStartCommand SHOW_NOTIF  stop service");
                stopSelf();
                return super.onStartCommand(intent, flags, startId);
            }
        } else if ("android.intent.action.timer_alert".equals(action) || "com.android.timerservice.resume".equals(action)) {
            this.mBeginTime = this.mPreferences.getLong("beginTime", 0);
            this.mLeaveTime = this.mPreferences.getLong("leaveTime", 0);
            Log.i("TimerService", "onStartCommand : mBeginTime = " + this.mBeginTime + "  mLeaveTime = " + this.mLeaveTime);
            HwLog.d("timer_notify", "TimerService  onStartCommand ACTION_TIMER_SERVICE_RESUME mBeginTime = " + this.mBeginTime + "  mLeaveTime = " + this.mLeaveTime);
            this.mHandler.removeCallbacks(this.runnable);
            this.mHandler.post(this.runnable);
            if ("android.intent.action.timer_alert".equals(action)) {
                startDeskClockFromBackGround();
                HwLog.d("timer_notify", "TimerService  onStartCommand ACTION_TIMER_ALERT");
                this.mHandler.removeCallbacks(this.updateNotifyRunnable);
                clearSavedNotification();
                this.mNotificationManager.cancel(2147483645);
            }
        } else if ("com.android.timerservice.start".equals(action)) {
            this.isRun = true;
            this.mBeginTime = SystemClock.elapsedRealtime();
            this.mLeaveTime = this.mPreferences.getLong("leaveTime", 0);
            Log.i("TimerService", "onStartCommand : mBeginTime = " + this.mBeginTime + "  mLeaveTime = " + this.mLeaveTime);
            this.mAlertIntent = PendingIntent.getBroadcast(this, 0, new Intent("android.intent.action.timer_alert"), 134217728);
            if (Utils.isKitKatOrLater()) {
                this.mAlarmManager.setExact(2, this.mBeginTime + this.mLeaveTime, this.mAlertIntent);
            } else {
                this.mAlarmManager.set(2, this.mBeginTime + this.mLeaveTime, this.mAlertIntent);
            }
            Log.i("TimerService", "onStartCommand : AlarmManager set timer");
            this.mEditor.putLong("beginTime", this.mBeginTime);
            this.mEditor.commit();
            this.mHandler.removeCallbacksAndMessages(null);
            this.mHandler.post(this.runnable);
            loadSoundPool();
        } else if ("com.android.timerservice.startplay".equals(action)) {
            Log.d("TimerService", "onStartCommand : start play mediaplayer");
            startPlay();
        } else if ("com.android.timerservice.stoppaly".equals(action)) {
            Log.d("TimerService", "onStartCommand : stop play mediaplayer");
            stopPlayer(intent.getBooleanExtra("service_stopself", true));
        } else if ("com.deskclock.timer.soundpool.pause".equals(action)) {
            pausePool();
        } else if ("com.deskclock.timer.soundpool.resume".equals(action)) {
            resumePool();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void releaseWakeLock() {
        if (this.mWakeLock != null && this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
            this.mWakeLock = null;
        }
    }

    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d("TimerService", "onTaskRemoved");
        this.mEditor.putBoolean("stopForce", true);
        this.mEditor.commit();
        this.mShowNotify = false;
        this.mHandler.removeCallbacks(this.updateNotifyRunnable);
        this.mNotificationManager.cancel(2147483645);
    }

    private void disableTimerAlert() {
        ((AlarmManager) getSystemService("alarm")).cancel(PendingIntent.getBroadcast(this, 0, new Intent("android.intent.action.timer_alert"), 134217728));
        Log.i("TimerService", "onDestroy->disableTimerAlert : AlarmManager cancel timer");
    }

    public void onDestroy() {
        Log.d("TimerService", "onDestroy");
        disableTimerAlert();
        if (this.mAudioMgr != null) {
            this.mAudioMgr.abandonAudioFocus(this.mAudioFocusChangeListener);
            this.mAudioMgr = null;
        }
        this.mEditor.putBoolean("stopForce", true);
        this.mEditor.commit();
        releaseWakeLock();
        AlarmAlertWakeLock.releaseCpuLock();
        this.mHandler.removeCallbacksAndMessages(null);
        unregisterReceiver(this.mReceiver);
        unregisterReceiver(this.mSystemReceiver);
        if (this.mTelephonyManager != null) {
            try {
                this.mTelephonyManager.listen(this.mStateListener, 0);
            } catch (Exception e) {
                Log.iRelease("TimerService", "onDestroy->PhoneStateListener faild");
                e.printStackTrace();
            }
        }
        stopPool();
        releasePool();
        super.onDestroy();
    }

    private void setRogChangeFlag() {
        this.mEditor.putInt("rogchange", 1);
        this.mEditor.commit();
    }

    private void clearPreference() {
        this.mEditor.remove("beginTime");
        this.mEditor.remove("leaveTime");
        this.mEditor.remove("pauseTime");
        this.mEditor.remove("state");
        this.mEditor.remove("time");
        this.mEditor.remove("picked_time");
        this.mEditor.commit();
    }

    private void pauseMedia() {
        android.util.Log.e("shuhongbing", "pauseMedia");
        if (this.mMediaPlayer != null && this.mMediaPlayer.isPlaying()) {
            android.util.Log.e("shuhongbing", "mMediaPlayer.pause()");
            this.mMediaPlayer.pause();
        }
        if (this.mVibrator != null) {
            android.util.Log.e("shuhongbing", "mVibrator.cancel();");
            this.mVibrator.cancel();
        }
    }

    private void registerSysReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.ACTION_SHUTDOWN");
        filter.addAction("android.intent.action.REBOOT");
        filter.addAction("com.huawei.systemmamanger.action.KILL_ROGAPP_STARTED");
        registerReceiver(this.mSystemReceiver, filter);
    }

    public String updateNotificationTime(long time) {
        String notificationInfo;
        long days = 0;
        Resources res = getResources();
        long minutes = (time / 1000) / 60;
        long hours = minutes / 60;
        minutes -= 60 * hours;
        if (hours >= 24) {
            days = hours / 24;
            hours -= 24 * days;
        }
        HwLog.d("timer_notify", "updateNotificationTime time = " + time + ", days = " + days + ", hours = " + hours + ", minutes =" + minutes);
        String hour;
        if (days > 0) {
            String day = res.getQuantityString(R.plurals.timer_days_format, (int) days, new Object[]{Integer.valueOf((int) days)});
            if (hours > 0) {
                hour = res.getQuantityString(R.plurals.timer_hours_format, (int) hours, new Object[]{Integer.valueOf((int) hours)});
                notificationInfo = res.getString(R.string.timer_notify_string_token_two, new Object[]{day, hour});
            } else {
                notificationInfo = day;
                notificationInfo = res.getString(R.string.timer_notify_string_token, new Object[]{day});
            }
        } else if (hours > 0) {
            hour = res.getQuantityString(R.plurals.timer_hours_format, (int) hours, new Object[]{Integer.valueOf((int) hours)});
            if (minutes > 0) {
                String minute = res.getQuantityString(R.plurals.timer_minutes_format, (int) minutes, new Object[]{Integer.valueOf((int) minutes)});
                notificationInfo = res.getString(R.string.timer_notify_string_token_two, new Object[]{hour, minute});
            } else {
                notificationInfo = hour;
                notificationInfo = res.getString(R.string.timer_notify_string_token, new Object[]{hour});
            }
        } else if (minutes > 0) {
            notificationInfo = res.getQuantityString(R.plurals.timer_minutes_format, (int) minutes, new Object[]{Integer.valueOf((int) minutes)});
            notificationInfo = res.getString(R.string.timer_notify_string_token, new Object[]{res.getQuantityString(R.plurals.timer_minutes_format, (int) minutes, new Object[]{Integer.valueOf((int) minutes)})});
        } else {
            notificationInfo = res.getString(R.string.less_format, new Object[]{Integer.valueOf(1)});
        }
        HwLog.d("timer_notify", "updateNotificationTime," + notificationInfo);
        return notificationInfo;
    }

    public void setNotification(String messge, boolean timerRunning) {
        HwLog.d("timer_notify", "TimerService setNotification");
        Context context = getApplicationContext();
        Resources res = context.getResources();
        Builder builder = new Builder(context).setVisibility(0).setContentTitle(res.getString(R.string.timer_title_new)).setSmallIcon(Utils.getBitampIcon(context, R.drawable.ic_notify_timer)).setContentText(messge).setStyle(new DecoratedCustomViewStyle());
        builder.setContentIntent(getToTimerPageIntent());
        if (timerRunning) {
            builder.addAction(0, res.getString(R.string.notify_pause), getPausePendingIntent());
        } else {
            builder.addAction(0, res.getString(R.string.notify_start), getStartPendingIntent());
        }
        this.mNotificationManager.notify(2147483645, builder.build());
    }

    private void saveNotification(long startTime, long leftTime) {
        HwLog.d("timer_notify", "TimerService saveNotification");
        Editor editor = this.mPreferences.edit();
        editor.putLong("beginTime", startTime);
        editor.putLong("leaveTime", leftTime);
        editor.putInt("state", this.mState);
        editor.apply();
    }

    public boolean showSavedNotification() {
        HwLog.d("timer_notify", "TimerService showSavedNotification");
        long total = this.mPreferences.getLong("leaveTime", -1);
        long totalTime = total;
        long beginTime = this.mPreferences.getLong("beginTime", -1);
        HwLog.d("timer_notify", "totalTime = " + total + ", beginTime" + beginTime);
        int state = this.mPreferences.getInt("state", 3);
        if (state == 3) {
            return false;
        }
        if (beginTime != -1) {
            totalTime = total - (Utils.getTimeNow() - beginTime);
        }
        boolean timerRunning = false;
        boolean bShow = false;
        if (state == 1) {
            timerRunning = true;
            bShow = true;
        } else if (state == 2) {
            timerRunning = false;
            bShow = true;
        }
        if (bShow) {
            setNotification(updateNotificationTime(totalTime), timerRunning);
            if (timerRunning) {
                this.mTotalTime = total;
                this.mHandler.removeCallbacks(this.updateNotifyRunnable);
                this.mHandler.postDelayed(this.updateNotifyRunnable, 10000);
            }
        }
        return true;
    }

    private void clearSavedNotification() {
        HwLog.d("timer_notify", "TimerService clearSavedNotification");
        Editor editor = this.mPreferences.edit();
        editor.remove("leaveTime");
        editor.remove("state");
        editor.remove("leaveTime");
        editor.apply();
    }

    public PendingIntent getPausePendingIntent() {
        Intent intent = new Intent(this, TimerService.class);
        intent.setAction("timer.action.pause");
        return PendingIntent.getService(this, 0, intent, 0);
    }

    public PendingIntent getToTimerPageIntent() {
        Intent intent = new Intent(this, AlarmsMainActivity.class);
        intent.putExtra("deskclock.select.tab", 3);
        return PendingIntent.getActivity(this, 0, intent, 0);
    }

    public PendingIntent getStartPendingIntent() {
        Intent intent = new Intent(this, TimerService.class);
        intent.setAction("timer.action.start");
        return PendingIntent.getService(this, 0, intent, 0);
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    private void rePlayAgain(Uri defaultUri) {
        this.mMediaPlayer = null;
        this.mMediaPlayer = new MediaPlayer();
        try {
            this.mMediaPlayer.setDataSource(this.mContext, defaultUri);
            startPlayer(this.mMediaPlayer);
        } catch (IllegalArgumentException e) {
            Log.e("TimerService", "rePlayAgain : IllegalArgumentException = " + e.getMessage());
        } catch (SecurityException e2) {
            Log.e("TimerService", "rePlayAgain : SecurityException = " + e2.getMessage());
        } catch (IllegalStateException e3) {
            Log.e("TimerService", "rePlayAgain : IllegalStateException = " + e3.getMessage());
        } catch (IOException e4) {
            Log.e("TimerService", "rePlayAgain : IOException = " + e4.getMessage());
        }
    }

    private void startPlay() {
        Log.e("TimerService", "startPlay : flipMute = " + this.flipMute);
        if (!this.flipMute) {
            this.mUri = onRestoreRingtone();
            Log.e("TimerService", "startPlay : mUri = " + this.mUri);
            if (this.mMediaPlayer == null) {
                this.mMediaPlayer = new MediaPlayer();
                this.mMediaPlayer.setOnErrorListener(new OnErrorListener() {
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        Log.iRelease("TimerService", "startPlay : Error occurred while playing audio. what = " + what + " extra = " + extra);
                        mp.stop();
                        mp.release();
                        TimerService.this.rePlayAgain(Uri.parse(TimerService.this.defaultRingtone));
                        return true;
                    }
                });
                Log.e("TimerService", "startPlay : new mediaplay ");
            } else if (this.mMediaPlayer.isPlaying()) {
                Log.e("TimerService", "startPlay : mediaplay is play.");
                return;
            } else {
                this.mMediaPlayer.reset();
                Log.e("TimerService", "startPlay : reset mediaplay.");
            }
            if (this.mUri != null) {
                try {
                    if (this.mCallState) {
                        setDataSourceFromResource(getResources(), this.mMediaPlayer, R.raw.in_call_alarm);
                        this.mHandler.postDelayed(new TimerTask() {
                            public void run() {
                                TimerService.this.stopPlayer(false);
                            }
                        }, 2000);
                    } else {
                        this.mMediaPlayer.setDataSource(this.mContext, this.mUri);
                    }
                    startPlayer(this.mMediaPlayer);
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e2) {
                    isDRMRingtone(this.mUri);
                    try {
                        this.mMediaPlayer.reset();
                        this.mMediaPlayer.setDataSource(this.mContext, Uri.parse(this.defaultRingtone));
                        startPlayer(this.mMediaPlayer);
                    } catch (RuntimeException e3) {
                        Log.e("TimerService", "timerAlert : RuntimeException = " + e3.getMessage());
                        throw e3;
                    } catch (Exception e22) {
                        Log.e("TimerService", "timerAlert : Exception = " + e22.getMessage());
                    }
                }
            }
        }
    }

    private void isDRMRingtone(Uri alert) {
        if (CompatUtils.hasPermission(this, "android.permission.READ_EXTERNAL_STORAGE")) {
            Log.d("TimerService", "isDRMRingtone : DrmUtils.DRM_ENABLED = " + DrmUtils.DRM_ENABLED);
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
                            Log.d("TimerService", "isDRMRingtone : isDrm = " + isDrm);
                            if (isDrm && !DrmUtils.haveRightsForAction(str, 1)) {
                                this.mEditor.putString("ringtone", RingtoneHelper.getActualUri(this.mContext, Uri.parse(this.defaultRingtone)));
                                this.mEditor.commit();
                            }
                        }
                        cursor.close();
                    } catch (Throwable th) {
                        cursor.close();
                    }
                }
                Log.d("TimerService", "isDRMRingtone : filePath = " + str);
            }
            return;
        }
        Log.iRelease("TimerService", "isDRMRingtone->has no READ_EXTERNAL_STORAGE permissions");
    }

    private void stopPlayer(boolean stopSelf) {
        Log.d("TimerService", "stopPlayer");
        if (this.mMediaPlayer != null) {
            if (this.mMediaPlayer.isPlaying()) {
                this.mMediaPlayer.stop();
                Log.v("TimerService", "stopPlayer : is playing...");
            }
            this.mMediaPlayer.reset();
            this.mMediaPlayer.release();
        }
        this.mMediaPlayer = null;
        if (stopSelf) {
            stopSelf();
            this.flipMute = false;
        } else {
            this.flipMute = true;
        }
        resumeVolume();
    }

    private void startPlayer(MediaPlayer player) throws IOException, IllegalArgumentException, IllegalStateException {
        if (this.mAudioMgr == null) {
            this.mAudioMgr = (AudioManager) getSystemService("audio");
        }
        if (this.mAudioMgr.requestAudioFocus(this.mAudioFocusChangeListener, 3, 2) != 1) {
            Log.d("TimerService", "startPlayer : A failed focus change request.");
        }
        volume = this.mAudioMgr.getStreamVolume(4);
        player.setAudioStreamType(4);
        player.setLooping(true);
        player.setVolume((float) volume, (float) volume);
        player.prepare();
        player.start();
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

    protected Uri onRestoreRingtone() {
        Uri ringtoneUri = Uri.parse(this.mPreferences.getString("ringtone", this.defaultRingtone));
        if (ringtoneUri == null) {
            return Uri.parse(this.defaultRingtone);
        }
        if ("silent".equals(ringtoneUri.toString())) {
            return null;
        }
        if (!this.defaultRingtone.equals(ringtoneUri.toString())) {
            ringtoneUri = RingtoneHelper.getUriByPath(this.mContext, ringtoneUri);
        }
        if ("content://settings/system/ringtone1".equals(ringtoneUri.toString())) {
            ringtoneUri = Uri.parse(this.defaultRingtone);
        }
        return ringtoneUri;
    }

    private void getDefaultRingtone() {
        if (this.defaultRingtone == null) {
            String titleRingtone = SystemProperties.get("ro.config.deskclock_timer_alert", "Timer_Beep.ogg");
            Cursor mediaCursor = this.mContext.getContentResolver().query(Uri.parse("content://media/internal/audio/media"), PROJECTION, "_display_name= ?", new String[]{titleRingtone}, null);
            if (mediaCursor == null || !mediaCursor.moveToFirst()) {
                Cursor cursor = getContentResolver().query(Uri.parse("content://media/internal/audio/media"), PROJECTION, "_display_name= ?", SELECTIONARGS, null);
                if (cursor == null || !cursor.moveToFirst()) {
                    this.defaultRingtone = "silent";
                } else {
                    this.defaultRingtone = "content://media/internal/audio/media/" + cursor.getInt(0);
                }
                if (cursor != null) {
                    cursor.close();
                }
            } else {
                this.defaultRingtone = "content://media/internal/audio/media/" + mediaCursor.getInt(0);
            }
            if (mediaCursor != null) {
                mediaCursor.close();
            }
        }
    }

    private void resumeVolume() {
        AudioManager audioManager = (AudioManager) this.mContext.getSystemService("audio");
        Log.e("TimerService", "resumeVolume : 1volume = " + volume);
        if (volume > 0) {
            audioManager.setStreamVolume(4, volume, 0);
        }
        Editor editor = Utils.getDefaultSharedPreferences(this.mContext).edit();
        editor.remove("systemAlarmVolume");
        editor.commit();
    }

    private void loadSoundPool() {
        if (this.mSoundPool != null) {
            stopPool();
            releasePool();
        }
        this.mSoundPool = new SoundPool(1, 3, 1);
        this.mPoolId = this.mSoundPool.load(this.mContext, R.raw.timer_tick, 1);
        this.mSoundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                if (!TimerService.this.isTopActivity(".timer.TimerAlertActivity")) {
                    TimerService.this.playPool();
                }
            }
        });
        Log.dRelease("TimerService", "loadSoundPool.");
    }

    private void playPool() {
        if (this.mSoundPool != null) {
            this.mStreamId = this.mSoundPool.play(this.mPoolId, 1.0f, 1.0f, 0, -1, 1.0f);
        }
        if (this.mStreamId == 0) {
            Log.w("TimerService", "can not play the soundpool file.");
        }
        Log.dRelease("TimerService", "playPool.");
    }

    private void resumePool() {
        if (this.mSoundPool == null) {
            loadSoundPool();
        } else if (this.mStreamId != 0) {
            this.mSoundPool.resume(this.mStreamId);
        }
        Log.dRelease("TimerService", "resumePool.");
    }

    private void pausePool() {
        if (!(this.mSoundPool == null || this.mStreamId == 0)) {
            this.mSoundPool.pause(this.mStreamId);
        }
        Log.dRelease("TimerService", "pausePool.");
    }

    private void stopPool() {
        if (!(this.mSoundPool == null || this.mStreamId == 0)) {
            this.mSoundPool.stop(this.mStreamId);
        }
        Log.dRelease("TimerService", "stopPool.");
    }

    private void releasePool() {
        if (this.mSoundPool != null) {
            this.mSoundPool.release();
            this.mSoundPool = null;
        }
        Log.dRelease("TimerService", "releasePool.");
    }
}
