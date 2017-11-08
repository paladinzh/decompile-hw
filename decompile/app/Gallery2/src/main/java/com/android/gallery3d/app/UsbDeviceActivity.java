package com.android.gallery3d.app;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.app.GalleryMain;

public final class UsbDeviceActivity extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, GalleryMain.class);
        intent.addFlags(335544320);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            GalleryLog.e("UsbDeviceActivity", "unable to start Gallery activity." + e.getMessage());
        }
        finish();
    }
}
