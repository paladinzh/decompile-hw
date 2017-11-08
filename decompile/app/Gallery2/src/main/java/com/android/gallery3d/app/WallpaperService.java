package com.android.gallery3d.app;

import android.app.IntentService;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.WallpaperUtils;
import com.huawei.gallery.util.UIUtils;

public class WallpaperService extends IntentService {
    public WallpaperService() {
        super("WallpaperService");
    }

    public WallpaperService(String name) {
        super(name);
    }

    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        GalleryLog.d("WallpaperService", "request action :" + action);
        if ("com.huawei.wallpaper.action.CHECK".equals(action)) {
            Drawable wallpaper = WallpaperManager.getInstance(this).getDrawable();
            if (wallpaper != null) {
                Bitmap bitmap = UIUtils.getBitmapFromDrawable(wallpaper);
                WallpaperConfig.setWallpaperConfig(this, WallpaperUtils.checkBitmapLine(bitmap, bitmap.getWidth(), bitmap.getHeight()));
            }
        } else if ("com.huawei.wallpaper.action.SET_FIX".equals(action)) {
            boolean fixed = intent.getBooleanExtra("fixed", false);
            long start = System.currentTimeMillis();
            WallpaperConfig.setWallpaperConfig(this, fixed);
            GalleryLog.d("WallpaperService", "update database cost time : " + (System.currentTimeMillis() - start) + " ms");
        } else if ("com.huawei.wallpaper.action.CLEAR_FIX".equals(action)) {
            WallpaperConfig.setWallpaperConfig(this, false);
        }
    }
}
