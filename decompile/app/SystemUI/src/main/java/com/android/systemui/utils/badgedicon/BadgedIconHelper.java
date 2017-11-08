package com.android.systemui.utils.badgedicon;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.trustspace.TrustSpaceManager;
import com.android.systemui.R;
import com.android.systemui.utils.HwLog;
import com.huawei.android.app.ActivityManagerEx;

public class BadgedIconHelper {
    private static final String TAG = BadgedIconHelper.class.getSimpleName();

    public static Drawable getTrustSpaceBadgedDrawable(Resources res, Drawable drawable) {
        int badgedWidth = drawable.getIntrinsicWidth();
        int badgedHeight = drawable.getIntrinsicHeight();
        Drawable badgeDrawable = res.getDrawable(R.drawable.ic_trustspace_badge, null);
        Bitmap bitmap = Bitmap.createBitmap(badgedWidth, badgedHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, badgedWidth, badgedHeight);
        drawable.draw(canvas);
        badgeDrawable.setBounds(0, 0, badgedWidth, badgedHeight);
        badgeDrawable.draw(canvas);
        return new BitmapDrawable(bitmap);
    }

    public static int getBadgedIconType(int hwFlags, String packageName) {
        boolean isCloneProcess;
        int i = 0;
        if ((hwFlags & 1) != 0) {
            isCloneProcess = true;
        } else {
            isCloneProcess = false;
        }
        if (isCloneProcess) {
            return 1;
        }
        if (TrustSpaceManager.getDefault().isIntentProtectedApp(packageName)) {
            i = 2;
        }
        return i;
    }

    public static boolean isCloneProcess(int pid, String tag) {
        boolean isCloned = false;
        if (pid >= 0) {
            isCloned = !ActivityManagerEx.isClonedProcess(pid) ? tag != null ? tag.contains("_hwclone") : false : true;
        }
        HwLog.i(TAG, "isCloneProcess = " + isCloned);
        return isCloned;
    }

    public static Icon getBitampIcon(Context context, int resId) {
        Config config;
        Drawable drawable = context.getResources().getDrawable(resId);
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        if (drawable.getOpacity() != -1) {
            config = Config.ARGB_8888;
        } else {
            config = Config.RGB_565;
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return Icon.createWithBitmap(bitmap);
    }
}
