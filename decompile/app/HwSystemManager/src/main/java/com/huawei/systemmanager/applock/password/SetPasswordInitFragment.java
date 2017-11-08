package com.huawei.systemmanager.applock.password;

import android.content.Intent;
import com.huawei.systemmanager.R;

public class SetPasswordInitFragment extends AbsSetPasswordFragment {
    protected int getPwdFragmentTitle() {
        return R.string.applock_init_password_title;
    }

    int firstInputDefaultHintId() {
        return R.string.applock_init_password_input;
    }

    int confirmInputDefaultHintId() {
        return R.string.applock_init_password_confirm;
    }

    void postPasswordSetFinished() {
        getActivity().startActivity(new Intent(getContext(), PasswordProtectInitActivity.class));
    }

    boolean doInputFirstCheck(String password) {
        return true;
    }
}
