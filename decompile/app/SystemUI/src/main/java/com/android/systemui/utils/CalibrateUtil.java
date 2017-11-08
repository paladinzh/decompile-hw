package com.android.systemui.utils;

import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.android.systemui.R;
import huawei.android.hardware.fingerprint.FingerprintManagerEx;

public class CalibrateUtil {
    public static void sendCalibrate(Context mContext) {
        if (isFpNeedCalibrate(new FingerprintManagerEx(mContext))) {
            notifyFingerprintCalibration(mContext);
        } else {
            Log.i("CalibrateUtil", "the fingerprint sensor needn't calibration.");
        }
    }

    public static boolean isFpNeedCalibrate(FingerprintManagerEx managerEx) {
        boolean ret = false;
        try {
            ret = ((Boolean) Class.forName("huawei.android.hardware.fingerprint.FingerprintManagerEx").getDeclaredMethod("isFpNeedCalibrate", null).invoke(managerEx, (Object[]) null)).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static void notifyFingerprintCalibration(Context context) {
        Builder builder = new Builder(context);
        builder.setSmallIcon(R.drawable.abc_list_divider_mtrl_alpha);
        builder.setOngoing(false);
        builder.setPriority(1);
        builder.setContentIntent(PendingIntent.getActivity(context, 0, getCalibrationIntroActivity(), 134217728));
        builder.setContentTitle(context.getString(R.string.fp_calibrate_notify_title));
        builder.setContentText(context.getString(R.string.fp_calibrate_notify_tip));
        builder.setAutoCancel(true);
        ((NotificationManager) context.getSystemService("notification")).notify(null, 110, builder.build());
    }

    public static Intent getCalibrationIntroActivity() {
        Intent intent = new Intent();
        intent.setPackage("com.android.settings");
        intent.setAction("com.android.settings.ACTION_FINGERPRINT_CALIBRATION_INTRO");
        return intent;
    }
}
