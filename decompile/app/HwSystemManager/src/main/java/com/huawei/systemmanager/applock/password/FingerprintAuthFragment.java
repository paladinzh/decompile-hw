package com.huawei.systemmanager.applock.password;

import android.app.Activity;
import android.app.KeyguardManager;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.common.base.Strings;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.applock.fingerprint.FingerprintAuthUtils;
import com.huawei.systemmanager.applock.fingerprint.IFingerprintAuth;
import com.huawei.systemmanager.applock.fingerprint.IFingerprintAuthCallback;
import com.huawei.systemmanager.applock.password.callback.ActivityPostCallback;
import com.huawei.systemmanager.applock.utils.compatibility.AppLockPwdUtils;
import com.huawei.systemmanager.applock.utils.compatibility.AuthRetryUtil;
import com.huawei.systemmanager.applock.utils.compatibility.AuthRetryUtil.TimeKeeperSuffix;
import com.huawei.systemmanager.applock.utils.sp.FingerprintBindUtils;
import com.huawei.systemmanager.applock.utils.sp.LockingPackageUtils;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;

public class FingerprintAuthFragment extends AbsPasswordAuthFragment implements IFingerprintAuthCallback {
    private static final int HANDLE_FINISH_ATTACHED_ACTIVITY_MSG = 2;
    private static final int HANDLE_RESUME_FINGERPRINT_MSG = 1;
    private static final String TAG = "FingerprintAuthFragment";
    private IFingerprintAuth mFingerAuth = null;
    private ImageView mFingerImage = null;
    private View mFingerLayout = null;
    private boolean mFirstResumeFinger = true;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    FingerprintAuthFragment.this.resumeFingerprint();
                    return;
                case 2:
                    FingerprintAuthFragment.this.finishAttachedActivity();
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mHandlerResumeFingerMsg = false;
    private boolean mIdentified = false;
    private MyCountDown mMyCountDown = null;
    private View mPinLayout = null;
    private TextView mScanHintTv = null;
    private TextView mScanTitleTv = null;
    private boolean mUserClickChgPin = false;

    private class MyCountDown extends CountDownTimer {
        public MyCountDown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        public void onTick(long millisUntilFinished) {
            int remainedSecs = (int) (millisUntilFinished / 1000);
            FingerprintAuthFragment.this.mScanHintTv.setText(FingerprintAuthFragment.this.mAppContext.getResources().getQuantityString(R.plurals.applock_fingerprint_error_locked, remainedSecs, new Object[]{Integer.valueOf(remainedSecs)}));
        }

        public void onFinish() {
            FingerprintAuthFragment.this.setDefaultImgAndHint();
            FingerprintAuthFragment.this.mHandlerResumeFingerMsg = false;
            FingerprintAuthFragment.this.sendResumeFingerprintMsg(0);
        }
    }

    public void onPause() {
        HwLog.v(TAG, "onPause");
        removeResumeFingerprintMsg();
        releaseFingerprint();
        this.mHandlerResumeFingerMsg = false;
        if (this.mMyCountDown != null) {
            this.mMyCountDown.cancel();
        }
        super.onPause();
    }

    public void onResume() {
        HwLog.v(TAG, "onResume");
        super.onResume();
        this.mHandlerResumeFingerMsg = false;
        setDefaultImgAndHint();
        if (this.mFirstResumeFinger) {
            this.mFirstResumeFinger = false;
            sendResumeFingerprintMsg(0);
            return;
        }
        if (FingerprintBindUtils.getFingerprintBindStatus(this.mAppContext) && !this.mUserClickChgPin) {
            changeToFingerAuthView();
        }
        sendResumeFingerprintMsg(1500);
    }

    public void onDestroy() {
        super.onDestroy();
        HwLog.v(TAG, "onDestroy");
        if (this.mFingerAuth != null) {
            this.mFingerAuth.release();
            this.mFingerAuth = null;
        }
    }

    protected int getPwdFragmentLayoutId() {
        return R.layout.app_lock_login_auth;
    }

    protected int getPwdFragmentTitle() {
        return R.string.ActionBar_EnterAppLock_Title;
    }

    protected int getPasswordHint() {
        return R.string.applock_verify_password_tip;
    }

    protected void initExtendDataOnCreate() {
    }

    protected void initExtendViewOnCreateView(View view) {
        initFingerprintViews(view);
    }

    protected void inputFinished(String password) {
        if (AppLockPwdUtils.verifyPassword(getActivity().getApplicationContext(), password)) {
            doAuthSuccess(true);
        } else {
            promptPasswordInputFailed();
        }
    }

    public void onFingerprintAuthSuccess() {
        HwLog.i(TAG, "Fingerprint onIdentified");
        this.mIdentified = true;
        releaseFingerprint();
        this.mScanHintTv.setText(R.string.applock_fingerprint_scan_success);
        this.mFingerImage.setImageResource(R.drawable.fingerprint_success);
        doAuthSuccess(false);
    }

    public void onFingerprintAuthFailed() {
        HwLog.i(TAG, "onFingerprintAuthFailed");
        this.mFingerImage.startAnimation(this.mShakeAnimation);
        promptFingerprintScanFailed(false);
    }

    public void onFingerprintAuthLockout() {
        HwLog.i(TAG, "onFingerprintAuthLockout");
        this.mFingerImage.setImageResource(R.drawable.fingerprint_forbidden);
        promptFingerprintScanFailed(true);
    }

    public void resumeFingerprintImmediately() {
        HwLog.v(TAG, "resumeFingerprintImmediately");
        sendResumeFingerprintMsg(0);
    }

    private void resumeFingerprint() {
        this.mHandlerResumeFingerMsg = true;
        if (this.mUserClickChgPin) {
            HwLog.w(TAG, "resumeFingerprint abort because user changed to pin view already");
        } else if (this.mIdentified) {
            HwLog.v(TAG, "resumeFingerprint mIdentified true");
        } else if (((KeyguardManager) this.mAppContext.getSystemService("keyguard")).isKeyguardLocked()) {
            HwLog.w(TAG, "resumeFingerprint keyguard is locked, ignore");
        } else {
            HwLog.d(TAG, "Fingerprint createAuth");
            if (this.mFingerAuth == null) {
                this.mFingerAuth = FingerprintAuthUtils.createAuth(this.mAppContext);
            }
            if (AuthRetryUtil.isTimeKeeperLocking(this.mAppContext, TimeKeeperSuffix.SUFFIX_APPLOCK_PASSWORD) || this.mFingerAuth == null || !FingerprintAuthUtils.checkFingerprintReadyNotClose(this.mFingerAuth) || !FingerprintBindUtils.getFingerprintBindStatus(this.mAppContext)) {
                HwLog.w(TAG, "resumeFingerprint not match the condition that using fingerprint!");
                this.mFingerImage.setContentDescription("Finger Device exception!");
                changeToPinAuthView(false);
                return;
            }
            HwLog.i(TAG, "Fingerprint startIdentify");
            this.mFingerAuth.startAuthenticate(this);
            changeToFingerAuthView();
            removeResumeFingerprintMsg();
        }
    }

    private void releaseFingerprint() {
        if (this.mFingerAuth != null) {
            HwLog.v(TAG, "Fingerprint closeAuth");
            FingerprintAuthUtils.closeAuth(this.mFingerAuth);
            return;
        }
        HwLog.v(TAG, "Fingerprint closeAuth already closed!");
    }

    private void initFingerprintViews(View view) {
        Button chgPinBtn = (Button) view.findViewById(R.id.app_lock_change_to_pin_btn);
        this.mFingerImage = (ImageView) view.findViewById(R.id.app_lock_finger_scan_image);
        chgPinBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                FingerprintAuthFragment.this.changeToPinAuthView(true);
            }
        });
        ((Button) view.findViewById(R.id.app_lock_cancel_btn)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                Activity activity = FingerprintAuthFragment.this.getActivity();
                if (activity != null) {
                    activity.onBackPressed();
                }
            }
        });
        this.mPinLayout = view.findViewById(R.id.app_lock_pin_auth_layout);
        this.mFingerLayout = view.findViewById(R.id.app_lock_finger_auth_layout);
        this.mScanTitleTv = (TextView) view.findViewById(R.id.app_lock_finger_scan_title);
        this.mScanHintTv = (TextView) view.findViewById(R.id.app_lock_finger_scan_hint);
        if (!Strings.isNullOrEmpty(LockingPackageUtils.getLockingPackageName(this.mAppContext, ""))) {
            this.mScanTitleTv.setText(this.mAppContext.getString(R.string.applock_fingerprint_access_otherapp_title, new Object[]{HsmPackageManager.getInstance().getLabel(unlockPkg).trim()}));
        }
    }

    private void setDefaultImgAndHint() {
        this.mFingerImage.setImageResource(R.drawable.img_fingerprint_app_lock);
        this.mScanHintTv.setText(R.string.applock_fingerprint_scan_finger_hint);
    }

    private void changeToPinAuthView(boolean userClick) {
        this.mPinLayout.setVisibility(0);
        this.mFingerLayout.setVisibility(8);
        if (userClick) {
            this.mUserClickChgPin = true;
        }
        releaseFingerprint();
    }

    private void changeToFingerAuthView() {
        this.mPinLayout.setVisibility(8);
        this.mFingerLayout.setVisibility(0);
    }

    private void promptFingerprintScanFailed(boolean isLockOut) {
        if (this.mFingerAuth == null) {
            HwLog.w(TAG, "promptFingerprintScanFailed mFingerAuth is null, isLockOut=" + isLockOut);
            return;
        }
        HwLog.i(TAG, "promptFingerprintScanFailed isLockOut=" + isLockOut);
        if (isLockOut) {
            long failedTime = this.mFingerAuth.getRemainedTime();
            int remainedSecs = (int) (failedTime / 1000);
            this.mScanHintTv.setText(this.mAppContext.getResources().getQuantityString(R.plurals.applock_fingerprint_error_locked, remainedSecs, new Object[]{Integer.valueOf(remainedSecs)}));
            if (this.mMyCountDown != null) {
                this.mMyCountDown.cancel();
            }
            this.mMyCountDown = new MyCountDown(failedTime, 1000);
            this.mMyCountDown.start();
            releaseFingerprint();
        } else {
            int remainedNum = this.mFingerAuth.getRemainedNum();
            this.mScanHintTv.setText(this.mAppContext.getResources().getQuantityString(R.plurals.applock_fingerprint_error_try_more, remainedNum, new Object[]{Integer.valueOf(remainedNum)}));
        }
    }

    private void doAuthSuccess(boolean isPasswordVerify) {
        HwLog.d(TAG, "doAuthInputSuccess");
        String[] strArr = new String[2];
        strArr[0] = HsmStatConst.PARAM_OP;
        strArr[1] = isPasswordVerify ? "1" : "0";
        HsmStat.statE((int) Events.E_APPLOCK_IS_FINGER_OR_PASSWORD, HsmStatConst.constructJsonParams(strArr));
        clearPasswordRetryRecords();
        if (isPasswordVerify) {
            this.mHandler.sendEmptyMessage(2);
        } else {
            this.mHandler.sendEmptyMessageDelayed(2, 500);
        }
    }

    private void sendResumeFingerprintMsg(long resumeDelay) {
        if (!this.mHandlerResumeFingerMsg) {
            this.mHandler.removeMessages(1);
            Message msg = this.mHandler.obtainMessage(1);
            if (0 < resumeDelay) {
                this.mHandler.sendMessageDelayed(msg, resumeDelay);
            } else {
                this.mHandler.sendMessage(msg);
            }
        }
    }

    private void removeResumeFingerprintMsg() {
        this.mHandler.removeMessages(1);
    }

    private void finishAttachedActivity() {
        ActivityPostCallback callback = (ActivityPostCallback) getActivity();
        HwLog.i(TAG, "finishAttachedActivity callback is null ? " + (callback == null));
        if (callback != null) {
            callback.onPostFinish();
        }
    }
}
