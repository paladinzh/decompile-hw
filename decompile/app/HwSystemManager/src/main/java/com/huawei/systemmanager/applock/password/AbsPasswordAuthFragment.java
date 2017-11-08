package com.huawei.systemmanager.applock.password;

import com.huawei.systemmanager.R;
import com.huawei.systemmanager.applock.utils.compatibility.AppLockPwdUtils;
import com.huawei.systemmanager.applock.utils.compatibility.AuthRetryUtil;
import com.huawei.systemmanager.applock.utils.compatibility.AuthRetryUtil.TimeKeeperSuffix;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.timekeeper.TimeKeeper;
import com.huawei.timekeeper.TimeObserver;
import com.huawei.timekeeper.TimeTickInfo;

public abstract class AbsPasswordAuthFragment extends AbsPasswordFragment implements TimeObserver {
    private static final String TAG = "AbsPasswordAuthFragment";
    private TimeKeeper mTimeKeeper = null;

    public void onResume() {
        super.onResume();
        if (this.mTimeKeeper == null) {
            this.mTimeKeeper = AuthRetryUtil.getTimeKeeper(this.mAppContext, TimeKeeperSuffix.SUFFIX_APPLOCK_PASSWORD);
        }
        if (!checkAndStartPasswordCountdown()) {
            resumeDefaultComponentAttr();
        }
    }

    public void onPause() {
        unregisterTimeKeeperCallback();
        super.onPause();
    }

    protected boolean shouldShowForgetPwd() {
        if (AppLockPwdUtils.isPasswordSet(getActivity().getApplicationContext())) {
            return AppLockPwdUtils.isPasswordProtectionSet(getActivity().getApplicationContext());
        }
        return false;
    }

    public void onTimeTick(TimeTickInfo timeTickInfo) {
        updatePasswordHintMsg(AuthRetryUtil.getRemainingLockoutTime(this.mAppContext, timeTickInfo));
    }

    public void onTimeFinish() {
        HwLog.i(TAG, "onTimeFinish called");
        resumeDefaultComponentAttr();
        unregisterTimeKeeperCallback();
    }

    protected void promptPasswordInputFailed() {
        HwLog.d(TAG, "promptPasswordInputFailed");
        shakePhone();
        shakeView();
        increasePasswordRetry();
    }

    protected void clearPasswordRetryRecords() {
        AuthRetryUtil.resetTimeKeeper(this.mAppContext, TimeKeeperSuffix.SUFFIX_APPLOCK_PASSWORD);
        AuthRetryUtil.resetTimeKeeper(this.mAppContext, TimeKeeperSuffix.SUFFIX_APPLOCK_ANSWER);
    }

    private void increasePasswordRetry() {
        if (this.mTimeKeeper.addErrorCount() == 0) {
            startPasswordCountdown();
            return;
        }
        updatePasswordHintMsg(this.mAppContext.getString(R.string.app_lock_passwd_error, new Object[]{Utility.getLocaleNumber(leftTime)}));
    }

    private void startPasswordCountdown() {
        clearPasswordBuf();
        setNumPadEnabled(false);
        registerTimeKeeperCallback();
    }

    private boolean checkAndStartPasswordCountdown() {
        int remainChange = this.mTimeKeeper.getRemainingChance();
        HwLog.i(TAG, "checkAndStartPasswordCountdown, remainChange:" + remainChange);
        if (remainChange > 0) {
            return false;
        }
        startPasswordCountdown();
        return true;
    }

    private void resumeDefaultComponentAttr() {
        setNumPadEnabled(true);
        updatePasswordHintMsg(getPasswordHint());
    }

    private void registerTimeKeeperCallback() {
        if (this.mTimeKeeper != null) {
            HwLog.i(TAG, "registerTimeKeeperCallback");
            this.mTimeKeeper.registerObserver(this);
        }
    }

    private void unregisterTimeKeeperCallback() {
        if (this.mTimeKeeper != null && this.mTimeKeeper.isObserverRegistered(this)) {
            HwLog.i(TAG, "unregisterTimeKeeperCallback");
            this.mTimeKeeper.unregisterObserver(this);
        }
    }
}
