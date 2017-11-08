package com.android.gallery3d.util;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.Utils;

public class ContextedUtils implements Callback {
    private final Application mContext;
    private Handler mHandler = new Handler(Looper.getMainLooper(), this);
    private Toast mToast;

    public ContextedUtils(Application context) {
        this.mContext = context;
    }

    public void showToastWithNoQueue(CharSequence text, int duration) {
        this.mHandler.removeMessages(1);
        this.mHandler.obtainMessage(1, duration, 0, text).sendToTarget();
    }

    public static ContextedUtils getContextedUtils(Context context) {
        Utils.assertTrue(context != null);
        if (context instanceof GalleryApp) {
            return ((GalleryApp) context).getContextedUtils();
        }
        return ((GalleryApp) context.getApplicationContext()).getContextedUtils();
    }

    public static void showToastQuickly(Context context, int resId, int duration) {
        showToastQuickly(context, context.getString(resId), duration);
    }

    public static void showToastQuickly(Context context, CharSequence text, int duration) {
        getContextedUtils(context).showToastWithNoQueue(text, duration);
    }

    public void hideToast() {
        hideToast(this.mToast);
    }

    public static void hideToast(Toast toast) {
        if (toast != null) {
            toast.cancel();
        }
    }

    public static void hideToast(Context context) {
        getContextedUtils(context).hideToast();
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                if (this.mToast != null) {
                    this.mToast.cancel();
                }
                this.mToast = Toast.makeText(this.mContext, (CharSequence) msg.obj, msg.arg1);
                this.mToast.show();
                return true;
            default:
                return false;
        }
    }
}
