package com.huawei.mms.ui;

import android.os.Handler;
import android.util.Log;
import com.android.mms.HwCustMmsConfigImpl;

public class HwCustHwRecipientsEditorImpl extends HwCustHwRecipientsEditor {
    private static final String TAG = "HwCustHwRecipientsEditorImpl";
    private Handler handler = new Handler();
    private HwRecipientsEditor recipientsEditor;
    Runnable runnable = new Runnable() {
        public void run() {
            HwCustHwRecipientsEditorImpl.this.recipientsEditor.requestFocus();
        }
    };

    public void handleInvalidRecipent(HwRecipientsEditor recipientsEditor) {
        Log.i(TAG, "handleInvalidRecipent");
        if (HwCustMmsConfigImpl.isInvalidAddressRequestFocus() && recipientsEditor != null) {
            this.recipientsEditor = recipientsEditor;
            this.handler.post(this.runnable);
        }
    }
}
