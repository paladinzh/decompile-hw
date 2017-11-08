package com.android.contacts.activities;

import android.os.SystemProperties;

public class HwCustContactSelectionActivityImpl extends HwCustContactSelectionActivity {
    private static int FINISH_RESULT_CODE = 101;

    public void fingerPrintBindContactsFinish(ContactSelectionActivity activity) {
        if (SystemProperties.getBoolean("ro.config.fp_launch_app", false) && activity.getIntent().getBooleanExtra("FingerPrint_pause_finish", false)) {
            activity.setResult(FINISH_RESULT_CODE);
            activity.finish();
        }
    }
}
