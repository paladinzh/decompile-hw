package com.android.systemui.screenshot;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import com.android.systemui.R;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUiUtil;
import fyusion.vislib.BuildConfig;

public class HwScreenshotUtil {
    static void shareScreenshot(Context ctx, Uri imageUri) {
        HwLog.i("GlobalScreenshot.HwScreenshotUtil", "shareScreenshot");
        if (imageUri != null) {
            Intent sharingIntent = new Intent("android.intent.action.SEND");
            sharingIntent.setType("image/jpeg");
            sharingIntent.putExtra("android.intent.extra.STREAM", imageUri);
            Intent chooserIntent = Intent.createChooser(sharingIntent, ctx.getText(R.string.screenshot_share));
            chooserIntent.addFlags(268468224);
            try {
                SystemUiUtil.dismissKeyguard();
                ctx.startActivity(chooserIntent);
                return;
            } catch (ActivityNotFoundException e) {
                HwLog.e("GlobalScreenshot.HwScreenshotUtil", "shareScreenshot catch ActivityNotFoundException: " + e.getMessage());
                return;
            } catch (Exception e2) {
                HwLog.e("GlobalScreenshot.HwScreenshotUtil", "shareScreenshot catch Exception: " + e2.getMessage());
                return;
            }
        }
        showToast(ctx);
    }

    static void editScreenshot(Context ctx, Uri imageUri) {
        HwLog.i("GlobalScreenshot.HwScreenshotUtil", "editScreenshot");
        if (imageUri != null) {
            Intent intent = new Intent("com.huawei.gallery.action.SCREENSHOTEDIT");
            intent.setDataAndType(imageUri, "image/jpeg").setFlags(1);
            if (SystemUiUtil.isIntentExist(ctx, intent)) {
                intent.setPackage("com.android.gallery3d");
            } else {
                intent.setAction("action_nextgen_edit");
                if (!SystemUiUtil.isIntentExist(ctx, intent)) {
                    intent.setAction("android.intent.action.EDIT");
                }
            }
            intent.addFlags(268468224);
            try {
                SystemUiUtil.dismissKeyguard();
                ctx.startActivity(intent);
                return;
            } catch (ActivityNotFoundException e) {
                HwLog.e("GlobalScreenshot.HwScreenshotUtil", "editScreenshot catch ActivityNotFoundException: " + e.getMessage());
                return;
            } catch (Exception e2) {
                HwLog.e("GlobalScreenshot.HwScreenshotUtil", "editScreenshot catch Exception: " + e2.getMessage());
                return;
            }
        }
        showToast(ctx);
    }

    static void scrollScreenshot(Context ctx, Uri imageUri) {
        try {
            Intent startIntent = new Intent();
            startIntent.setAction("com.huawei.HwMultiScreenShot.start");
            startIntent.setClassName("com.huawei.HwMultiScreenShot", "com.huawei.HwMultiScreenShot.MultiScreenShotService");
            ctx.startService(startIntent);
        } catch (SecurityException e) {
            HwLog.e("GlobalScreenshot.HwScreenshotUtil", "scrollScreenshot catch SecurityException: " + e.getMessage());
        } catch (Exception e2) {
            HwLog.e("GlobalScreenshot.HwScreenshotUtil", "Can't start load multi-screenshot service: " + e2.getMessage());
        }
        deleteSaveImageInBackgroundData(ctx.getContentResolver(), imageUri);
    }

    private static void deleteSaveImageInBackgroundData(ContentResolver resolver, Uri uri) {
        if (resolver == null || uri == null) {
            HwLog.e("GlobalScreenshot.HwScreenshotUtil", "deleteSaveImageInBackgroundData invalid input parameter.");
            return;
        }
        if (resolver.delete(uri, BuildConfig.FLAVOR, new String[0]) < 0) {
            HwLog.w("GlobalScreenshot.HwScreenshotUtil", "deleteSaveImageInBackgroundData failed");
        }
    }

    static void showToast(Context ctx) {
        Toast.makeText(ctx, ctx.getString(R.string.screenshot_fail_tips), 0).show();
    }
}
