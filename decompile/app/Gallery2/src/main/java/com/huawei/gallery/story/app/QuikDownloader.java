package com.huawei.gallery.story.app;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.net.Uri;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryUtils;

public class QuikDownloader {
    private static Activity sActivity = null;
    private static final OnDismissListener sDialogDismissListener = new OnDismissListener() {
        public void onDismiss(DialogInterface dialog) {
            QuikDownloader.sActivity = null;
        }
    };
    private static final OnClickListener sDialogListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            QuikDownloader.downloadQuikApp(QuikDownloader.sActivity);
        }
    };

    public static void showQuikDownloadDialog(Activity activity, int stringId) {
        sActivity = activity;
        new Builder(activity).setTitle(activity.getResources().getString(stringId)).setPositiveButton(R.string.photoshare_download_short, sDialogListener).setNegativeButton(R.string.cancel, null).setOnDismissListener(sDialogDismissListener).show();
    }

    private static void downloadQuikApp(Activity activity) {
        Intent intent;
        if (GalleryUtils.IS_CHINESE_VERSION) {
            intent = new Intent("com.huawei.appmarket.intent.action.AppDetail");
            intent.putExtra("APP_PACKAGENAME", "com.stupeflix.replay");
            intent.setPackage("com.huawei.appmarket");
        } else {
            intent = new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=com.stupeflix.replay"));
            intent.setPackage("com.android.vending");
        }
        intent.setFlags(268435456);
        GalleryUtils.startActivityCatchSecurityEx(activity, intent);
    }
}
