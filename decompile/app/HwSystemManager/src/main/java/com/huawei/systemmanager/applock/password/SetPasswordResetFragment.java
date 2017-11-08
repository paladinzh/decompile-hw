package com.huawei.systemmanager.applock.password;

import com.huawei.systemmanager.R;
import com.huawei.systemmanager.applock.password.callback.ActivityPostCallback;
import com.huawei.systemmanager.applock.utils.compatibility.AppLockPwdUtils;

public class SetPasswordResetFragment extends AbsSetPasswordFragment {
    protected int getPwdFragmentTitle() {
        return R.string.applock_reset_password_title;
    }

    int firstInputDefaultHintId() {
        return R.string.applock_reset_password_input;
    }

    int confirmInputDefaultHintId() {
        return R.string.applock_reset_password_confirm;
    }

    boolean doInputFirstCheck(String password) {
        if (!AppLockPwdUtils.verifyPassword(getActivity().getApplicationContext(), password)) {
            return true;
        }
        promptSameWithOldPassword();
        clearPasswordBuf();
        return false;
    }

    void postPasswordSetFinished() {
        ((ActivityPostCallback) getActivity()).onPostFinish();
    }

    private void promptSameWithOldPassword() {
        updatePasswordHintMsg((int) R.string.applock_reset_password_failed_same_as_old);
    }
}
