package com.android.settings;

import android.content.Context;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.widget.Toast;

public class HwCustIccLockSettingsImpl extends HwCustIccLockSettings {
    private static final String LEFT_RETRY_TIME_BELOW_ZERO = "-1";
    private static final String LEFT_RETRY_TIME_ZERO = "0";
    private Context mContext;

    public HwCustIccLockSettingsImpl(Context context) {
        super(context);
        this.mContext = context;
    }

    public void iccLockChangedSuccessCustAfter(boolean is_enabled) {
        if (!SystemProperties.getBoolean("ro.config.icclock_change", false)) {
            return;
        }
        if (is_enabled) {
            Toast.makeText(this.mContext, 2131629135, 0).show();
        } else {
            Toast.makeText(this.mContext, 2131629136, 0).show();
        }
    }

    public void showLeftRetryCounter(EditPinPreference mPinDialog, boolean mToState) {
        if (SystemProperties.getBoolean("ro.config.showPinRetryTimes", false) && !mToState) {
            String attempts = SystemProperties.get("gsm.sim.num.pin");
            if (!TextUtils.isEmpty(attempts) && !attempts.equals(LEFT_RETRY_TIME_ZERO) && !attempts.equals(LEFT_RETRY_TIME_BELOW_ZERO)) {
                mPinDialog.setDialogTitle((CharSequence) this.mContext.getResources().getString(2131625192) + this.mContext.getResources().getString(2131627355) + attempts);
            }
        }
    }
}
