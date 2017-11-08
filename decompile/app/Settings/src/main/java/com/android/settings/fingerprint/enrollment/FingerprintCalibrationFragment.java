package com.android.settings.fingerprint.enrollment;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.settings.fingerprint.utils.BiometricManager;
import com.android.settings.fingerprint.utils.BiometricManager.CalibrationCallback;
import com.android.settings.fingerprint.utils.BiometricManager.CalibrationProgress;
import com.android.settings.sdencryption.view.MainCircleProgressView;
import com.android.settings.sdencryption.view.MainScreenRollingView;

public class FingerprintCalibrationFragment extends Fragment implements OnClickListener, CalibrationCallback {
    private Activity mActivity;
    private BiometricManager mBiometricManager;
    private int mCalibState = 0;
    private int mCurProgress = 0;
    private AlertDialog mErrorDialog;
    private Handler mEventHandler = new ProgressHandler();
    private Button mFinishButton;
    private boolean mFinished = false;
    private MainCircleProgressView mMainProgressView;
    private TextView mMainTip;
    private TextView mMainTitle;
    private TextView mProgressUnit;
    private MainScreenRollingView mRollingView;
    private AlphaAnimation mTipInAnim = new AlphaAnimation(0.0f, 1.0f);
    private AlphaAnimation mTipOutAnim = new AlphaAnimation(1.0f, 0.0f);
    private int mTipState = 0;
    private TextSwitchAnimListener mTipSwitchAnimListener;
    private int mUserId;

    private class ErrorDialogListener implements DialogInterface.OnClickListener {
        private ErrorDialogListener() {
        }

        public void onClick(DialogInterface dialog, int which) {
            if (FingerprintCalibrationFragment.this.mActivity != null && !FingerprintCalibrationFragment.this.mActivity.isFinishing()) {
                FingerprintCalibrationFragment.this.mActivity.finish();
            }
        }
    }

    private class ProgressHandler extends Handler {
        private ProgressHandler() {
        }

        public void handleMessage(Message msg) {
            Log.d("FingerprintCalibrationFragment", "msg handled = " + msg.what);
            Activity activity = FingerprintCalibrationFragment.this.getActivity();
            if (activity == null || activity.isFinishing()) {
                Log.w("FingerprintCalibrationFragment", "activity not available, ignore message = " + msg.what);
                return;
            }
            if (msg.what == 100) {
                FingerprintCalibrationFragment.this.handleMessageUpdate(msg);
            } else if (msg.what == 101) {
                FingerprintCalibrationFragment.this.handleMessageUpdateTip(msg);
            } else if (msg.what == 102) {
                FingerprintCalibrationFragment.this.handleMessageRestoreTip(msg);
            } else if (msg.what == 104) {
                FingerprintCalibrationFragment.this.handleMessageTerminate(msg);
            } else if (msg.what == 103) {
                FingerprintCalibrationFragment.this.handleMessageAlreadyCalib(msg);
            }
        }
    }

    public static class TextSwitchAnimListener implements AnimationListener {
        public Animation nextAnim;
        public int nextTextResId = 0;
        public TextView textView;

        public TextSwitchAnimListener(TextView textView, int nextTextResId, Animation nextAnim) {
            this.textView = textView;
            this.nextTextResId = nextTextResId;
            this.nextAnim = nextAnim;
        }

        public void onAnimationStart(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
            this.textView.setText(this.nextTextResId);
            this.textView.startAnimation(this.nextAnim);
        }

        public void onAnimationRepeat(Animation animation) {
        }
    }

    private void handleMessageTerminate(Message msg) {
        Log.d("FingerprintCalibrationFragment", "handleMessageTerminate enter, errcode = " + msg.arg1);
        if (this.mCalibState == 1) {
            Log.w("FingerprintCalibrationFragment", "hardware error after calibration complete, do nothing. errcode = " + msg.arg1);
        } else if (this.mCalibState == 2) {
            Log.w("FingerprintCalibrationFragment", "hardware error already claimed, do nothing. errcode = " + msg.arg1);
        } else {
            alertError(msg.arg1);
            this.mCalibState = 2;
        }
    }

    private void handleMessageUpdateTip(Message msg) {
        Log.d("FingerprintCalibrationFragment", "handleMessageUpdateTip enter");
        if (this.mCalibState != 0) {
            Log.w("FingerprintCalibrationFragment", "ignore tip update cause calibration state = " + this.mCalibState);
        } else {
            switchTextWithAlpha(this.mMainTip, 2131628949, this.mTipInAnim, this.mTipOutAnim, this.mTipSwitchAnimListener);
        }
    }

    private void handleMessageRestoreTip(Message msg) {
        Log.d("FingerprintCalibrationFragment", "handleMessageRestoreTip enter");
        if (this.mCalibState != 0) {
            Log.w("FingerprintCalibrationFragment", "ignore tip restore cause calibration state = " + this.mCalibState);
        } else if (this.mTipState != 1) {
            Log.w("FingerprintCalibrationFragment", "ignore tip restore cause tip state = " + this.mTipState);
        } else {
            switchTextWithAlpha(this.mMainTip, 2131628948, this.mTipInAnim, this.mTipOutAnim, this.mTipSwitchAnimListener);
            this.mTipState = 0;
        }
    }

    private void handleMessageAlreadyCalib(Message msg) {
        Log.d("FingerprintCalibrationFragment", "handleMessageAlreadyCalib enter");
        if (this.mCalibState != 0) {
            Log.w("FingerprintCalibrationFragment", "ignore already calibration cause calibration state = " + this.mCalibState);
        } else {
            completeCalibration();
        }
    }

    private void completeCalibration() {
        this.mMainProgressView.updateScoreImmidiately(100);
        this.mRollingView.setNumberImmediately(100);
        this.mFinishButton.setVisibility(0);
        this.mCalibState = 1;
        switchTextWithAlpha(this.mMainTip, 2131628947, this.mTipInAnim, this.mTipOutAnim, this.mTipSwitchAnimListener);
        Log.i("FingerprintCalibrationFragment", "Calibration completed!");
    }

    private void handleMessageUpdate(Message msg) {
        int target = msg.arg1;
        int delta = target - msg.arg2;
        Log.i("FingerprintCalibrationFragment", "handleMessageUpdate, new = " + target + ", delta = " + delta);
        if (this.mCalibState != 0) {
            Log.w("FingerprintCalibrationFragment", "ignore progress update cause calibration state = " + this.mCalibState);
            return;
        }
        if (target >= 100) {
            completeCalibration();
        } else {
            int duration = (delta * 5760) / 100;
            this.mMainProgressView.updateSocre(target, duration);
            this.mRollingView.setNumberByDuration(target, duration);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(2130968780, container, false);
        adjustViewSize(fragmentView);
        prepareViews(fragmentView);
        return fragmentView;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adjustActionBar();
    }

    private void adjustActionBar() {
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void init() {
        this.mActivity = getActivity();
        this.mBiometricManager = BiometricManager.open(this.mActivity);
        this.mUserId = this.mActivity.getIntent().getIntExtra("android.intent.extra.USER", UserHandle.myUserId());
    }

    private void prepareViews(View fragmentView) {
        this.mMainTitle = (TextView) fragmentView.findViewById(2131886581);
        this.mMainTip = (TextView) fragmentView.findViewById(2131886582);
        this.mMainProgressView = (MainCircleProgressView) fragmentView.findViewById(2131886584);
        this.mRollingView = (MainScreenRollingView) fragmentView.findViewById(2131886585);
        this.mProgressUnit = (TextView) fragmentView.findViewById(2131886586);
        this.mFinishButton = (Button) fragmentView.findViewById(2131886588);
        updateView();
    }

    private void updateView() {
        this.mProgressUnit.setVisibility(0);
        this.mFinishButton.setOnClickListener(this);
        this.mFinishButton.setVisibility(8);
        this.mMainProgressView.setCompleteStatus();
        this.mMainProgressView.updateScoreImmidiately(0);
        this.mTipInAnim.setDuration(250);
        this.mTipOutAnim.setDuration(250);
        this.mTipSwitchAnimListener = new TextSwitchAnimListener(this.mMainTip, -1, this.mTipInAnim);
        this.mTipOutAnim.setAnimationListener(this.mTipSwitchAnimListener);
        this.mMainTitle.setText(2131628951);
        this.mMainTip.setText(2131628948);
    }

    private void adjustViewSize(View fragmentView) {
        RelativeLayout layout = (RelativeLayout) fragmentView.findViewById(2131886583);
        DisplayMetrics displayMetrics = this.mActivity.getResources().getDisplayMetrics();
        layout.getLayoutParams().width = displayMetrics.widthPixels / 2;
        layout.getLayoutParams().height = layout.getLayoutParams().width;
    }

    private void switchTextWithAlpha(TextView textView, int resId, AlphaAnimation inAnim, AlphaAnimation outAnim, TextSwitchAnimListener listener) {
        if (listener == null) {
            listener = new TextSwitchAnimListener(textView, resId, inAnim);
        }
        textView.clearAnimation();
        inAnim.setDuration(250);
        outAnim.setDuration(250);
        listener.textView = textView;
        listener.nextTextResId = resId;
        listener.nextAnim = inAnim;
        outAnim.setAnimationListener(listener);
        textView.startAnimation(outAnim);
    }

    public void onClick(View v) {
        finishActivity(4000);
    }

    public void onResume() {
        super.onResume();
        update();
    }

    public void onPause() {
        super.onPause();
        clear();
    }

    public void onCalibrationProgress(CalibrationProgress progress) {
        if (progress == null) {
            Log.e("FingerprintCalibrationFragment", "CalibrationProgress is null!");
            return;
        }
        int newProgress = (progress.currentStep * 100) / progress.totalSteps;
        if (newProgress <= this.mCurProgress) {
            Log.i("FingerprintCalibrationFragment", "invalid new progress, new = " + newProgress + ", current = " + this.mCurProgress);
            return;
        }
        int tempCurProgress = this.mCurProgress;
        this.mCurProgress = newProgress;
        this.mEventHandler.sendMessage(this.mEventHandler.obtainMessage(100, newProgress, tempCurProgress));
    }

    public void onCalibrationHelp(int code) {
        Log.d("FingerprintCalibrationFragment", "onCalibrationHelp! code = " + code);
        if (code == 2018) {
            if (this.mTipState == 0) {
                this.mEventHandler.removeMessages(101);
                this.mEventHandler.sendEmptyMessage(101);
                this.mTipState = 1;
            }
            this.mEventHandler.removeMessages(102);
            this.mEventHandler.sendMessageDelayed(this.mEventHandler.obtainMessage(102), 1250);
        }
    }

    public void onCalibrationError(int code) {
        Log.d("FingerprintCalibrationFragment", "onCalibrationError! code = " + code);
        if (code == 1103) {
            this.mEventHandler.sendEmptyMessage(103);
        } else {
            this.mEventHandler.sendMessage(this.mEventHandler.obtainMessage(104, code, 0));
        }
    }

    private void finishActivity(int resultCode) {
        this.mActivity = getActivity();
        if (this.mActivity != null && !this.mActivity.isFinishing()) {
            this.mActivity.setResult(resultCode);
            this.mActivity.finish();
        }
    }

    private void update() {
        if (this.mBiometricManager != null) {
            this.mBiometricManager.startCalibration(this, this.mUserId);
        }
        updateView();
        this.mTipState = 0;
        this.mCalibState = 0;
        this.mCurProgress = 0;
    }

    private void clear() {
        if (this.mErrorDialog != null && this.mErrorDialog.isShowing()) {
            this.mErrorDialog.dismiss();
        }
        if (this.mCalibState != 1) {
            if (this.mBiometricManager != null) {
                this.mBiometricManager.cancelCalibration();
            }
            finishActivity(0);
            return;
        }
        finishActivity(4000);
    }

    private void alertError(int code) {
        if (isAdded()) {
            if (this.mErrorDialog == null) {
                Builder builder = new Builder(this.mActivity);
                builder.setTitle(2131624537);
                builder.setMessage(String.valueOf(code));
                builder.setPositiveButton(2131627651, new ErrorDialogListener());
                builder.setCancelable(false);
                this.mErrorDialog = builder.create();
            }
            if (!this.mErrorDialog.isShowing()) {
                this.mErrorDialog.show();
            }
        }
    }
}
