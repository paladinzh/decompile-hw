package com.huawei.systemmanager.applock.password;

import com.huawei.systemmanager.R;
import com.huawei.systemmanager.applock.password.callback.BackPressedCallback;
import com.huawei.systemmanager.applock.utils.compatibility.AppLockPwdUtils;
import com.huawei.systemmanager.util.HwLog;

public abstract class AbsSetPasswordFragment extends AbsPasswordFragment implements BackPressedCallback {
    private static final String TAG = "SetPasswordFragmentBase";
    private String mFirstInput = null;
    private boolean mIsConfirmInput = false;

    abstract int confirmInputDefaultHintId();

    abstract boolean doInputFirstCheck(String str);

    abstract int firstInputDefaultHintId();

    abstract void postPasswordSetFinished();

    protected int getPasswordHint() {
        return firstInputDefaultHintId();
    }

    protected int getPwdFragmentLayoutId() {
        return R.layout.app_lock_set_password;
    }

    protected void inputFinished(String password) {
        if (this.mIsConfirmInput) {
            doInputFinishedConfirm(password);
        } else {
            doInputFinishedFirst(password);
        }
    }

    public boolean onBackButtonPressed() {
        if (this.mIsConfirmInput) {
            HwLog.d(TAG, "onBackButtonPressed: is in confirm input!");
            changeToFirstInput();
            return true;
        }
        HwLog.d(TAG, "onBackButtonPressed: is in first input!");
        return false;
    }

    protected boolean shouldShowForgetPwd() {
        return false;
    }

    private void doInputFinishedFirst(String password) {
        if (doInputFirstCheck(password)) {
            changeToConfirmInput(password);
        }
    }

    private void doInputFinishedConfirm(String password) {
        if (this.mFirstInput.equals(password)) {
            setPasswordAndDoPost(password);
        } else {
            promptDifferentConfirmPassword();
        }
    }

    private void changeToFirstInput() {
        clearPasswordBuf();
        updatePasswordHintMsg(firstInputDefaultHintId());
        this.mIsConfirmInput = false;
        this.mFirstInput = null;
    }

    private void changeToConfirmInput(String firstInput) {
        clearPasswordBuf();
        updatePasswordHintMsg(confirmInputDefaultHintId());
        this.mFirstInput = firstInput;
        this.mIsConfirmInput = true;
    }

    private void promptDifferentConfirmPassword() {
        updatePasswordHintMsg((int) R.string.applock_set_reset_password_different);
        clearPasswordBuf();
    }

    private void setPasswordAndDoPost(String password) {
        AppLockPwdUtils.setPassword(getActivity().getApplicationContext(), password);
        postPasswordSetFinished();
    }
}
