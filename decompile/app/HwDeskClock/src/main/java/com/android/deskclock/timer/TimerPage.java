package com.android.deskclock.timer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.android.deskclock.AlarmsMainActivity;
import com.android.deskclock.ClockFragment;
import com.android.deskclock.R;
import com.android.deskclock.alarmclock.SettingsActivity;
import com.android.deskclock.timer.TimerPicker.OnTimerListener;
import com.android.util.ClockReporter;
import com.android.util.Config;
import com.android.util.Log;
import com.android.util.Utils;
import com.huawei.immersion.Vibetonz;

public class TimerPage extends ClockFragment implements OnClickListener {
    private static final String[] PROJECTION = new String[]{"_id"};
    private static final String[] SELECTIONARGS = new String[]{"Beep-Beep-Beep Alarm"};
    private static boolean isFirstRun = true;
    private static boolean isFromCts = false;
    private static int mTimerState = 3;
    private long mBeginTime;
    private ImageView mBtnPause;
    private ImageView mBtnReset;
    private ImageView mBtnSetting;
    private ImageView mBtnStart;
    private long mCurrTime = 0;
    private long mDragTime = 0;
    private Editor mEditor;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    TimerPage.this.resetTimer();
                    Activity activity = TimerPage.this.getActivity();
                    if (activity != null) {
                        Intent intent = new Intent(activity, TimerAlertActivity.class);
                    }
                    TimerPage.isFromCts = false;
                    break;
                case 213:
                    TimerPage.this.mTimeTurn = true;
                    TimerPage.this.rotateShortPlayer();
                    break;
                case 214:
                    TimerPage.this.mTimeTurn = false;
                    break;
                case 215:
                    TimerPage.this.soundPool.stop(TimerPage.this.mResetID);
                    break;
                case 216:
                    TimerPage.this.mEditor.remove("leaveTime");
                    TimerPage.this.mEditor.remove("beginTime");
                    TimerPage.this.mEditor.commit();
                    break;
                case 1000:
                    TimerPage.this.closeTimer();
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private Vibetonz mImmDevice;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                if ("com.android.deskclock.ALARM_ALERT".equals(intent.getAction())) {
                    TimerPage.this.closeTimer();
                } else if ("com.android.deskclock.timer".equals(intent.getAction()) && TimerPage.mTimerState != 0) {
                    long leaveTime = intent.getLongExtra("leaveTime", 0);
                    if (leaveTime >= 0) {
                        TimerPage.this.mTimerPicker.updateTimer(leaveTime);
                    } else if (TimerPage.mTimerState == 1 && !TimerPage.isFirstRun()) {
                        TimerPage.this.mHandler.sendEmptyMessage(100);
                    }
                }
            }
        }
    };
    private long mLeaveTime;
    private boolean mMove = false;
    private SharedPreferences mPreferences;
    private int mResetID = 0;
    private boolean mTimeTurn = false;
    private TimerPicker mTimerPicker;
    private RelativeLayout mTimerView;
    private long mTotalTime = 0;
    private int playFile;
    private SoundPool soundPool;
    private RelativeLayout startAndStop;

    public static void setIsFromCTS(boolean isfromcts) {
        isFromCts = isfromcts;
    }

    private void setObject2Null() {
        this.mTimerView = null;
        this.mBtnStart = null;
        this.mBtnReset = null;
        this.mBtnSetting = null;
        this.mTimerPicker = null;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("TimerPage", "onCreateView");
        this.mTimerView = (RelativeLayout) inflater.inflate(R.layout.timer_activity, container, false);
        stopForce();
        initStatus(true);
        return this.mTimerView;
    }

    private void changeViewPadding() {
        if (!Utils.isLandScreen(getActivity())) {
            float density = getResources().getDisplayMetrics().density;
            if (density - 2.0f >= 0.01f) {
                if (density - 2.5f < 0.01f) {
                    this.mTimerPicker.setPadding(0, (int) getResources().getDimension(R.dimen.timer_picker_paddingtop_3_small), 0, 0);
                } else if (density - 2.75f < 0.01f) {
                    this.mTimerPicker.setPadding(0, (int) getResources().getDimension(R.dimen.timer_picker_paddingtop_3_medium), 0, 0);
                } else if (Math.abs(density - 3.125f) < 0.01f) {
                    this.mTimerPicker.setPadding(0, (int) getResources().getDimension(R.dimen.timer_picker_paddingtop_4_small), 0, 0);
                } else if (Math.abs(density - 3.5f) < 0.01f) {
                    this.mTimerPicker.setPadding(0, (int) getResources().getDimension(R.dimen.timer_picker_paddingtop_4_medium), 0, 0);
                }
            }
        }
    }

    private void initStatus(boolean status) {
        Activity activity = getActivity();
        if (activity != null) {
            initializeData();
            setmTimerState(this.mPreferences.getInt("state", 3));
            this.mTotalTime = this.mPreferences.getLong("time", 0);
            this.mLeaveTime = this.mPreferences.getLong("leaveTime", 0);
            long currentTime = SystemClock.elapsedRealtime();
            this.mBeginTime = this.mPreferences.getLong("beginTime", 0);
            this.mCurrTime = this.mLeaveTime;
            if (mTimerState == 1) {
                this.mCurrTime = this.mLeaveTime - (currentTime - this.mBeginTime);
            }
            isFirstRun = this.mPreferences.getBoolean("is_first_run", true);
            this.mTimerPicker.recoverData(this.mTotalTime, this.mCurrTime);
            stateControl();
            switch (mTimerState) {
                case 0:
                    setmTimerState(0);
                    this.mTimerPicker.performSwitchStopAction();
                    break;
                case 1:
                    setmTimerState(1);
                    this.mTimerPicker.performSwitchRunningAction();
                    this.mBtnReset.setVisibility(0);
                    Intent intent = new Intent(activity, TimerService.class);
                    intent.setAction("com.android.timerservice.resume");
                    activity.startService(intent);
                    break;
                case 2:
                    this.mTimerPicker.performSwitchPauseAciton();
                    setmTimerState(2);
                    break;
                case 3:
                    this.mTimerPicker.recoverPickedTime(this.mPreferences.getString("picked_time", "0/0/0"));
                    setmTimerState(3);
                    break;
            }
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        Log.d("TimerPage", "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
        if (2 == mTimerState || mTimerState == 0) {
            this.mEditor.putLong("leaveTime", this.mCurrTime);
            this.mEditor.putBoolean("isPause", true);
            this.mEditor.commit();
        }
    }

    private void stopForce() {
        boolean destroy = this.mPreferences.getBoolean("stopForce", true);
        int rogChange = this.mPreferences.getInt("rogchange", 0);
        if (!(destroy || TimerService.serviceRunning || rogChange != 0)) {
            clearPreferenceStopForce();
        }
        if (rogChange == 1) {
            this.mEditor.remove("rogchange");
            this.mEditor.commit();
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        Log.d("TimerPage", "onCreate");
        Activity activity = getActivity();
        if (activity != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.android.deskclock.ALARM_ALERT");
            filter.addAction("com.android.deskclock.timer");
            activity.registerReceiver(this.mIntentReceiver, filter, "com.huawei.deskclock.broadcast.permission", null);
            this.mPreferences = Utils.getSharedPreferences(activity, "timer", 0);
            this.mEditor = this.mPreferences.edit();
            this.soundPool = new SoundPool(1, 3, 1);
            this.playFile = this.soundPool.load(activity, R.raw.timer_turn, 1);
            Bundle bundle = getArguments();
            if (!(bundle == null || bundle.getLong("currentTime") == 0)) {
                this.mCurrTime = getArguments().getLong("currentTime");
            }
            if (Vibetonz.isVibrateOn(getActivity())) {
                this.mImmDevice = Vibetonz.getInstance();
            }
            super.onCreate(savedInstanceState);
        }
    }

    private void initializeData() {
        this.mBtnStart = (ImageView) this.mTimerView.findViewById(R.id.timer_btn_start);
        this.mBtnReset = (ImageView) this.mTimerView.findViewById(R.id.timer_btn_reset);
        this.mBtnPause = (ImageView) this.mTimerView.findViewById(R.id.timer_btn_pause);
        this.mBtnSetting = (ImageView) this.mTimerView.findViewById(R.id.timer_btn_setting);
        this.mBtnReset.setAlpha(0.2f);
        this.startAndStop = (RelativeLayout) this.mTimerView.findViewById(R.id.startandstop);
        this.mBtnSetting.setClickable(true);
        this.mTimerPicker = (TimerPicker) this.mTimerView.findViewById(R.id.timer_picker);
        changeViewPadding();
        this.mTimerPicker.setOnTimerListener(new OnTimerListener() {
            public void onTimeOut() {
                TimerPage.mTimerState = 3;
                TimerPage.setisFirstRun(true);
                TimerPage.this.mTimerPicker.performResetAction();
                TimerPage.this.mCurrTime = 0;
                TimerPage.this.mBtnReset.setVisibility(0);
                TimerPage.this.mBtnReset.setClickable(true);
                TimerPage.this.stateControl();
            }

            public void onTimerPickScroll(int pickedTime) {
                if (pickedTime == 0) {
                    TimerPage.this.startAndStop.setAlpha(0.2f);
                    TimerPage.this.mBtnReset.setAlpha(0.2f);
                    TimerPage.this.mBtnReset.setEnabled(false);
                    TimerPage.this.mBtnStart.setEnabled(false);
                    return;
                }
                TimerPage.this.startAndStop.setAlpha(1.0f);
                TimerPage.this.mBtnReset.setAlpha(1.0f);
                TimerPage.this.mBtnReset.setEnabled(true);
                TimerPage.this.mBtnStart.setEnabled(true);
            }

            public void onPause(int currentTime) {
            }
        });
        this.mBtnStart.setOnClickListener(this);
        this.mBtnReset.setOnClickListener(this);
        this.mBtnPause.setOnClickListener(this);
        this.mBtnSetting.setOnClickListener(this);
    }

    private void stateControl() {
        AlarmsMainActivity.wakeLock(Config.clockTabIndex());
        switch (mTimerState) {
            case 0:
                this.mBtnStart.setVisibility(0);
                this.mBtnReset.setVisibility(0);
                isFirstRun = true;
                this.mBtnReset.setClickable(true);
                return;
            case 1:
                this.startAndStop.setAlpha(1.0f);
                this.mBtnStart.setEnabled(true);
                this.mBtnReset.setEnabled(true);
                this.mBtnReset.setVisibility(0);
                this.mBtnStart.setVisibility(4);
                this.mBtnPause.setVisibility(0);
                this.mBtnReset.setClickable(true);
                this.mBtnReset.setAlpha(1.0f);
                return;
            case 2:
                this.mBtnStart.setVisibility(0);
                this.mBtnReset.setVisibility(0);
                this.mBtnPause.setVisibility(4);
                this.mBtnReset.setClickable(true);
                this.mBtnReset.setAlpha(1.0f);
                return;
            case 3:
                setisFirstRun(true);
                if (this.mTimerPicker.getTotalTime() == 0) {
                    this.startAndStop.setAlpha(0.4f);
                    this.mBtnStart.setEnabled(false);
                    this.mBtnReset.setEnabled(false);
                } else {
                    this.startAndStop.setAlpha(1.0f);
                    this.mBtnStart.setEnabled(true);
                    this.mBtnReset.setEnabled(true);
                }
                this.mBtnStart.setClickable(true);
                this.mBtnStart.setVisibility(0);
                this.mBtnPause.setVisibility(4);
                this.mBtnReset.setVisibility(4);
                this.mBtnReset.setAlpha(0.4f);
                this.mBtnReset.setVisibility(0);
                return;
            default:
                return;
        }
    }

    public void onClick(View v) {
        Activity activity = getActivity();
        if (activity != null) {
            Intent intent = new Intent(activity, TimerService.class);
            intent.setAction("com.android.timerservice.start");
            switch (v.getId()) {
                case R.id.timer_btn_reset:
                    ClockReporter.reportEventMessage(getActivity(), 52, "");
                    setIsFromCTS(false);
                    resetTimer();
                    activity.stopService(new Intent(activity, TimerService.class));
                    setisFirstRun(true);
                    this.mBtnReset.setVisibility(4);
                    this.mTimerPicker.performResetAction();
                    break;
                case R.id.timer_btn_start:
                    if (mTimerState != 1) {
                        startAction(intent, activity);
                        break;
                    }
                    break;
                case R.id.timer_btn_pause:
                    if (mTimerState == 1) {
                        pauseAction(intent, activity);
                        break;
                    }
                    break;
                case R.id.timer_btn_setting:
                    ClockReporter.reportEventContainMessage(getActivity(), 10, "tab:4", 0);
                    Intent intentToSettings = new Intent(getActivity(), SettingsActivity.class);
                    intentToSettings.setAction("action_from_timer");
                    startActivity(intentToSettings);
                    break;
            }
            stateControl();
            android.util.Log.d("timer_pager", isFirstRun + "");
        }
    }

    private void startAction(Intent intent, Activity activity) {
        this.mMove = false;
        if (isFirstRun) {
            this.mCurrTime = this.mTimerPicker.getTotalTime();
            this.mTotalTime = this.mCurrTime;
            this.mEditor.putLong("time", this.mTotalTime);
            this.mEditor.putLong("origin_picked_time", this.mCurrTime);
        } else {
            this.mCurrTime = this.mCurrTime == 0 ? this.mTimerPicker.getCurrentTime() : this.mCurrTime;
        }
        if (this.mCurrTime > 0 && !this.mMove) {
            setmTimerState(1);
            this.mEditor.putLong("leaveTime", this.mCurrTime);
            this.mEditor.putInt("state", mTimerState);
            this.mEditor.commit();
            activity.startService(intent);
            if (isFirstRun) {
                setisFirstRun(false);
                this.mTimerPicker.performStartAction();
            }
            startTimerInService();
        }
    }

    public void startTimerInService() {
        Intent intent = new Intent(getActivity(), TimerService.class);
        intent.setAction("timer.action.start");
        intent.putExtra("show_notify", false);
        intent.putExtra("report_id", 18);
        getActivity().startService(intent);
    }

    private void pauseAction(Intent intent, Activity activity) {
        if (mTimerState == 1) {
            setmTimerState(2);
            long currTime = SystemClock.elapsedRealtime();
            this.mEditor.putLong("pauseTime", currTime);
            this.mEditor.putInt("state", mTimerState);
            long leaveTime = this.mPreferences.getLong("leaveTime", 0) - (currTime - this.mPreferences.getLong("beginTime", 0));
            Log.e("TimerPage", "mcurrentime = " + this.mCurrTime + ", leaveTime = " + leaveTime);
            this.mCurrTime = leaveTime;
            this.mEditor.commit();
            this.mTimerPicker.performPauseAction();
            activity.stopService(intent);
            pauseTimerInService();
        }
    }

    public void pauseTimerInService() {
        Intent intent = new Intent(getActivity(), TimerService.class);
        intent.setAction("timer.action.pause");
        intent.putExtra("show_notify", false);
        intent.putExtra("report_id", 51);
        getActivity().startService(intent);
    }

    private void closeTimer() {
        if (getActivity() != null) {
            setIsFromCTS(false);
            if (mTimerState != 3) {
                Log.i("TimerPage", "closeTimer : mTimerState != STATE_TIMER when closeTimer");
                return;
            }
            setmTimerState(3);
            stopPlayer(true);
            clearPreference();
        }
    }

    private void clearPreference() {
        this.mEditor.remove("isPause");
        this.mEditor.remove("pauseTime");
        this.mEditor.remove("state");
        this.mEditor.remove("time");
        this.mEditor.remove("timer_picker_current_time");
        this.mEditor.remove("leaveTime");
        this.mEditor.remove("timer_picker_count_number");
        this.mEditor.remove("time");
        this.mEditor.remove("timer_panel_slow_factor");
        this.mEditor.remove("count_cell_sweep_angle");
        this.mEditor.remove("beginTime");
        this.mEditor.remove("picked_time");
        this.mEditor.commit();
    }

    private void clearPreferenceStopForce() {
        this.mEditor.remove("isPause");
        this.mEditor.remove("pauseTime");
        this.mEditor.remove("state");
        this.mEditor.remove("time");
        this.mEditor.remove("timer_picker_count_number");
        this.mEditor.remove("time");
        this.mEditor.remove("timer_panel_slow_factor");
        this.mEditor.remove("count_cell_sweep_angle");
        this.mEditor.remove("picked_time");
        this.mEditor.commit();
    }

    public void onDestroy() {
        Log.d("TimerPage", "onDestroy");
        this.mTimerPicker.resetTime();
        if (!isFromCts) {
            this.mEditor.remove("timer_skip_message");
            this.mEditor.commit();
        }
        this.mHandler.removeCallbacksAndMessages(null);
        try {
            super.onDestroy();
        } catch (Exception e) {
            Log.e("TimerPage", "onDestroy : Exception = " + e.getMessage());
        }
        if (this.soundPool != null) {
            this.soundPool.release();
        }
        Activity activity = getActivity();
        if (activity != null) {
            activity.unregisterReceiver(this.mIntentReceiver);
        }
        setObject2Null();
    }

    private void dealQuickAction() {
        Activity activity = getActivity();
        if (activity == null) {
            Log.iRelease("TimerPage", "dealQuickAction actvity is null.");
            return;
        }
        Intent intent = activity.getIntent();
        if (intent == null) {
            Log.iRelease("TimerPage", "dealQuickAction intent is null.");
        } else if (intent.getIntExtra("deskclock.select.tab", -1) != 3) {
            Log.iRelease("TimerPage", "dealQuickAction is not stopwatch.");
        } else if (intent.getBooleanExtra("is_quickaction_type", false)) {
            int state = intent.getIntExtra("quickaction_type_state", 1);
            if (state == mTimerState) {
                Log.iRelease("TimerPage", "dealQuickAction state is same.");
                return;
            }
            int action_type = intent.getIntExtra("quickaction_type", 1);
            if (action_type == 1) {
                this.mEditor.putLong("time", 60000);
                this.mEditor.putLong("leaveTime", 60000);
                this.mEditor.putInt("state", state).commit();
                this.mCurrTime = 60000;
                this.mBtnStart.performClick();
            } else if (action_type == 2) {
                this.mBtnPause.performClick();
            } else if (action_type == 3) {
                this.mBtnStart.performClick();
            }
            ClockReporter.reportEventContainMessage(getActivity(), 75, "STARTWAY", 1);
        } else {
            Log.iRelease("TimerPage", "dealQuickAction is not quickaction.");
        }
    }

    public void onStart() {
        super.onStart();
        Log.d("TimerPage", "onStart");
        Activity activity = getActivity();
        if (activity != null) {
            initStatus(true);
            if (mTimerState == 1) {
                Intent intent = new Intent("android.intent.action.timer_resume");
                intent.setPackage(activity.getPackageName());
                activity.sendBroadcast(intent);
            }
            dealQuickAction();
            forCTSTest();
            Log.v("TimerPage", "onStart : isFromCts = " + isFromCts + "  skipMessage = " + this.mPreferences.getString("timer_skip_message", null));
        }
    }

    public void onResume() {
        super.onResume();
        Log.dRelease("TimerPage", "onResume tag = " + Config.clockTabIndex());
        if (mTimerState == 1 && Config.clockTabIndex() == 3) {
            Activity activity = getActivity();
            if (activity != null) {
                Intent intent = new Intent(activity, TimerService.class);
                intent.setAction("com.deskclock.timer.soundpool.resume");
                activity.startService(intent);
            }
        }
        if (Vibetonz.isVibrateOn(getContext()) && this.mImmDevice != null) {
            Config.updateVibratePause(false);
        }
    }

    private void forCTSTest() {
        Activity activity = getActivity();
        if (activity != null) {
            Intent intent = activity.getIntent();
            if (intent != null && intent.getAction() != null) {
                if ("com.android.deskclock.timer.otherstart".equals(intent.getAction())) {
                    long timerTime = intent.getLongExtra("timer.intent.extra", 0);
                    boolean skipUI = intent.getBooleanExtra("timer_skip_ui", false);
                    this.mEditor.putString("timer_skip_message", intent.getStringExtra("timer_skip_message"));
                    this.mEditor.commit();
                    if (timerTime > 0) {
                        startTimerFromOtherAPP(timerTime, skipUI);
                    }
                }
            }
        }
    }

    public void onPause() {
        super.onPause();
        this.mEditor.putBoolean("isPause", true);
        this.mEditor.commit();
        Log.dRelease("TimerPage", "onPause");
        if (mTimerState == 1) {
            Activity activity = getActivity();
            if (activity != null) {
                Intent soundpool = new Intent(activity, TimerService.class);
                soundpool.setAction("com.deskclock.timer.soundpool.pause");
                activity.startService(soundpool);
            }
        }
        if (Vibetonz.isVibrateOn(getContext()) && this.mImmDevice != null) {
            Config.updateVibratePause(true);
        }
    }

    public void onDestroyView() {
        super.onDestroyView();
        Log.d("TimerPage", "onDestroyView");
    }

    public static synchronized void updateIsFirstRun(boolean bRun) {
        synchronized (TimerPage.class) {
            isFirstRun = bRun;
        }
    }

    public static boolean isFirstRun() {
        return isFirstRun;
    }

    public static void setisFirstRun(boolean isFirstRun) {
        isFirstRun = isFirstRun;
    }

    private void resetTimer() {
        setmTimerState(3);
        clearPreference();
    }

    public void onFragmentPause() {
        super.onFragmentPause();
        if (mTimerState == 3) {
            Log.i("TimerPage", "onFragmentPause : change to other tab when STATE_TIMER");
        }
        Log.dRelease("TimerPage", "onFragmentPause : change to other tab when STATE_TIMER");
        if (mTimerState == 1) {
            Activity activity = getActivity();
            if (activity != null) {
                Intent soundpool = new Intent(activity, TimerService.class);
                soundpool.setAction("com.deskclock.timer.soundpool.pause");
                activity.startService(soundpool);
            }
        }
    }

    public void onFragmentResume() {
        super.onFragmentResume();
        Log.dRelease("TimerPage", "onFragmentResume : change to other tab when STATE_TIMER");
        if (mTimerState == 1) {
            Activity activity = getActivity();
            if (activity != null) {
                Intent intent = new Intent(activity, TimerService.class);
                intent.setAction("com.deskclock.timer.soundpool.resume");
                activity.startService(intent);
            }
        }
    }

    public void onStop() {
        super.onStop();
        Log.d("TimerPage", "onStop");
        Activity activity = getActivity();
        if (mTimerState == 1) {
            if (activity != null) {
                Intent intent = new Intent("android.intent.action.timer_pause");
                intent.setPackage(activity.getPackageName());
                activity.sendBroadcast(intent);
            }
            this.mEditor.putInt("state", 1);
            updateIsFirstRun(false);
        } else if (mTimerState == 0 || mTimerState == 3) {
            updateIsFirstRun(true);
        }
        if (mTimerState == 3) {
            this.mEditor.putString("picked_time", this.mTimerPicker.getPickedTime());
        }
        this.mEditor.putInt("state", mTimerState);
        synchronized (this) {
            this.mEditor.putBoolean("is_first_run", isFirstRun);
        }
        this.mEditor.commit();
    }

    private void stopPlayer(boolean stopSelf) {
        Activity activity = getActivity();
        if (activity != null) {
            Intent intent = new Intent(activity, TimerService.class);
            intent.setAction("com.android.timerservice.stoppaly");
            intent.putExtra("service_stopself", stopSelf);
            activity.startService(intent);
        }
    }

    private void rotateShortPlayer() {
        this.soundPool.play(this.playFile, 1.0f, 1.0f, 0, 0, 1.0f);
        if (Vibetonz.isVibrateOn(getActivity()) && this.mImmDevice != null && Config.clockTabIndex() == 3) {
            this.mImmDevice.playIvtEffect(400);
        }
    }

    public static synchronized void setmTimerState(int mTimerState) {
        synchronized (TimerPage.class) {
            mTimerState = mTimerState;
        }
    }

    private void startTimerFromOtherAPP(long currentTime, boolean skipUi) {
        Log.d("TimerPage", "startTimerFromOtherAPP");
        Activity activity = getActivity();
        if (activity != null) {
            setmTimerState(3);
            this.mTotalTime = currentTime;
            this.mEditor.putLong("time", this.mTotalTime);
            this.mEditor.putLong("leaveTime", this.mTotalTime);
            this.mEditor.putInt("state", mTimerState);
            this.mEditor.commit();
            this.mTimerPicker.recoverPickedTime(formatTime(this.mTotalTime));
            setisFirstRun(true);
            this.mBtnStart.performClick();
            isFromCts = true;
            if (skipUi) {
                activity.finish();
            }
        }
    }

    private String formatTime(long seconds) {
        int second;
        int totalSeconds = ((int) seconds) / 1000;
        int hour = 0;
        int minute = 0;
        int totalMinutes = totalSeconds / 60;
        if (totalMinutes == 0) {
            second = totalSeconds;
        } else if (totalMinutes <= 0 || totalMinutes >= 60) {
            hour = totalMinutes / 60;
            minute = totalMinutes - (hour * 60);
            second = totalSeconds - (totalMinutes * 60);
        } else {
            minute = totalMinutes;
            second = totalSeconds % 60;
        }
        return hour + "/" + minute + "/" + second;
    }

    public Long queryCurTime() {
        return Long.valueOf(this.mCurrTime);
    }
}
