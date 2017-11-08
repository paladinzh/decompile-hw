package com.android.settings.inputmethod;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.inputmethod.InputMethodManager;

public class InputMethodDialogReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if ("android.settings.SHOW_INPUT_METHOD_PICKER".equals(intent.getAction())) {
            ((InputMethodManager) context.getSystemService("input_method")).showInputMethodPicker(true);
        }
    }
}
