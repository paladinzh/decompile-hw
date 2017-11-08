package com.android.systemui;

import android.content.Context;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

public class SysUIToast {
    public static Toast makeText(Context context, CharSequence text, int duration) {
        Toast toast = Toast.makeText(context, text, duration);
        LayoutParams windowParams = toast.getWindowParams();
        windowParams.privateFlags |= 16;
        return toast;
    }
}
