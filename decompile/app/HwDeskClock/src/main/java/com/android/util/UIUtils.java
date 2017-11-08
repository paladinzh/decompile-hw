package com.android.util;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import java.lang.ref.WeakReference;

public final class UIUtils {
    private static double sDeviceSize = -1.0d;

    public interface DialogCallBack {
        void cancel();

        void confirm();
    }

    private static double calculateDeviceSize(Context context) {
        if (-1.0d != sDeviceSize) {
            return sDeviceSize;
        }
        if (context == null) {
            return -1.0d;
        }
        WindowManager manager = (WindowManager) context.getSystemService("window");
        DisplayMetrics displayMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getRealMetrics(displayMetrics);
        sDeviceSize = Math.sqrt(Math.pow((double) (((float) displayMetrics.widthPixels) / displayMetrics.xdpi), 2.0d) + Math.pow((double) (((float) displayMetrics.heightPixels) / displayMetrics.ydpi), 2.0d));
        Log.i("UIUtils", "calculateDeviceSize() sDeviceSize:" + sDeviceSize);
        return sDeviceSize;
    }

    public static double getDeviceSize(Context context) {
        if (-1.0d == sDeviceSize) {
            return calculateDeviceSize(context);
        }
        return sDeviceSize;
    }

    public static boolean isBtvPadScreenDevice() {
        return sDeviceSize > 8.0d;
    }

    public static AlertDialog createAlertDialog(Context context, String title, String message, int okBtnSId, int cancelBtnSId, final DialogCallBack callBack, int btnRedStyle) {
        WeakReference<Context> local = new WeakReference(context);
        if (local.get() == null) {
            return null;
        }
        Builder builder = new Builder((Context) local.get());
        builder.setTitle(message).setPositiveButton(okBtnSId, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                callBack.confirm();
                dialog.dismiss();
            }
        }).setNegativeButton(cancelBtnSId, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                callBack.cancel();
                dialog.dismiss();
            }
        }).setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });
        AlertDialog aDialog = builder.show();
        switch (btnRedStyle) {
            case 1:
                aDialog.getButton(-1).setTextColor(-65536);
                break;
            case 2:
                aDialog.getButton(-2).setTextColor(-65536);
                break;
        }
        return aDialog;
    }
}
