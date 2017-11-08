package com.huawei.gallery.util;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import com.android.gallery3d.R;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ReportToBigData;
import java.io.File;

public class VideoEditorController {
    private static Activity sActivity = null;
    private static final OnDismissListener sDialogDismissListener = new OnDismissListener() {
        public void onDismiss(DialogInterface dialog) {
            VideoEditorController.sActivity = null;
        }
    };
    private static final OnClickListener sDialogListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            VideoEditorController.downloadPPQ(VideoEditorController.sActivity);
        }
    };
    private static Intent sIntent = new Intent("huawei.intent.action.VIDEO_EDIT");

    public static void editVideo(Activity activity, String filePath, int requestCode) {
        GalleryLog.d("VideoEditorController", "edit video is called !");
        if (!enterHWVideoEditor(activity, filePath, requestCode) && !enterPpqShareVideoEditor(activity, filePath)) {
            if (isInstalledPPQ(activity)) {
                ContextedUtils.showToastQuickly((Context) activity, (int) R.string.no_available_applications, 0);
            } else if (sActivity == null) {
                sActivity = activity;
                new Builder(activity).setTitle(activity.getResources().getString(R.string.download_ppq_notes)).setPositiveButton(R.string.photoshare_download_short, sDialogListener).setNegativeButton(R.string.cancel, null).setOnDismissListener(sDialogDismissListener).show();
            }
        }
    }

    public static boolean isSupportVideoEdit() {
        return !GalleryUtils.isActivityAvailable(sIntent) ? GalleryUtils.IS_CHINESE_VERSION : true;
    }

    private static boolean enterPpqShareVideoEditor(Activity activity, String filePath) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.ppqshare");
        intent.setPackage("com.iqiyi.share");
        intent.setData(Uri.fromFile(new File(filePath)));
        intent.addFlags(268435456);
        boolean result = startActivity(activity, intent);
        if (result) {
            ReportToBigData.report(80);
        }
        return result;
    }

    private static boolean enterHWVideoEditor(Activity activity, String filePath, int requestCode) {
        Intent intent = new Intent();
        intent.setAction("huawei.intent.action.VIDEO_EDIT");
        intent.putExtra("file", filePath);
        return startActivityForResult(activity, intent, requestCode);
    }

    private static void downloadPPQ(Activity activity) {
        Intent intent = new Intent("com.huawei.appmarket.intent.action.AppDetail");
        intent.putExtra("APP_PACKAGENAME", "com.iqiyi.share");
        intent.setPackage("com.huawei.appmarket");
        intent.setFlags(268435456);
        startActivity(activity, intent);
        ReportToBigData.report(102);
    }

    private static boolean startActivity(Activity activity, Intent intent) {
        try {
            activity.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            GalleryLog.d("VideoEditorController", "Start action:" + intent.getAction() + " failed!");
            return false;
        }
    }

    private static boolean startActivityForResult(Activity activity, Intent intent, int requestCode) {
        try {
            activity.startActivityForResult(intent, requestCode);
            return true;
        } catch (ActivityNotFoundException e) {
            GalleryLog.d("VideoEditorController", "Start action:" + intent.getAction() + " failed!");
            return false;
        }
    }

    private static boolean isInstalledPPQ(Activity activity) {
        try {
            activity.getPackageManager().getPackageInfo("com.iqiyi.share", 1);
            return true;
        } catch (NameNotFoundException e) {
            GalleryLog.d("VideoEditorController", "Get package info:com.iqiyi.share failed!, reason: NameNotFoundException.");
            return false;
        }
    }
}
