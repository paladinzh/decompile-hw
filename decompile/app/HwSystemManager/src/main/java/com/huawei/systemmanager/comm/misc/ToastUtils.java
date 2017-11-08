package com.huawei.systemmanager.comm.misc;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

public final class ToastUtils {
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());
    private static Toast mToast;

    private ToastUtils() {
    }

    public static void toastShortMsg(String msg) {
        if (!TextUtils.isEmpty(msg)) {
            makeAndShowToast((CharSequence) msg, 0);
        }
    }

    public static void toastShortMsg(int resId) {
        makeAndShowToast(resId, 0);
    }

    public static void toastLongMsg(String msg) {
        if (!TextUtils.isEmpty(msg)) {
            makeAndShowToast((CharSequence) msg, 1);
        }
    }

    public static void toastLongMsg(int resId) {
        makeAndShowToast(resId, 1);
    }

    public static void toastMsgMultInstance(final int resId) {
        HANDLER.post(new Runnable() {
            public void run() {
                Toast.makeText(GlobalContext.getContext(), resId, 0).show();
            }
        });
    }

    private static void makeAndShowToast(final CharSequence text, final int duration) {
        HANDLER.post(new Runnable() {
            public void run() {
                ToastUtils.makeToast(text, duration);
                ToastUtils.mToast.show();
            }
        });
    }

    private static void makeAndShowToast(final int resId, final int duration) {
        HANDLER.post(new Runnable() {
            public void run() {
                ToastUtils.makeToast(resId, duration);
                ToastUtils.mToast.show();
            }
        });
    }

    @SuppressLint({"ShowToast"})
    private static void makeToast(CharSequence text, int duration) {
        if (mToast == null) {
            mToast = Toast.makeText(GlobalContext.getContext(), text, duration);
            return;
        }
        mToast.setDuration(duration);
        mToast.setText(text);
    }

    @SuppressLint({"ShowToast"})
    private static void makeToast(int resId, int duration) {
        if (mToast == null) {
            mToast = Toast.makeText(GlobalContext.getContext(), resId, duration);
            return;
        }
        mToast.setDuration(duration);
        mToast.setText(resId);
    }
}
