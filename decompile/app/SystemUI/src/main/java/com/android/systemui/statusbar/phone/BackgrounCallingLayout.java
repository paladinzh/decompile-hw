package com.android.systemui.statusbar.phone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.android.systemui.R;
import com.android.systemui.tint.TintTextView;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SecurityCodeCheck;
import com.android.systemui.utils.analyze.BDReporter;
import fyusion.vislib.BuildConfig;

public class BackgrounCallingLayout extends FrameLayout {
    private static boolean isForegroundActivity = false;
    private static long mCallConnectedTimeMills = -1;
    private static boolean sIsCalllinearlayouShowing;
    private BackgrounCallingOnClickListener mBackgrounCallingOnClickListener;
    private BackgrounCallingOnTouchListener mBackgrounCallingOnTouchListener;
    private ImageView mCallLightImage = null;
    private TextView mCallTextView = null;
    private TextView mCallTimeTextView = null;
    private CallTimer mCallTimer = new CallTimer(new Runnable() {
        public void run() {
            BackgrounCallingLayout.this.updateCallTime();
            BackgrounCallingLayout.this.updateKeyguard();
        }
    });
    private BroadcastReceiver mCallingStatusReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (SecurityCodeCheck.isValidIntentAndAction(intent)) {
                String action = intent.getAction();
                if ("InCallScreenIsForegroundActivity".equals(action)) {
                    BackgrounCallingLayout.isForegroundActivity = intent.getBooleanExtra("IsForegroundActivity", true);
                }
                BackgrounCallingLayout.this.mPhoneState = BackgrounCallingLayout.this.getPhoneState();
                HwLog.i("BackgrounCallingLayout", "mCallingStatusReceiver:onReceive::action=" + action + ", isForegroundActivity=" + BackgrounCallingLayout.isForegroundActivity + ", mPhoneState=" + BackgrounCallingLayout.this.mPhoneState);
                BackgrounCallingLayout.this.updateCallLayout(intent);
            }
        }
    };
    private Handler mHandler = null;
    private LinearLayout mInCallingLayout;
    int mPhoneState = 0;
    private BroadcastReceiver mPhoneStatusReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (SecurityCodeCheck.isValidIntentAndAction(intent) && "android.intent.action.PHONE_STATE".equals(intent.getAction())) {
                BackgrounCallingLayout.this.mPhoneState = BackgrounCallingLayout.this.getPhoneState();
                HwLog.i("BackgrounCallingLayout", "mPhoneStatusReceiver:onReceive::mPhoneState=" + BackgrounCallingLayout.this.mPhoneState);
            }
        }
    };
    private boolean mShowKeyguard;
    private PhoneStatusBarView mStatusBarWindow;
    private TelephonyManager mTelephonyPhone = null;
    private boolean mUnregister = false;
    private PowerManager powerManager;

    private static class BackgrounCallingOnClickListener implements OnClickListener {
        private Context mContext;
        private TelecomManager mTelephonyPhone;

        public BackgrounCallingOnClickListener(Context context) {
            this.mContext = context;
        }

        public void onClick(View v) {
            BDReporter.c(this.mContext, 362);
            this.mTelephonyPhone = (TelecomManager) this.mContext.getSystemService("telecom");
            if (this.mTelephonyPhone != null) {
                this.mTelephonyPhone.showInCallScreen(false);
            }
        }
    }

    private static class BackgrounCallingOnTouchListener implements OnTouchListener {
        PhoneStatusBarView mStatusBarWindow = null;

        public BackgrounCallingOnTouchListener(PhoneStatusBarView statusBarWindow) {
            this.mStatusBarWindow = statusBarWindow;
        }

        public boolean onTouch(View v, MotionEvent event) {
            this.mStatusBarWindow.onTouchEvent(event);
            return false;
        }
    }

    private static class CallTimer extends Handler {
        private boolean isTimerRunning = false;
        private Runnable mCallback;
        private long mInterval;
        private CallTimerCallback mIntervalCallback;
        private long mLastReportedTime;

        private class CallTimerCallback implements Runnable {
            private CallTimerCallback() {
            }

            public void run() {
                CallTimer.this.periodicUpdateTimer();
            }
        }

        public CallTimer(Runnable callback) {
            this.mCallback = callback;
        }

        public void startTimer(long interval) {
            if (interval > 0) {
                if (this.mIntervalCallback == null) {
                    this.mIntervalCallback = new CallTimerCallback();
                }
                cancelTimer();
                this.isTimerRunning = true;
                this.mInterval = interval;
                this.mLastReportedTime = SystemClock.uptimeMillis();
                periodicUpdateTimer();
            }
        }

        public void cancelTimer() {
            removeCallbacks(this.mIntervalCallback);
            this.isTimerRunning = false;
        }

        private void periodicUpdateTimer() {
            if (this.isTimerRunning) {
                long nextReport = this.mLastReportedTime + this.mInterval;
                while (SystemClock.uptimeMillis() >= nextReport) {
                    nextReport += this.mInterval;
                }
                postAtTime(this.mIntervalCallback, nextReport);
                this.mLastReportedTime = nextReport;
                this.mCallback.run();
            }
        }
    }

    public BackgrounCallingLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.powerManager = (PowerManager) context.getSystemService("power");
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        creatHandler();
        this.mInCallingLayout = (LinearLayout) findViewById(R.id.is_on_calling);
        this.mCallLightImage = (ImageView) findViewById(R.id.call_going_ficker);
        this.mTelephonyPhone = TelephonyManager.from(getContext());
        setCalllinearlayoutShowing(false);
        this.mBackgrounCallingOnClickListener = new BackgrounCallingOnClickListener(getContext());
        setOnClickListener(this.mBackgrounCallingOnClickListener);
        IntentFilter callingFilter = new IntentFilter();
        callingFilter.addAction("InCallScreenIsForegroundActivity");
        getContext().registerReceiverAsUser(this.mCallingStatusReceiver, UserHandle.ALL, callingFilter, "com.android.systemui.permission.BackgrounCallingLayout", null);
        IntentFilter phoneStateFilter = new IntentFilter();
        phoneStateFilter.addAction("android.intent.action.PHONE_STATE");
        getContext().registerReceiver(this.mPhoneStatusReceiver, phoneStateFilter);
        TintTextView clock = (TintTextView) findViewById(R.id.clock);
        clock.setIsResever(false);
        clock.setTextColor(-1);
    }

    public static boolean getCalllinearlayoutShowing() {
        return sIsCalllinearlayouShowing;
    }

    public static void setCalllinearlayouShowing(boolean sIsCalllinearlayouShowing) {
        sIsCalllinearlayouShowing = sIsCalllinearlayouShowing;
    }

    public void setCalllinearlayoutShowing(boolean isCalllinearshow) {
        if (!isCalllinearshow) {
            setVisibility(8);
            if (this.mHandler != null) {
                this.mHandler.removeMessages(1);
            }
        }
        setCalllinearlayouShowing(isCalllinearshow);
        updateKeyguard();
    }

    public void updateStatusBarBackground() {
        initCallingText();
        if (this.mCallTextView != null) {
            if (isBackgroundRingingstate()) {
                this.mCallTextView.setText(R.string.call_going_return_incoming_call);
            } else {
                this.mCallTextView.setText(R.string.call_going_return_call);
            }
        }
        setVisibility(0);
        this.mCallLightImage.setBackgroundResource(R.drawable.call_notifi_bg);
        this.mHandler.sendEmptyMessage(1);
        setCalllinearlayoutShowing(true);
        if (mCallConnectedTimeMills != -1) {
            this.mCallTimer.startTimer(1000);
        }
    }

    private void creatHandler() {
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        float alpha = Math.abs(((float) ((System.currentTimeMillis() / 32) % 50)) - 25.0f) / 25.0f;
                        BackgrounCallingLayout.this.mHandler.removeMessages(1);
                        if (BackgrounCallingLayout.this.mCallLightImage != null) {
                            BackgrounCallingLayout.this.mCallLightImage.setAlpha(alpha);
                            BackgrounCallingLayout.this.mHandler.sendEmptyMessageDelayed(1, 32);
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        };
    }

    public boolean isTalking() {
        return this.mPhoneState != 0;
    }

    private int getPhoneState() {
        int simCount = this.mTelephonyPhone.getPhoneCount();
        for (int i = 0; i < simCount; i++) {
            int phoneState = this.mTelephonyPhone.getCallState(i);
            if (phoneState != 0) {
                HwLog.i("BackgrounCallingLayout", "simCount:" + simCount + " phoneState:" + phoneState);
                return phoneState;
            }
        }
        return 0;
    }

    private boolean isBackgroundRingingstate() {
        if (isForegroundActivity || 1 != getPhoneState()) {
            return false;
        }
        return this.powerManager.isInteractive();
    }

    private boolean isCallingInBackground() {
        if (isForegroundActivity) {
            return false;
        }
        return 2 == getPhoneState() || 1 == getPhoneState();
    }

    private void updateCallLayout(Intent intent) {
        boolean isBackground = isCallingInBackground();
        HwLog.i("BackgrounCallingLayout", "updateCallLayout:isBackground=" + isBackground + ", isInteractive=" + this.powerManager.isInteractive());
        if (isBackground) {
            mCallConnectedTimeMills = intent.getLongExtra("connectTimeMillis", -1);
            updateStatusBarBackground();
            if (mCallConnectedTimeMills != -1) {
                this.mCallTimer.startTimer(1000);
                return;
            }
            return;
        }
        if (this.mCallTimeTextView != null) {
            this.mCallTimeTextView.setText(BuildConfig.FLAVOR);
        }
        setCalllinearlayoutShowing(false);
        if (this.mCallTimer != null) {
            this.mCallTimer.cancelTimer();
        }
    }

    private void updateCallTime() {
        Log.d("BackgrounCallingLayout", "in updateCallTime, mCallConnectedTimeMills=" + mCallConnectedTimeMills);
        long duration = SystemClock.elapsedRealtime() - mCallConnectedTimeMills;
        if (this.mCallTimeTextView != null && duration != 0) {
            this.mCallTimeTextView.setText(DateUtils.formatElapsedTime(duration / 1000));
        }
    }

    public void initCallingText() {
        LayoutParams mLayoutParams = new LayoutParams(-1, -1);
        View mTempView = View.inflate(getContext(), R.layout.is_on_calling, null);
        this.mCallTextView = (TextView) mTempView.findViewById(R.id.call_text);
        this.mCallTimeTextView = (TextView) mTempView.findViewById(R.id.call_time_text);
        if (this.mInCallingLayout != null) {
            this.mInCallingLayout.removeAllViews();
            this.mInCallingLayout.addView(mTempView, mLayoutParams);
        }
        if (this.mCallTextView != null) {
            this.mCallTextView.setText(R.string.call_going_return_call);
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        dismiss();
        this.mCallTimer.cancelTimer();
    }

    public void setStatusBarView(PhoneStatusBarView statusBarWindow) {
        this.mStatusBarWindow = statusBarWindow;
        this.mBackgrounCallingOnTouchListener = new BackgrounCallingOnTouchListener(this.mStatusBarWindow);
        setOnTouchListener(this.mBackgrounCallingOnTouchListener);
    }

    public void dismiss() {
        if (!this.mUnregister) {
            getContext().unregisterReceiver(this.mCallingStatusReceiver);
            getContext().unregisterReceiver(this.mPhoneStatusReceiver);
            this.mUnregister = true;
        }
        this.mCallTextView = null;
        this.mCallTimeTextView = null;
    }

    public void onKeyguardShowing(boolean show) {
        this.mShowKeyguard = show;
        updateKeyguard();
    }

    public void updateKeyguard() {
        if (this.mShowKeyguard && HwPhoneStatusBar.getInstance().getKeyguardStatusBarView() != null) {
            if (sIsCalllinearlayouShowing) {
                HwPhoneStatusBar.getInstance().getKeyguardStatusBarView().setAlpha(0.0f);
                HwPhoneStatusBar.getInstance().getKeyguardStatusBarView().setVisibility(4);
            } else {
                HwPhoneStatusBar.getInstance().getKeyguardStatusBarView().setAlpha(1.0f);
                HwPhoneStatusBar.getInstance().getKeyguardStatusBarView().setVisibility(0);
            }
        }
    }

    public boolean isIsCalllinearlayouShowing() {
        return sIsCalllinearlayouShowing;
    }

    public boolean isInterestEvent(MotionEvent event) {
        return getHeight() >= ((int) event.getY());
    }
}
