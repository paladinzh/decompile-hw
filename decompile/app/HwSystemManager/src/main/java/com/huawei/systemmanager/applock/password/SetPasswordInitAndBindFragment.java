package com.huawei.systemmanager.applock.password;

import com.huawei.systemmanager.applock.password.callback.ActivityPostCallback;
import com.huawei.systemmanager.applock.utils.sp.FingerprintBindUtils;

public class SetPasswordInitAndBindFragment extends SetPasswordInitFragment {
    void postPasswordSetFinished() {
        FingerprintBindUtils.setFingerprintBindStatus(getActivity(), true);
        ((ActivityPostCallback) getActivity()).onPostFinish();
    }
}
