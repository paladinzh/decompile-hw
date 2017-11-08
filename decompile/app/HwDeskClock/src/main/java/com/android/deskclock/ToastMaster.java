package com.android.deskclock;

import android.content.Context;
import android.widget.Toast;

public class ToastMaster {
    private static Toast mToast = null;

    private ToastMaster() {
    }

    public static synchronized void showToast(Context context, CharSequence text, int duration) {
        synchronized (ToastMaster.class) {
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(context, text, duration);
            mToast.show();
        }
    }

    public static synchronized void showToast(Context context, int resId, int duration) {
        synchronized (ToastMaster.class) {
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(context, resId, duration);
            mToast.show();
        }
    }

    public static void setToast(Toast toast) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = toast;
    }

    public static void cancelToast() {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = null;
    }
}
