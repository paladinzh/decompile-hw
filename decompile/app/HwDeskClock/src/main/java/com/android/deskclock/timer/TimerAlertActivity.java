package com.android.deskclock.timer;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.deskclock.AlarmAlertWakeLock;
import com.android.deskclock.DigitalClock;
import com.android.deskclock.MotionManager;
import com.android.deskclock.MotionManager.MotionListener;
import com.android.deskclock.R;
import com.android.deskclock.alarmclock.BallFrameView;
import com.android.deskclock.alarmclock.HwCustCoopSensorManager;
import com.android.deskclock.alarmclock.PortCallPanelView;
import com.android.util.ClockReporter;
import com.android.util.Config;
import com.android.util.Log;
import com.android.util.Utils;
import com.android.util.WeakenVolume;
import com.huawei.cust.HwCustUtils;
import ucd.RythmSurfaceView;

public class TimerAlertActivity extends Activity implements OnClickListener {
    private AudioManager mAudioMgr = null;
    private Context mContext;
    private HwCustCoopSensorManager mCoopSensor = ((HwCustCoopSensorManager) HwCustUtils.createObj(HwCustCoopSensorManager.class, new Object[0]));
    private Editor mEditor;
    private BallFrameView mFrameView;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    Log.e("TimerAlertActivity", "mHandler->handleMessage : Config.MSG_LOCK_SUCESS");
                    TimerAlertActivity.this.closeAlert();
                    ClockReporter.reportEventMessage(TimerAlertActivity.this.mContext, 92, "");
                    TimerAlertActivity.this.finish();
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                String action = intent.getAction();
                if ("com.android.deskclock.ALARM_ALERT".equals(action)) {
                    TimerAlertActivity.this.closeAlert();
                } else if ("com.android.deskclock.timer".equals(action) && intent.getExtras() != null) {
                    TimerAlertActivity.this.setTime(intent.getLongExtra("leaveTime", 0));
                }
            }
        }
    };
    private KeyguardManager mKeyguardManager;
    private MotionListener mMotionListener = new MotionListener() {
        public void flipMute() {
            TimerAlertActivity.this.stopPlayer(false);
            if (TimerAlertActivity.this.mMotionManager != null) {
                TimerAlertActivity.this.mMotionManager.stopTimerFlipMuteGestureListener();
            }
            if (TimerAlertActivity.this.mCoopSensor != null && TimerAlertActivity.this.mCoopSensor.isRegister()) {
                TimerAlertActivity.this.mCoopSensor.clear();
            }
            ClockReporter.reportEventContainMessage(TimerAlertActivity.this.getApplicationContext(), 71, "flip 1", 0);
        }

        public void pickupReduce() {
            TimerAlertActivity.this.stopWeakenVL();
            TimerAlertActivity.this.volume = ((AudioManager) TimerAlertActivity.this.mContext.getSystemService("audio")).getStreamVolume(4);
            if (TimerAlertActivity.this.volume > 1) {
                TimerAlertActivity.this.startWeakenVL(TimerAlertActivity.this.mContext);
                ClockReporter.reportEventContainMessage(TimerAlertActivity.this.getApplicationContext(), 72, "pick 1", 0);
            }
        }
    };
    private MotionManager mMotionManager = null;
    private SharedPreferences mPreferences;
    private RythmSurfaceView mRythmView;
    private TextView mShowTime;
    private BroadcastReceiver mSystemReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                Log.w("TimerAlertActivity", "the intent is null or the action is null.");
                return;
            }
            if ("android.intent.action.SCREEN_OFF".equals(intent.getAction())) {
                TimerAlertActivity.this.closeAlert();
            }
        }
    };
    private String mText;
    private RelativeLayout mView;
    private WeakenVolume mWeakenVolume = null;
    StatusBarManager statusBarManager = null;
    private int volume = 0;

    private void registerSysReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_OFF");
        registerReceiver(this.mSystemReceiver, filter);
    }

    private void clearPreference() {
        this.mEditor.remove("isPause");
        this.mEditor.remove("mLeftDegree");
        this.mEditor.remove("mRightDegree");
        this.mEditor.remove("rightOrigin");
        this.mEditor.remove("leftOrigin");
        this.mEditor.remove("mMoveRoundType");
        this.mEditor.remove("beginTime");
        this.mEditor.remove("leaveTime");
        this.mEditor.remove("pauseTime");
        this.mEditor.remove("state");
        this.mEditor.remove("time");
        this.mEditor.remove("picked_time");
        this.mEditor.commit();
    }

    private void startPlayer() {
        Intent intent = new Intent(this, TimerService.class);
        intent.setAction("com.android.timerservice.startplay");
        startService(intent);
    }

    private void stopPlayer(boolean stopSelf) {
        Intent intent = new Intent(this, TimerService.class);
        intent.setAction("com.android.timerservice.stoppaly");
        intent.putExtra("service_stopself", stopSelf);
        startService(intent);
    }

    private void beginGesture() {
        if (this.mCoopSensor == null || !this.mCoopSensor.isCoop()) {
            startGestureListener();
        } else {
            this.mCoopSensor.startListener(this, this.mMotionListener);
        }
    }

    private void startGestureListener() {
        boolean z = true;
        this.volume = ((AudioManager) getSystemService("audio")).getStreamVolume(4);
        this.mMotionManager = MotionManager.getInstance(this);
        MotionManager motionManager = this.mMotionManager;
        MotionListener motionListener = this.mMotionListener;
        if (this.volume <= 1) {
            z = false;
        }
        motionManager.startTimerGestureListener(motionListener, z);
    }

    private void startWeakenVL(Context context) {
        this.mWeakenVolume = new WeakenVolume(context, this.volume, 2, this.mMotionManager);
        this.mWeakenVolume.setRun(true);
        this.mWeakenVolume.setName("TimerWeakenVolume");
        this.mWeakenVolume.start();
    }

    private void stopWeakenVL() {
        if (this.mWeakenVolume != null) {
            this.mWeakenVolume.setRun(false);
            this.mWeakenVolume.stopThread();
            this.mWeakenVolume = null;
        }
    }

    private void setTime(long totalTime) {
        formatDuration(Math.abs(totalTime / 1000));
    }

    private void formatDuration(long duration) {
        long Hour = duration / 3600;
        long Min = (duration / 60) % 60;
        long Sec = duration % 60;
        String[] formats = getResources().getStringArray(R.array.Tips_alarmsTimer_overtime);
        if (0 == Hour && 0 == Min) {
            this.mText = String.format(formats[0], new Object[]{Long.valueOf(Sec)});
        } else if (0 == Hour) {
            this.mText = String.format(formats[1], new Object[]{Long.valueOf(Min), Long.valueOf(Sec)});
        } else {
            this.mText = String.format(formats[2], new Object[]{Long.valueOf(Hour), Long.valueOf(Min), Long.valueOf(Sec)});
        }
        this.mShowTime.setText(this.mText);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        if (!Config.istablet()) {
            setRequestedOrientation(1);
        }
        Window win = getWindow();
        win.addFlags(524288);
        LayoutParams winParams = win.getAttributes();
        winParams.buttonBrightness = 0.0f;
        win.setAttributes(winParams);
        win.addFlags(2098304);
        win.addFlags(134217728);
        this.mView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.timer_alert_dialog, (ViewGroup) null);
        setContentView(this.mView);
        this.statusBarManager = (StatusBarManager) getSystemService("statusbar");
        this.mKeyguardManager = (KeyguardManager) getSystemService("keyguard");
        this.mView.setSystemUiVisibility(((this.mView.getSystemUiVisibility() | 16777216) | 2097152) | 65536);
        RelativeLayout musicLayout = (RelativeLayout) this.mView.findViewById(R.id.relate);
        this.mRythmView = new RythmSurfaceView(this);
        this.mRythmView.attachView(musicLayout);
        registerReceiver();
        init();
        updateView();
        beginGesture();
        String skipMessage = this.mPreferences.getString("timer_skip_message", null);
        if (!TextUtils.isEmpty(skipMessage)) {
            TextView extraTextView = new TextView(this);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(-2, -2);
            lp.addRule(3, R.id.snooze_wrap_layout);
            lp.addRule(14);
            extraTextView.setLayoutParams(lp);
            this.mView.addView(extraTextView);
            extraTextView.setText(skipMessage);
            extraTextView.setTextSize(15.0f);
            extraTextView.setTypeface(Utils.getmRobotoXianBlackTypeface());
            extraTextView.setTextColor(-1);
        }
    }

    private void init() {
        this.mContext = this;
        this.mAudioMgr = (AudioManager) getSystemService("audio");
        this.mShowTime = (TextView) this.mView.findViewById(R.id.timer_alert_text);
        this.mPreferences = Utils.getSharedPreferences(this, "timer", 0);
        this.mEditor = this.mPreferences.edit();
        this.mFrameView = (BallFrameView) this.mView.findViewById(R.id.timer_close_layout);
        this.mFrameView.setMainHandler(this.mHandler);
        int heightPixels = getResources().getDisplayMetrics().heightPixels;
        int widthPixels = getResources().getDisplayMetrics().widthPixels;
        BallFrameView ballFrameView = this.mFrameView;
        if (widthPixels < heightPixels) {
            heightPixels = widthPixels;
        }
        ballFrameView.setCoverViewWidth(heightPixels);
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.android.deskclock.ALARM_ALERT");
        filter.addAction("com.android.deskclock.timer");
        filter.addAction("android.intent.action.SCREEN_OFF");
        registerReceiver(this.mIntentReceiver, filter, "com.huawei.deskclock.broadcast.permission", null);
        registerSysReceiver();
    }

    private void updateView() {
        Context context = this;
        if (Utils.isLandScreen(this)) {
            DigitalClock digitalclock = (DigitalClock) this.mView.findViewById(R.id.digitalclock);
            RelativeLayout snooze = (RelativeLayout) findViewById(R.id.snooze_layout);
            int singlewidth = getResources().getDisplayMetrics().widthPixels / 12;
            int size = (singlewidth * 4) + (singlewidth - Utils.dip2px(this, 67));
            int heightPixels = getResources().getDisplayMetrics().heightPixels;
            int marginleft = Utils.dip2px(this, 54);
            LinearLayout.LayoutParams snoozeRlp = new LinearLayout.LayoutParams(singlewidth * 5, -1);
            snoozeRlp.setMargins(marginleft, 0, 0, 0);
            snooze.setLayoutParams(snoozeRlp);
            RelativeLayout.LayoutParams digitalclockRlp = new RelativeLayout.LayoutParams(-1, -2);
            digitalclockRlp.setMargins(0, (heightPixels - size) / 2, singlewidth, 0);
            digitalclockRlp.addRule(10, R.id.timer_close_layout);
            digitalclock.setLayoutParams(digitalclockRlp);
            RelativeLayout.LayoutParams mFrameViewRlp = new RelativeLayout.LayoutParams(-1, getResources().getDimensionPixelSize(R.dimen.alarm_ball_height));
            mFrameViewRlp.setMargins(0, 0, singlewidth, (heightPixels - size) / 2);
            mFrameViewRlp.addRule(12);
            this.mFrameView.setLayoutParams(mFrameViewRlp);
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.timer_alert_dialog, (ViewGroup) null);
        setContentView(this.mView);
        this.mView.setSystemUiVisibility(((this.mView.getSystemUiVisibility() | 16777216) | 2097152) | 65536);
        this.mRythmView = new RythmSurfaceView(this);
        this.mRythmView.attachView((RelativeLayout) this.mView.findViewById(R.id.relate));
        init();
        updateView();
        this.mFrameView.restoreAnimal();
    }

    protected void onResume() {
        super.onResume();
        AlarmAlertWakeLock.acquireFullWakeLock();
        this.mRythmView.onResume();
        startPlayer();
        Log.i("TimerAlertActivity", "onResume");
    }

    public void onClick(View v) {
    }

    private void closeAlert() {
        Log.i("TimerAlertActivity", "Timer is close");
        if (this.mMotionManager != null) {
            this.mMotionManager.stopTimerGestureListener();
        }
        if (this.mCoopSensor != null && this.mCoopSensor.isRegister()) {
            this.mCoopSensor.clear();
        }
        clearPreference();
        stopPlayer(true);
        TimerPage.setisFirstRun(true);
        stopService(new Intent(this, TimerService.class));
        finish();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (event.getKeyCode()) {
            case 24:
            case PortCallPanelView.DEFAUT_RADIUS /*25*/:
                Intent intent = new Intent("action_type_timer_silent");
                intent.setPackage(getPackageName());
                sendBroadcast(intent);
                return true;
            case 82:
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    protected void onPause() {
        super.onPause();
        this.mRythmView.onPause();
    }

    protected void onDestroy() {
        super.onDestroy();
        AlarmAlertWakeLock.releaseFullLock();
        this.mRythmView.onDestroy();
        this.statusBarManager.disable(0);
        closeAlert();
        this.mHandler.removeCallbacksAndMessages(null);
        unregisterReceiver(this.mIntentReceiver);
        unregisterReceiver(this.mSystemReceiver);
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            Log.i("TimerAlertActivity", "No focus >>> close timer");
            closeAlert();
        }
    }

    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.zoom_exit);
    }
}
