package com.android.keyguard;

import android.content.Context;
import android.os.SystemProperties;
import com.huawei.keyguard.util.KeyguardToast;

public class HwCustKeyguardAbsKeyInputViewImpl extends HwCustKeyguardAbsKeyInputView {
    public void showPINChangeSuccessToast(Context context) {
        if ("true".equalsIgnoreCase(SystemProperties.get("ro.config.show_pin_change", "false"))) {
            KeyguardToast.makeText(context, R$string.puk_unblock_succeeded, 1).show();
        }
    }
}
