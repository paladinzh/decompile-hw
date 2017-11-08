package com.huawei.systemmanager.applock.password;

import com.huawei.systemmanager.R;
import com.huawei.systemmanager.applock.password.callback.ActivityPostCallback;
import com.huawei.systemmanager.applock.utils.compatibility.AppLockPwdUtils;
import com.huawei.systemmanager.applock.utils.sp.FingerprintBindUtils;

public class BindFingerprintAuthFragment extends AbsPasswordAuthFragment {
    protected int getPwdFragmentLayoutId() {
        return R.layout.app_lock_bind_fingerprint_auth;
    }

    protected int getPwdFragmentTitle() {
        return R.string.ActionBar_EnterAppLock_Title;
    }

    protected int getPasswordHint() {
        return R.string.applock_verify_password_tip;
    }

    protected void inputFinished(String password) {
        if (AppLockPwdUtils.verifyPassword(getActivity().getApplicationContext(), password)) {
            setFingerprintBinder();
        } else {
            promptPasswordInputFailed();
        }
    }

    private void setFingerprintBinder() {
        FingerprintBindUtils.setFingerprintBindStatus(getActivity().getApplicationContext(), true);
        ((ActivityPostCallback) getActivity()).onPostFinish();
    }
}
