package com.android.gallery3d.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.view.Display;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.util.BundleUtils;
import com.huawei.gallery.wallpaper.WallpaperConstant;

public class Wallpaper extends Activity {
    private Uri mPickedItem;
    private int mState = 0;

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (bundle != null) {
            this.mState = bundle.getInt("activity-state");
            this.mPickedItem = (Uri) bundle.getParcelable("picked-item");
        }
    }

    protected void onSaveInstanceState(Bundle saveState) {
        saveState.putInt("activity-state", this.mState);
        if (this.mPickedItem != null) {
            saveState.putParcelable("picked-item", this.mPickedItem);
        }
    }

    @TargetApi(13)
    private Point getDefaultDisplaySize(Point size) {
        Display d = getWindowManager().getDefaultDisplay();
        if (VERSION.SDK_INT >= 13) {
            d.getRealSize(size);
        } else {
            size.set(d.getWidth(), d.getHeight());
        }
        return size;
    }

    protected void onResume() {
        Intent request;
        super.onResume();
        Intent intent = getIntent();
        switch (this.mState) {
            case 0:
                boolean fromLauncher = "com.android.camera.action.CROP_WALLPAPER".equals(intent.getAction());
                this.mPickedItem = intent.getData();
                if (this.mPickedItem != null || fromLauncher) {
                    GalleryLog.d("Wallpaper", "called from launcher ");
                    this.mState = 1;
                    break;
                }
                request = new Intent("android.intent.action.GET_CONTENT").setClass(this, DialogPicker.class).setType("image/*");
                request.putExtra("fetch-content-for-wallpaper", true);
                request.putExtra("crop", "wallpaper");
                startActivity(setupCropParameter(intent, request));
                finish();
                return;
                break;
            case 1:
                break;
        }
        request = new Intent("com.android.camera.action.CROP");
        request.setDataAndType(this.mPickedItem, "image/*");
        startActivity(setupCropParameter(intent, request).setClass(this, WallpaperConstant.CROP_WALLPAPER_CLASS));
        finish();
    }

    private Intent setupCropParameter(Intent inIntent, Intent outIntent) {
        int width = getWallpaperDesiredMinimumWidth();
        int height = getWallpaperDesiredMinimumHeight();
        Point size = getDefaultDisplaySize(new Point());
        float spotlightX = ((float) size.x) / ((float) width);
        float spotlightY = ((float) size.y) / ((float) height);
        Bundle extras = inIntent.getExtras();
        String setAsTheme = null;
        if (extras != null) {
            setAsTheme = BundleUtils.getString(extras, "set-as-theme");
        }
        outIntent.addFlags(33554432).putExtra("outputX", width).putExtra("outputY", height).putExtra("aspectX", width).putExtra("aspectY", height).putExtra("fixedX", size.x).putExtra("fixedY", Math.max(size.y, height)).putExtra("spotlightX", spotlightX).putExtra("spotlightY", spotlightY).putExtra("scale", true).putExtra("scaleUpIfNeeded", true).putExtra("set-as-theme", setAsTheme);
        return outIntent;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != -1) {
            setResult(resultCode);
            finish();
            return;
        }
        this.mState = requestCode;
        if (this.mState == 1) {
            this.mPickedItem = data.getData();
        }
    }
}
